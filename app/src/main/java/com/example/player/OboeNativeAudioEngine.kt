package com.example.player

import android.util.Log

object OboeNativeAudioEngine {

    private const val TAG = "OboeNativeAudioEngine"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("spotlocal_audio")
            isNativeLibraryLoaded = true
            Log.i(TAG, "C++ spotlocal_audio library loaded successfully.")
        } catch (e: Throwable) {
            Log.d(TAG, "C++ JNI fallback active: ${e.message}")
            isNativeLibraryLoaded = false
        }
    }

    external fun getEngineVersion(): String
    external fun calculateBiquadCoefficientsNative(bandGainsDb: FloatArray, sampleRate: Float): FloatArray?

    fun calculateBiquadCoefficients(bandGainsDb: FloatArray, sampleRate: Float = 44100f): FloatArray? {
        if (!isNativeLibraryLoaded) return null
        return try {
            calculateBiquadCoefficientsNative(bandGainsDb, sampleRate)
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating C++ biquad coefficients: ${e.message}")
            null
        }
    }
}
