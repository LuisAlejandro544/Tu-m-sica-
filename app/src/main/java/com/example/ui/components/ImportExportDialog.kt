package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyError
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun CreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name.trim(), description.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                modifier = Modifier.testTag("confirm_create_playlist_button")
            ) {
                Text("Crear", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = SpotifyTextMuted)
            }
        },
        title = {
            Text(
                text = "Poner nombre a tu lista",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = SpotifyTextWhite
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la lista", color = SpotifyTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyTextMuted,
                        focusedTextColor = SpotifyTextWhite,
                        unfocusedTextColor = SpotifyTextWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("playlist_name_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)", color = SpotifyTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SpotifyGreen,
                        unfocusedBorderColor = SpotifyTextMuted,
                        focusedTextColor = SpotifyTextWhite,
                        unfocusedTextColor = SpotifyTextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        containerColor = SpotifyCardGrey,
        titleContentColor = SpotifyTextWhite,
        textContentColor = SpotifyTextWhite
    )
}

@Composable
fun TrackOptionsDialog(
    track: TrackEntity,
    onDismiss: () -> Unit,
    onFavoriteToggle: (TrackEntity) -> Unit,
    onDeleteTrack: (TrackEntity) -> Unit,
    onPickCustomCover: (TrackEntity) -> Unit
) {
    var showFileInfo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", color = SpotifyTextMuted)
            }
        },
        title = {
            Column {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = SpotifyTextWhite
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = SpotifyTextMuted
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (showFileInfo) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text("Álbum: ${track.album}", color = SpotifyTextWhite, fontSize = 12.sp)
                        Text("Carpeta: ${track.folderName}", color = SpotifyTextWhite, fontSize = 12.sp)
                        Text("Duración: ${formatDuration(track.durationMs)}", color = SpotifyTextWhite, fontSize = 12.sp)
                        Text("Tamaño: ${track.fileSizeBytes / 1024 / 1024} MB", color = SpotifyTextWhite, fontSize = 12.sp)
                        Text("Carátula WebP: ${track.coverArtPath ?: "Generada por semilla"}", color = SpotifyGreen, fontSize = 10.sp, maxLines = 2)
                        Text("URI: ${track.uriString}", color = SpotifyTextMuted, fontSize = 10.sp, maxLines = 2)
                    }
                }

                TextButton(
                    onClick = {
                        onFavoriteToggle(track)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = SpotifyGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (track.isFavorite) "Quitar de Favoritas" else "Añadir a Canciones que te gustan",
                        color = SpotifyTextWhite
                    )
                }

                TextButton(
                    onClick = {
                        onDismiss()
                        onPickCustomCover(track)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = SpotifyGreen)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Cambiar Carátula (Procesar WebP)", color = SpotifyTextWhite)
                }

                TextButton(
                    onClick = { showFileInfo = !showFileInfo },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = SpotifyTextWhite)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Información del archivo", color = SpotifyTextWhite)
                }

                TextButton(
                    onClick = {
                        onDeleteTrack(track)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = SpotifyError)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "Eliminar de la biblioteca", color = SpotifyError)
                }
            }
        },
        containerColor = SpotifyCardGrey,
        titleContentColor = SpotifyTextWhite,
        textContentColor = SpotifyTextWhite
    )
}
