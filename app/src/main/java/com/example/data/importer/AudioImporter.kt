package com.example.data.importer

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.data.db.TrackEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object AudioImporter {

    private const val TAG = "AudioImporter"

    /**
     * Inspects a list of audio URIs selected manually by the user, extracts ID3 tags,
     * extracts cover art if available, and returns a list of TrackEntity objects.
     */
    suspend fun processImportedUris(context: Context, uris: List<Uri>): List<TrackEntity> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<TrackEntity>()

        for (uri in uris) {
            try {
                // Take persistable permission if possible
                try {
                    val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flags)
                } catch (e: Exception) {
                    // Ignored if uri doesn't support persistable permissions
                }

                val fileName = getFileName(context, uri) ?: "Pista importada"
                
                // Skip if obvious junk non-audio or whatsapp voice notes if user selected folder by mistake,
                // but process audio formats (.mp3, .m4a, .flac, .aac, .wav, .ogg, .opus)
                if (!isAudioFile(fileName, context, uri)) {
                    Log.d(TAG, "Skipping non-audio file: $fileName")
                    continue
                }

                // Execute memory-safe Rust metadata parser verification for ID3/FLAC/OGG/WAV
                val rustMetadata = com.example.data.rust.RustMetadataParser.parseAudioTags(context, uri, fileName)
                Log.d(TAG, "Rust Tag Reader processed file ($fileName): Format=${rustMetadata.format}")

                var title = ""
                var artist = ""
                var album = ""
                var durationMs = 0L
                var coverPath: String? = null

                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)

                    title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: ""
                    artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: ""
                    album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: ""
                    
                    val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    durationMs = durationStr?.toLongOrNull() ?: 0L

                    val rawLyrics = try {
                        retriever.extractMetadata(1000) ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE + 5)
                    } catch (e: Exception) {
                        null
                    }

                    // Extract embedded album art or generate seed-based WebP cover art
                    val embeddedPicture = retriever.embeddedPicture
                    if (embeddedPicture != null && embeddedPicture.isNotEmpty()) {
                        coverPath = com.example.data.storage.LocalStorageManager.saveWebpCoverFromBytes(
                            context,
                            embeddedPicture,
                            uri.hashCode().toString()
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error reading metadata for $uri", e)
                } finally {
                    try {
                        retriever.release()
                    } catch (e: Exception) {
                        // Ignore
                    }
                }

                // Fallbacks for empty metadata
                if (title.isBlank()) {
                    title = cleanFileName(fileName)
                }
                if (artist.isBlank()) {
                    artist = "Artista desconocido"
                }
                if (album.isBlank()) {
                    album = "Álbum local"
                }

                // If no embedded cover art was found, generate seed-based WebP cover art
                if (coverPath == null) {
                    coverPath = com.example.data.storage.LocalStorageManager.generateSeedWebpCover(
                        context = context,
                        title = title,
                        artist = artist,
                        seedInput = uri.toString()
                    )
                }

                val fileSize = getFileSize(context, uri)

                tracks.add(
                    TrackEntity(
                        uriString = uri.toString(),
                        title = title,
                        artist = artist,
                        album = album,
                        durationMs = durationMs,
                        fileSizeBytes = fileSize,
                        folderName = getFolderNameFromUri(uri) ?: "Mi Música",
                        dateImported = System.currentTimeMillis(),
                        coverArtPath = coverPath,
                        isFavorite = false,
                        playCount = 0,
                        isSample = false
                    )
                )

            } catch (e: Exception) {
                Log.e(TAG, "Failed to import file $uri", e)
            }
        }

        tracks
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    private fun getFileSize(context: Context, uri: Uri): Long {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1 && cursor.moveToFirst()) {
                    return cursor.getLong(sizeIndex)
                }
            }
        }
        return 0L
    }

    private fun cleanFileName(fileName: String): String {
        val nameWithoutExt = fileName.substringBeforeLast(".")
        return nameWithoutExt.replace("_", " ").replace("-", " ").trim()
    }

    private fun isAudioFile(fileName: String, context: Context, uri: Uri): Boolean {
        val lower = fileName.lowercase()
        val validExts = listOf(".mp3", ".m4a", ".aac", ".flac", ".wav", ".ogg", ".opus", ".3gp")
        if (validExts.any { lower.endsWith(it) }) return true

        val mime = context.contentResolver.getType(uri)
        return mime != null && (mime.startsWith("audio/") || mime == "application/ogg")
    }

    private fun getFolderNameFromUri(uri: Uri): String? {
        val path = uri.path ?: return null
        val segments = path.split("/")
        return if (segments.size > 2) segments[segments.size - 2] else null
    }

    private fun saveCoverToCache(context: Context, bytes: ByteArray, hash: Int): String? {
        return try {
            val dir = File(context.cacheDir, "album_covers")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "cover_$hash.jpg")
            FileOutputStream(file).use { out ->
                out.write(bytes)
            }
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save album cover", e)
            null
        }
    }
}
