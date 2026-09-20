package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val path: String,
    val trackNumber: Int,
    val discNumber: Int,
    val dateAdded: Long,
    val folderPath: String,
    val playCount: Int = 0,
    val lastPlayed: Long = 0L
) {
    fun toSong(): Song = Song(
        id = id,
        title = title,
        artist = artist,
        album = album,
        albumId = albumId,
        duration = duration,
        path = path,
        trackNumber = trackNumber,
        discNumber = discNumber,
        dateAdded = dateAdded,
        folderPath = folderPath,
        playCount = playCount,
        lastPlayed = lastPlayed
    )

    companion object {
        fun fromSong(song: Song): SongEntity = SongEntity(
            id = song.id,
            title = song.title,
            artist = song.artist,
            album = song.album,
            albumId = song.albumId,
            duration = song.duration,
            path = song.path,
            trackNumber = song.trackNumber,
            discNumber = song.discNumber,
            dateAdded = song.dateAdded,
            folderPath = song.folderPath,
            playCount = song.playCount,
            lastPlayed = song.lastPlayed
        )
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "playlist_songs",
    primaryKeys = ["playlistId", "songId", "position"],
    indices = [Index("playlistId"), Index("songId")]
)
data class PlaylistSongCrossRef(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)

@Entity(tableName = "queue_items")
data class QueueItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val songId: Long,
    val position: Int
)

@Entity(tableName = "excluded_folders")
data class ExcludedFolderEntity(
    @PrimaryKey val path: String,
    val name: String
)
