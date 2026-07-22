package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.PlaylistEntity
import com.example.data.db.TrackEntity
import com.example.data.repository.MusicRepository
import com.example.player.MusicPlayerManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SpotifyTab {
    HOME, SEARCH, LIBRARY
}

sealed class PlaylistDetailTarget {
    object Favorites : PlaylistDetailTarget()
    data class CustomPlaylist(val playlist: PlaylistEntity) : PlaylistDetailTarget()
    data class Folder(val folderName: String) : PlaylistDetailTarget()
}

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = MusicRepository(db)
    val playerManager = MusicPlayerManager(application)
    val volumeController = com.example.player.VolumeController(application)
    val volumeState: StateFlow<com.example.player.VolumeState> = volumeController.volumeState

    // Navigation state
    private val _currentTab = MutableStateFlow(SpotifyTab.HOME)
    val currentTab: StateFlow<SpotifyTab> = _currentTab.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _openedPlaylistDetail = MutableStateFlow<PlaylistDetailTarget?>(null)
    val openedPlaylistDetail: StateFlow<PlaylistDetailTarget?> = _openedPlaylistDetail.asStateFlow()

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Library filter chips
    private val _selectedFilterChip = MutableStateFlow("Todo")
    val selectedFilterChip: StateFlow<String> = _selectedFilterChip.asStateFlow()

    // Dialogs
    private val _showImportInfoDialog = MutableStateFlow(false)
    val showImportInfoDialog: StateFlow<Boolean> = _showImportInfoDialog.asStateFlow()

    private val _showCreatePlaylistDialog = MutableStateFlow(false)
    val showCreatePlaylistDialog: StateFlow<Boolean> = _showCreatePlaylistDialog.asStateFlow()

    private val _showTrackOptionsDialog = MutableStateFlow<TrackEntity?>(null)
    val showTrackOptionsDialog: StateFlow<TrackEntity?> = _showTrackOptionsDialog.asStateFlow()

    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _importMessage = MutableStateFlow<String?>(null)
    val importMessage: StateFlow<String?> = _importMessage.asStateFlow()

    // Repository Flows
    val allTracks: StateFlow<List<TrackEntity>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<TrackEntity>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentTracks: StateFlow<List<TrackEntity>> = repository.recentTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistEntity>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = repository.folders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered search tracks
    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: StateFlow<List<TrackEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) repository.allTracks
            else repository.searchTracks(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tracks for the currently opened playlist / folder / favorites
    @OptIn(ExperimentalCoroutinesApi::class)
    val activePlaylistTracks: StateFlow<List<TrackEntity>> = _openedPlaylistDetail
        .flatMapLatest { target ->
            when (target) {
                is PlaylistDetailTarget.Favorites -> repository.favoriteTracks
                is PlaylistDetailTarget.CustomPlaylist -> repository.getTracksForPlaylist(target.playlist.id)
                is PlaylistDetailTarget.Folder -> repository.getTracksByFolder(target.folderName)
                null -> MutableStateFlow(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live reactive current playing track that reflects isFavorite / coverArt changes
    val currentTrack: StateFlow<TrackEntity?> = combine(playerManager.currentTrack, allTracks) { playing, tracks ->
        if (playing == null) null
        else tracks.find { it.id == playing.id } ?: playing
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Load default demo sampler on first launch if library is completely empty
        viewModelScope.launch {
            repository.loadDemoTracksIfEmpty(getApplication())
        }
    }

    // Actions
    fun selectTab(tab: SpotifyTab) {
        _currentTab.value = tab
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    fun openPlaylistDetail(target: PlaylistDetailTarget?) {
        _openedPlaylistDetail.value = target
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterChip(chip: String) {
        _selectedFilterChip.value = chip
    }

    fun playTrack(track: TrackEntity, customQueue: List<TrackEntity>? = null) {
        val q = customQueue ?: allTracks.value
        val idx = q.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playerManager.setQueueAndPlay(q, idx)
    }

    fun toggleFavorite(track: TrackEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(track)
        }
    }

    fun importUris(uris: List<Uri>) {
        viewModelScope.launch {
            _importMessage.value = "Importando ${uris.size} archivo(s)..."
            val count = repository.importUris(getApplication(), uris)
            _importMessage.value = if (count > 0) {
                "Se importaron $count canción(es) correctamente a tu biblioteca local."
            } else {
                "No se encontraron archivos de audio válidos."
            }
        }
    }

    fun importUrisAndPlay(uris: List<Uri>) {
        viewModelScope.launch {
            _importMessage.value = "Abriendo archivo de audio..."
            val count = repository.importUris(getApplication(), uris)
            if (count > 0) {
                val firstUriStr = uris.first().toString()
                val all = repository.allTracks.first()
                val importedTrack = all.find { it.uriString == firstUriStr } ?: all.firstOrNull()
                if (importedTrack != null) {
                    playTrack(importedTrack, all)
                    _isPlayerExpanded.value = true
                    _importMessage.value = "Reproduciendo: ${importedTrack.title}"
                }
            } else {
                _importMessage.value = "No se pudo procesar el archivo de audio."
            }
        }
    }

    fun updateCustomCoverArt(track: TrackEntity, imageUri: Uri) {
        viewModelScope.launch {
            _importMessage.value = "Procesando carátula WebP en segundo plano..."
            val success = repository.updateCustomCoverArt(getApplication(), track, imageUri)
            _importMessage.value = if (success) {
                "Carátula WebP guardada correctamente sin congelar la app."
            } else {
                "No se pudo procesar la carátula elegida."
            }
        }
    }

    fun updateLyrics(track: TrackEntity, newLyrics: String) {
        viewModelScope.launch {
            repository.updateLyrics(getApplication(), track, newLyrics)
            _importMessage.value = "Letras actualizadas correctamente."
        }
    }

    fun clearImportMessage() {
        _importMessage.value = null
    }

    fun deleteTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.deleteTrack(track)
        }
    }

    fun createPlaylist(name: String, description: String = "") {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            _showCreatePlaylistDialog.value = false
        }
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
        }
    }

    fun exportLibrary(destinationUri: Uri) {
        viewModelScope.launch {
            val success = repository.exportLibraryToJson(getApplication(), destinationUri)
            _importMessage.value = if (success) {
                "Copia de seguridad exportada con éxito."
            } else {
                "Error al exportar la copia de seguridad."
            }
            _showExportDialog.value = false
        }
    }

    fun setShowImportInfoDialog(show: Boolean) {
        _showImportInfoDialog.value = show
    }

    fun setShowCreatePlaylistDialog(show: Boolean) {
        _showCreatePlaylistDialog.value = show
    }

    fun setShowTrackOptionsDialog(track: TrackEntity?) {
        _showTrackOptionsDialog.value = track
    }

    fun setShowExportDialog(show: Boolean) {
        _showExportDialog.value = show
    }

    // 3D Audio FX
    val is3dAudioEnabled: StateFlow<Boolean> = playerManager.is3dAudioEnabled
    val audio3dStrength: StateFlow<Float> = playerManager.audio3dStrength
    val audio3dMode: StateFlow<com.example.player.Audio3dSpeakerMode> = playerManager.audio3dMode

    fun set3dAudioEnabled(enabled: Boolean) {
        playerManager.set3dAudioEnabled(enabled)
    }

    fun set3dAudioStrength(strength: Float) {
        playerManager.set3dAudioStrength(strength)
    }

    fun set3dAudioMode(mode: com.example.player.Audio3dSpeakerMode) {
        playerManager.set3dAudioMode(mode)
    }

    // Crossfade & Volume Normalizer (EBU R128)
    val crossfadeDurationSec: StateFlow<Float> = playerManager.crossfadeDurationSec
    val isVolumeNormalizerEnabled: StateFlow<Boolean> = playerManager.isVolumeNormalizerEnabled
    val targetLufs: StateFlow<Float> = playerManager.targetLufs

    fun setCrossfadeDuration(sec: Float) {
        playerManager.setCrossfadeDuration(sec)
    }

    fun setVolumeNormalizerEnabled(enabled: Boolean) {
        playerManager.setVolumeNormalizerEnabled(enabled)
    }

    fun setTargetLufs(lufs: Float) {
        playerManager.setTargetLufs(lufs)
    }

    // ID3 Tag Cleaning
    fun cleanTrackTags(track: TrackEntity) {
        viewModelScope.launch {
            val result = com.example.util.Id3TagCleaner.cleanTrack(track)
            if (result.hasChanges) {
                repository.updateTrack(result.cleanedTrack)
                _importMessage.value = "Etiquetas ID3 corregidas para '${result.cleanedTrack.title}'."
            } else {
                _importMessage.value = "Las etiquetas ya están limpias."
            }
        }
    }

    fun cleanAllLibraryTags() {
        viewModelScope.launch {
            val allTracks = repository.allTracks.first()
            var count = 0
            allTracks.forEach { track ->
                val result = com.example.util.Id3TagCleaner.cleanTrack(track)
                if (result.hasChanges) {
                    repository.updateTrack(result.cleanedTrack)
                    count++
                }
            }
            _importMessage.value = "Se limpiaron y corrigieron etiquetas de $count canciones."
        }
    }

    // Acoustic Fingerprint & Duplicates
    private val _duplicateClusters = MutableStateFlow<List<com.example.util.DuplicateCluster>>(emptyList())
    val duplicateClusters: StateFlow<List<com.example.util.DuplicateCluster>> = _duplicateClusters.asStateFlow()

    private val _isScanningDuplicates = MutableStateFlow(false)
    val isScanningDuplicates: StateFlow<Boolean> = _isScanningDuplicates.asStateFlow()

    fun scanDuplicates() {
        viewModelScope.launch {
            _isScanningDuplicates.value = true
            val allTracks = repository.allTracks.first()
            val clusters = com.example.util.AudioFingerprintEngine.findDuplicates(allTracks)
            _duplicateClusters.value = clusters
            _isScanningDuplicates.value = false
            if (clusters.isEmpty()) {
                _importMessage.value = "¡No se encontraron canciones duplicadas en tu biblioteca!"
            }
        }
    }

    fun deleteDuplicateTrack(track: TrackEntity) {
        viewModelScope.launch {
            repository.deleteTrack(track)
            scanDuplicates()
            _importMessage.value = "Canción duplicada eliminada con éxito."
        }
    }

    // Custom Volume Controller HUD
    fun setVolumePercent(percent: Float) {
        volumeController.setVolumePercent(percent, showHud = true)
    }

    fun adjustVolume(deltaStep: Int) {
        volumeController.adjustVolume(deltaStep)
    }

    fun toggleMuteVolume() {
        volumeController.toggleMute()
    }

    fun showVolumeHud() {
        volumeController.showHud()
    }

    fun hideVolumeHud() {
        volumeController.hideHud()
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
        volumeController.release()
    }
}
