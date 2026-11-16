package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "edit_projects")
data class EditProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectType: String, // "PHOTO" or "VIDEO"
    val title: String,
    val sourcePath: String,
    val previewPath: String,
    val stateJson: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
)
