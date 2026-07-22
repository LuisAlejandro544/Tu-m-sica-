package com.example.ui.components.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.SpotifyGreen
import java.io.File

@Composable
fun PlayerAlbumArt(
    coverArtPath: String?,
    modifier: Modifier = Modifier
) {
    val coverFile = coverArtPath?.let { File(it) }
    Surface(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp)),
        color = Color.White.copy(alpha = 0.05f),
        tonalElevation = 8.dp
    ) {
        if (coverFile != null && coverFile.exists()) {
            AsyncImage(
                model = coverFile,
                contentDescription = "Portada",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(110.dp)
                )
            }
        }
    }
}
