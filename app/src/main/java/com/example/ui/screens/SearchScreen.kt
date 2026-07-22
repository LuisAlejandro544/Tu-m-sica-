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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.TrackEntity
import com.example.ui.components.TrackItem
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyCardGrey
import com.example.ui.theme.SpotifyDarkGreen
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite

data class CategoryTile(
    val name: String,
    val color1: Color,
    val color2: Color
)

@Composable
fun SearchScreen(
    searchQuery: String,
    searchResults: List<TrackEntity>,
    currentTrack: TrackEntity?,
    onSearchQueryChange: (String) -> Unit,
    onTrackClick: (TrackEntity) -> Unit,
    onFavoriteToggle: (TrackEntity) -> Unit,
    onOptionsClick: (TrackEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf(
        CategoryTile("Pop", Color(0xFF148A08), Color(0xFF0D5E04)),
        CategoryTile("Rock", Color(0xFFE91429), Color(0xFF8D0B18)),
        CategoryTile("Hip-Hop", Color(0xFFBC5900), Color(0xFF723600)),
        CategoryTile("Electrónica", Color(0xFF27856A), Color(0xFF15483A)),
        CategoryTile("Descargas", Color(0xFF1E3264), Color(0xFF0F1A35)),
        CategoryTile("Álbumes", Color(0xFF8D67AB), Color(0xFF4C375C))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        // Search Header
        Text(
            text = "Buscar",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp
            ),
            color = SpotifyTextWhite,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
        )

        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "¿Qué quieres escuchar?",
                    color = SpotifyTextMuted,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = SpotifyTextWhite
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar",
                            tint = SpotifyTextWhite
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SpotifyCardGrey,
                unfocusedContainerColor = SpotifyCardGrey,
                focusedBorderColor = SpotifyGreen,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = SpotifyTextWhite,
                unfocusedTextColor = SpotifyTextWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("search_text_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotBlank()) {
            // Search Results List
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No se encontraron canciones que coincidan con \"$searchQuery\"",
                        color = SpotifyTextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(searchResults, key = { it.id }) { track ->
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
        } else {
            // Explore Categories Grid
            Text(
                text = "Explorar tus géneros",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = SpotifyTextWhite,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(categories) { category ->
                    Box(
                        modifier = Modifier
                            .height(96.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(category.color1, category.color2)
                                )
                            )
                            .clickable {
                                onSearchQueryChange(category.name)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.TopStart
                    ) {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = SpotifyTextWhite
                        )
                    }
                }
            }
        }
    }
}
