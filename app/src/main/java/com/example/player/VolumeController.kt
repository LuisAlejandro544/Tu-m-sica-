package com.example.player

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import com.example.util.DebugLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VolumeState(
    val currentVolume: Int = 10,
    val maxVolume: Int = 15,
    val volumePercent: Float = 0.66f, // 0.0 to 1.0
    val isMuted: Boolean = false,
    val isVisible: Boolean = false
)

class VolumeController(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _volumeState = MutableStateFlow(VolumeState())
    val volumeState: StateFlow<VolumeState> = _volumeState.asStateFlow()

    private var previousNonMuteVolume = 10
    private var hideHandler = Handler(Looper.getMainLooper())
    private var hideRunnable = Runnable {
        _volumeState.value = _volumeState.value.copy(isVisible = false)
    }

    private val volumeObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            syncVolumeFromSystem(showHud = true)
        }
    }

    init {
        syncVolumeFromSystem(showHud = false)
        try {
            val volumeUri = Settings.System.getUriFor("volume_music") ?: Settings.System.CONTENT_URI
            context.contentResolver.registerContentObserver(
                volumeUri,
                true,
                volumeObserver
            )
        } catch (e: Exception) {
            DebugLogger.logWarning("VolumeController", "No se pudo registrar ContentObserver de volumen: ${e.message}")
        }
    }

    fun syncVolumeFromSystem(showHud: Boolean = true) {
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val percent = (current.toFloat() / max.toFloat()).coerceIn(0f, 1f)
        val isMuted = current == 0

        _volumeState.value = VolumeState(
            currentVolume = current,
            maxVolume = max,
            volumePercent = percent,
            isMuted = isMuted,
            isVisible = if (showHud) true else _volumeState.value.isVisible
        )

        if (showHud) {
            scheduleHudDismiss()
        }
    }

    fun setVolumePercent(percent: Float, showHud: Boolean = true) {
        val safePercent = percent.coerceIn(0f, 1f)
        val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
        val targetVolume = (safePercent * max).toInt().coerceIn(0, max)

        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
            if (targetVolume > 0) {
                previousNonMuteVolume = targetVolume
            }
        } catch (e: Exception) {
            DebugLogger.logWarning("VolumeController", "Error cambiando volumen: ${e.message}")
        }

        _volumeState.value = VolumeState(
            currentVolume = targetVolume,
            maxVolume = max,
            volumePercent = safePercent,
            isMuted = targetVolume == 0,
            isVisible = showHud
        )

        if (showHud) {
            scheduleHudDismiss()
        }
    }

    fun adjustVolume(deltaStep: Int) {
        val current = _volumeState.value.currentVolume
        val max = _volumeState.value.maxVolume
        val newVol = (current + deltaStep).coerceIn(0, max)
        val newPercent = newVol.toFloat() / max.toFloat()

        setVolumePercent(newPercent, showHud = true)
    }

    fun toggleMute() {
        if (_volumeState.value.isMuted) {
            val restoreVol = if (previousNonMuteVolume > 0) previousNonMuteVolume else (_volumeState.value.maxVolume / 2)
            val percent = restoreVol.toFloat() / _volumeState.value.maxVolume.toFloat()
            setVolumePercent(percent, showHud = true)
        } else {
            previousNonMuteVolume = _volumeState.value.currentVolume.coerceAtLeast(1)
            setVolumePercent(0f, showHud = true)
        }
    }

    fun showHud() {
        _volumeState.value = _volumeState.value.copy(isVisible = true)
        scheduleHudDismiss()
    }

    fun hideHud() {
        hideHandler.removeCallbacks(hideRunnable)
        _volumeState.value = _volumeState.value.copy(isVisible = false)
    }

    private fun scheduleHudDismiss() {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, 2500)
    }

    fun release() {
        try {
            context.contentResolver.unregisterContentObserver(volumeObserver)
        } catch (e: Exception) {
            /* ignore */
        }
        hideHandler.removeCallbacks(hideRunnable)
    }
}
