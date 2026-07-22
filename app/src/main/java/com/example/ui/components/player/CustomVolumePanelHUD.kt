package com.example.ui.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.VolumeState
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun CustomVolumePanelHUD(
    volumeState: VolumeState,
    onVolumeChangePercent: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onHideHud: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = volumeState.isVisible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 16.dp, top = 80.dp, bottom = 120.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Surface(
                modifier = Modifier
                    .width(76.dp)
                    .height(340.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF282828),
                                Color(0xFF181818)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                SpotifyGreen.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .testTag("custom_volume_panel_hud"),
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 14.dp, horizontal = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Mute / Volume Icon
                    val volumeIcon = when {
                        volumeState.isMuted || volumeState.volumePercent <= 0.01f -> Icons.Default.VolumeOff
                        volumeState.volumePercent < 0.35f -> Icons.Default.VolumeMute
                        volumeState.volumePercent < 0.70f -> Icons.Default.VolumeDown
                        else -> Icons.Default.VolumeUp
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                if (volumeState.isMuted) Color(0xFFEF5350).copy(alpha = 0.2f)
                                else SpotifyGreen.copy(alpha = 0.2f)
                            )
                            .clickable { onToggleMute() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = volumeIcon,
                            contentDescription = "Volumen",
                            tint = if (volumeState.isMuted) Color(0xFFEF5350) else SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Percentage Badge
                    val percentInt = (volumeState.volumePercent * 100).toInt()
                    Text(
                        text = if (volumeState.isMuted) "Silencio" else "$percentInt%",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (volumeState.isMuted) Color(0xFFEF5350) else SpotifyTextWhite,
                            fontSize = 12.sp
                        )
                    )

                    // Vertical Custom Sleek Volume Fill Bar
                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                            .pointerInput(Unit) {
                                detectVerticalDragGestures { change, dragAmount ->
                                    change.consume()
                                    // Dragging UP decreases Y, which means INCREASE volume
                                    val deltaPercent = -dragAmount / 200f
                                    onVolumeChangePercent(volumeState.volumePercent + deltaPercent)
                                }
                            },
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Fill Level
                        val fillHeightFraction = if (volumeState.isMuted) 0f else volumeState.volumePercent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fillHeightFraction.coerceIn(0f, 1f))
                                .clip(RoundedCornerShape(18.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            SpotifyGreen,
                                            Color(0xFF179443)
                                        )
                                    )
                                )
                        )

                        // Equalizer Wave Indicator Lines inside bar
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (i in 1..7) {
                                Box(
                                    modifier = Modifier
                                        .width(if (i % 2 == 0) 16.dp else 10.dp)
                                        .height(2.dp)
                                        .background(Color.White.copy(alpha = 0.35f))
                                )
                            }
                        }
                    }

                    // Quick Preset Volume Level Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PresetChip(label = "0%", onClick = { onVolumeChangePercent(0.0f) })
                        PresetChip(label = "50%", onClick = { onVolumeChangePercent(0.5f) })
                        PresetChip(label = "100%", onClick = { onVolumeChangePercent(1.0f) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SpotifyTextMuted
            )
        )
    }
}
