package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val filePath: String,
    val capturedAtMillis: Long,
    val isFavorite: Boolean = false,
    val width: Int = 0,
    val height: Int = 0,
    val savedToDeviceGallery: Boolean = false,
    val isArchived: Boolean = false,
    val archivedAtMillis: Long? = null
)
