package com.example.data.ai

enum class StemMode {
    ORIGINAL,       // Audio completo original
    VOCALS_ONLY,    // Solo la pista vocal separada por la IA
    INSTRUMENTAL,   // Solo instrumental (Acompañamiento)
    KARAOKE         // Instrumental + Voces atenuadas al -12dB
}

data class StemSeparationState(
    val isProcessing: Boolean = false,
    val progressPercent: Int = 0,
    val currentStemMode: StemMode = StemMode.ORIGINAL,
    val vocalGainDb: Float = 0f,
    val instGainDb: Float = 0f,
    val modelName: String = "Mobile-U-Net 2MB INT8 (Python/ONNX AI)"
)
