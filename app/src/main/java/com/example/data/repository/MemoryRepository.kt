package com.example.data.repository

import android.net.Uri
import com.example.data.local.JournalEntryEntity
import com.example.data.local.MediaStorageHelper
import com.example.data.local.MemoryDao
import com.example.data.local.PhotoEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.StoredPhotoResult
import com.example.data.local.StoredVideoResult
import com.example.data.local.UserSettings
import com.example.data.local.VideoEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.data.model.ArchiveItemType
import com.example.data.model.ArchivedMemoryItem
import com.example.data.model.GallerySortOrder
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseOutlineType
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class MemoryRepository(
    private val dao: MemoryDao,
    private val storage: MediaStorageHelper,
    private val prefs: SettingsPreferences
) {
    val allJournalEntries: Flow<List<JournalEntryEntity>> = dao.observeAllJournalEntries()
    val allVideoJournals: Flow<List<VideoJournalEntryEntity>> = dao.observeAllVideoJournals()
    val userSettings: Flow<UserSettings> = prefs.settingsFlow

    fun observePhotos(sortOrder: GallerySortOrder): Flow<List<PhotoEntity>> = when (sortOrder) {
        GallerySortOrder.NEWEST -> dao.observeAllPhotos()
        GallerySortOrder.OLDEST -> dao.observeAllPhotosOldestFirst()
    }

    fun observeVideos(sortOrder: GallerySortOrder): Flow<List<VideoEntity>> = when (sortOrder) {
        GallerySortOrder.NEWEST -> dao.observeAllVideos()
        GallerySortOrder.OLDEST -> dao.observeAllVideosOldestFirst()
    }

    fun observeArchivedItems(): Flow<List<ArchivedMemoryItem>> = combine(
        dao.observeArchivedPhotos(),
        dao.observeArchivedJournalEntries(),
        dao.observeArchivedVideos(),
        dao.observeArchivedVideoJournals()
    ) { archivedPhotos, archivedJournals, archivedVideos, archivedVideoJournals ->
        val items = mutableListOf<ArchivedMemoryItem>()
        archivedPhotos.forEach { photo ->
            items += ArchivedMemoryItem(
                uniqueKey = "photo_${photo.id}",
                entityId = photo.id,
                type = ArchiveItemType.PHOTO,
                thumbnailPath = photo.filePath,
                mediaPath = photo.filePath,
                caption = "Archived photograph",
                place = "",
                originalTimestampMillis = photo.capturedAtMillis,
                archivedAtMillis = photo.archivedAtMillis ?: photo.capturedAtMillis
            )
        }
        archivedJournals.forEach { entry ->
            items += ArchivedMemoryItem(
                uniqueKey = "pjournal_${entry.id}",
                entityId = entry.id,
                type = ArchiveItemType.PHOTO_JOURNAL,
                thumbnailPath = entry.photoPath,
                mediaPath = entry.photoPath,
                caption = entry.caption,
                place = entry.place,
                originalTimestampMillis = entry.capturedAtMillis,
                archivedAtMillis = entry.archivedAtMillis ?: entry.updatedAtMillis
            )
        }
        archivedVideos.forEach { video ->
            items += ArchivedMemoryItem(
                uniqueKey = "video_${video.id}",
                entityId = video.id,
                type = ArchiveItemType.VIDEO,
                thumbnailPath = video.thumbnailPath,
                mediaPath = video.filePath,
                caption = "Archived video memory",
                place = "",
                originalTimestampMillis = video.capturedAtMillis,
                archivedAtMillis = video.archivedAtMillis ?: video.capturedAtMillis
            )
        }
        archivedVideoJournals.forEach { vj ->
            items += ArchivedMemoryItem(
                uniqueKey = "vjournal_${vj.id}",
                entityId = vj.id,
                type = ArchiveItemType.VIDEO_JOURNAL,
                thumbnailPath = vj.thumbnailPath,
                mediaPath = vj.videoPath,
                caption = vj.caption,
                place = vj.place,
                originalTimestampMillis = vj.capturedAtMillis,
                archivedAtMillis = vj.archivedAtMillis ?: vj.updatedAtMillis
            )
        }
        items.sortedByDescending { it.archivedAtMillis }
    }

    fun observeJournalEntry(entryId: Long): Flow<JournalEntryEntity?> =
        dao.observeJournalEntryById(entryId)

    fun createTempCaptureFile(): File = storage.createTempCaptureFile()
    fun createTempVideoFile(): File = storage.createTempVideoFile()

    suspend fun maybeApplyGuideOverlayToTempPhoto(
        tempPath: String,
        outlineType: PoseOutlineType,
        config: PoseOutlineConfig
    ) {
        storage.maybeCompositeGuideOnTempPhoto(tempPath, outlineType, config)
    }

    suspend fun importPickedPhoto(uri: Uri): Pair<String, Long> {
        val result = storage.importUriToPrivateStorage(uri)
        return result.filePath to System.currentTimeMillis()
    }

    suspend fun importPickedVideo(uri: Uri): StoredVideoResult =
        storage.importVideoUriToPrivateStorage(uri)

    suspend fun saveStandalonePhoto(
        tempOrPrivatePath: String,
        capturedAtMillis: Long,
        exportToDeviceGallery: Boolean = true
    ): Long {
        val committedPath = if (tempOrPrivatePath.contains("/captures/")) {
            storage.commitTempPhotoToPrivateStorage(tempOrPrivatePath)
        } else {
            val file = File(tempOrPrivatePath)
            if (file.parentFile?.name == "photos") {
                StoredPhotoResult(file.absolutePath, 0, 0)
            } else {
                storage.commitTempPhotoToPrivateStorage(tempOrPrivatePath)
            }
        }
        val exported = if (exportToDeviceGallery) {
            storage.exportPhotoToDeviceGallery(committedPath.filePath)
        } else {
            false
        }
        val photo = PhotoEntity(
            filePath = committedPath.filePath,
            capturedAtMillis = capturedAtMillis,
            width = committedPath.width,
            height = committedPath.height,
            savedToDeviceGallery = exported
        )
        return dao.insertPhoto(photo)
    }

    suspend fun saveStandaloneVideo(
        tempOrPrivatePath: String,
        thumbnailPath: String?,
        durationMillis: Long,
        capturedAtMillis: Long,
        exportToDeviceGallery: Boolean = true
    ): Long {
        val stored = if (tempOrPrivatePath.contains("/video_captures/") || thumbnailPath == null) {
            storage.commitTempVideoToPrivateStorage(tempOrPrivatePath)
        } else {
            StoredVideoResult(tempOrPrivatePath, thumbnailPath, durationMillis)
        }
        val exported = if (exportToDeviceGallery) {
            storage.exportVideoToDeviceGallery(stored.filePath)
        } else {
            false
        }
        val video = VideoEntity(
            filePath = stored.filePath,
            thumbnailPath = stored.thumbnailPath,
            capturedAtMillis = capturedAtMillis,
            durationMillis = stored.durationMillis,
            savedToDeviceGallery = exported
        )
        return dao.insertVideo(video)
    }

    suspend fun saveNewJournalMemory(
        tempOrPrivatePath: String,
        existingPhotoId: Long?,
        place: String,
        capturedAtMillis: Long,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToDeviceGallery: Boolean
    ): Long {
        val photoId: Long
        val finalPhotoPath: String
        if (existingPhotoId != null) {
            val existing = dao.getPhotoById(existingPhotoId)
            if (existing != null) {
                photoId = existing.id
                finalPhotoPath = existing.filePath
            } else {
                val stored = storage.commitTempPhotoToPrivateStorage(tempOrPrivatePath)
                finalPhotoPath = stored.filePath
                photoId = dao.insertPhoto(
                    PhotoEntity(
                        filePath = finalPhotoPath,
                        capturedAtMillis = capturedAtMillis,
                        width = stored.width,
                        height = stored.height
                    )
                )
            }
        } else {
            val stored = if (File(tempOrPrivatePath).parentFile?.name == "photos") {
                StoredPhotoResult(tempOrPrivatePath, 0, 0)
            } else {
                storage.commitTempPhotoToPrivateStorage(tempOrPrivatePath)
            }
            finalPhotoPath = stored.filePath
            val exported = if (alsoSaveToDeviceGallery) {
                storage.exportPhotoToDeviceGallery(finalPhotoPath)
            } else {
                false
            }
            photoId = dao.insertPhoto(
                PhotoEntity(
                    filePath = finalPhotoPath,
                    capturedAtMillis = capturedAtMillis,
                    width = stored.width,
                    height = stored.height,
                    savedToDeviceGallery = exported
                )
            )
        }

        val now = System.currentTimeMillis()
        val entry = JournalEntryEntity(
            photoId = photoId,
            photoPath = finalPhotoPath,
            place = place.trim(),
            capturedAtMillis = capturedAtMillis,
            outfit = outfit.trim(),
            goodThingOne = goodThingOne.trim(),
            goodThingTwo = goodThingTwo.trim(),
            caption = caption.trim(),
            templateId = templateId,
            stickersCsv = stickersCsv,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        return dao.insertJournalEntry(entry)
    }

    suspend fun saveNewVideoJournalMemory(
        tempOrPrivatePath: String,
        existingVideoId: Long?,
        existingThumbnailPath: String?,
        durationMillis: Long,
        place: String,
        capturedAtMillis: Long,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToDeviceGallery: Boolean
    ): Long {
        val videoId: Long
        val finalVideoPath: String
        val finalThumbPath: String
        val finalDuration: Long

        if (existingVideoId != null) {
            val existing = dao.getVideoById(existingVideoId)
            if (existing != null) {
                videoId = existing.id
                finalVideoPath = existing.filePath
                finalThumbPath = existing.thumbnailPath
                finalDuration = existing.durationMillis
            } else {
                val stored = storage.commitTempVideoToPrivateStorage(tempOrPrivatePath)
                finalVideoPath = stored.filePath
                finalThumbPath = stored.thumbnailPath
                finalDuration = stored.durationMillis
                videoId = dao.insertVideo(
                    VideoEntity(
                        filePath = finalVideoPath,
                        thumbnailPath = finalThumbPath,
                        capturedAtMillis = capturedAtMillis,
                        durationMillis = finalDuration
                    )
                )
            }
        } else {
            val stored = if (File(tempOrPrivatePath).parentFile?.name == "videos" && existingThumbnailPath != null) {
                StoredVideoResult(tempOrPrivatePath, existingThumbnailPath, durationMillis)
            } else {
                storage.commitTempVideoToPrivateStorage(tempOrPrivatePath)
            }
            finalVideoPath = stored.filePath
            finalThumbPath = stored.thumbnailPath
            finalDuration = stored.durationMillis
            val exported = if (alsoSaveToDeviceGallery) {
                storage.exportVideoToDeviceGallery(finalVideoPath)
            } else {
                false
            }
            videoId = dao.insertVideo(
                VideoEntity(
                    filePath = finalVideoPath,
                    thumbnailPath = finalThumbPath,
                    capturedAtMillis = capturedAtMillis,
                    durationMillis = finalDuration,
                    savedToDeviceGallery = exported
                )
            )
        }

        val now = System.currentTimeMillis()
        val entry = VideoJournalEntryEntity(
            videoId = videoId,
            videoPath = finalVideoPath,
            thumbnailPath = finalThumbPath,
            place = place.trim(),
            capturedAtMillis = capturedAtMillis,
            durationMillis = finalDuration,
            outfit = outfit.trim(),
            goodThingOne = goodThingOne.trim(),
            goodThingTwo = goodThingTwo.trim(),
            caption = caption.trim(),
            templateId = templateId,
            stickersCsv = stickersCsv,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        return dao.insertVideoJournal(entry)
    }

    suspend fun updateExistingJournalEntry(entry: JournalEntryEntity) {
        dao.updateJournalEntry(entry.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun updateExistingVideoJournalEntry(entry: VideoJournalEntryEntity) {
        dao.updateVideoJournal(entry.copy(updatedAtMillis = System.currentTimeMillis()))
    }

    suspend fun getJournalEntryById(entryId: Long): JournalEntryEntity? =
        dao.getJournalEntryById(entryId)

    suspend fun getJournalEntryByPhotoId(photoId: Long): JournalEntryEntity? =
        dao.getJournalEntryByPhotoId(photoId)

    suspend fun getVideoJournalById(entryId: Long): VideoJournalEntryEntity? =
        dao.getVideoJournalById(entryId)

    suspend fun getVideoJournalByVideoId(videoId: Long): VideoJournalEntryEntity? =
        dao.getVideoJournalByVideoId(videoId)

    suspend fun getPhotoById(photoId: Long): PhotoEntity? =
        dao.getPhotoById(photoId)

    suspend fun getVideoById(videoId: Long): VideoEntity? =
        dao.getVideoById(videoId)

    suspend fun toggleJournalFavorite(entry: JournalEntryEntity) {
        val next = !entry.isFavorite
        dao.setJournalFavorite(entry.id, next)
        dao.setPhotoFavorite(entry.photoId, next)
    }

    suspend fun togglePhotoFavorite(photo: PhotoEntity) {
        val next = !photo.isFavorite
        dao.setPhotoFavorite(photo.id, next)
        dao.getJournalEntryByPhotoId(photo.id)?.let { entry ->
            dao.setJournalFavorite(entry.id, next)
        }
    }

    suspend fun toggleVideoFavorite(video: VideoEntity) {
        val next = !video.isFavorite
        dao.setVideoFavorite(video.id, next)
        dao.getVideoJournalByVideoId(video.id)?.let { entry ->
            dao.setVideoJournalFavorite(entry.id, next)
        }
    }

    suspend fun toggleVideoJournalFavorite(entry: VideoJournalEntryEntity) {
        val next = !entry.isFavorite
        dao.setVideoJournalFavorite(entry.id, next)
        dao.setVideoFavorite(entry.videoId, next)
    }

    // Soft-delete: move items to MY ARCHIVE without deleting media files
    suspend fun archiveJournalEntry(entry: JournalEntryEntity) {
        dao.archiveJournalEntryById(entry.id, System.currentTimeMillis())
    }

    suspend fun archivePhotoAndRelatedJournal(photo: PhotoEntity) {
        val now = System.currentTimeMillis()
        dao.archivePhotoById(photo.id, now)
        dao.getJournalEntryByPhotoId(photo.id)?.let { entry ->
            dao.archiveJournalEntryById(entry.id, now)
        }
    }

    suspend fun archiveVideoJournalEntry(entry: VideoJournalEntryEntity) {
        dao.archiveVideoJournalById(entry.id, System.currentTimeMillis())
    }

    suspend fun archiveVideoAndRelatedJournal(video: VideoEntity) {
        val now = System.currentTimeMillis()
        dao.archiveVideoById(video.id, now)
        dao.getVideoJournalByVideoId(video.id)?.let { entry ->
            dao.archiveVideoJournalById(entry.id, now)
        }
    }

    // Restore from MY ARCHIVE back to active collections
    suspend fun restoreArchivedItem(item: ArchivedMemoryItem) {
        when (item.type) {
            ArchiveItemType.PHOTO -> dao.restorePhotoById(item.entityId)
            ArchiveItemType.PHOTO_JOURNAL -> {
                dao.getJournalEntryById(item.entityId)?.let { entry ->
                    dao.restorePhotoById(entry.photoId)
                }
                dao.restoreJournalEntryById(item.entityId)
            }
            ArchiveItemType.VIDEO -> dao.restoreVideoById(item.entityId)
            ArchiveItemType.VIDEO_JOURNAL -> {
                dao.getVideoJournalById(item.entityId)?.let { entry ->
                    dao.restoreVideoById(entry.videoId)
                }
                dao.restoreVideoJournalById(item.entityId)
            }
        }
    }

    suspend fun restoreAllArchived() {
        dao.restoreAllArchivedPhotos()
        dao.restoreAllArchivedJournalEntries()
        dao.restoreAllArchivedVideos()
        dao.restoreAllArchivedVideoJournals()
    }

    // Permanent delete (only inside MY ARCHIVE after explicit user confirmation)
    suspend fun permanentlyDeleteArchivedItem(item: ArchivedMemoryItem) {
        when (item.type) {
            ArchiveItemType.PHOTO -> {
                val linkedJournal = dao.getAnyJournalEntryByPhotoId(item.entityId)
                dao.deletePhotoById(item.entityId)
                if (linkedJournal == null) {
                    storage.deletePhotoFile(item.mediaPath)
                }
            }
            ArchiveItemType.PHOTO_JOURNAL -> {
                val entry = dao.getJournalEntryById(item.entityId)
                dao.deleteJournalEntryById(item.entityId)
                if (entry != null && dao.getPhotoById(entry.photoId) == null) {
                    storage.deletePhotoFile(entry.photoPath)
                }
            }
            ArchiveItemType.VIDEO -> {
                val linkedJournal = dao.getAnyVideoJournalByVideoId(item.entityId)
                dao.deleteVideoById(item.entityId)
                if (linkedJournal == null) {
                    storage.deleteVideoFiles(item.mediaPath, item.thumbnailPath)
                }
            }
            ArchiveItemType.VIDEO_JOURNAL -> {
                val entry = dao.getVideoJournalById(item.entityId)
                dao.deleteVideoJournalById(item.entityId)
                if (entry != null && dao.getVideoById(entry.videoId) == null) {
                    storage.deleteVideoFiles(entry.videoPath, entry.thumbnailPath)
                }
            }
        }
    }

    suspend fun exportPhotoToDeviceGallery(filePath: String, photoId: Long? = null): Boolean {
        val ok = storage.exportPhotoToDeviceGallery(filePath)
        if (ok && photoId != null) {
            dao.markPhotoExportedToGallery(photoId)
        }
        return ok
    }

    suspend fun exportVideoToDeviceGallery(filePath: String, videoId: Long? = null): Boolean {
        val ok = storage.exportVideoToDeviceGallery(filePath)
        if (ok && videoId != null) {
            dao.markVideoExportedToGallery(videoId)
        }
        return ok
    }

    fun getShareableUri(filePath: String): Uri? = storage.getShareableUri(filePath)

    suspend fun exportBackupZip(outputStream: OutputStream) {
        val photos = dao.getAllPhotosOnce()
        val entries = dao.getAllJournalEntriesOnce()
        storage.exportBackupZip(outputStream, photos, entries)
    }

    suspend fun exportReadableJournal(outputStream: OutputStream) {
        val entries = dao.getAllJournalEntriesOnce()
        storage.exportReadableJournalText(outputStream, entries)
    }

    suspend fun importBackupZip(inputStream: InputStream): Int {
        val restored = storage.importBackupZip(inputStream)
        val pathToNewPhotoId = mutableMapOf<String, Long>()
        restored.photos.forEach { photo ->
            val newId = dao.insertPhoto(photo)
            pathToNewPhotoId[photo.filePath] = newId
        }
        var count = 0
        restored.entries.forEach { entry ->
            val mappedPhotoId = pathToNewPhotoId[entry.photoPath]
                ?: dao.insertPhoto(
                    PhotoEntity(
                        filePath = entry.photoPath,
                        capturedAtMillis = entry.capturedAtMillis,
                        isFavorite = entry.isFavorite
                    )
                )
            dao.insertJournalEntry(entry.copy(photoId = mappedPhotoId))
            count++
        }
        return count.coerceAtLeast(restored.photos.size)
    }

    suspend fun deleteAllAppData() {
        dao.deleteAllJournalEntries()
        dao.deleteAllPhotos()
        dao.deleteAllVideoJournals()
        dao.deleteAllVideos()
        dao.deleteAllEditProjects()
        storage.clearAllStoredFiles()
        prefs.clearAll()
    }

    // Photo & Video Editor Non-Destructive Copy Saving & Projects
    fun observePhotoEditProjects(): Flow<List<com.example.data.local.EditProjectEntity>> =
        dao.observeEditProjectsByType("PHOTO")

    fun observeVideoEditProjects(): Flow<List<com.example.data.local.EditProjectEntity>> =
        dao.observeEditProjectsByType("VIDEO")

    suspend fun getEditProjectById(id: Long): com.example.data.local.EditProjectEntity? =
        dao.getEditProjectById(id)

    suspend fun saveEditProject(project: com.example.data.local.EditProjectEntity): Long =
        dao.insertEditProject(project.copy(updatedAtMillis = System.currentTimeMillis()))

    suspend fun deleteEditProject(id: Long) = dao.deleteEditProjectById(id)

    suspend fun saveEditedPhotoCopy(
        bitmap: android.graphics.Bitmap,
        asPng: Boolean,
        exportToDeviceGallery: Boolean
    ): Pair<Long, String> {
        val stored = storage.saveEditedPhotoBitmap(bitmap, asPng)
        val exported = if (exportToDeviceGallery) {
            storage.exportPhotoToDeviceGallery(stored.filePath)
        } else {
            false
        }
        val entity = PhotoEntity(
            filePath = stored.filePath,
            capturedAtMillis = System.currentTimeMillis(),
            width = stored.width,
            height = stored.height,
            savedToDeviceGallery = exported
        )
        val id = dao.insertPhoto(entity)
        return id to stored.filePath
    }

    suspend fun saveExportedVideoCopy(
        tempVideoFile: File,
        exportToDeviceGallery: Boolean
    ): Pair<Long, String> {
        val stored = storage.commitExportedVideoFile(tempVideoFile)
        val exported = if (exportToDeviceGallery) {
            storage.exportVideoToDeviceGallery(stored.filePath)
        } else {
            false
        }
        val entity = VideoEntity(
            filePath = stored.filePath,
            thumbnailPath = stored.thumbnailPath,
            capturedAtMillis = System.currentTimeMillis(),
            durationMillis = stored.durationMillis,
            savedToDeviceGallery = exported
        )
        val id = dao.insertVideo(entity)
        return id to stored.filePath
    }

    suspend fun importAudioUri(uri: Uri): Pair<String, Long> =
        storage.importAudioUriToPrivateStorage(uri)

    fun createCutoutFile(): File = storage.createCutoutFile()
    fun getAudioDir(): File = storage.getAudioDir()

    suspend fun toggleFavoriteTemplate(templateId: String) = prefs.toggleFavoriteTemplate(templateId)
    suspend fun toggleFavoriteSticker(stickerId: String) = prefs.toggleFavoriteSticker(stickerId)

    suspend fun setThemeMode(mode: ThemeMode) = prefs.setThemeMode(mode)
    suspend fun setThemePalette(palette: ThemePalette) = prefs.setThemePalette(palette)
    suspend fun setDefaultTimerSeconds(seconds: Int) = prefs.setDefaultTimerSeconds(seconds)

    suspend fun setPhotoPoseOutlineEnabled(enabled: Boolean) = prefs.setPhotoPoseOutlineEnabled(enabled)
    suspend fun setVideoPoseGuideEnabled(enabled: Boolean) = prefs.setVideoPoseGuideEnabled(enabled)
    suspend fun setOutlineScale(scale: Float) = prefs.setOutlineScale(scale)
    suspend fun setOutlineVerticalOffset(offset: Float) = prefs.setOutlineVerticalOffset(offset)
    suspend fun setOutlineAlpha(alpha: Float) = prefs.setOutlineAlpha(alpha)
    suspend fun resetOutlineStyle() = prefs.resetOutlineStyle()
    suspend fun setSavePhotoWithGuide(enabled: Boolean) = prefs.setSavePhotoWithGuide(enabled)
    suspend fun setSaveVideoWithGuide(enabled: Boolean) = prefs.setSaveVideoWithGuide(enabled)
}
