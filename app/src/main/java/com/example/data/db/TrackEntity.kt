package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uriString: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val fileSizeBytes: Long = 0,
    val folderName: String = "Importados",
    val dateImported: Long = System.currentTimeMillis(),
    val coverArtPath: String? = null,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val isSample: Boolean = false,
    val lyrics: String? = null,
    val loudnessLufs: Float? = -14.0f,
    val acousticHash: String? = null
)
