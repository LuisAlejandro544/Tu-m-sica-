package com.example.ui.components.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SpeakerGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.Audio3dSpeakerMode
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun PlayerAudio3dEnhancerView(
    is3dAudioEnabled: Boolean,
    audio3dStrength: Float,
    audio3dMode: Audio3dSpeakerMode,
    on3dAudioEnabledToggle: (Boolean) -> Unit,
    on3dStrengthChange: (Float) -> Unit,
    on3dModeSelect: (Audio3dSpeakerMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_audio_3d_enhancer_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Title Bar + Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (is3dAudioEnabled) SpotifyGreen.copy(alpha = 0.2f)
                                else Color.White.copy(alpha = 0.08f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Mejora de Audio 3D",
                            tint = if (is3dAudioEnabled) SpotifyGreen else SpotifyTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Mejora de Audio (3D Espacial)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = if (is3dAudioEnabled) "Inmersión 3D Activa" else "Desactivado",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (is3dAudioEnabled) SpotifyGreen else SpotifyTextMuted
                        )
                    }
                }

                Switch(
                    checked = is3dAudioEnabled,
                    onCheckedChange = on3dAudioEnabledToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SpotifyTextWhite,
                        checkedTrackColor = SpotifyGreen,
                        uncheckedThumbColor = SpotifyTextMuted,
                        uncheckedTrackColor = SpotifyCardGrey
                    )
                )
            }

            AnimatedVisibility(visible = is3dAudioEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Modo de Altavoces / Salida
                    Text(
                        text = "Configuración de Altavoces / Dispositivo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SpotifyTextMuted
                        )
                    )

                    Audio3dSpeakerMode.values().forEach { mode ->
                        val isSelected = audio3dMode == mode
                        val icon = when (mode) {
                            Audio3dSpeakerMode.DUAL_SPEAKER -> Icons.Default.SpeakerGroup
                            Audio3dSpeakerMode.SINGLE_SPEAKER -> Icons.Default.Speaker
                            Audio3dSpeakerMode.HEADPHONES_3D -> Icons.Default.Headphones
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) SpotifyGreen.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.03f)
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { on3dModeSelect(mode) }
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) SpotifyGreen else SpotifyTextMuted,
                                    modifier = Modifier.size(26.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SpotifyTextWhite else SpotifyTextWhite.copy(alpha = 0.8f)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = mode.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = SpotifyTextMuted
                                    )
                                }
                            }
                        }
                    }

                    // Slider de Intensidad de Espacialización 3D
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intensidad del Efecto 3D",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = SpotifyTextMuted
                                )
                            )
                            Text(
                                text = "${(audio3dStrength * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SpotifyGreen
                                )
                            )
                        }

                        Slider(
                            value = audio3dStrength,
                            onValueChange = on3dStrengthChange,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = SpotifyGreen,
                                activeTrackColor = SpotifyGreen,
                                inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
