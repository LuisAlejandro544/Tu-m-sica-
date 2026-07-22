package com.example.util

import com.example.data.db.TrackEntity

data class TagCorrectionResult(
    val originalTrack: TrackEntity,
    val cleanedTrack: TrackEntity,
    val hasChanges: Boolean,
    val changesDescription: List<String>
)

object Id3TagCleaner {

    private val JUNK_PATTERNS = listOf(
        Regex("(?i)\\s*[\\[\\(\\{](official|audio|video|music video|lyric video|hd|4k|1080p|mp3|ytmp3|remastered|hq|clip|extended)[\\]\\)\\}]\\s*"),
        Regex("(?i)\\s*[\\[\\(\\{]ytmp3\\.cc[\\]\\)\\}]\\s*"),
        Regex("(?i)ytmp3\\.cc"),
        Regex("(?i)www\\.[a-zA-Z0-9-]+\\.(com|net|org|cc|io)"),
        Regex("(?i)\\.mp3$"),
        Regex("(?i)\\.flac$"),
        Regex("(?i)\\.wav$"),
        Regex("(?i)\\.ogg$")
    )

    /**
     * Cleans metadata tags (title, artist, album) for a single TrackEntity.
     */
    fun cleanTrack(track: TrackEntity): TagCorrectionResult {
        val changes = mutableListOf<String>()
        var newTitle = track.title
        var newArtist = track.artist
        var newAlbum = track.album

        // 1. Clean Junk Patterns from Title
        var titleCleaned = newTitle
        JUNK_PATTERNS.forEach { pattern ->
            if (pattern.containsMatchIn(titleCleaned)) {
                titleCleaned = pattern.replace(titleCleaned, " ")
            }
        }
        titleCleaned = titleCleaned.replace("_", " ").replace(Regex("\\s+"), " ").trim()

        // 2. Auto-Extract "Artist - Title" from Title if Title contains hyphen and Artist is empty/unknown
        if ((newArtist.isBlank() || newArtist.equals("Desconocido", ignoreCase = true) || newArtist.equals("Unknown Artist", ignoreCase = true)) && titleCleaned.contains(" - ")) {
            val parts = titleCleaned.split(" - ", limit = 2)
            if (parts.size == 2 && parts[0].trim().isNotBlank() && parts[1].trim().isNotBlank()) {
                newArtist = toTitleCase(parts[0].trim())
                titleCleaned = parts[1].trim()
                changes.add("Extraído artista de nombre de archivo: '${newArtist}'")
            }
        }

        // 3. Format Title to Title Case
        val titleFormatted = toTitleCase(titleCleaned)
        if (titleFormatted != track.title) {
            changes.add("Título limpiado: '${track.title}' ➔ '$titleFormatted'")
            newTitle = titleFormatted
        }

        // 4. Format Artist
        var artistCleaned = newArtist
        JUNK_PATTERNS.forEach { pattern ->
            if (pattern.containsMatchIn(artistCleaned)) {
                artistCleaned = pattern.replace(artistCleaned, " ")
            }
        }
        artistCleaned = toTitleCase(artistCleaned.replace("_", " ").replace(Regex("\\s+"), " ").trim())
        if (artistCleaned.isBlank()) artistCleaned = "Artista Desconocido"
        if (artistCleaned != track.artist) {
            changes.add("Artista corregido: '${track.artist}' ➔ '$artistCleaned'")
            newArtist = artistCleaned
        }

        // 5. Format Album
        var albumCleaned = newAlbum
        JUNK_PATTERNS.forEach { pattern ->
            if (pattern.containsMatchIn(albumCleaned)) {
                albumCleaned = pattern.replace(albumCleaned, " ")
            }
        }
        albumCleaned = toTitleCase(albumCleaned.replace("_", " ").replace(Regex("\\s+"), " ").trim())
        if (albumCleaned.isBlank()) albumCleaned = "Álbum Desconocido"
        if (albumCleaned != track.album) {
            newAlbum = albumCleaned
        }

        val cleanedEntity = track.copy(
            title = newTitle,
            artist = newArtist,
            album = newAlbum
        )

        return TagCorrectionResult(
            originalTrack = track,
            cleanedTrack = cleanedEntity,
            hasChanges = changes.isNotEmpty(),
            changesDescription = changes
        )
    }

    /**
     * Clean a batch of tracks.
     */
    fun cleanTracksBatch(tracks: List<TrackEntity>): List<TagCorrectionResult> {
        return tracks.map { cleanTrack(it) }
    }

    private fun toTitleCase(input: String): String {
        if (input.isBlank()) return input
        val acronyms = setOf("AC/DC", "R.E.M.", "EDM", "DJ", "FT", "FEAT", "VS", "MC", "EP", "LP")
        return input.split(" ").joinToString(" ") { word ->
            val upperWord = word.uppercase()
            if (acronyms.contains(upperWord)) {
                upperWord
            } else if (word.length <= 1) {
                word.uppercase()
            } else {
                word.substring(0, 1).uppercase() + word.substring(1).lowercase()
            }
        }
    }
}
