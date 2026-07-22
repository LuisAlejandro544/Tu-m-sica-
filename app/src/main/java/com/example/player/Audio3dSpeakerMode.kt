package com.example.player

enum class Audio3dSpeakerMode(val displayName: String, val description: String) {
    DUAL_SPEAKER(
        displayName = "2 Bocinas (Estéreo)",
        description = "Optimiza la separación estéreo, espacialidad 3D y balance de fase acústica"
    ),
    SINGLE_SPEAKER(
        displayName = "1 Bocina (Mono+)",
        description = "Usa psicoacústica espacial y reverberación virtual para crear profundidad armónica"
    ),
    HEADPHONES_3D(
        displayName = "Auriculares 360°",
        description = "Inmersión binaural completa tridimensional de 360 grados"
    )
}
