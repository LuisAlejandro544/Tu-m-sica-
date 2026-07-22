package com.example.ui.components.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.rust.RustEqualizerEngine
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

enum class EqPreset(val label: String, val bandGainsDb: FloatArray) {
    FLAT("Plano", floatArrayOf(0f, 0f, 0f, 0f, 0f)),
    BASS_BOOST("Bajos Potentes", floatArrayOf(8f, 5f, 1f, -1f, -2f)),
    ROCK("Rock", floatArrayOf(5f, 3f, -1f, 3f, 6f)),
    POP("Pop", floatArrayOf(-1f, 2f, 5f, 2f, -2f)),
    JAZZ("Jazz", floatArrayOf(4f, 2f, -2f, 2f, 5f)),
    VOCAL("Voz Clara", floatArrayOf(-3f, 1f, 6f, 3f, -1f)),
    ACOUSTIC("Acústico", floatArrayOf(4f, 2f, 1f, 3f, 4f)),
    ELECTRONIC("Electrónica", floatArrayOf(6f, 4f, 0f, 3f, 5f)),
    CUSTOM("Personalizado", floatArrayOf(0f, 0f, 0f, 0f, 0f))
}

@Composable
fun PlayerEqualizerView(
    isEqEnabled: Boolean,
    bandGainsDb: FloatArray, // 5 values: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz
    currentPreset: EqPreset,
    onEqEnabledToggle: (Boolean) -> Unit,
    onBandGainChange: (bandIndex: Int, newGainDb: Float) -> Unit,
    onPresetSelect: (EqPreset) -> Unit,
    onResetEq: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bandLabels = remember { listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("player_equalizer_view")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Ecualizador C++ & Rust",
                    tint = if (isEqEnabled) SpotifyGreen else SpotifyTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Ecualizador Avanzado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SpotifyTextWhite
                        )
                    )
                    Text(
                        text = "Motor NDK C++ & Rust DSP",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            color = SpotifyGreen
                        )
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onResetEq,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Restablecer EQ",
                        tint = SpotifyTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Switch(
                    checked = isEqEnabled,
                    onCheckedChange = onEqEnabledToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SpotifyGreen,
                        uncheckedThumbColor = SpotifyTextMuted,
                        uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("eq_switch_toggle")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Preset Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(EqPreset.values()) { preset ->
                val isSelected = preset == currentPreset
                val chipBg by animateColorAsState(
                    targetValue = if (isSelected) SpotifyGreen else Color.White.copy(alpha = 0.08f),
                    label = "chipBg"
                )
                val chipText = if (isSelected) Color.Black else SpotifyTextWhite

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(chipBg)
                        .clickable { onPresetSelect(preset) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = chipText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rust DSP Calculated Frequency Response Curve Canvas
        val curvePoints = remember(bandGainsDb.toList(), isEqEnabled) {
            if (isEqEnabled) {
                RustEqualizerEngine.calculateEqResponseCurve(bandGainsDb, 60)
            } else {
                FloatArray(60) { 0f }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(8.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxWidth().height(64.dp)) {
                val width = size.width
                val height = size.height
                val midY = height / 2f

                // Zero dB Reference Line
                drawLine(
                    color = Color.White.copy(alpha = 0.15f),
                    start = androidx.compose.ui.geometry.Offset(0f, midY),
                    end = androidx.compose.ui.geometry.Offset(width, midY),
                    strokeWidth = 1f
                )

                if (curvePoints.isNotEmpty()) {
                    val path = Path()
                    val dx = width / (curvePoints.size - 1).coerceAtLeast(1)

                    curvePoints.forEachIndexed { i, db ->
                        val x = i * dx
                        // Map -18dB to +18dB across height
                        val clampedDb = db.coerceIn(-18f, 18f)
                        val y = midY - (clampedDb / 18f) * (midY - 4f)

                        if (i == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    // Draw Line
                    drawPath(
                        path = path,
                        color = if (isEqEnabled) SpotifyGreen else SpotifyTextMuted,
                        style = Stroke(width = 3f)
                    )

                    // Draw Fill Gradient under path
                    val fillPath = Path().apply {
                        addPath(path)
                        lineTo(width, height)
                        lineTo(0f, height)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                (if (isEqEnabled) SpotifyGreen else SpotifyTextMuted).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5 Band Gain Sliders
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 0 until 5) {
                val gainDb = bandGainsDb.getOrElse(i) { 0f }
                val label = bandLabels.getOrElse(i) { "" }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (gainDb > 0) "+${gainDb.toInt()} dB" else "${gainDb.toInt()} dB",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (gainDb != 0f && isEqEnabled) SpotifyGreen else SpotifyTextMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = gainDb,
                        onValueChange = { newGain ->
                            if (isEqEnabled) {
                                onBandGainChange(i, newGain)
                            }
                        },
                        valueRange = -12f..12f,
                        steps = 23,
                        enabled = isEqEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isEqEnabled) SpotifyGreen else SpotifyTextMuted,
                            activeTrackColor = if (isEqEnabled) SpotifyGreen else SpotifyTextMuted,
                            inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("eq_band_slider_$i")
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = SpotifyTextWhite
                    )
                }
            }
        }
    }
}
