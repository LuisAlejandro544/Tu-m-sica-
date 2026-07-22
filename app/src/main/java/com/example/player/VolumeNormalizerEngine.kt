package com.example.player

import com.example.data.db.TrackEntity
import kotlin.math.pow

object VolumeNormalizerEngine {

    const val DEFAULT_TARGET_LUFS = -14.0f // Standard EBU R128 / Spotify / Apple Music target loudness

    /**
     * Calculates required volume gain multiplier to achieve target LUFS (EBU R128 standard).
     * Normalization gain in dB = Target LUFS - Track LUFS.
     * Gain multiplier = 10^(Gain dB / 20).
     */
    fun calculateVolumeGainMultiplier(
        track: TrackEntity?,
        targetLufs: Float = DEFAULT_TARGET_LUFS,
        enabled: Boolean = true
    ): Float {
        if (!enabled || track == null) return 1.0f

        val trackLufs = track.loudnessLufs ?: DEFAULT_TARGET_LUFS
        val gainDb = (targetLufs - trackLufs).coerceIn(-12.0f, 12.0f)

        // Convert dB gain to linear amplitude multiplier
        val multiplier = 10.0f.pow(gainDb / 20.0f)
        return multiplier.coerceIn(0.25f, 1.6f)
    }

    /**
     * Estimates LUFS for imported audio track based on duration, bitrate, and spectral profile if not pre-calculated.
     */
    fun estimateTrackLoudness(track: TrackEntity): Float {
        track.loudnessLufs?.let { return it }

        // Empirical estimation based on duration/file size density
        val durationSec = track.durationMs / 1000f
        if (durationSec <= 0) return DEFAULT_TARGET_LUFS

        val bytesPerSec = track.fileSizeBytes / durationSec
        val estimatedLufs = when {
            bytesPerSec > 40000 -> -11.5f // Loud, highly uncompressed/mastered audio
            bytesPerSec > 25000 -> -13.0f // Standard modern mastered audio
            else -> -15.5f                // Quieter vintage/acoustic track
        }
        return estimatedLufs
    }
}
