package com.example.data.ai

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StemSeparatorEngine {

    private const val TAG = "StemSeparatorEngine"
    private val inferenceRunner = OnnxInferenceRunner()

    private val _separationState = MutableStateFlow(StemSeparationState())
    val separationState: StateFlow<StemSeparationState> = _separationState.asStateFlow()

    fun setStemMode(mode: StemMode) {
        val (vocalGain, instGain) = when (mode) {
            StemMode.ORIGINAL -> Pair(0f, 0f)
            StemMode.VOCALS_ONLY -> Pair(0f, -60f)
            StemMode.INSTRUMENTAL -> Pair(-60f, 0f)
            StemMode.KARAOKE -> Pair(-14f, 2f)
        }
        _separationState.value = _separationState.value.copy(
            currentStemMode = mode,
            vocalGainDb = vocalGain,
            instGainDb = instGain
        )
        Log.i(TAG, "Stem Mode changed to $mode (Vocal Gain: ${vocalGain}dB, Inst Gain: ${instGain}dB)")
    }

    fun processTrackStems(trackTitle: String, onComplete: () -> Unit = {}) {
        _separationState.value = _separationState.value.copy(
            isProcessing = true,
            progressPercent = 10
        )
        Log.i(TAG, "Starting ONNX/Python Stem separation AI model for track: $trackTitle")
        inferenceRunner.runInference(trackTitle)
        _separationState.value = _separationState.value.copy(
            isProcessing = false,
            progressPercent = 100
        )
        onComplete()
    }
}
