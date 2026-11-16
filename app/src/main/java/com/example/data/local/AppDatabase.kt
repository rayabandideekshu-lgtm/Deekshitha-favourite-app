package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PhotoEntity::class,
        JournalEntryEntity::class,
        VideoEntity::class,
        VideoJournalEntryEntity::class,
        EditProjectEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE photos ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE photos ADD COLUMN archivedAtMillis INTEGER")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE journal_entries ADD COLUMN archivedAtMillis INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS videos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        filePath TEXT NOT NULL,
                        thumbnailPath TEXT NOT NULL,
                        capturedAtMillis INTEGER NOT NULL,
                        durationMillis INTEGER NOT NULL DEFAULT 0,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        savedToDeviceGallery INTEGER NOT NULL DEFAULT 0,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        archivedAtMillis INTEGER
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS video_journal_entries (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        videoId INTEGER NOT NULL,
                        videoPath TEXT NOT NULL,
                        thumbnailPath TEXT NOT NULL,
                        place TEXT NOT NULL,
                        capturedAtMillis INTEGER NOT NULL,
                        durationMillis INTEGER NOT NULL DEFAULT 0,
                        outfit TEXT NOT NULL,
                        goodThingOne TEXT NOT NULL,
                        goodThingTwo TEXT NOT NULL,
                        caption TEXT NOT NULL,
                        templateId TEXT NOT NULL,
                        stickersCsv TEXT NOT NULL,
                        isFavorite INTEGER NOT NULL DEFAULT 0,
                        createdAtMillis INTEGER NOT NULL,
                        updatedAtMillis INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        archivedAtMillis INTEGER
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS edit_projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        projectType TEXT NOT NULL,
                        title TEXT NOT NULL,
                        sourcePath TEXT NOT NULL,
                        previewPath TEXT NOT NULL,
                        stateJson TEXT NOT NULL,
                        updatedAtMillis INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pose_memories_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
