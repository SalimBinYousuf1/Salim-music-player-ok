package com.example.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val trackNumber: Int = 0,
    val discNumber: Int = 0,
    val dateAdded: Long = 0,
    val folderPath: String = "",
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
) {
    val contentUri: Uri
        get() = Uri.parse("content://media/external/audio/media/$id")

    val albumArtUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")

    val formattedDuration: String
        get() {
            val totalSecs = (duration / 1000).coerceAtLeast(0)
            val minutes = totalSecs / 60
            val seconds = totalSecs % 60
            return "%d:%02d".format(minutes, seconds)
        }
}
