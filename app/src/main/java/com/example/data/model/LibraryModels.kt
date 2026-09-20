package com.example.data.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val year: Int = 0
) {
    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$id")
}

data class Artist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int = 0
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int
)

enum class LibraryTab(val label: String) {
    SONGS("Songs"),
    ALBUMS("Albums"),
    ARTISTS("Artists"),
    PLAYLISTS("Playlists"),
    FOLDERS("Folders")
}

enum class SortOption(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    DATE_ADDED("Recently Added"),
    DURATION("Duration")
}

enum class RepeatState {
    OFF,
    ALL,
    ONE
}
