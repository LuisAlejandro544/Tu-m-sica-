package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ai.StemMode
import com.example.data.ai.StemSeparatorEngine
import com.example.data.db.TrackEntity
import com.example.player.RepeatMode
import com.example.ui.components.player.PlayerAdvancedOptionsSheet
import com.example.ui.components.player.PlayerAlbumArt
import com.example.ui.components.player.PlayerFooterBadge
import com.example.ui.components.player.PlayerLyricsView
import com.example.ui.components.player.PlayerPlaybackControls
import com.example.ui.components.player.PlayerSeekBar
import com.example.ui.components.player.PlayerTopBar
import com.example.ui.components.player.PlayerTrackHeader
import com.example.ui.theme.SpotifyBlack
import com.example.ui.theme.SpotifyDarkSlate
import com.example.ui.theme.SpotifyGreen

import com.example.ui.components.player.EqPreset

@Composable
fun PlayerFullScreen(
    currentTrack: TrackEntity?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    playbackSpeed: Float = 1.0f,
    playbackPitch: Float = 1.0f,
    isEqEnabled: Boolean = true,
    bandGainsDb: FloatArray = floatArrayOf(0f, 0f, 0f, 0f, 0f),
    eqPreset: EqPreset = EqPreset.FLAT,
    is3dAudioEnabled: Boolean = true,
    audio3dStrength: Float = 0.8f,
    audio3dMode: com.example.player.Audio3dSpeakerMode = com.example.player.Audio3dSpeakerMode.DUAL_SPEAKER,
    crossfadeDurationSec: Float = 3.0f,
    isVolumeNormalizerEnabled: Boolean = true,
    targetLufs: Float = -14.0f,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNextTrack: () -> Unit,
    onPreviousTrack: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeat: () -> Unit,
    onToggleFavorite: (TrackEntity) -> Unit,
    onSpeedChange: (Float) -> Unit = {},
    onPitchChange: (Float) -> Unit = {},
    onStemModeSelected: (StemMode) -> Unit = {},
    onEqEnabledToggle: (Boolean) -> Unit = {},
    onBandGainChange: (Int, Float) -> Unit = { _, _ -> },
    onPresetSelect: (EqPreset) -> Unit = {},
    onResetEq: () -> Unit = {},
    on3dAudioEnabledToggle: (Boolean) -> Unit = {},
    on3dStrengthChange: (Float) -> Unit = {},
    on3dModeSelect: (com.example.player.Audio3dSpeakerMode) -> Unit = {},
    onCrossfadeDurationChange: (Float) -> Unit = {},
    onVolumeNormalizerToggle: (Boolean) -> Unit = {},
    onTargetLufsChange: (Float) -> Unit = {},
    onUpdateLyrics: (TrackEntity, String) -> Unit = { _, _ -> },
    onVolumeClick: () -> Unit = {},
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {

    if (currentTrack == null) return

    var isUserScrubbing by remember { mutableStateOf(false) }
    var scrubPosition by remember { mutableFloatStateOf(0f) }
    var showAdvancedOptionsSheet by remember { mutableStateOf(false) }

    val stemState by StemSeparatorEngine.separationState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        SpotifyDarkSlate,
                        SpotifyBlack,
                        SpotifyBlack
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
            .testTag("full_screen_player")
    ) {
        // 1. Fixed Top Bar
        PlayerTopBar(
            albumName = currentTrack.album,
            onCollapse = onCollapse,
            onVolumeClick = onVolumeClick,
            onOptionsClick = { showAdvancedOptionsSheet = true }
        )

        // 2. Scrollable Spotify Player View (Album Art + Controls + Lyrics below)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Large Album Art
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                PlayerAlbumArt(coverArtPath = currentTrack.coverArtPath)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Track Title & Artist Info
            PlayerTrackHeader(
                track = currentTrack,
                onToggleFavorite = onToggleFavorite
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Seek Bar
            PlayerSeekBar(
                currentPositionMs = currentPositionMs,
                durationMs = durationMs,
                isUserScrubbing = isUserScrubbing,
                scrubPosition = scrubPosition,
                onScrubChange = {
                    isUserScrubbing = true
                    scrubPosition = it
                },
                onScrubFinished = { pos ->
                    isUserScrubbing = false
                    onSeekTo(pos)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls
            PlayerPlaybackControls(
                isPlaying = isPlaying,
                isShuffle = isShuffle,
                repeatMode = repeatMode,
                onTogglePlayPause = onTogglePlayPause,
                onNextTrack = onNextTrack,
                onPreviousTrack = onPreviousTrack,
                onToggleShuffle = onToggleShuffle,
                onToggleRepeat = onToggleRepeat
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Badge
            PlayerFooterBadge(isSampleTrack = currentTrack.isSample)

            Spacer(modifier = Modifier.height(24.dp))

            // Spotify-style Lyrics Section Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Lyrics,
                    contentDescription = null,
                    tint = SpotifyGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LETRAS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 14.sp
                    ),
                    color = SpotifyGreen
                )
            }

            // Spotify-Style Inline Scrollable Lyrics Card
            PlayerLyricsView(
                track = currentTrack,
                currentPositionMs = currentPositionMs,
                onUpdateLyrics = { newLyrics -> onUpdateLyrics(currentTrack, newLyrics) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Advanced Options Bottom Sheet (DSP Speed & Pitch + IA Stem Separator + Audio 3D)
    if (showAdvancedOptionsSheet) {
        PlayerAdvancedOptionsSheet(
            track = currentTrack,
            playbackSpeed = playbackSpeed,
            playbackPitch = playbackPitch,
            stemState = stemState,
            isEqEnabled = isEqEnabled,
            bandGainsDb = bandGainsDb,
            eqPreset = eqPreset,
            is3dAudioEnabled = is3dAudioEnabled,
            audio3dStrength = audio3dStrength,
            audio3dMode = audio3dMode,
            crossfadeDurationSec = crossfadeDurationSec,
            isVolumeNormalizerEnabled = isVolumeNormalizerEnabled,
            targetLufs = targetLufs,
            onSpeedChange = onSpeedChange,
            onPitchChange = onPitchChange,
            onStemModeSelected = onStemModeSelected,
            onEqEnabledToggle = onEqEnabledToggle,
            onBandGainChange = onBandGainChange,
            onPresetSelect = onPresetSelect,
            onResetEq = onResetEq,
            on3dAudioEnabledToggle = on3dAudioEnabledToggle,
            on3dStrengthChange = on3dStrengthChange,
            on3dModeSelect = on3dModeSelect,
            onCrossfadeDurationChange = onCrossfadeDurationChange,
            onVolumeNormalizerToggle = onVolumeNormalizerToggle,
            onTargetLufsChange = onTargetLufsChange,
            onDismiss = { showAdvancedOptionsSheet = false }
        )
    }
}
