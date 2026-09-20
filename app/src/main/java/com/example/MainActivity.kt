package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.ui.MainViewModel
import com.example.ui.components.MiniPlayer
import com.example.ui.screens.AddToPlaylistSheet
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.ArtistDetailScreen
import com.example.ui.screens.EqualizerSheet
import com.example.ui.screens.FolderDetailScreen
import com.example.ui.screens.LibraryHomeScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PermissionPrimingScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.QueueSheet
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsSheet
import com.example.ui.screens.SleepTimerSheet
import com.example.ui.theme.SalimTheme

sealed class ScreenDestination {
    object Library : ScreenDestination()
    object Search : ScreenDestination()
    data class AlbumDetail(val album: Album) : ScreenDestination()
    data class ArtistDetail(val artist: Artist) : ScreenDestination()
    data class PlaylistDetail(val playlist: Playlist) : ScreenDestination()
    data class FolderDetail(val folder: Folder) : ScreenDestination()
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val dominantColor by viewModel.dominantColor.collectAsState()

            SalimTheme(
                themeMode = themeMode,
                adaptiveAccent = dominantColor
            ) {
                SalimApp(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAudioPermission()) {
            viewModel.scanLibrary()
        }
    }

    private fun hasAudioPermission(): Boolean {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun SalimApp(viewModel: MainViewModel) {
    var hasPermission by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        val perm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        val granted = ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        hasPermission = granted
        if (granted) {
            viewModel.scanLibrary()
        }
    }

    if (!hasPermission) {
        PermissionPrimingScreen(
            onPermissionGranted = {
                hasPermission = true
                viewModel.scanLibrary()
            }
        )
        return
    }

    // App state
    val songs by viewModel.songs.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val currentSort by viewModel.currentSort.collectAsState()
    val scanProgress by viewModel.scanProgress.collectAsState()

    // Playback state
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentPosMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val queue by viewModel.queue.collectAsState()
    val queueIndex by viewModel.queueIndex.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val shuffleMode by viewModel.shuffleMode.collectAsState()
    val isMiniPlayerDismissed by viewModel.isMiniPlayerDismissed.collectAsState()

    // Sheets & Full Screen overlays
    var isNowPlayingExpanded by remember { mutableStateOf(false) }
    var isQueueSheetOpen by remember { mutableStateOf(false) }
    var isEqualizerSheetOpen by remember { mutableStateOf(false) }
    var isSleepTimerSheetOpen by remember { mutableStateOf(false) }
    var isSettingsSheetOpen by remember { mutableStateOf(false) }
    var songForPlaylistModal by remember { mutableStateOf<Song?>(null) }

    // Screen destination navigation
    var currentScreen by remember { mutableStateOf<ScreenDestination>(ScreenDestination.Library) }

    // Back handling
    BackHandler(enabled = isNowPlayingExpanded || currentScreen !is ScreenDestination.Library) {
        if (isNowPlayingExpanded) {
            isNowPlayingExpanded = false
        } else {
            currentScreen = ScreenDestination.Library
        }
    }

    val progressFraction = if (durationMs > 0) (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f

    Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Main Content Router
            when (val screen = currentScreen) {
                is ScreenDestination.Library -> {
                    LibraryHomeScreen(
                        songs = songs,
                        albums = albums,
                        artists = artists,
                        playlists = playlists,
                        folders = folders,
                        currentSort = currentSort,
                        scanProgress = scanProgress,
                        onSongClick = { song, list, index ->
                            viewModel.playSong(song, list, index)
                        },
                        onAlbumClick = { album ->
                            currentScreen = ScreenDestination.AlbumDetail(album)
                        },
                        onArtistClick = { artist ->
                            currentScreen = ScreenDestination.ArtistDetail(artist)
                        },
                        onPlaylistClick = { playlist ->
                            currentScreen = ScreenDestination.PlaylistDetail(playlist)
                        },
                        onFolderClick = { folder ->
                            currentScreen = ScreenDestination.FolderDetail(folder)
                        },
                        onSongLongClick = { song ->
                            songForPlaylistModal = song
                        },
                        onSortOptionSelected = { sort ->
                            viewModel.setSortOption(sort)
                        },
                        onOpenSearch = {
                            currentScreen = ScreenDestination.Search
                        },
                        onOpenSettings = {
                            isSettingsSheetOpen = true
                        },
                        onCreatePlaylist = {
                            songForPlaylistModal = songs.firstOrNull()
                        },
                        onRescanClick = {
                            viewModel.scanLibrary()
                        }
                    )
                }
                is ScreenDestination.Search -> {
                    val query by viewModel.searchQuery.collectAsState()
                    val results by viewModel.searchResults.collectAsState()

                    SearchScreen(
                        query = query,
                        results = results,
                        onQueryChange = { viewModel.searchQuery.value = it },
                        onSongClick = { song, list ->
                            val idx = list.indexOf(song).coerceAtLeast(0)
                            viewModel.playSong(song, list, idx)
                        },
                        onBack = {
                            viewModel.searchQuery.value = ""
                            currentScreen = ScreenDestination.Library
                        }
                    )
                }
                is ScreenDestination.AlbumDetail -> {
                    val albumSongs by viewModel.getSongsForAlbum(screen.album.id).collectAsState(initial = emptyList())
                    AlbumDetailScreen(
                        album = screen.album,
                        songs = albumSongs,
                        onSongClick = { song, list, index ->
                            viewModel.playSong(song, list, index)
                        },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playSong(list.first(), list, 0)
                        },
                        onBack = { currentScreen = ScreenDestination.Library }
                    )
                }
                is ScreenDestination.ArtistDetail -> {
                    val artistSongs by viewModel.getSongsForArtist(screen.artist.name).collectAsState(initial = emptyList())
                    ArtistDetailScreen(
                        artist = screen.artist,
                        songs = artistSongs,
                        onSongClick = { song, list, index ->
                            viewModel.playSong(song, list, index)
                        },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playSong(list.first(), list, 0)
                        },
                        onBack = { currentScreen = ScreenDestination.Library }
                    )
                }
                is ScreenDestination.PlaylistDetail -> {
                    val playlistSongs by viewModel.getSongsForPlaylist(screen.playlist.id).collectAsState(initial = emptyList())
                    PlaylistDetailScreen(
                        playlist = screen.playlist,
                        songs = playlistSongs,
                        onSongClick = { song, list, index ->
                            viewModel.playSong(song, list, index)
                        },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playSong(list.first(), list, 0)
                        },
                        onDeletePlaylist = {
                            viewModel.deletePlaylist(screen.playlist.id)
                            currentScreen = ScreenDestination.Library
                        },
                        onBack = { currentScreen = ScreenDestination.Library }
                    )
                }
                is ScreenDestination.FolderDetail -> {
                    val folderSongs by viewModel.getSongsForFolder(screen.folder.path).collectAsState(initial = emptyList())
                    FolderDetailScreen(
                        folder = screen.folder,
                        songs = folderSongs,
                        onSongClick = { song, list, index ->
                            viewModel.playSong(song, list, index)
                        },
                        onPlayAll = { list ->
                            if (list.isNotEmpty()) viewModel.playSong(list.first(), list, 0)
                        },
                        onBack = { currentScreen = ScreenDestination.Library }
                    )
                }
            }

            // Docked MiniPlayer above navigation bars
            MiniPlayer(
                song = currentSong,
                isPlaying = isPlaying,
                progress = progressFraction,
                isDismissed = isMiniPlayerDismissed,
                onExpand = { isNowPlayingExpanded = true },
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onDismiss = { viewModel.dismissMiniPlayer() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )

            // Full Screen Now Playing overlay with continuous vertical transition
            AnimatedVisibility(
                visible = isNowPlayingExpanded && currentSong != null,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                NowPlayingScreen(
                    song = currentSong,
                    isPlaying = isPlaying,
                    currentPositionMs = currentPosMs,
                    durationMs = durationMs,
                    repeatState = repeatMode,
                    isShuffle = shuffleMode,
                    hasPrevious = queueIndex > 0,
                    hasNext = queueIndex < queue.size - 1,
                    onCollapse = { isNowPlayingExpanded = false },
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onSkipNext = { viewModel.skipNext() },
                    onSkipPrevious = { viewModel.skipPrevious() },
                    onToggleRepeat = { viewModel.toggleRepeat() },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onOpenQueue = { isQueueSheetOpen = true },
                    onOpenEqualizer = { isEqualizerSheetOpen = true },
                    onOpenSleepTimer = { isSleepTimerSheetOpen = true },
                    onAddToPlaylist = { songForPlaylistModal = currentSong }
                )
            }
        }
    }

    // Modal Sheets
    if (isQueueSheetOpen) {
        QueueSheet(
            queue = queue,
            currentIndex = queueIndex,
            isPlaying = isPlaying,
            onDismiss = { isQueueSheetOpen = false },
            onJumpTo = { viewModel.jumpToQueueIndex(it) },
            onRemove = { viewModel.removeFromQueue(it) },
            onMoveUp = { idx -> viewModel.reorderQueue(idx, idx - 1) },
            onMoveDown = { idx -> viewModel.reorderQueue(idx, idx + 1) }
        )
    }

    if (isEqualizerSheetOpen) {
        val eqState by viewModel.equalizerState.collectAsState()
        EqualizerSheet(
            state = eqState,
            onToggleEnabled = { viewModel.toggleEqualizer() },
            onSetBandLevel = { idx, lvl -> viewModel.setBandLevel(idx, lvl) },
            onSetBassBoost = { strength -> viewModel.setBassBoost(strength) },
            onApplyPreset = { preset -> viewModel.applyPreset(preset) },
            onDismiss = { isEqualizerSheetOpen = false }
        )
    }

    if (isSleepTimerSheetOpen) {
        val remainingSecs by viewModel.sleepTimerSeconds.collectAsState()
        SleepTimerSheet(
            remainingSeconds = remainingSecs,
            onStartTimer = { mins -> viewModel.startSleepTimer(mins) },
            onCancelTimer = { viewModel.cancelSleepTimer() },
            onDismiss = { isSleepTimerSheetOpen = false }
        )
    }

    if (isSettingsSheetOpen) {
        val themeMode by viewModel.themeMode.collectAsState()
        val gapless by viewModel.gaplessPlayback.collectAsState()
        val crossfade by viewModel.crossfadeSeconds.collectAsState()
        val excludedFolders by viewModel.excludedFolders.collectAsState()

        SettingsSheet(
            themeMode = themeMode,
            gaplessEnabled = gapless,
            crossfadeSeconds = crossfade,
            excludedFolders = excludedFolders,
            isScanning = scanProgress.isScanning,
            onSetThemeMode = { viewModel.setThemeMode(it) },
            onSetGapless = { viewModel.setGaplessPlayback(it) },
            onSetCrossfade = { viewModel.setCrossfadeSeconds(it) },
            onRemoveExcludedFolder = { viewModel.removeExcludedFolder(it) },
            onTriggerScan = { viewModel.scanLibrary() },
            onDismiss = { isSettingsSheetOpen = false }
        )
    }

    if (songForPlaylistModal != null) {
        AddToPlaylistSheet(
            song = songForPlaylistModal,
            playlists = playlists,
            onAddToPlaylist = { playlistId, songId ->
                viewModel.addSongToPlaylist(playlistId, songId)
            },
            onCreatePlaylistAndAdd = { name, songId ->
                viewModel.createPlaylist(name, songId)
            },
            onDismiss = { songForPlaylistModal = null }
        )
    }
}
