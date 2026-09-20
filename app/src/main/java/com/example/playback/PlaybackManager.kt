package com.example.playback

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.compose.ui.graphics.Color
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.palette.graphics.Palette
import com.example.MainActivity
import com.example.data.local.MusicDao
import com.example.data.local.MusicDatabase
import com.example.data.local.PreferencesManager
import com.example.data.model.RepeatState
import com.example.data.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(UnstableApi::class)
class PlaybackManager private constructor(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val musicDao: MusicDao = MusicDatabase.getInstance(context).musicDao()
    private val preferencesManager = PreferencesManager(context)

    val audioEffectManager = AudioEffectManager()

    lateinit var sleepTimerManager: SleepTimerManager
        private set

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            true
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    var mediaSession: MediaSession? = null
        private set

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatState.OFF)
    val repeatMode: StateFlow<RepeatState> = _repeatMode.asStateFlow()

    private val _shuffleMode = MutableStateFlow(false)
    val shuffleMode: StateFlow<Boolean> = _shuffleMode.asStateFlow()

    private val _dominantColor = MutableStateFlow(Color(0xFF8E8E93))
    val dominantColor: StateFlow<Color> = _dominantColor.asStateFlow()

    private val _isMiniPlayerDismissed = MutableStateFlow(false)
    val isMiniPlayerDismissed: StateFlow<Boolean> = _isMiniPlayerDismissed.asStateFlow()

    private var positionJob: Job? = null

    init {
        sleepTimerManager = SleepTimerManager(
            scope = scope,
            onFadeVolume = { volume ->
                exoPlayer.volume = volume
            },
            onTimerExpired = {
                pause()
            }
        )

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(context, exoPlayer)
            .setSessionActivity(pendingIntent)
            .build()

        setupPlayerListener()
        startPositionTracking()
    }

    private fun setupPlayerListener() {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _isMiniPlayerDismissed.value = false
                    startPositionTracking()
                } else {
                    stopPositionTracking()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _durationMs.value = exoPlayer.duration.coerceAtLeast(0L)
                    audioEffectManager.bindAudioSession(exoPlayer.audioSessionId)
                } else if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val index = exoPlayer.currentMediaItemIndex
                _queueIndex.value = index
                val song = _queue.value.getOrNull(index)
                _currentSong.value = song
                _durationMs.value = song?.duration ?: 0L
                _currentPositionMs.value = 0L

                song?.let { s ->
                    scope.launch(Dispatchers.IO) {
                        musicDao.recordPlay(s.id)
                        preferencesManager.saveLastPlaybackState(s.id, 0L)
                        extractDominantColor(s)
                    }
                }
            }
        })
    }

    private suspend fun extractDominantColor(song: Song) = withContext(Dispatchers.IO) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, song.albumArtUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSampleSize(4)
                }
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, song.albumArtUri)
            }

            if (bitmap != null) {
                val palette = Palette.from(bitmap).generate()
                val swatch = palette.dominantSwatch
                    ?: palette.vibrantSwatch
                    ?: palette.mutedSwatch
                    ?: palette.lightVibrantSwatch

                swatch?.let {
                    _dominantColor.value = Color(it.rgb)
                }
            }
        } catch (_: Exception) {
            // Calm neutral fallback
            _dominantColor.value = Color(0xFF8E8E93)
        }
    }

    private fun startPositionTracking() {
        stopPositionTracking()
        positionJob = scope.launch {
            while (isActive) {
                if (exoPlayer.isPlaying) {
                    _currentPositionMs.value = exoPlayer.currentPosition.coerceAtLeast(0L)
                }
                delay(200)
            }
        }
    }

    private fun stopPositionTracking() {
        positionJob?.cancel()
        positionJob = null
    }

    fun playSong(song: Song, queueList: List<Song> = listOf(song), indexInQueue: Int = 0) {
        _isMiniPlayerDismissed.value = false
        _queue.value = queueList
        _queueIndex.value = indexInQueue
        _currentSong.value = song

        val mediaItems = queueList.map { item ->
            MediaItem.Builder()
                .setMediaId(item.id.toString())
                .setUri(item.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(item.title)
                        .setArtist(item.artist)
                        .setAlbumTitle(item.album)
                        .setArtworkUri(item.albumArtUri)
                        .build()
                )
                .build()
        }

        exoPlayer.setMediaItems(mediaItems, indexInQueue, 0L)
        exoPlayer.prepare()
        exoPlayer.play()

        scope.launch(Dispatchers.IO) {
            musicDao.saveQueue(queueList.map { it.id })
        }
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        _isMiniPlayerDismissed.value = false
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun seekTo(positionMs: Long) {
        _currentPositionMs.value = positionMs
        exoPlayer.seekTo(positionMs)
    }

    fun skipToNext() {
        if (exoPlayer.hasNextMediaItem()) {
            exoPlayer.seekToNextMediaItem()
        } else if (_repeatMode.value == RepeatState.ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(0, 0L)
        }
    }

    fun skipToPrevious() {
        if (exoPlayer.currentPosition > 3000) {
            seekTo(0L)
        } else if (exoPlayer.hasPreviousMediaItem()) {
            exoPlayer.seekToPreviousMediaItem()
        } else if (_repeatMode.value == RepeatState.ALL && _queue.value.isNotEmpty()) {
            exoPlayer.seekTo(_queue.value.size - 1, 0L)
        } else {
            seekTo(0L)
        }
    }

    fun toggleShuffle() {
        val next = !_shuffleMode.value
        _shuffleMode.value = next
        exoPlayer.shuffleModeEnabled = next
    }

    fun toggleRepeat() {
        val next = when (_repeatMode.value) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ALL -> RepeatState.ONE
            RepeatState.ONE -> RepeatState.OFF
        }
        _repeatMode.value = next
        exoPlayer.repeatMode = when (next) {
            RepeatState.OFF -> Player.REPEAT_MODE_OFF
            RepeatState.ALL -> Player.REPEAT_MODE_ALL
            RepeatState.ONE -> Player.REPEAT_MODE_ONE
        }
    }

    fun jumpToQueueIndex(index: Int) {
        if (index in 0 until _queue.value.size) {
            exoPlayer.seekTo(index, 0L)
            exoPlayer.play()
        }
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex in _queue.value.indices && toIndex in _queue.value.indices && fromIndex != toIndex) {
            val list = _queue.value.toMutableList()
            val moved = list.removeAt(fromIndex)
            list.add(toIndex, moved)
            _queue.value = list
            exoPlayer.moveMediaItem(fromIndex, toIndex)

            scope.launch(Dispatchers.IO) {
                musicDao.saveQueue(list.map { it.id })
            }
        }
    }

    fun removeFromQueue(index: Int) {
        if (index in _queue.value.indices) {
            val list = _queue.value.toMutableList()
            list.removeAt(index)
            _queue.value = list
            exoPlayer.removeMediaItem(index)

            if (list.isEmpty()) {
                stop()
            }
        }
    }

    fun dismissMiniPlayer() {
        _isMiniPlayerDismissed.value = true
        pause()
    }

    fun stop() {
        exoPlayer.stop()
        _currentSong.value = null
        _isPlaying.value = false
        _queue.value = emptyList()
        _queueIndex.value = -1
        _currentPositionMs.value = 0L
    }

    fun release() {
        stopPositionTracking()
        audioEffectManager.release()
        exoPlayer.release()
        mediaSession?.release()
        mediaSession = null
        INSTANCE = null
    }

    companion object {
        @Volatile
        private var INSTANCE: PlaybackManager? = null

        fun getInstance(context: Context): PlaybackManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PlaybackManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
