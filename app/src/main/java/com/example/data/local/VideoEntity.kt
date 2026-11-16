package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val thumbnailPath: String,
    val capturedAtMillis: Long,
    val durationMillis: Long = 0L,
    val isFavorite: Boolean = false,
    val savedToDeviceGallery: Boolean = false,
    val isArchived: Boolean = false,
    val archivedAtMillis: Long? = null
)
