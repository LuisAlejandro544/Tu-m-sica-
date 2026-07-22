package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistTrackCrossRef
import com.example.data.db.TrackEntity
import com.example.data.importer.AudioImporter
import com.example.data.importer.SampleAudioGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter

class MusicRepository(private val db: AppDatabase) {

    private val trackDao = db.trackDao()
    private val playlistDao = db.playlistDao()

    val allTracks: Flow<List<TrackEntity>> = trackDao.getAllTracks()
    val favoriteTracks: Flow<List<TrackEntity>> = trackDao.getFavoriteTracks()
    val recentTracks: Flow<List<TrackEntity>> = trackDao.getRecentTracks()
    val folders: Flow<List<String>> = trackDao.getFolders()
    val playlists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    suspend fun loadDemoTracksIfEmpty(context: Context) = withContext(Dispatchers.IO) {
        val currentTracks = trackDao.getAllTracks().first()
        if (currentTracks.isEmpty()) {
            val demos = SampleAudioGenerator.createDemoTracks(context)
            trackDao.insertTracks(demos)

            // Create default "Canciones que te gustan" playlist entry or favorites setup
            val favs = demos.filter { it.isFavorite }
            if (favs.isNotEmpty()) {
                val favPlaylistId = playlistDao.insertPlaylist(
                    PlaylistEntity(
                        name = "Canciones que te gustan",
                        description = "Tus canciones favoritas importadas localmente"
                    )
                )
                demos.forEach { track ->
                    if (track.isFavorite) {
                        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(favPlaylistId, track.id))
                    }
                }
            }
            com.example.data.storage.LocalStorageManager.syncMetadataJsonCache(context, demos)
        } else {
            com.example.data.storage.LocalStorageManager.syncMetadataJsonCache(context, currentTracks)
        }
    }

    suspend fun importUris(context: Context, uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val newTracks = AudioImporter.processImportedUris(context, uris)
        if (newTracks.isNotEmpty()) {
            trackDao.insertTracks(newTracks)
            val updatedAll = trackDao.getAllTracks().first()
            com.example.data.storage.LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        }
        newTracks.size
    }

    suspend fun updateCustomCoverArt(context: Context, track: TrackEntity, imageUri: Uri): Boolean = withContext(Dispatchers.IO) {
        val webpPath = com.example.data.storage.LocalStorageManager.saveCustomWebpCoverFromUri(
            context = context,
            imageUri = imageUri,
            trackId = track.id
        ) ?: return@withContext false

        val updatedTrack = track.copy(coverArtPath = webpPath)
        trackDao.updateTrack(updatedTrack)

        val updatedAll = trackDao.getAllTracks().first()
        com.example.data.storage.LocalStorageManager.syncMetadataJsonCache(context, updatedAll)
        true
    }

    suspend fun toggleFavorite(track: TrackEntity) = withContext(Dispatchers.IO) {
        val updated = track.copy(isFavorite = !track.isFavorite)
        trackDao.updateTrack(updated)
    }

    suspend fun updateTrack(track: TrackEntity) = withContext(Dispatchers.IO) {
        trackDao.updateTrack(track)
    }

    suspend fun deleteTrack(track: TrackEntity) = withContext(Dispatchers.IO) {
        trackDao.deleteTrack(track)
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylist(
            PlaylistEntity(
                name = name,
                description = description
            )
        )
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId, trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    fun getTracksForPlaylist(playlistId: Long): Flow<List<TrackEntity>> {
        return playlistDao.getTracksForPlaylist(playlistId)
    }

    fun getTracksByFolder(folderName: String): Flow<List<TrackEntity>> {
        return trackDao.getTracksByFolder(folderName)
    }

    fun searchTracks(query: String): Flow<List<TrackEntity>> {
        return trackDao.searchTracks(query)
    }

    suspend fun exportLibraryToJson(context: Context, destinationUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val tracks = trackDao.getAllTracks().first()
            val playlistsList = playlistDao.getAllPlaylists().first()

            val rootJson = JSONObject()
            rootJson.put("app", "SpotLocal")
            rootJson.put("version", "1.0")
            rootJson.put("exportedAt", System.currentTimeMillis())

            val tracksArray = JSONArray()
            tracks.forEach { t ->
                val trackObj = JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("artist", t.artist)
                    put("album", t.album)
                    put("durationMs", t.durationMs)
                    put("uriString", t.uriString)
                    put("folderName", t.folderName)
                    put("isFavorite", t.isFavorite)
                }
                tracksArray.put(trackObj)
            }
            rootJson.put("tracks", tracksArray)

            val playlistArray = JSONArray()
            playlistsList.forEach { p ->
                val pObj = JSONObject().apply {
                    put("id", p.id)
                    put("name", p.name)
                    put("description", p.description)
                }
                playlistArray.put(pObj)
            }
            rootJson.put("playlists", playlistArray)

            context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(rootJson.toString(2))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateLyrics(context: Context, track: TrackEntity, lyricsText: String) = withContext(Dispatchers.IO) {
        val updated = track.copy(lyrics = lyricsText.ifBlank { null })
        trackDao.updateTrack(updated)
        val all = trackDao.getAllTracks().first()
        com.example.data.storage.LocalStorageManager.syncMetadataJsonCache(context, all)
    }

    suspend fun clearAppCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        com.example.data.storage.LocalStorageManager.clearAllCache(context)
    }
}
