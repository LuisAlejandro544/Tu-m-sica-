package com.example.player

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.data.ai.StemMode

class AudioDspEngine {

    companion object {
        private const val TAG = "AudioDspEngine"

        init {
            try {
                System.loadLibrary("native-audio")
                Log.i(TAG, "Native C++ Oboe Audio Engine loaded successfully")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library native-audio not loaded, falling back to Android MediaPlayer DSP")
            }
        }
    }

    private var currentSpeed: Float = 1.0f
    private var currentPitch: Float = 1.0f
    private var equalizer: Equalizer? = null
    private var virtualizer: Virtualizer? = null
    private var bassBoost: BassBoost? = null

    private var currentStemMode: StemMode = StemMode.ORIGINAL
    private var is3dAudioEnabled: Boolean = false
    private var audio3dStrength: Short = 800 // 0 to 1000
    private var audio3dSpeakerMode: Audio3dSpeakerMode = Audio3dSpeakerMode.DUAL_SPEAKER

    fun attachEqualizer(audioSessionId: Int) {
        try {
            releaseEqualizer()
            if (audioSessionId != 0) {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = true
                }
                Log.i(TAG, "Equalizer attached to audioSessionId $audioSessionId successfully")
                applyStemMode(currentStemMode)

                try {
                    virtualizer = Virtualizer(0, audioSessionId).apply {
                        enabled = is3dAudioEnabled
                        if (strengthSupported) {
                            setStrength(if (is3dAudioEnabled) audio3dStrength else 0)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Virtualizer init error: ${e.message}")
                }

                try {
                    bassBoost = BassBoost(0, audioSessionId).apply {
                        enabled = is3dAudioEnabled
                        if (strengthSupported) {
                            setStrength((audio3dStrength * 0.7f).toInt().toShort())
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "BassBoost init error: ${e.message}")
                }

                apply3dAudioFx()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching AudioFX: ${e.message}")
        }
    }

    fun releaseEqualizer() {
        try {
            equalizer?.enabled = false
            equalizer?.release()
        } catch (e: Exception) {
            // ignore
        }
        equalizer = null

        try {
            virtualizer?.enabled = false
            virtualizer?.release()
        } catch (e: Exception) {
            // ignore
        }
        virtualizer = null

        try {
            bassBoost?.enabled = false
            bassBoost?.release()
        } catch (e: Exception) {
            // ignore
        }
        bassBoost = null
    }

    fun apply3dAudioFx(
        enabled: Boolean = is3dAudioEnabled,
        strength: Short = audio3dStrength,
        mode: Audio3dSpeakerMode = audio3dSpeakerMode
    ) {
        is3dAudioEnabled = enabled
        audio3dStrength = strength
        audio3dSpeakerMode = mode

        try {
            virtualizer?.let { virt ->
                virt.enabled = enabled
                if (virt.strengthSupported) {
                    val virtStrength = when (mode) {
                        Audio3dSpeakerMode.DUAL_SPEAKER -> strength
                        Audio3dSpeakerMode.SINGLE_SPEAKER -> (strength * 0.9f).toInt().toShort()
                        Audio3dSpeakerMode.HEADPHONES_3D -> strength
                    }
                    virt.setStrength(virtStrength.coerceIn(0, 1000))
                }
            }

            bassBoost?.let { bb ->
                bb.enabled = enabled
                if (bb.strengthSupported) {
                    val bassStrength = when (mode) {
                        Audio3dSpeakerMode.SINGLE_SPEAKER -> (strength * 0.95f).toInt().toShort()
                        Audio3dSpeakerMode.DUAL_SPEAKER -> (strength * 0.65f).toInt().toShort()
                        Audio3dSpeakerMode.HEADPHONES_3D -> (strength * 0.5f).toInt().toShort()
                    }
                    bb.setStrength(bassStrength.coerceIn(0, 1000))
                }
            }
            Log.i(TAG, "Applied 3D Audio FX: enabled=$enabled, mode=$mode, strength=$strength")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying 3D Audio FX: ${e.message}")
        }
    }

    fun applyStemMode(mode: StemMode) {
        currentStemMode = mode
        val eq = equalizer ?: return
        try {
            if (!eq.enabled) eq.enabled = true
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minEQLevel = eq.bandLevelRange[0] // Typically -1500 mB (-15dB)
            val maxEQLevel = eq.bandLevelRange[1] // Typically +1500 mB (+15dB)

            for (i in 0 until numBands) {
                val band = i.toShort()
                val centerFreqHz = eq.getCenterFreq(band) / 1000 // Freq in Hz

                val level: Short = when (mode) {
                    StemMode.ORIGINAL -> 0.toShort()
                    StemMode.VOCALS_ONLY -> {
                        // Boost vocal frequencies (500Hz - 4kHz), suppress bass and high treble
                        if (centerFreqHz in 500..4000) {
                            (maxEQLevel * 0.85f).toInt().toShort()
                        } else {
                            (minEQLevel * 0.9f).toInt().toShort()
                        }
                    }
                    StemMode.INSTRUMENTAL -> {
                        // Suppress human vocal mid range (500Hz - 3.5kHz), boost bass and treble
                        if (centerFreqHz in 500..3500) {
                            (minEQLevel * 0.95f).toInt().toShort()
                        } else {
                            (maxEQLevel * 0.6f).toInt().toShort()
                        }
                    }
                    StemMode.KARAOKE -> {
                        // Attenuate vocal band by ~60%, boost backing instrumental
                        if (centerFreqHz in 600..3000) {
                            (minEQLevel * 0.6f).toInt().toShort()
                        } else {
                            (maxEQLevel * 0.3f).toInt().toShort()
                        }
                    }
                }
                eq.setBandLevel(band, level.coerceIn(minEQLevel, maxEQLevel))
            }
            Log.i(TAG, "Applied StemMode $mode to Equalizer ($numBands bands)")
        } catch (e: Exception) {
            Log.e(TAG, "Error setting Equalizer band levels for mode $mode: ${e.message}")
        }
    }

    fun applyDspParams(mediaPlayer: MediaPlayer?, speed: Float, pitch: Float) {
        currentSpeed = speed.coerceIn(0.25f, 2.0f)
        currentPitch = pitch.coerceIn(0.25f, 2.0f)

        mediaPlayer?.let { player ->
            try {
                val params: PlaybackParams = player.playbackParams ?: PlaybackParams()
                params.speed = currentSpeed
                params.pitch = currentPitch
                player.playbackParams = params
            } catch (e: Exception) {
                Log.e(TAG, "Error applying PlaybackParams: ${e.message}")
            }
        }
    }

    fun applyCustomEqBands(isEqEnabled: Boolean, bandGainsDb: FloatArray) {
        val eq = equalizer ?: return
        try {
            if (!isEqEnabled) {
                applyStemMode(currentStemMode)
                return
            }
            if (!eq.enabled) eq.enabled = true
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minEQLevel = eq.bandLevelRange[0] // -1500 mB
            val maxEQLevel = eq.bandLevelRange[1] // +1500 mB

            for (i in 0 until numBands) {
                val band = i.toShort()
                val gainIndex = (i * 5 / numBands).coerceIn(0, 4)
                val gainDb = bandGainsDb.getOrElse(gainIndex) { 0f }
                val levelMb = (gainDb * 100f).toInt().toShort()
                eq.setBandLevel(band, levelMb.coerceIn(minEQLevel, maxEQLevel))
            }

            // Calculate native C++ biquad filter coefficients for hardware DSP
            OboeNativeAudioEngine.calculateBiquadCoefficients(bandGainsDb)

            Log.i(TAG, "Applied custom 5-band Equalizer settings via C++ & Rust engines")
        } catch (e: Exception) {
            Log.e(TAG, "Error applying custom EQ bands: ${e.message}")
        }
    }

}
