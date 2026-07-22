package com.example.data.rust

import android.util.Log

object RustEqualizerEngine {

    private const val TAG = "RustEqualizerEngine"
    private var isNativeLibraryLoaded = false

    init {
        try {
            System.loadLibrary("spotlocal_rust_parser")
            isNativeLibraryLoaded = true
            Log.i(TAG, "Rust spotlocal_rust_parser JNI bridge loaded successfully.")
        } catch (e: Throwable) {
            Log.d(TAG, "Rust JNI fallback active: ${e.message}")
            isNativeLibraryLoaded = false
        }
    }

    private external fun calculateEqResponseNative(bandGains: FloatArray, numPoints: Int): FloatArray?

    /**
     * Calculates logarithmic frequency response curve points across 20Hz-20kHz using Rust DSP math.
     */
    fun calculateEqResponseCurve(bandGainsDb: FloatArray, numPoints: Int = 60): FloatArray {
        if (isNativeLibraryLoaded) {
            try {
                val res = calculateEqResponseNative(bandGainsDb, numPoints)
                if (res != null && res.isNotEmpty()) {
                    return res
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error calling Rust native calculateEqResponseNative: ${e.message}")
            }
        }

        // Pure Kotlin fallback calculation if native lib not compiled
        val centerFreqs = floatArrayOf(60f, 230f, 910f, 3600f, 14000f)
        val response = FloatArray(numPoints)
        for (i in 0 until numPoints) {
            val frac = i.toFloat() / (numPoints - 1).coerceAtLeast(1)
            val freq = 20f * Math.pow(1000.0, frac.toDouble()).toFloat()

            var totalDb = 0f
            for (b in 0 until 5) {
                val gain = bandGainsDb.getOrElse(b) { 0f }
                val fc = centerFreqs[b]
                val ratio = freq / fc
                val logRatio = Math.log(ratio.toDouble()).toFloat()
                val bell = Math.exp(-0.5 * Math.pow((logRatio / 0.7f).toDouble(), 2.0)).toFloat()
                totalDb += gain * bell
            }
            response[i] = totalDb.coerceIn(-18f, 18f)
        }
        return response
    }
}
