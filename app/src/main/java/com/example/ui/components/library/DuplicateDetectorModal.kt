package com.example.ui.components.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.util.DuplicateCluster

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuplicateDetectorModal(
    clusters: List<DuplicateCluster>,
    isScanning: Boolean,
    onScan: () -> Unit,
    onPlayTrack: (TrackEntity) -> Unit,
    onDeleteTrack: (TrackEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpotifyCardGrey,
        contentColor = SpotifyTextWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .testTag("duplicate_detector_modal"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            .background(SpotifyGreen.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Huella Acústica",
                            tint = SpotifyGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Detector de Duplicados",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyTextWhite
                            )
                        )
                        Text(
                            text = "Fingerprinting acústico y análisis de espectro",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                    }
                }

                Button(
                    onClick = onScan,
                    colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !isScanning
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SpotifyCardGrey,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Escanear",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SpotifyCardGrey
                            )
                        )
                    }
                }
            }

            if (clusters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = SpotifyTextMuted,
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = if (isScanning) "Escaneando huellas acústicas..." else "Presiona Escanear para analizar la biblioteca",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SpotifyTextMuted
                        )
                    }
                }
            } else {
                Text(
                    text = "Grupos de duplicados detectados (${clusters.size})",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = SpotifyGreen
                    )
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(clusters, key = { it.clusterId }) { cluster ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Match Type Header
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB74D),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = cluster.matchType.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFFB74D)
                                        )
                                    )
                                }

                                Text(
                                    text = cluster.matchReason,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = SpotifyTextMuted
                                )

                                // Original / Primary Track
                                Text(
                                    text = "Principal (Conservar):",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SpotifyGreen
                                )
                                DuplicateTrackRow(
                                    track = cluster.primaryTrack,
                                    isPrimary = true,
                                    onPlay = { onPlayTrack(cluster.primaryTrack) },
                                    onDelete = null
                                )

                                // Duplicate Tracks
                                Text(
                                    text = "Duplicado(s) Sugerido(s) a Eliminar:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFEF5350)
                                )
                                cluster.duplicates.forEach { dupTrack ->
                                    DuplicateTrackRow(
                                        track = dupTrack,
                                        isPrimary = false,
                                        onPlay = { onPlayTrack(dupTrack) },
                                        onDelete = { onDeleteTrack(dupTrack) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateTrackRow(
    track: TrackEntity,
    isPrimary: Boolean,
    onPlay: () -> Unit,
    onDelete: (() -> Unit)?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isPrimary) SpotifyGreen.copy(alpha = 0.1f)
                else Color.White.copy(alpha = 0.03f)
            )
            .border(
                width = 1.dp,
                color = if (isPrimary) SpotifyGreen.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SpotifyTextWhite
                ),
                maxLines = 1
            )
            Text(
                text = "${track.artist} • ${(track.fileSizeBytes / 1024 / 1024)} MB",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = SpotifyTextMuted,
                maxLines = 1
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPlay) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = SpotifyGreen
                )
            }

            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Duplicado",
                        tint = Color(0xFFEF5350)
                    )
                }
            }
        }
    }
}
