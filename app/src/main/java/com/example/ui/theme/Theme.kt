package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpotifyDarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyDarkGreen,
    onPrimaryContainer = SpotifyTextWhite,
    secondary = SpotifyLightGreen,
    onSecondary = Color.Black,
    secondaryContainer = SpotifyCardGrey,
    onSecondaryContainer = SpotifyTextWhite,
    background = SpotifyBlack,
    onBackground = SpotifyTextWhite,
    surface = SpotifyDarkSlate,
    onSurface = SpotifyTextWhite,
    surfaceVariant = SpotifyCardGrey,
    onSurfaceVariant = SpotifyTextMuted,
    surfaceContainer = SpotifyHoverGrey,
    error = SpotifyError,
    onError = Color.White
)

@Composable
fun SpotLocalTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Disabled to preserve Spotify green/charcoal look
    content: @Composable () -> Unit
) {
    val colorScheme = SpotifyDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    SpotLocalTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

