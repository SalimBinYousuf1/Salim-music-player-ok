package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.mediastore.ScanProgress
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.LibraryTab
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.SortOption
import com.example.ui.components.SalimIcons
import com.example.ui.components.SquircleArtwork
import com.example.ui.theme.LocalAdaptiveAccent
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.SalimSurfaceGlass
import com.example.ui.theme.SalimSurfaceGlassLight
import com.example.ui.theme.SquircleShape

@Composable
fun LibraryHomeScreen(
    songs: List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    folders: List<Folder>,
    currentSort: SortOption,
    scanProgress: ScanProgress,
    onSongClick: (Song, List<Song>, Int) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onSortOptionSelected: (SortOption) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onCreatePlaylist: () -> Unit,
    onRescanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val accent = LocalAdaptiveAccent.current
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    var showSortSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // App Top Header: Title "Salim", Search, Sort, Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Salim",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSearch()
                    },
                    modifier = Modifier.size(40.dp).testTag("header_search_button")
                ) {
                    SalimIcons.Search(size = 20.dp, tint = MaterialTheme.colorScheme.onSurface)
                }

                if (selectedTab == LibraryTab.SONGS) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showSortSheet = true
                        },
                        modifier = Modifier.size(40.dp).testTag("header_sort_button")
                    ) {
                        SalimIcons.Sort(size = 20.dp, tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onOpenSettings()
                    },
                    modifier = Modifier.size(40.dp).testTag("header_settings_button")
                ) {
                    SalimIcons.Settings(size = 20.dp, tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Real Scan Progress Notification (NOT a mock spinner)
        AnimatedVisibility(
            visible = scanProgress.isScanning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Scanning device audio...",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = accent
                    )
                    Text(
                        text = "${scanProgress.currentCount} / ${scanProgress.totalCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                val scanFrac = if (scanProgress.totalCount > 0) {
                    (scanProgress.currentCount.toFloat() / scanProgress.totalCount.toFloat()).coerceIn(0f, 1f)
                } else 0f
                LinearProgressIndicator(
                    progress = { scanFrac },
                    modifier = Modifier.fillMaxWidth().height(2.dp),
                    color = accent,
                    strokeCap = StrokeCap.Round
                )
            }
        }

        // Category Tab Bar
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 20.dp,
            containerColor = Color.Transparent,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    height = 2.dp,
                    color = accent
                )
            }
        ) {
            LibraryTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        selectedTab = tab
                    },
                    text = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize = 15.sp
                            ),
                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content Area based on selected category tab
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                LibraryTab.SONGS -> SongsList(
                    songs = songs,
                    onSongClick = onSongClick,
                    onSongLongClick = onSongLongClick,
                    onRescanClick = onRescanClick
                )
                LibraryTab.ALBUMS -> AlbumsGrid(
                    albums = albums,
                    onAlbumClick = onAlbumClick
                )
                LibraryTab.ARTISTS -> ArtistsList(
                    artists = artists,
                    onArtistClick = onArtistClick
                )
                LibraryTab.PLAYLISTS -> PlaylistsList(
                    playlists = playlists,
                    onPlaylistClick = onPlaylistClick,
                    onCreatePlaylist = onCreatePlaylist
                )
                LibraryTab.FOLDERS -> FoldersList(
                    folders = folders,
                    onFolderClick = onFolderClick
                )
            }
        }
    }

    if (showSortSheet) {
        SortSheet(
            currentSort = currentSort,
            onSelectSort = {
                onSortOptionSelected(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false }
        )
    }
}

@Composable
private fun SongsList(
    songs: List<Song>,
    onSongClick: (Song, List<Song>, Int) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onRescanClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (songs.isEmpty()) {
        EmptyLibraryState(
            title = "No songs found",
            subtitle = "Salim plays music stored locally on your device.\nAdd audio files to your phone's storage, then tap rescan.",
            actionLabel = "Rescan Storage",
            onAction = onRescanClick
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("songs_list"),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(songs, key = { _, s -> s.id }) { index, song ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSongClick(song, songs, index)
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SquircleArtwork(
                        uri = song.albumArtUri,
                        size = 46.dp,
                        cornerRadius = 12.dp,
                        elevation = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${song.artist} • ${song.album}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDuration(song.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSongLongClick(song)
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        SalimIcons.Plus(size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumsGrid(
    albums: List<Album>,
    onAlbumClick: (Album) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (albums.isEmpty()) {
        EmptyLibraryState(
            title = "No albums found",
            subtitle = "Your audio albums will appear here once audio files are indexed.",
            actionLabel = null,
            onAction = null
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("albums_grid"),
            contentPadding = PaddingValues(bottom = 96.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape(16.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAlbumClick(album)
                        }
                        .padding(4.dp)
                ) {
                    SquircleArtwork(
                        uri = album.albumArtUri,
                        size = 156.dp,
                        cornerRadius = 24.dp,
                        elevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${album.artist} • ${album.songCount} songs",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtistsList(
    artists: List<Artist>,
    onArtistClick: (Artist) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (artists.isEmpty()) {
        EmptyLibraryState(
            title = "No artists found",
            subtitle = "Artists in your local music collection will appear here.",
            actionLabel = null,
            onAction = null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("artists_list"),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(artists, key = { it.name }) { artist ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onArtistClick(artist)
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = artist.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${artist.songCount} songs • ${artist.albumCount} albums",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SalimIcons.ChevronRight(size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PlaylistsList(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("playlists_list")
    ) {
        // Create Playlist Action Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(SquircleShape(14.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCreatePlaylist()
                }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = SquircleShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                SalimIcons.Plus(size = 20.dp, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "New Playlist...",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (playlists.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No custom playlists yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShape(12.dp))
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onPlaylistClick(playlist)
                            }
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        SalimIcons.ChevronRight(size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun FoldersList(
    folders: List<Folder>,
    onFolderClick: (Folder) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    if (folders.isEmpty()) {
        EmptyLibraryState(
            title = "No folders found",
            subtitle = "Audio folders on internal storage will appear here.",
            actionLabel = null,
            onAction = null
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("folders_list"),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(folders, key = { it.path }) { folder ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFolderClick(folder)
                        }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SalimIcons.Folder(size = 28.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.name,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${folder.songCount} tracks • ${folder.path}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    SalimIcons.ChevronRight(size = 18.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryState(
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: (() -> Unit)?
) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SalimIcons.MusicNote(size = 48.dp, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onAction,
                    shape = SquircleShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text(actionLabel, color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    currentSort: SortOption,
    onSelectSort: (SortOption) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = LocalIsDarkTheme.current
    val accent = LocalAdaptiveAccent.current
    val haptic = LocalHapticFeedback.current

    val sheetBg = if (isDark) SalimSurfaceGlass else SalimSurfaceGlassLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        shape = SquircleShape(28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(SquircleShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
        ) {
            Text(
                text = "Sort Songs By",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            listOf(
                SortOption.TITLE to "Song Title",
                SortOption.ARTIST to "Artist Name",
                SortOption.DATE_ADDED to "Recently Added",
                SortOption.DURATION to "Duration"
            ).forEach { (option, label) ->
                val isSelected = currentSort == option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSelectSort(option)
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) accent else MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        SalimIcons.Check(size = 18.dp, tint = accent)
                    }
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSecs = (ms / 1000).coerceAtLeast(0)
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return "%d:%02d".format(mins, secs)
}
