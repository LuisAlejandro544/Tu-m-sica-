package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.data.db.TrackEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MusicPlayerManager(private val context: Context) {

    companion object {
        @Volatile
        var instance: MusicPlayerManager? = null
    }

    init {
        instance = this
    }

    private val TAG = "MusicPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var positionUpdateJob: Job? = null

    private val dspEngine = AudioDspEngine()

    private val _currentTrack = MutableStateFlow<TrackEntity?>(null)
    val currentTrack: StateFlow<TrackEntity?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _queue = MutableStateFlow<List<TrackEntity>>(emptyList())
    val queue: StateFlow<List<TrackEntity>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.NONE)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch: StateFlow<Float> = _playbackPitch.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _isEqEnabled = MutableStateFlow(true)
    val isEqEnabled: StateFlow<Boolean> = _isEqEnabled.asStateFlow()

    private val _bandGainsDb = MutableStateFlow(floatArrayOf(0f, 0f, 0f, 0f, 0f))
    val bandGainsDb: StateFlow<FloatArray> = _bandGainsDb.asStateFlow()

    private val _eqPreset = MutableStateFlow(com.example.ui.components.player.EqPreset.FLAT)
    val eqPreset: StateFlow<com.example.ui.components.player.EqPreset> = _eqPreset.asStateFlow()

    private val _is3dAudioEnabled = MutableStateFlow(true)
    val is3dAudioEnabled: StateFlow<Boolean> = _is3dAudioEnabled.asStateFlow()

    private val _audio3dStrength = MutableStateFlow(0.8f)
    val audio3dStrength: StateFlow<Float> = _audio3dStrength.asStateFlow()

    private val _audio3dMode = MutableStateFlow(Audio3dSpeakerMode.DUAL_SPEAKER)
    val audio3dMode: StateFlow<Audio3dSpeakerMode> = _audio3dMode.asStateFlow()

    // Crossfade (Fundido Cruzado)
    private val _crossfadeDurationSec = MutableStateFlow(3.0f)
    val crossfadeDurationSec: StateFlow<Float> = _crossfadeDurationSec.asStateFlow()

    // Volume Normalization (EBU R128)
    private val _isVolumeNormalizerEnabled = MutableStateFlow(true)
    val isVolumeNormalizerEnabled: StateFlow<Boolean> = _isVolumeNormalizerEnabled.asStateFlow()

    private val _targetLufs = MutableStateFlow(VolumeNormalizerEngine.DEFAULT_TARGET_LUFS)
    val targetLufs: StateFlow<Float> = _targetLufs.asStateFlow()

    fun setCrossfadeDuration(seconds: Float) {
        _crossfadeDurationSec.value = seconds.coerceIn(0.0f, 12.0f)
    }

    fun setVolumeNormalizerEnabled(enabled: Boolean) {
        _isVolumeNormalizerEnabled.value = enabled
        applyVolumeSettings()
    }

    fun setTargetLufs(lufs: Float) {
        _targetLufs.value = lufs.coerceIn(-24.0f, -8.0f)
        applyVolumeSettings()
    }

    private fun applyVolumeSettings() {
        val player = mediaPlayer ?: return
        val track = _currentTrack.value
        val mult = VolumeNormalizerEngine.calculateVolumeGainMultiplier(
            track = track,
            targetLufs = _targetLufs.value,
            enabled = _isVolumeNormalizerEnabled.value
        )
        try {
            player.setVolume(mult, mult)
        } catch (e: Exception) {
            Log.e(TAG, "Error applying normalized volume multiplier: ${e.message}")
        }
    }

    fun set3dAudioEnabled(enabled: Boolean) {
        _is3dAudioEnabled.value = enabled
        dspEngine.apply3dAudioFx(
            enabled = enabled,
            strength = (_audio3dStrength.value * 1000f).toInt().toShort(),
            mode = _audio3dMode.value
        )
    }

    fun set3dAudioStrength(strength: Float) {
        val s = strength.coerceIn(0f, 1f)
        _audio3dStrength.value = s
        dspEngine.apply3dAudioFx(
            enabled = _is3dAudioEnabled.value,
            strength = (s * 1000f).toInt().toShort(),
            mode = _audio3dMode.value
        )
    }

    fun set3dAudioMode(mode: Audio3dSpeakerMode) {
        _audio3dMode.value = mode
        dspEngine.apply3dAudioFx(
            enabled = _is3dAudioEnabled.value,
            strength = (_audio3dStrength.value * 1000f).toInt().toShort(),
            mode = mode
        )
    }

    fun setEqEnabled(enabled: Boolean) {
        _isEqEnabled.value = enabled
        dspEngine.applyCustomEqBands(enabled, _bandGainsDb.value)
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        if (bandIndex in 0..4) {
            val newGains = _bandGainsDb.value.copyOf()
            newGains[bandIndex] = gainDb.coerceIn(-12f, 12f)
            _bandGainsDb.value = newGains
            _eqPreset.value = com.example.ui.components.player.EqPreset.CUSTOM
            dspEngine.applyCustomEqBands(_isEqEnabled.value, newGains)
        }
    }

    fun setEqPreset(preset: com.example.ui.components.player.EqPreset) {
        _eqPreset.value = preset
        if (preset != com.example.ui.components.player.EqPreset.CUSTOM) {
            val newGains = preset.bandGainsDb.copyOf()
            _bandGainsDb.value = newGains
            dspEngine.applyCustomEqBands(_isEqEnabled.value, newGains)
        }
    }

    fun resetEq() {
        setEqPreset(com.example.ui.components.player.EqPreset.FLAT)
    }


    fun setSpeed(speed: Float) {
        val safeSpeed = speed.coerceIn(0.25f, 2.0f)
        _playbackSpeed.value = safeSpeed
        applyPlaybackParams()
    }

    fun setPitch(pitch: Float) {
        val safePitch = pitch.coerceIn(0.25f, 2.0f)
        _playbackPitch.value = safePitch
        applyPlaybackParams()
    }

    private fun applyPlaybackParams() {
        dspEngine.applyDspParams(mediaPlayer, _playbackSpeed.value, _playbackPitch.value)
    }

    fun setQueueAndPlay(tracks: List<TrackEntity>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        _queue.value = tracks
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        _currentIndex.value = safeIndex
        playTrack(tracks[safeIndex])
    }

    fun playTrack(track: TrackEntity) {
        try {
            stopAndRelease()

            _currentTrack.value = track
            _playbackError.value = null

            val uri = Uri.parse(track.uriString)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, uri)
                prepare()
                start()
            }

            mediaPlayer?.let { player ->
                dspEngine.attachEqualizer(player.audioSessionId)
                dspEngine.applyStemMode(com.example.data.ai.StemSeparatorEngine.separationState.value.currentStemMode)
                dspEngine.applyCustomEqBands(_isEqEnabled.value, _bandGainsDb.value)
                dspEngine.apply3dAudioFx(_is3dAudioEnabled.value, (_audio3dStrength.value * 1000f).toInt().toShort(), _audio3dMode.value)
            }

            applyPlaybackParams()
            applyVolumeSettings()

            _durationMs.value = mediaPlayer?.duration?.toLong() ?: track.durationMs
            _isPlaying.value = true

            startPositionUpdates()
            notifyService()

            mediaPlayer?.setOnCompletionListener {
                onTrackCompleted()
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                _playbackError.value = "Error al reproducir el archivo. Verifica los permisos o el archivo."
                _isPlaying.value = false
                true
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to play track ${track.title}", e)
            _playbackError.value = "No se pudo abrir este archivo de audio (${e.localizedMessage})"
            _isPlaying.value = false
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopPositionUpdates()
        } else {
            player.start()
            _isPlaying.value = true
            startPositionUpdates()
        }
        notifyService()
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            player.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
            notifyService()
        }
    }

    fun nextTrack() {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return

        if (_repeatMode.value == RepeatMode.ONE) {
            _currentTrack.value?.let { playTrack(it) }
            return
        }

        var nextIdx = _currentIndex.value + 1
        if (_isShuffle.value) {
            nextIdx = (0 until currentQ.size).random()
        }

        if (nextIdx < currentQ.size) {
            _currentIndex.value = nextIdx
            playTrack(currentQ[nextIdx])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            _currentIndex.value = 0
            playTrack(currentQ[0])
        } else {
            _isPlaying.value = false
            stopPositionUpdates()
        }
    }

    fun previousTrack() {
        val currentQ = _queue.value
        if (currentQ.isEmpty()) return

        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        var prevIdx = _currentIndex.value - 1
        if (prevIdx < 0) {
            prevIdx = if (_repeatMode.value == RepeatMode.ALL) currentQ.lastIndex else 0
        }

        _currentIndex.value = prevIdx
        playTrack(currentQ[prevIdx])
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun toggleRepeat() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.NONE -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.NONE
        }
    }

    fun clearError() {
        _playbackError.value = null
    }

    private fun onTrackCompleted() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                _currentTrack.value?.let { playTrack(it) }
            }
            RepeatMode.ALL, RepeatMode.NONE -> {
                nextTrack()
            }
        }
    }

    private var isCrossfadeInProgress = false

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPos = player.currentPosition.toLong()
                        val totalDur = _durationMs.value
                        _currentPositionMs.value = currentPos

                        val xfadeSec = _crossfadeDurationSec.value
                        if (xfadeSec > 0.5f && totalDur > 10000 && !isCrossfadeInProgress) {
                            val remainingMs = totalDur - currentPos
                            if (remainingMs in 100..((xfadeSec * 1000).toLong())) {
                                isCrossfadeInProgress = true
                                launch {
                                    val steps = 10
                                    val delayMs = (remainingMs / steps).coerceAtLeast(50L)
                                    for (i in 1..steps) {
                                        val volFactor = (1.0f - (i.toFloat() / steps)).coerceIn(0f, 1f)
                                        val normMult = VolumeNormalizerEngine.calculateVolumeGainMultiplier(_currentTrack.value, _targetLufs.value, _isVolumeNormalizerEnabled.value)
                                        val effVol = normMult * volFactor
                                        try {
                                            player.setVolume(effVol, effVol)
                                        } catch (e: Exception) { /* ignore */ }
                                        delay(delayMs)
                                    }
                                    isCrossfadeInProgress = false
                                    nextTrack()
                                }
                            }
                        }
                    }
                }
                delay(250)
            }
        }
    }

    fun setStemMode(mode: com.example.data.ai.StemMode) {
        com.example.data.ai.StemSeparatorEngine.setStemMode(mode)
        dspEngine.applyStemMode(mode)
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    private fun stopAndRelease() {
        stopPositionUpdates()
        dspEngine.releaseEqualizer()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignore
        }
        mediaPlayer = null
        notifyService()
    }

    private fun notifyService() {
        MediaPlaybackService.syncNotification(
            context = context,
            track = _currentTrack.value,
            playing = _isPlaying.value,
            positionMs = _currentPositionMs.value,
            duration = _durationMs.value,
            speed = _playbackSpeed.value
        )
    }

    fun release() {
        stopAndRelease()
    }
}
