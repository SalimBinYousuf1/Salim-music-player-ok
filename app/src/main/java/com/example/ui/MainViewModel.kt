package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ExcludedFolderEntity
import com.example.data.local.MusicDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.mediastore.MediaStoreScanner
import com.example.data.mediastore.ScanProgress
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.SortOption
import com.example.data.repository.MusicRepository
import com.example.playback.PlaybackManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val database = MusicDatabase.getInstance(application)
    val preferencesManager = PreferencesManager(application)
    val musicRepository = MusicRepository(database.musicDao(), preferencesManager)
    val mediaStoreScanner = MediaStoreScanner(application, database.musicDao())
    val playbackManager = PlaybackManager.getInstance(application)

    val songs: StateFlow<List<Song>> = musicRepository.allSongs
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val albums: StateFlow<List<Album>> = musicRepository.allAlbums
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val artists: StateFlow<List<Artist>> = musicRepository.allArtists
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val playlists: StateFlow<List<Playlist>> = musicRepository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val folders: StateFlow<List<Folder>> = musicRepository.allFolders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentSort: StateFlow<SortOption> = preferencesManager.sortOption
        .stateIn(viewModelScope, SharingStarted.Lazily, SortOption.TITLE)

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.Lazily, ThemeMode.DARK)

    val gaplessPlayback: StateFlow<Boolean> = preferencesManager.gaplessPlayback
        .stateIn(viewModelScope, SharingStarted.Lazily, true)

    val crossfadeSeconds: StateFlow<Int> = preferencesManager.crossfadeSeconds
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val excludedFolders: StateFlow<List<ExcludedFolderEntity>> = musicRepository.excludedFolders
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val scanProgress: StateFlow<ScanProgress> = mediaStoreScanner.scanProgress

    // Search query & live reactive search flow
    val searchQuery = MutableStateFlow("")
    val searchResults: StateFlow<List<Song>> = searchQuery
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else musicRepository.search(q)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Playback state forwarding
    val currentSong = playbackManager.currentSong
    val isPlaying = playbackManager.isPlaying
    val currentPositionMs = playbackManager.currentPositionMs
    val durationMs = playbackManager.durationMs
    val queue = playbackManager.queue
    val queueIndex = playbackManager.queueIndex
    val repeatMode = playbackManager.repeatMode
    val shuffleMode = playbackManager.shuffleMode
    val dominantColor = playbackManager.dominantColor
    val isMiniPlayerDismissed = playbackManager.isMiniPlayerDismissed
    val equalizerState = playbackManager.audioEffectManager.state
    val sleepTimerSeconds = playbackManager.sleepTimerManager.remainingSeconds

    fun scanLibrary() {
        viewModelScope.launch {
            mediaStoreScanner.scanLibrary()
        }
    }

    fun setSortOption(option: SortOption) {
        viewModelScope.launch {
            preferencesManager.setSortOption(option)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setGaplessPlayback(enabled)
        }
    }

    fun setCrossfadeSeconds(seconds: Int) {
        viewModelScope.launch {
            preferencesManager.setCrossfadeSeconds(seconds)
        }
    }

    fun removeExcludedFolder(path: String) {
        viewModelScope.launch {
            musicRepository.removeExcludedFolder(path)
            mediaStoreScanner.scanLibrary()
        }
    }

    // Playback actions
    fun playSong(song: Song, queueList: List<Song>, indexInQueue: Int) {
        playbackManager.playSong(song, queueList, indexInQueue)
    }

    fun togglePlayPause() {
        playbackManager.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playbackManager.seekTo(positionMs)
    }

    fun skipNext() {
        playbackManager.skipToNext()
    }

    fun skipPrevious() {
        playbackManager.skipToPrevious()
    }

    fun toggleRepeat() {
        playbackManager.toggleRepeat()
    }

    fun toggleShuffle() {
        playbackManager.toggleShuffle()
    }

    fun jumpToQueueIndex(index: Int) {
        playbackManager.jumpToQueueIndex(index)
    }

    fun reorderQueue(from: Int, to: Int) {
        playbackManager.reorderQueue(from, to)
    }

    fun removeFromQueue(index: Int) {
        playbackManager.removeFromQueue(index)
    }

    fun dismissMiniPlayer() {
        playbackManager.dismissMiniPlayer()
    }

    // Audio effects
    fun toggleEqualizer() {
        playbackManager.audioEffectManager.toggleEnabled()
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        playbackManager.audioEffectManager.setBandLevel(bandIndex, levelMilliBels)
    }

    fun setBassBoost(strength: Short) {
        playbackManager.audioEffectManager.setBassBoost(strength)
    }

    fun applyPreset(preset: String) {
        playbackManager.audioEffectManager.applyPreset(preset)
    }

    // Sleep timer
    fun startSleepTimer(minutes: Int) {
        playbackManager.sleepTimerManager.startTimer(minutes)
    }

    fun cancelSleepTimer() {
        playbackManager.sleepTimerManager.cancelTimer()
    }

    // Playlist actions
    fun createPlaylist(name: String, songIdToAdd: Long? = null) {
        viewModelScope.launch {
            val id = musicRepository.createPlaylist(name)
            if (songIdToAdd != null) {
                musicRepository.addSongToPlaylist(id, songIdToAdd)
            }
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            musicRepository.deletePlaylist(playlistId)
        }
    }

    fun getSongsForAlbum(albumId: Long) = musicRepository.getSongsForAlbum(albumId)
    fun getSongsForArtist(artistName: String) = musicRepository.getSongsForArtist(artistName)
    fun getSongsForPlaylist(playlistId: Long) = musicRepository.getSongsForPlaylist(playlistId)
    fun getSongsForFolder(folderPath: String) = musicRepository.getSongsForFolder(folderPath)
}
