package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PlayerFullScreen
import com.example.ui.screens.PlaylistDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.SpotifyBlack
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.PlaylistDetailTarget
import com.example.ui.viewmodel.SpotifyTab

private data class PlaylistDetailInfo(
    val title: String,
    val subtitle: String,
    val isFav: Boolean,
    val isFolder: Boolean
)

@Composable
fun SpotLocalMainScaffold(
    viewModel: PlayerViewModel,
    snackbarHostState: SnackbarHostState,
    currentTab: SpotifyTab,
    isPlayerExpanded: Boolean,
    allTracks: List<TrackEntity>,
    favoriteTracks: List<TrackEntity>,
    recentTracks: List<TrackEntity>,
    playlists: List<PlaylistEntity>,
    folders: List<String>,
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: com.example.player.RepeatMode,
    playbackSpeed: Float,
    playbackPitch: Float,
    searchQuery: String,
    searchResults: List<TrackEntity>,
    selectedFilter: String,
    showCreatePlaylistDialog: Boolean,
    trackOptionsTarget: TrackEntity?,
    onLaunchImportPicker: () -> Unit,
    onLaunchExportPicker: () -> Unit,
    onPickCustomCover: (TrackEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDebugConsole by remember { mutableStateOf(false) }

    val openedPlaylistDetail by viewModel.openedPlaylistDetail.collectAsStateWithLifecycle()
    val activePlaylistTracks by viewModel.activePlaylistTracks.collectAsStateWithLifecycle()

    val isEqEnabled by viewModel.playerManager.isEqEnabled.collectAsStateWithLifecycle()
    val bandGainsDb by viewModel.playerManager.bandGainsDb.collectAsStateWithLifecycle()
    val eqPreset by viewModel.playerManager.eqPreset.collectAsStateWithLifecycle()

    val is3dAudioEnabled by viewModel.is3dAudioEnabled.collectAsStateWithLifecycle()
    val audio3dStrength by viewModel.audio3dStrength.collectAsStateWithLifecycle()
    val audio3dMode by viewModel.audio3dMode.collectAsStateWithLifecycle()

    val crossfadeDurationSec by viewModel.crossfadeDurationSec.collectAsStateWithLifecycle()
    val isVolumeNormalizerEnabled by viewModel.isVolumeNormalizerEnabled.collectAsStateWithLifecycle()
    val targetLufs by viewModel.targetLufs.collectAsStateWithLifecycle()

    val duplicateClusters by viewModel.duplicateClusters.collectAsStateWithLifecycle()
    val isScanningDuplicates by viewModel.isScanningDuplicates.collectAsStateWithLifecycle()
    var showDuplicateModal by remember { mutableStateOf(false) }

    val volumeState by viewModel.volumeState.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpotifyBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SpotifyBlack,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Sticky Mini Player
                    if (currentTrack != null && !isPlayerExpanded) {
                        MiniPlayer(
                            currentTrack = currentTrack,
                            isPlaying = isPlaying,
                            currentPositionMs = currentPositionMs,
                            durationMs = durationMs,
                            onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onClickMiniPlayer = { viewModel.setPlayerExpanded(true) }
                        )
                    }

                    // Spotify Navigation Bar
                    SpotifyBottomNav(
                        currentTab = currentTab,
                        onTabSelected = {
                            viewModel.openPlaylistDetail(null)
                            viewModel.selectTab(it)
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (openedPlaylistDetail != null) {
                    val detail = openedPlaylistDetail!!
                    val info = when (detail) {
                        is PlaylistDetailTarget.Favorites -> PlaylistDetailInfo("Canciones que te gustan", "Lista de favoritos", true, false)
                        is PlaylistDetailTarget.CustomPlaylist -> PlaylistDetailInfo(detail.playlist.name, detail.playlist.description.ifBlank { "Lista creada por ti" }, false, false)
                        is PlaylistDetailTarget.Folder -> PlaylistDetailInfo(detail.folderName, "Carpeta de música local", false, true)
                    }

                    PlaylistDetailScreen(
                        title = info.title,
                        subtitle = info.subtitle,
                        tracks = activePlaylistTracks,
                        currentTrack = currentTrack,
                        isFavoritesList = info.isFav,
                        isFolderList = info.isFolder,
                        onBackClick = { viewModel.openPlaylistDetail(null) },
                        onTrackClick = { track, list -> viewModel.playTrack(track, list) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onOptionsClick = { viewModel.setShowTrackOptionsDialog(it) },
                        onPlayAllClick = { tracks -> viewModel.playerManager.setQueueAndPlay(tracks, 0) }
                    )
                } else {
                    when (currentTab) {
                        SpotifyTab.HOME -> {
                            HomeScreen(
                                allTracks = allTracks,
                                favoriteTracks = favoriteTracks,
                                recentTracks = recentTracks,
                                currentTrack = currentTrack,
                                onTrackClick = { track -> viewModel.playTrack(track) },
                                onFavoriteToggle = { track -> viewModel.toggleFavorite(track) },
                                onOptionsClick = { track -> viewModel.setShowTrackOptionsDialog(track) },
                                onImportClick = onLaunchImportPicker,
                                onExportClick = onLaunchExportPicker,
                                onOpenFavoritesClick = { viewModel.openPlaylistDetail(PlaylistDetailTarget.Favorites) },
                                onOpenFolderClick = { folder -> viewModel.openPlaylistDetail(PlaylistDetailTarget.Folder(folder)) }
                            )
                        }
                        SpotifyTab.SEARCH -> {
                            SearchScreen(
                                searchQuery = searchQuery,
                                searchResults = searchResults,
                                currentTrack = currentTrack,
                                onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                onTrackClick = { track -> viewModel.playTrack(track, searchResults) },
                                onFavoriteToggle = { track -> viewModel.toggleFavorite(track) },
                                onOptionsClick = { track -> viewModel.setShowTrackOptionsDialog(track) }
                            )
                        }
                        SpotifyTab.LIBRARY -> {
                            LibraryScreen(
                                selectedFilter = selectedFilter,
                                allTracks = allTracks,
                                favoriteTracks = favoriteTracks,
                                playlists = playlists,
                                folders = folders,
                                currentTrack = currentTrack,
                                onFilterSelect = { viewModel.setFilterChip(it) },
                                onTrackClick = { track -> viewModel.playTrack(track) },
                                onFavoriteToggle = { track -> viewModel.toggleFavorite(track) },
                                onOptionsClick = { track -> viewModel.setShowTrackOptionsDialog(track) },
                                onCreatePlaylistClick = { viewModel.setShowCreatePlaylistDialog(true) },
                                onImportClick = onLaunchImportPicker,
                                onOpenFavoritesClick = { viewModel.openPlaylistDetail(PlaylistDetailTarget.Favorites) },
                                onOpenPlaylistClick = { playlist -> viewModel.openPlaylistDetail(PlaylistDetailTarget.CustomPlaylist(playlist)) },
                                onOpenFolderClick = { folder -> viewModel.openPlaylistDetail(PlaylistDetailTarget.Folder(folder)) },
                                onScanDuplicatesClick = {
                                    showDuplicateModal = true
                                    viewModel.scanDuplicates()
                                },
                                onCleanTagsBatchClick = { viewModel.cleanAllLibraryTags() }
                            )
                        }
                    }
                }
            }
        }

        // Full Screen Player Modal Overlay
        AnimatedVisibility(
            visible = isPlayerExpanded,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PlayerFullScreen(
                currentTrack = currentTrack,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                playbackSpeed = playbackSpeed,
                playbackPitch = playbackPitch,
                isEqEnabled = isEqEnabled,
                bandGainsDb = bandGainsDb,
                eqPreset = eqPreset,
                is3dAudioEnabled = is3dAudioEnabled,
                audio3dStrength = audio3dStrength,
                audio3dMode = audio3dMode,
                crossfadeDurationSec = crossfadeDurationSec,
                isVolumeNormalizerEnabled = isVolumeNormalizerEnabled,
                targetLufs = targetLufs,
                onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                onSeekTo = { pos -> viewModel.playerManager.seekTo(pos) },
                onNextTrack = { viewModel.playerManager.nextTrack() },
                onPreviousTrack = { viewModel.playerManager.previousTrack() },
                onToggleShuffle = { viewModel.playerManager.toggleShuffle() },
                onToggleRepeat = { viewModel.playerManager.toggleRepeat() },
                onToggleFavorite = { track -> viewModel.toggleFavorite(track) },
                onSpeedChange = { speed -> viewModel.playerManager.setSpeed(speed) },
                onPitchChange = { pitch -> viewModel.playerManager.setPitch(pitch) },
                onStemModeSelected = { mode -> viewModel.playerManager.setStemMode(mode) },
                onEqEnabledToggle = { enabled -> viewModel.playerManager.setEqEnabled(enabled) },
                onBandGainChange = { bandIndex, gain -> viewModel.playerManager.setBandGain(bandIndex, gain) },
                onPresetSelect = { preset -> viewModel.playerManager.setEqPreset(preset) },
                onResetEq = { viewModel.playerManager.resetEq() },
                on3dAudioEnabledToggle = { enabled -> viewModel.set3dAudioEnabled(enabled) },
                on3dStrengthChange = { strength -> viewModel.set3dAudioStrength(strength) },
                on3dModeSelect = { mode -> viewModel.set3dAudioMode(mode) },
                onCrossfadeDurationChange = { sec -> viewModel.setCrossfadeDuration(sec) },
                onVolumeNormalizerToggle = { enabled -> viewModel.setVolumeNormalizerEnabled(enabled) },
                onTargetLufsChange = { lufs -> viewModel.setTargetLufs(lufs) },
                onUpdateLyrics = { track, newLyrics -> viewModel.updateLyrics(track, newLyrics) },
                onVolumeClick = { viewModel.showVolumeHud() },
                onCollapse = { viewModel.setPlayerExpanded(false) }
            )
        }

        // Floating Debug Log Console Trigger (Only visible in Debug builds)
        if (com.example.BuildConfig.DEBUG) {
            FloatingDebugButton(
                onClick = { showDebugConsole = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 42.dp, end = 16.dp)
            )
        }

        // Debug Log Console Modal
        if (showDebugConsole) {
            DebugLogConsoleModal(
                onDismiss = { showDebugConsole = false }
            )
        }

        // Dialogs
        if (showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                onDismiss = { viewModel.setShowCreatePlaylistDialog(false) },
                onCreate = { name, desc -> viewModel.createPlaylist(name, desc) }
            )
        }

        trackOptionsTarget?.let { track ->
            TrackOptionsDialog(
                track = track,
                onDismiss = { viewModel.setShowTrackOptionsDialog(null) },
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onCleanTags = { viewModel.cleanTrackTags(it) },
                onDeleteTrack = { viewModel.deleteTrack(it) },
                onPickCustomCover = { onPickCustomCover(it) }
            )
        }

        if (showDuplicateModal) {
            com.example.ui.components.library.DuplicateDetectorModal(
                clusters = duplicateClusters,
                isScanning = isScanningDuplicates,
                onScan = { viewModel.scanDuplicates() },
                onPlayTrack = { viewModel.playTrack(it) },
                onDeleteTrack = { viewModel.deleteDuplicateTrack(it) },
                onDismiss = { showDuplicateModal = false }
            )
        }

        // Custom Volume Panel HUD
        com.example.ui.components.player.CustomVolumePanelHUD(
            volumeState = volumeState,
            onVolumeChangePercent = { viewModel.setVolumePercent(it) },
            onToggleMute = { viewModel.toggleMuteVolume() },
            onHideHud = { viewModel.hideVolumeHud() }
        )
    }
}
