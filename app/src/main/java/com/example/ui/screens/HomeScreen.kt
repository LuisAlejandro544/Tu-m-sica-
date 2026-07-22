package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Outbox
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.db.TrackEntity
import com.example.ui.components.TrackItem
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyDarkGreen
import com.example.ui.theme.SpotifyDarkSlate
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite
import java.io.File
import java.util.Calendar

@Composable
fun HomeScreen(
    allTracks: List<TrackEntity>,
    favoriteTracks: List<TrackEntity>,
    recentTracks: List<TrackEntity>,
    currentTrack: TrackEntity?,
    onTrackClick: (TrackEntity) -> Unit,
    onFavoriteToggle: (TrackEntity) -> Unit,
    onOptionsClick: (TrackEntity) -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onOpenFavoritesClick: () -> Unit = {},
    onOpenFolderClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val greeting = rememberGreeting()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SpotifyTextWhite
                )

                Row {
                    IconButton(
                        onClick = onExportClick,
                        modifier = Modifier.testTag("home_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Outbox,
                            contentDescription = "Exportar Copia",
                            tint = SpotifyTextWhite
                        )
                    }

                    Button(
                        onClick = onImportClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SpotifyGreen,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("home_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Importar",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Quick Grid 2x2
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Card 1: Canciones que te gustan
                    QuickGridCard(
                        title = "Canciones que te gustan",
                        subtext = "${favoriteTracks.size} temas",
                        imageDrawableRes = R.drawable.playlist_liked_cover_1784737937201,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenFavoritesClick
                    )

                    // Quick Card 2: Tu Música Local
                    QuickGridCard(
                        title = "Archivos Importados",
                        subtext = "${allTracks.size} archivos",
                        imageDrawableRes = R.drawable.import_folder_cover_1784737948197,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val firstFolder = allTracks.firstOrNull()?.folderName ?: "Música"
                            onOpenFolderClick(firstFolder)
                        }
                    )
                }
            }
        }

        // Highlight Card: Sin archivos basura
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SpotifyDarkSlate),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    SpotifyDarkGreen.copy(alpha = 0.4f),
                                    SpotifyDarkSlate
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SpotifyGreen,
                            modifier = Modifier.size(36.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Sin audios ni tonos basura",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = SpotifyTextWhite
                            )
                            Text(
                                text = "Tú eliges exactamente qué música importar. No escaneamos notas de voz ni efectos del sistema.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = SpotifyTextMuted
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Row: Recientes
        if (recentTracks.isNotEmpty()) {
            item {
                Text(
                    text = "Importaciones Recientes",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = SpotifyTextWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recentTracks, key = { it.id }) { track ->
                        RecentTrackCard(
                            track = track,
                            onTrackClick = onTrackClick
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Title for Main Track List
        item {
            Text(
                text = "Tus canciones importadas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = SpotifyTextWhite,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Main List
        if (allTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = SpotifyTextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No has importado música todavía",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = SpotifyTextWhite
                        )
                        Text(
                            text = "Presiona 'Importar' para elegir tus archivos de audio de tu almacenamiento.",
                            style = MaterialTheme.typography.bodySmall,
                            color = SpotifyTextMuted
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onImportClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SpotifyGreen)
                        ) {
                            Text("Seleccionar Archivos", color = Color.Black)
                        }
                    }
                }
            }
        } else {
            items(allTracks, key = { it.id }) { track ->
                TrackItem(
                    track = track,
                    isCurrentPlaying = currentTrack?.id == track.id,
                    onTrackClick = onTrackClick,
                    onFavoriteToggle = onFavoriteToggle,
                    onOptionsClick = onOptionsClick
                )
            }
        }
    }
}

@Composable
fun QuickGridCard(
    title: String,
    subtext: String,
    imageDrawableRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SpotifyCardGrey)
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageDrawableRes,
            contentDescription = null,
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.padding(end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                ),
                color = SpotifyTextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = SpotifyTextMuted
            )
        }
    }
}

@Composable
fun RecentTrackCard(
    track: TrackEntity,
    onTrackClick: (TrackEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(SpotifyCardGrey)
            .clickable { onTrackClick(track) }
            .padding(8.dp)
    ) {
        val coverFile = track.coverArtPath?.let { File(it) }
        if (coverFile != null && coverFile.exists()) {
            AsyncImage(
                model = coverFile,
                contentDescription = null,
                modifier = Modifier
                    .size(114.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(114.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = track.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
            color = SpotifyTextWhite,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = track.artist,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
            color = SpotifyTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun rememberGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..11 -> "Buenos días"
        in 12..19 -> "Buenas tardes"
        else -> "Buenas noches"
    }
}
