package com.example.player

enum class RepeatMode {
    NONE, ALL, ONE
}

data class PlaybackParamsState(
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f
)
