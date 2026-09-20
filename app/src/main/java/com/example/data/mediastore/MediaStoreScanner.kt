package com.example.data.mediastore

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.local.ExcludedFolderEntity
import com.example.data.local.MusicDao
import com.example.data.local.SongEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

data class ScanProgress(
    val isScanning: Boolean = false,
    val currentCount: Int = 0,
    val totalCount: Int = 0
)

class MediaStoreScanner(
    private val context: Context,
    private val musicDao: MusicDao
) {
    private val _scanProgress = MutableStateFlow(ScanProgress())
    val scanProgress: StateFlow<ScanProgress> = _scanProgress.asStateFlow()

    suspend fun scanLibrary(): Int = withContext(Dispatchers.IO) {
        _scanProgress.value = ScanProgress(isScanning = true, currentCount = 0, totalCount = 0)

        val excludedFolders = musicDao.getExcludedFolders().first().map { it.path }

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATE_ADDED
        )

        // Only music tracks longer than 10 seconds to exclude notification sounds
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val songs = mutableListOf<SongEntity>()
        val validSongIds = mutableListOf<Long>()

        try {
            val cursor: Cursor? = context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                null,
                sortOrder
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val trackCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
                val dateAddedCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

                val total = c.count
                _scanProgress.value = ScanProgress(isScanning = true, currentCount = 0, totalCount = total)

                var count = 0
                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val title = c.getString(titleCol) ?: "Unknown Title"
                    val artist = c.getString(artistCol) ?: "Unknown Artist"
                    val album = c.getString(albumCol) ?: "Unknown Album"
                    val albumId = c.getLong(albumIdCol)
                    val duration = c.getLong(durationCol)
                    val path = c.getString(dataCol) ?: ""
                    val trackRaw = c.getInt(trackCol)
                    val dateAdded = c.getLong(dateAddedCol)

                    // Track number parsing (can be 1001 for disc 1 track 1)
                    val discNumber = if (trackRaw >= 1000) trackRaw / 1000 else 1
                    val trackNumber = if (trackRaw >= 1000) trackRaw % 1000 else trackRaw

                    val file = File(path)
                    val folderPath = file.parent ?: ""

                    // Check if folder is excluded
                    val isExcluded = excludedFolders.any { excluded ->
                        folderPath.startsWith(excluded, ignoreCase = true)
                    }

                    if (!isExcluded && file.exists()) {
                        val songEntity = SongEntity(
                            id = id,
                            title = title.trim(),
                            artist = if (artist.trim() == "<unknown>") "Unknown Artist" else artist.trim(),
                            album = if (album.trim() == "<unknown>") "Unknown Album" else album.trim(),
                            albumId = albumId,
                            duration = duration,
                            path = path,
                            trackNumber = trackNumber,
                            discNumber = discNumber,
                            dateAdded = dateAdded,
                            folderPath = folderPath
                        )
                        songs.add(songEntity)
                        validSongIds.add(id)
                    }

                    count++
                    if (count % 15 == 0 || count == total) {
                        _scanProgress.value = ScanProgress(isScanning = true, currentCount = count, totalCount = total)
                    }
                }
            }

            if (songs.isNotEmpty()) {
                musicDao.insertSongs(songs)
                musicDao.deleteStaleSongs(validSongIds)
            } else {
                musicDao.clearSongs()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _scanProgress.value = ScanProgress(isScanning = false, currentCount = songs.size, totalCount = songs.size)
        }

        return@withContext songs.size
    }
}
