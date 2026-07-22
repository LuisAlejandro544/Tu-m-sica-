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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.example.ui.components.TrackItem
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyHoverGrey
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

@Composable
fun LibraryScreen(
    selectedFilter: String,
    allTracks: List<TrackEntity>,
    favoriteTracks: List<TrackEntity>,
    playlists: List<PlaylistEntity>,
    folders: List<String>,
    currentTrack: TrackEntity?,
    onFilterSelect: (String) -> Unit,
    onTrackClick: (TrackEntity) -> Unit,
    onFavoriteToggle: (TrackEntity) -> Unit,
    onOptionsClick: (TrackEntity) -> Unit,
    onCreatePlaylistClick: () -> Unit,
    onImportClick: () -> Unit,
    onOpenFavoritesClick: () -> Unit = {},
    onOpenPlaylistClick: (PlaylistEntity) -> Unit = {},
    onOpenFolderClick: (String) -> Unit = {},
    onScanDuplicatesClick: () -> Unit = {},
    onCleanTagsBatchClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filterChips = listOf("Todo", "Canciones", "Listas", "Carpetas", "Favoritas")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SpotifyGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SL",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Tu Biblioteca",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = SpotifyTextWhite
                )
            }

            Row {
                IconButton(
                    onClick = onScanDuplicatesClick,
                    modifier = Modifier.testTag("scan_duplicates_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Detectar Duplicados",
                        tint = SpotifyGreen
                    )
                }

                IconButton(
                    onClick = onCleanTagsBatchClick,
                    modifier = Modifier.testTag("clean_tags_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoFixHigh,
                        contentDescription = "Limpiar Etiquetas ID3",
                        tint = SpotifyGreen
                    )
                }

                IconButton(
                    onClick = onCreatePlaylistClick,
                    modifier = Modifier.testTag("create_playlist_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear Lista",
                        tint = SpotifyTextWhite
                    )
                }
            }
        }

        // Filter Chips Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterChips) { chip ->
                FilterChip(
                    selected = selectedFilter == chip,
                    onClick = { onFilterSelect(chip) },
                    label = { Text(chip) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SpotifyGreen,
                        selectedLabelColor = Color.Black,
                        containerColor = SpotifyCardGrey,
                        labelColor = SpotifyTextWhite
                    ),
                    border = null,
                    modifier = Modifier.testTag("filter_chip_$chip")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Pinned Item: Canciones que te gustan
            if (selectedFilter == "Todo" || selectedFilter == "Favoritas" || selectedFilter == "Listas") {
                item {
                    LibraryLikedSongsItem(
                        favoriteCount = favoriteTracks.size,
                        onClick = onOpenFavoritesClick
                    )
                }
            }

            // Custom Playlists
            if (selectedFilter == "Todo" || selectedFilter == "Listas") {
                items(playlists, key = { "pl_${it.id}" }) { playlist ->
                    LibraryPlaylistItem(
                        playlist = playlist,
                        onClick = { onOpenPlaylistClick(playlist) }
                    )
                }
            }

            // Folders
            if (selectedFilter == "Todo" || selectedFilter == "Carpetas") {
                items(folders, key = { "folder_$it" }) { folderName ->
                    LibraryFolderItem(
                        folderName = folderName,
                        onClick = { onOpenFolderClick(folderName) }
                    )
                }
            }

            // Songs List
            if (selectedFilter == "Todo" || selectedFilter == "Canciones") {
                items(allTracks, key = { "track_${it.id}" }) { track ->
                    TrackItem(
                        track = track,
                        isCurrentPlaying = currentTrack?.id == track.id,
                        onTrackClick = onTrackClick,
                        onFavoriteToggle = onFavoriteToggle,
                        onOptionsClick = onOptionsClick
                    )
                }
            } else if (selectedFilter == "Favoritas") {
                items(favoriteTracks, key = { "fav_track_${it.id}" }) { track ->
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
}

@Composable
fun LibraryLikedSongsItem(
    favoriteCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = R.drawable.playlist_liked_cover_1784737937201,
            contentDescription = null,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = "Canciones que te gustan",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = SpotifyTextWhite
            )

            Text(
                text = "Lista • $favoriteCount canciones",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SpotifyTextMuted
            )
        }
    }
}

@Composable
fun LibraryPlaylistItem(
    playlist: PlaylistEntity,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SpotifyHoverGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = playlist.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = SpotifyTextWhite
            )

            Text(
                text = "Lista creada por ti",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SpotifyTextMuted
            )
        }
    }
}

@Composable
fun LibraryFolderItem(
    folderName: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(SpotifyHoverGrey),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = SpotifyGreen,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = folderName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = SpotifyTextWhite
            )

            Text(
                text = "Carpeta importada localmente",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = SpotifyTextMuted
            )
        }
    }
}
