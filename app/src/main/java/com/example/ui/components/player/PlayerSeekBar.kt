package com.example.ui.components.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.ui.components.formatDuration
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun PlayerSeekBar(
    currentPositionMs: Long,
    durationMs: Long,
    isUserScrubbing: Boolean,
    scrubPosition: Float,
    onScrubChange: (Float) -> Unit,
    onScrubFinished: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectivePosition = if (isUserScrubbing) scrubPosition.toLong() else currentPositionMs
    val maxRange = if (durationMs > 0) durationMs.toFloat() else 100f
    val currentVal = effectivePosition.toFloat().coerceIn(0f, maxRange)

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = currentVal,
            onValueChange = { onScrubChange(it) },
            onValueChangeFinished = { onScrubFinished(scrubPosition.toLong()) },
            valueRange = 0f..maxRange,
            colors = SliderDefaults.colors(
                thumbColor = SpotifyTextWhite,
                activeTrackColor = SpotifyGreen,
                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(effectivePosition),
                style = MaterialTheme.typography.labelSmall,
                color = SpotifyTextMuted
            )
            Text(
                text = formatDuration(durationMs),
                style = MaterialTheme.typography.labelSmall,
                color = SpotifyTextMuted
            )
        }
    }
}
