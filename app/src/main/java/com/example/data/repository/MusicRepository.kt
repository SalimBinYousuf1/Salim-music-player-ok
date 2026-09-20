package com.example.data.repository

import com.example.data.local.ExcludedFolderEntity
import com.example.data.local.MusicDao
import com.example.data.local.PlaylistEntity
import com.example.data.local.PreferencesManager
import com.example.data.local.SongEntity
import com.example.data.model.Album
import com.example.data.model.Artist
import com.example.data.model.Folder
import com.example.data.model.Playlist
import com.example.data.model.Song
import com.example.data.model.SortOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class MusicRepository(
    private val musicDao: MusicDao,
    private val preferencesManager: PreferencesManager
) {

    val allSongs: Flow<List<Song>> = combine(
        musicDao.getAllSongs(),
        preferencesManager.sortOption
    ) { entities, sort ->
        val songs = entities.map { it.toSong() }
        when (sort) {
            SortOption.TITLE -> songs.sortedBy { it.title.lowercase() }
            SortOption.ARTIST -> songs.sortedWith(compareBy({ it.artist.lowercase() }, { it.title.lowercase() }))
            SortOption.DATE_ADDED -> songs.sortedByDescending { it.dateAdded }
            SortOption.DURATION -> songs.sortedByDescending { it.duration }
        }
    }

    val allAlbums: Flow<List<Album>> = musicDao.getAllSongs().map { entities ->
        entities.groupBy { it.albumId }
            .map { (albumId, songs) ->
                val first = songs.first()
                Album(
                    id = albumId,
                    title = first.album,
                    artist = first.artist,
                    songCount = songs.size
                )
            }
            .sortedBy { it.title.lowercase() }
    }

    val allArtists: Flow<List<Artist>> = musicDao.getAllSongs().map { entities ->
        entities.groupBy { it.artist.lowercase() }
            .map { (_, songs) ->
                val first = songs.first()
                val albumCount = songs.map { it.albumId }.distinct().size
                Artist(
                    id = first.id,
                    name = first.artist,
                    songCount = songs.size,
                    albumCount = albumCount
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    val allFolders: Flow<List<Folder>> = musicDao.getAllSongs().map { entities ->
        entities.groupBy { it.folderPath }
            .filter { it.key.isNotEmpty() }
            .map { (path, songs) ->
                val name = File(path).name.ifEmpty { path }
                Folder(
                    path = path,
                    name = name,
                    songCount = songs.size
                )
            }
            .sortedBy { it.name.lowercase() }
    }

    val allPlaylists: Flow<List<Playlist>> = musicDao.getAllPlaylists().map { entities ->
        entities.map { entity ->
            Playlist(
                id = entity.id,
                name = entity.name,
                createdAt = entity.createdAt
            )
        }
    }

    fun getSongsForAlbum(albumId: Long): Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.filter { it.albumId == albumId }
            .map { it.toSong() }
            .sortedWith(compareBy({ it.discNumber }, { it.trackNumber }, { it.title.lowercase() }))
    }

    fun getSongsForArtist(artistName: String): Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.filter { it.artist.equals(artistName, ignoreCase = true) }
            .map { it.toSong() }
            .sortedWith(compareBy({ it.album.lowercase() }, { it.trackNumber }, { it.title.lowercase() }))
    }

    fun getSongsForFolder(folderPath: String): Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        entities.filter { it.folderPath == folderPath }
            .map { it.toSong() }
            .sortedBy { it.title.lowercase() }
    }

    fun getSongsForPlaylist(playlistId: Long): Flow<List<Song>> =
        musicDao.getSongsForPlaylist(playlistId).map { entities ->
            entities.map { it.toSong() }
        }

    fun search(query: String): Flow<List<Song>> = musicDao.getAllSongs().map { entities ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val q = query.trim().lowercase()
            entities.filter {
                it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
            }.map { it.toSong() }
        }
    }

    suspend fun createPlaylist(name: String): Long {
        return musicDao.insertPlaylist(PlaylistEntity(name = name.trim()))
    }

    suspend fun renamePlaylist(id: Long, newName: String) {
        musicDao.updatePlaylistName(id, newName.trim())
    }

    suspend fun deletePlaylist(id: Long) {
        musicDao.deletePlaylistSongs(id)
        musicDao.deletePlaylist(id)
    }

    suspend fun addSongToPlaylist(playlistId: Long, songId: Long) {
        musicDao.addSongToPlaylist(playlistId, songId)
    }

    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        musicDao.removeSongFromPlaylist(playlistId, songId)
    }

    suspend fun recordPlay(songId: Long) {
        musicDao.recordPlay(songId)
    }

    val excludedFolders: Flow<List<ExcludedFolderEntity>> = musicDao.getExcludedFolders()

    suspend fun addExcludedFolder(path: String, name: String) {
        musicDao.insertExcludedFolder(ExcludedFolderEntity(path = path, name = name))
    }

    suspend fun removeExcludedFolder(path: String) {
        musicDao.deleteExcludedFolder(path)
    }
}
