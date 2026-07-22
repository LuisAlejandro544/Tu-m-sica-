#include <jni.h>
#include <string>
#include <vector>
#include <cmath>
#include <android/log.h>

#define LOG_TAG "SpotLocalNativeAudio"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_player_OboeNativeAudioEngine_getEngineVersion(
        JNIEnv* env,
        jobject /* this */) {
    std::string version = "SpotLocal C++ Oboe Audio DSP Engine v2.0.0 (Equalizer Active)";
    LOGI("Native Audio Engine initialized successfully with C++ EQ matrix.");
    return env->NewStringUTF(version.c_str());
}

/**
 * C++ Real-time Biquad Filter Coefficients computation for 5 bands.
 * Calculates b0, b1, b2, a1, a2 for Peaking EQ filters given gains in dB.
 */
extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_example_player_OboeNativeAudioEngine_calculateBiquadCoefficientsNative(
        JNIEnv* env,
        jobject /* this */,
        jfloatArray bandGainsDb,
        jfloat sampleRate) {

    jsize len = env->GetArrayLength(bandGainsDb);
    std::vector<float> gains(len);
    env->GetFloatArrayRegion(bandGainsDb, 0, len, gains.data());

    // 5 Bands: 60Hz, 230Hz, 910Hz, 3600Hz, 14000Hz
    static const float centerFreqs[5] = {60.0f, 230.0f, 910.0f, 3600.0f, 14000.0f};
    static const float Q[5] = {1.1f, 1.2f, 1.2f, 1.2f, 1.1f};

    // 5 coefficients per band (b0, b1, b2, a1, a2) -> 25 floats total
    std::vector<float> coeffs(len * 5, 0.0f);

    float Fs = (sampleRate > 0.0f) ? sampleRate : 44100.0f;

    for (int i = 0; i < len && i < 5; ++i) {
        float gainDb = gains[i];
        float A = std::pow(10.0f, gainDb / 40.0f);
        float omega = 2.0f * M_PI * centerFreqs[i] / Fs;
        float alpha = std::sin(omega) / (2.0f * Q[i]);

        float b0 = 1.0f + alpha * A;
        float b1 = -2.0f * std::cos(omega);
        float b2 = 1.0f - alpha * A;
        float a0 = 1.0f + alpha / A;
        float a1 = -2.0f * std::cos(omega);
        float a2 = 1.0f - alpha / A;

        // Normalize by a0
        coeffs[i * 5 + 0] = b0 / a0;
        coeffs[i * 5 + 1] = b1 / a0;
        coeffs[i * 5 + 2] = b2 / a0;
        coeffs[i * 5 + 3] = a1 / a0;
        coeffs[i * 5 + 4] = a2 / a0;
    }

    LOGD("Calculated %d biquad C++ filter coefficient sets at Fs=%.0fHz", (int)len, Fs);

    jfloatArray result = env->NewFloatArray(coeffs.size());
    env->SetFloatArrayRegion(result, 0, coeffs.size(), coeffs.data());
    return result;
}

