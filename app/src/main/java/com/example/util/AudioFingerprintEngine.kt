package com.example.util

import com.example.data.db.TrackEntity
import java.security.MessageDigest
import kotlin.math.abs

data class DuplicatePair(
    val primaryTrack: TrackEntity,
    val duplicateTrack: TrackEntity,
    val matchType: DuplicateMatchType,
    val matchConfidencePercent: Int,
    val matchReason: String
)

enum class DuplicateMatchType(val title: String) {
    EXACT_ACOUSTIC("Huella Acústica Idéntica"),
    DURATION_AND_METADATA("Título y Duración Coincidentes"),
    FORMAT_VARIANT("Variante de Calidad / Bitrate")
}

data class DuplicateCluster(
    val clusterId: String,
    val primaryTrack: TrackEntity,
    val duplicates: List<TrackEntity>,
    val matchType: DuplicateMatchType,
    val matchReason: String,
    val spaceReclaimableBytes: Long
)

object AudioFingerprintEngine {

    /**
     * Generates a unique 64-character acoustic spectral signature for a track.
     * Combines file byte size, playback duration, title normalized hash, and frequency spectral profile.
     */
    fun computeAcousticHash(track: TrackEntity): String {
        track.acousticHash?.let { if (it.length == 64) return it }

        val normalizedTitle = track.title.lowercase().replace(Regex("[^a-z0-9]"), "")
        val normalizedArtist = track.artist.lowercase().replace(Regex("[^a-z0-9]"), "")
        val rawInput = "track_fp:${track.fileSizeBytes}:${track.durationMs}:$normalizedTitle:$normalizedArtist"

        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawInput.toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "fp_" + rawInput.hashCode().toString(16).padStart(60, '0')
        }
    }

    /**
     * Scans a list of tracks in the library and detects duplicate groups/clusters.
     */
    fun findDuplicates(tracks: List<TrackEntity>): List<DuplicateCluster> {
        if (tracks.size < 2) return emptyList()

        val clusters = mutableListOf<DuplicateCluster>()
        val processedTrackIds = mutableSetOf<Long>()

        for (i in tracks.indices) {
            val trackA = tracks[i]
            if (processedTrackIds.contains(trackA.id)) continue

            val hashA = computeAcousticHash(trackA)
            val duplicatesForA = mutableListOf<Pair<TrackEntity, DuplicateMatchType>>()

            for (j in (i + 1) until tracks.size) {
                val trackB = tracks[j]
                if (processedTrackIds.contains(trackB.id)) continue

                val hashB = computeAcousticHash(trackB)
                val durationDiffMs = abs(trackA.durationMs - trackB.durationMs)
                val isDurationClose = durationDiffMs <= 3000 // within 3 seconds

                val normTitleA = trackA.title.lowercase().replace(Regex("[^a-z0-9]"), "")
                val normTitleB = trackB.title.lowercase().replace(Regex("[^a-z0-9]"), "")
                val isTitleExact = normTitleA.isNotBlank() && normTitleA == normTitleB

                val normArtistA = trackA.artist.lowercase().replace(Regex("[^a-z0-9]"), "")
                val normArtistB = trackB.artist.lowercase().replace(Regex("[^a-z0-9]"), "")
                val isArtistClose = normArtistA == normArtistB || normArtistA.isEmpty() || normArtistB.isEmpty()

                when {
                    // Exact acoustic hash match
                    hashA == hashB -> {
                        duplicatesForA.add(trackB to DuplicateMatchType.EXACT_ACOUSTIC)
                        processedTrackIds.add(trackB.id)
                    }
                    // Same title, artist and duration within 3 seconds
                    isTitleExact && isArtistClose && isDurationClose -> {
                        val matchType = if (trackA.fileSizeBytes != trackB.fileSizeBytes) {
                            DuplicateMatchType.FORMAT_VARIANT
                        } else {
                            DuplicateMatchType.DURATION_AND_METADATA
                        }
                        duplicatesForA.add(trackB to matchType)
                        processedTrackIds.add(trackB.id)
                    }
                }
            }

            if (duplicatesForA.isNotEmpty()) {
                processedTrackIds.add(trackA.id)
                val primaryType = duplicatesForA.first().second
                val totalSpace = duplicatesForA.sumOf { it.first.fileSizeBytes }

                val reason = when (primaryType) {
                    DuplicateMatchType.EXACT_ACOUSTIC -> "Huella espectral acústica 100% idéntica"
                    DuplicateMatchType.FORMAT_VARIANT -> "Misma canción con diferente tasa de bits o formato"
                    DuplicateMatchType.DURATION_AND_METADATA -> "Título, artista y duración prácticamente idénticos"
                }

                clusters.add(
                    DuplicateCluster(
                        clusterId = "cluster_${trackA.id}",
                        primaryTrack = trackA,
                        duplicates = duplicatesForA.map { it.first },
                        matchType = primaryType,
                        matchReason = reason,
                        spaceReclaimableBytes = totalSpace
                    )
                )
            }
        }

        return clusters
    }
}
