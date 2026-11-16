package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val photoId: Long,
    val photoPath: String,
    val place: String,
    val capturedAtMillis: Long,
    val outfit: String,
    val goodThingOne: String,
    val goodThingTwo: String,
    val caption: String,
    val templateId: String,
    val stickersCsv: String,
    val isFavorite: Boolean = false,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val archivedAtMillis: Long? = null
)
