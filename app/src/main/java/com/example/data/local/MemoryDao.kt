package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    // Active Photos
    @Query("SELECT * FROM photos WHERE isArchived = 0 ORDER BY capturedAtMillis DESC")
    fun observeAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isArchived = 0 ORDER BY capturedAtMillis ASC")
    fun observeAllPhotosOldestFirst(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE isArchived = 1 ORDER BY archivedAtMillis DESC")
    fun observeArchivedPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos ORDER BY capturedAtMillis DESC")
    suspend fun getAllPhotosOnce(): List<PhotoEntity>

    @Query("SELECT * FROM photos WHERE id = :photoId LIMIT 1")
    suspend fun getPhotoById(photoId: Long): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Update
    suspend fun updatePhoto(photo: PhotoEntity)

    @Query("UPDATE photos SET isFavorite = :isFavorite WHERE id = :photoId")
    suspend fun setPhotoFavorite(photoId: Long, isFavorite: Boolean)

    @Query("UPDATE photos SET savedToDeviceGallery = 1 WHERE id = :photoId")
    suspend fun markPhotoExportedToGallery(photoId: Long)

    @Query("UPDATE photos SET isArchived = 1, archivedAtMillis = :archivedAt WHERE id = :photoId")
    suspend fun archivePhotoById(photoId: Long, archivedAt: Long)

    @Query("UPDATE photos SET isArchived = 0, archivedAtMillis = NULL WHERE id = :photoId")
    suspend fun restorePhotoById(photoId: Long)

    @Query("UPDATE photos SET isArchived = 0, archivedAtMillis = NULL WHERE isArchived = 1")
    suspend fun restoreAllArchivedPhotos()

    @Query("DELETE FROM photos WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: Long)

    // Active & Archived Photo Journals
    @Query("SELECT * FROM journal_entries WHERE isArchived = 0 ORDER BY capturedAtMillis DESC")
    fun observeAllJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries WHERE isArchived = 1 ORDER BY archivedAtMillis DESC")
    fun observeArchivedJournalEntries(): Flow<List<JournalEntryEntity>>

    @Query("SELECT * FROM journal_entries ORDER BY capturedAtMillis DESC")
    suspend fun getAllJournalEntriesOnce(): List<JournalEntryEntity>

    @Query("SELECT * FROM journal_entries WHERE id = :entryId LIMIT 1")
    fun observeJournalEntryById(entryId: Long): Flow<JournalEntryEntity?>

    @Query("SELECT * FROM journal_entries WHERE id = :entryId LIMIT 1")
    suspend fun getJournalEntryById(entryId: Long): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE photoId = :photoId AND isArchived = 0 LIMIT 1")
    suspend fun getJournalEntryByPhotoId(photoId: Long): JournalEntryEntity?

    @Query("SELECT * FROM journal_entries WHERE photoId = :photoId LIMIT 1")
    suspend fun getAnyJournalEntryByPhotoId(photoId: Long): JournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJournalEntry(entry: JournalEntryEntity): Long

    @Update
    suspend fun updateJournalEntry(entry: JournalEntryEntity)

    @Query("UPDATE journal_entries SET isFavorite = :isFavorite WHERE id = :entryId")
    suspend fun setJournalFavorite(entryId: Long, isFavorite: Boolean)

    @Query("UPDATE journal_entries SET isArchived = 1, archivedAtMillis = :archivedAt WHERE id = :entryId")
    suspend fun archiveJournalEntryById(entryId: Long, archivedAt: Long)

    @Query("UPDATE journal_entries SET isArchived = 0, archivedAtMillis = NULL WHERE id = :entryId")
    suspend fun restoreJournalEntryById(entryId: Long)

    @Query("UPDATE journal_entries SET isArchived = 0, archivedAtMillis = NULL WHERE isArchived = 1")
    suspend fun restoreAllArchivedJournalEntries()

    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteJournalEntryById(entryId: Long)

    // Videos
    @Query("SELECT * FROM videos WHERE isArchived = 0 ORDER BY capturedAtMillis DESC")
    fun observeAllVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isArchived = 0 ORDER BY capturedAtMillis ASC")
    fun observeAllVideosOldestFirst(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE isArchived = 1 ORDER BY archivedAtMillis DESC")
    fun observeArchivedVideos(): Flow<List<VideoEntity>>

    @Query("SELECT * FROM videos WHERE id = :videoId LIMIT 1")
    suspend fun getVideoById(videoId: Long): VideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoEntity): Long

    @Query("UPDATE videos SET isFavorite = :isFavorite WHERE id = :videoId")
    suspend fun setVideoFavorite(videoId: Long, isFavorite: Boolean)

    @Query("UPDATE videos SET savedToDeviceGallery = 1 WHERE id = :videoId")
    suspend fun markVideoExportedToGallery(videoId: Long)

    @Query("UPDATE videos SET isArchived = 1, archivedAtMillis = :archivedAt WHERE id = :videoId")
    suspend fun archiveVideoById(videoId: Long, archivedAt: Long)

    @Query("UPDATE videos SET isArchived = 0, archivedAtMillis = NULL WHERE id = :videoId")
    suspend fun restoreVideoById(videoId: Long)

    @Query("UPDATE videos SET isArchived = 0, archivedAtMillis = NULL WHERE isArchived = 1")
    suspend fun restoreAllArchivedVideos()

    @Query("DELETE FROM videos WHERE id = :videoId")
    suspend fun deleteVideoById(videoId: Long)

    // Video Journals
    @Query("SELECT * FROM video_journal_entries WHERE isArchived = 0 ORDER BY capturedAtMillis DESC")
    fun observeAllVideoJournals(): Flow<List<VideoJournalEntryEntity>>

    @Query("SELECT * FROM video_journal_entries WHERE isArchived = 1 ORDER BY archivedAtMillis DESC")
    fun observeArchivedVideoJournals(): Flow<List<VideoJournalEntryEntity>>

    @Query("SELECT * FROM video_journal_entries WHERE id = :entryId LIMIT 1")
    suspend fun getVideoJournalById(entryId: Long): VideoJournalEntryEntity?

    @Query("SELECT * FROM video_journal_entries WHERE videoId = :videoId AND isArchived = 0 LIMIT 1")
    suspend fun getVideoJournalByVideoId(videoId: Long): VideoJournalEntryEntity?

    @Query("SELECT * FROM video_journal_entries WHERE videoId = :videoId LIMIT 1")
    suspend fun getAnyVideoJournalByVideoId(videoId: Long): VideoJournalEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideoJournal(entry: VideoJournalEntryEntity): Long

    @Update
    suspend fun updateVideoJournal(entry: VideoJournalEntryEntity)

    @Query("UPDATE video_journal_entries SET isFavorite = :isFavorite WHERE id = :entryId")
    suspend fun setVideoJournalFavorite(entryId: Long, isFavorite: Boolean)

    @Query("UPDATE video_journal_entries SET isArchived = 1, archivedAtMillis = :archivedAt WHERE id = :entryId")
    suspend fun archiveVideoJournalById(entryId: Long, archivedAt: Long)

    @Query("UPDATE video_journal_entries SET isArchived = 0, archivedAtMillis = NULL WHERE id = :entryId")
    suspend fun restoreVideoJournalById(entryId: Long)

    @Query("UPDATE video_journal_entries SET isArchived = 0, archivedAtMillis = NULL WHERE isArchived = 1")
    suspend fun restoreAllArchivedVideoJournals()

    @Query("DELETE FROM video_journal_entries WHERE id = :entryId")
    suspend fun deleteVideoJournalById(entryId: Long)

    // Clear all
    @Query("DELETE FROM journal_entries")
    suspend fun deleteAllJournalEntries()

    @Query("DELETE FROM photos")
    suspend fun deleteAllPhotos()

    @Query("DELETE FROM video_journal_entries")
    suspend fun deleteAllVideoJournals()

    @Query("DELETE FROM videos")
    suspend fun deleteAllVideos()

    // Non-destructive Edit Projects
    @Query("SELECT * FROM edit_projects WHERE projectType = :projectType ORDER BY updatedAtMillis DESC")
    fun observeEditProjectsByType(projectType: String): Flow<List<EditProjectEntity>>

    @Query("SELECT * FROM edit_projects WHERE id = :id LIMIT 1")
    suspend fun getEditProjectById(id: Long): EditProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEditProject(project: EditProjectEntity): Long

    @Query("DELETE FROM edit_projects WHERE id = :id")
    suspend fun deleteEditProjectById(id: Long)

    @Query("DELETE FROM edit_projects")
    suspend fun deleteAllEditProjects()
}
