package com.example.ui.components.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite
import com.example.util.LrcParser

@Composable
fun PlayerLyricsView(
    track: TrackEntity,
    currentPositionMs: Long,
    onUpdateLyrics: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lyricsLines = remember(track.lyrics) { LrcParser.parse(track.lyrics) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Find current line index based on playback time
    val activeIndex = remember(lyricsLines, currentPositionMs) {
        if (lyricsLines.isEmpty()) -1
        else {
            var index = 0
            for (i in lyricsLines.indices) {
                if (lyricsLines[i].timeMs >= 0 && lyricsLines[i].timeMs <= currentPositionMs) {
                    index = i
                } else if (lyricsLines[i].timeMs > currentPositionMs) {
                    break
                }
            }
            index
        }
    }

    val listState = rememberLazyListState()

    LaunchedEffect(activeIndex) {
        if (activeIndex in lyricsLines.indices) {
            listState.animateScrollToItem((activeIndex - 1).coerceAtLeast(0))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(SpotifyCardGrey.copy(alpha = 0.85f))
            .padding(16.dp)
            .testTag("player_lyrics_container")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lyrics,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Letras de la canción",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = SpotifyTextWhite,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.testTag("edit_lyrics_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar letras",
                        tint = SpotifyTextMuted
                    )
                }
            }

            if (lyricsLines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No hay letras para esta canción.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyTextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showEditDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = Color.Black)
                        ) {
                            Text("Añadir o Pegar Letras (LRC)")
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    itemsIndexed(lyricsLines) { index, line ->
                        val isActive = index == activeIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isActive) SpotifyGreen else SpotifyTextWhite.copy(alpha = 0.5f),
                            label = "lyric_color"
                        )
                        val fontSize = if (isActive) 20.sp else 16.sp
                        val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

                        Text(
                            text = line.text,
                            color = textColor,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            lineHeight = 28.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditLyricsDialog(
            initialLyrics = track.lyrics ?: "",
            onSave = { newLyrics ->
                onUpdateLyrics(newLyrics)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun EditLyricsDialog(
    initialLyrics: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialLyrics) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SpotifyCardGrey,
        title = {
            Text("Editar Letras (LRC)", color = SpotifyTextWhite)
        },
        text = {
            Column {
                Text(
                    text = "Puedes pegar letras en formato sincronizado [mm:ss.xx] o texto plano.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("[00:01.00] Primera línea de la letra...", color = SpotifyTextMuted) },
                    minLines = 8,
                    maxLines = 12,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_lyrics_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SpotifyTextWhite,
                        unfocusedTextColor = SpotifyTextWhite,
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyTextMuted
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen, contentColor = Color.Black)
            ) {
                Text("Guardar Letras")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SpotifyTextMuted)
            }
        }
    )
}
