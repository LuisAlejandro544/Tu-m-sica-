package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyGreen
import com.example.ui.theme.SpotifyTextMuted
import com.example.ui.theme.SpotifyTextWhite
import com.example.ui.viewmodel.SpotifyTab

@Composable
fun SpotifyBottomNav(
    currentTab: SpotifyTab,
    onTabSelected: (SpotifyTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .background(SpotifyBlack)
            .windowInsetsPadding(WindowInsets.navigationBars),
        containerColor = SpotifyBlack,
        contentColor = SpotifyTextWhite
    ) {
        NavigationBarItem(
            selected = currentTab == SpotifyTab.HOME,
            onClick = { onTabSelected(SpotifyTab.HOME) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SpotifyTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpotifyTextWhite,
                selectedTextColor = SpotifyTextWhite,
                unselectedIconColor = SpotifyTextMuted,
                unselectedTextColor = SpotifyTextMuted,
                indicatorColor = SpotifyGreen.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_item_home")
        )

        NavigationBarItem(
            selected = currentTab == SpotifyTab.SEARCH,
            onClick = { onTabSelected(SpotifyTab.SEARCH) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SpotifyTab.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Buscar"
                )
            },
            label = { Text("Buscar") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpotifyTextWhite,
                selectedTextColor = SpotifyTextWhite,
                unselectedIconColor = SpotifyTextMuted,
                unselectedTextColor = SpotifyTextMuted,
                indicatorColor = SpotifyGreen.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_item_search")
        )

        NavigationBarItem(
            selected = currentTab == SpotifyTab.LIBRARY,
            onClick = { onTabSelected(SpotifyTab.LIBRARY) },
            icon = {
                Icon(
                    imageVector = if (currentTab == SpotifyTab.LIBRARY) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                    contentDescription = "Tu Biblioteca"
                )
            },
            label = { Text("Biblioteca") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = SpotifyTextWhite,
                selectedTextColor = SpotifyTextWhite,
                unselectedIconColor = SpotifyTextMuted,
                unselectedTextColor = SpotifyTextMuted,
                indicatorColor = SpotifyGreen.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_item_library")
        )
    }
}
