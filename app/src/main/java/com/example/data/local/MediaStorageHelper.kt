package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseOutlineType
import com.example.ui.components.compositeGuideOntoPhotoFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class StoredPhotoResult(
    val filePath: String,
    val width: Int,
    val height: Int
)

data class StoredVideoResult(
    val filePath: String,
    val thumbnailPath: String,
    val durationMillis: Long
)

data class RestoredBackupData(
    val photos: List<PhotoEntity>,
    val entries: List<JournalEntryEntity>
)

class MediaStorageHelper(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "photos").apply { if (!exists()) mkdirs() }

    private val videosDir: File
        get() = File(context.filesDir, "videos").apply { if (!exists()) mkdirs() }

    private val videoThumbsDir: File
        get() = File(context.filesDir, "video_thumbs").apply { if (!exists()) mkdirs() }

    private val tempCaptureDir: File
        get() = File(context.cacheDir, "captures").apply { if (!exists()) mkdirs() }

    private val tempVideosDir: File
        get() = File(context.cacheDir, "video_captures").apply { if (!exists()) mkdirs() }

    private val cutoutsDir: File
        get() = File(context.filesDir, "cutouts").apply { if (!exists()) mkdirs() }

    private val internalAudioDir: File
        get() = File(context.filesDir, "audio").apply { if (!exists()) mkdirs() }

    fun createTempCaptureFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(tempCaptureDir, "POSE_${stamp}_${UUID.randomUUID().toString().take(6)}.jpg")
    }

    fun createTempVideoFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(tempVideosDir, "VID_${stamp}_${UUID.randomUUID().toString().take(6)}.mp4")
    }

    fun createCutoutFile(): File {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(cutoutsDir, "CUTOUT_${stamp}_${UUID.randomUUID().toString().take(6)}.png")
    }

    fun getAudioDir(): File = internalAudioDir

    suspend fun maybeCompositeGuideOnTempPhoto(
        tempPath: String,
        outlineType: PoseOutlineType,
        config: PoseOutlineConfig
    ) = withContext(Dispatchers.IO) {
        if (!config.saveWithGuide || !config.enabled) return@withContext
        runCatching {
            compositeGuideOntoPhotoFile(
                sourcePath = tempPath,
                destPath = tempPath,
                outlineType = outlineType,
                scale = config.scale,
                verticalOffsetFraction = config.verticalOffsetFraction,
                alpha = config.alpha
            )
        }
    }

    suspend fun commitTempPhotoToPrivateStorage(tempFilePath: String): StoredPhotoResult =
        withContext(Dispatchers.IO) {
            val source = File(tempFilePath)
            require(source.exists()) { "Captured photo file is missing." }
            val dest = File(
                photosDir,
                "MEM_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            )
            source.copyTo(dest, overwrite = true)
            if (source.parentFile?.name == "captures") {
                source.delete()
            }
            val bounds = readImageBounds(dest)
            StoredPhotoResult(dest.absolutePath, bounds.first, bounds.second)
        }

    suspend fun commitTempVideoToPrivateStorage(tempFilePath: String): StoredVideoResult =
        withContext(Dispatchers.IO) {
            val source = File(tempFilePath)
            require(source.exists()) { "Recorded video file is missing." }
            val dest = File(
                videosDir,
                "MEMVID_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4"
            )
            source.copyTo(dest, overwrite = true)
            if (source.parentFile?.name == "video_captures") {
                source.delete()
            }
            val (thumbPath, durationMs) = extractVideoMetadata(dest)
            StoredVideoResult(dest.absolutePath, thumbPath, durationMs)
        }

    suspend fun importUriToPrivateStorage(uri: Uri): StoredPhotoResult =
        withContext(Dispatchers.IO) {
            val dest = File(
                photosDir,
                "IMP_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            } ?: error("Could not read selected photograph.")
            val bounds = readImageBounds(dest)
            StoredPhotoResult(dest.absolutePath, bounds.first, bounds.second)
        }

    suspend fun importVideoUriToPrivateStorage(uri: Uri): StoredVideoResult =
        withContext(Dispatchers.IO) {
            val dest = File(
                videosDir,
                "IMPVID_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            } ?: error("Could not read selected video.")
            val (thumbPath, durationMs) = extractVideoMetadata(dest)
            StoredVideoResult(dest.absolutePath, thumbPath, durationMs)
        }

    suspend fun saveEditedPhotoBitmap(bitmap: Bitmap, asPng: Boolean): StoredPhotoResult =
        withContext(Dispatchers.IO) {
            val ext = if (asPng) "png" else "jpg"
            val dest = File(
                photosDir,
                "EDIT_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.$ext"
            )
            FileOutputStream(dest).use { out ->
                val format = if (asPng) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                bitmap.compress(format, 95, out)
            }
            StoredPhotoResult(dest.absolutePath, bitmap.width, bitmap.height)
        }

    suspend fun commitExportedVideoFile(tempFile: File): StoredVideoResult =
        withContext(Dispatchers.IO) {
            require(tempFile.exists()) { "Exported video file missing." }
            val dest = File(
                videosDir,
                "EDITVID_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp4"
            )
            tempFile.copyTo(dest, overwrite = true)
            if (tempFile.parentFile?.name == "video_captures" || tempFile.parentFile?.name == "cache") {
                tempFile.delete()
            }
            val (thumbPath, durationMs) = extractVideoMetadata(dest)
            StoredVideoResult(dest.absolutePath, thumbPath, durationMs)
        }

    suspend fun importAudioUriToPrivateStorage(uri: Uri): Pair<String, Long> =
        withContext(Dispatchers.IO) {
            val dest = File(
                internalAudioDir,
                "AUD_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.m4a"
            )
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(dest).use { output -> input.copyTo(output) }
            } ?: error("Could not read selected audio file.")
            val durationMs = readAudioDurationMs(dest)
            dest.absolutePath to durationMs
        }

    private fun readAudioDurationMs(file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 5000L
        } catch (_: Exception) {
            5000L
        } finally {
            runCatching { retriever.release() }
        }
    }

    suspend fun exportPhotoToDeviceGallery(filePath: String): Boolean =
        withContext(Dispatchers.IO) {
            val source = File(filePath)
            if (!source.exists()) return@withContext false

            val displayName = "PoseMemories_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            }.jpg"

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_PICTURES}/PoseAndMemories"
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(source).use { input ->
                        input.copyTo(output)
                    }
                } ?: return@withContext false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val doneValues = ContentValues().apply {
                        put(MediaStore.Images.Media.IS_PENDING, 0)
                    }
                    resolver.update(uri, doneValues, null, null)
                }
                true
            } catch (_: Exception) {
                resolver.delete(uri, null, null)
                false
            }
        }

    suspend fun exportVideoToDeviceGallery(filePath: String): Boolean =
        withContext(Dispatchers.IO) {
            val source = File(filePath)
            if (!source.exists()) return@withContext false

            val displayName = "PoseMemories_${
                SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            }.mp4"

            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Video.Media.RELATIVE_PATH,
                        "${Environment.DIRECTORY_MOVIES}/PoseAndMemories"
                    )
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: return@withContext false

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    FileInputStream(source).use { input ->
                        input.copyTo(output)
                    }
                } ?: return@withContext false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val doneValues = ContentValues().apply {
                        put(MediaStore.Video.Media.IS_PENDING, 0)
                    }
                    resolver.update(uri, doneValues, null, null)
                }
                true
            } catch (_: Exception) {
                resolver.delete(uri, null, null)
                false
            }
        }

    fun getShareableUri(filePath: String): Uri? {
        val file = File(filePath)
        if (!file.exists()) return null
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun deletePhotoFile(filePath: String) {
        runCatching {
            val file = File(filePath)
            if (file.exists() && file.absolutePath.startsWith(context.filesDir.absolutePath)) {
                file.delete()
            }
        }
    }

    fun deleteVideoFiles(videoPath: String, thumbnailPath: String) {
        deletePhotoFile(videoPath)
        if (thumbnailPath.isNotBlank()) {
            deletePhotoFile(thumbnailPath)
        }
    }

    fun clearAllStoredFiles() {
        photosDir.listFiles()?.forEach { it.delete() }
        tempCaptureDir.listFiles()?.forEach { it.delete() }
        videosDir.listFiles()?.forEach { it.delete() }
        videoThumbsDir.listFiles()?.forEach { it.delete() }
        tempVideosDir.listFiles()?.forEach { it.delete() }
        cutoutsDir.listFiles()?.forEach { it.delete() }
        internalAudioDir.listFiles()?.forEach { it.delete() }
    }

    suspend fun exportBackupZip(
        outputStream: OutputStream,
        photos: List<PhotoEntity>,
        entries: List<JournalEntryEntity>
    ) = withContext(Dispatchers.IO) {
        ZipOutputStream(outputStream).use { zip ->
            val root = JSONObject()
            root.put("version", 1)
            root.put("exportedAtMillis", System.currentTimeMillis())

            val photosJson = JSONArray()
            val addedFiles = mutableSetOf<String>()
            photos.forEach { p ->
                val fileName = File(p.filePath).name
                photosJson.put(
                    JSONObject().apply {
                        put("id", p.id)
                        put("fileName", fileName)
                        put("capturedAtMillis", p.capturedAtMillis)
                        put("isFavorite", p.isFavorite)
                        put("width", p.width)
                        put("height", p.height)
                        put("savedToDeviceGallery", p.savedToDeviceGallery)
                        put("isArchived", p.isArchived)
                        if (p.archivedAtMillis != null) put("archivedAtMillis", p.archivedAtMillis)
                    }
                )
                val file = File(p.filePath)
                if (file.exists() && addedFiles.add(fileName)) {
                    zip.putNextEntry(ZipEntry("photos/$fileName"))
                    FileInputStream(file).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            root.put("photos", photosJson)

            val entriesJson = JSONArray()
            entries.forEach { e ->
                entriesJson.put(
                    JSONObject().apply {
                        put("id", e.id)
                        put("photoId", e.photoId)
                        put("photoFileName", File(e.photoPath).name)
                        put("place", e.place)
                        put("capturedAtMillis", e.capturedAtMillis)
                        put("outfit", e.outfit)
                        put("goodThingOne", e.goodThingOne)
                        put("goodThingTwo", e.goodThingTwo)
                        put("caption", e.caption)
                        put("templateId", e.templateId)
                        put("stickersCsv", e.stickersCsv)
                        put("isFavorite", e.isFavorite)
                        put("createdAtMillis", e.createdAtMillis)
                        put("updatedAtMillis", e.updatedAtMillis)
                        put("isArchived", e.isArchived)
                        if (e.archivedAtMillis != null) put("archivedAtMillis", e.archivedAtMillis)
                    }
                )
            }
            root.put("entries", entriesJson)

            zip.putNextEntry(ZipEntry("journal_backup.json"))
            zip.write(root.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }
    }

    suspend fun exportReadableJournalText(
        outputStream: OutputStream,
        entries: List<JournalEntryEntity>
    ) = withContext(Dispatchers.IO) {
        val formatter = SimpleDateFormat("EEEE, MMM d, yyyy • h:mm a", Locale.getDefault())
        val sb = StringBuilder()
        sb.appendLine("POSE & MEMORIES — PERSONAL PHOTO JOURNAL")
        sb.appendLine("Every pose tells a story. Every picture keeps a memory.")
        sb.appendLine("====================================================")
        sb.appendLine()
        entries.forEachIndexed { idx, entry ->
            sb.appendLine("${idx + 1}. ${entry.place.ifBlank { "Special Moment" }}")
            sb.appendLine("Date & Time: ${formatter.format(Date(entry.capturedAtMillis))}")
            if (entry.outfit.isNotBlank()) sb.appendLine("Outfit: ${entry.outfit}")
            if (entry.goodThingOne.isNotBlank() || entry.goodThingTwo.isNotBlank()) {
                sb.appendLine("Two Good Things:")
                if (entry.goodThingOne.isNotBlank()) sb.appendLine("  • ${entry.goodThingOne}")
                if (entry.goodThingTwo.isNotBlank()) sb.appendLine("  • ${entry.goodThingTwo}")
            }
            sb.appendLine("Caption:")
            sb.appendLine(entry.caption)
            sb.appendLine("----------------------------------------------------")
            sb.appendLine()
        }
        outputStream.use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
    }

    suspend fun importBackupZip(inputStream: InputStream): RestoredBackupData =
        withContext(Dispatchers.IO) {
            var manifestString: String? = null
            val restoredFiles = mutableMapOf<String, File>()

            ZipInputStream(inputStream).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (!entry.isDirectory) {
                        if (entry.name == "journal_backup.json") {
                            manifestString = zip.readBytes().toString(Charsets.UTF_8)
                        } else if (entry.name.startsWith("photos/")) {
                            val safeName = File(entry.name).name
                            if (safeName.isNotBlank()) {
                                val dest = File(photosDir, "RST_${System.currentTimeMillis()}_$safeName")
                                FileOutputStream(dest).use { out ->
                                    zip.copyTo(out)
                                }
                                restoredFiles[safeName] = dest
                            }
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }

            val jsonText = manifestString ?: error("Invalid backup file: missing journal_backup.json.")
            val root = JSONObject(jsonText)
            val photosArr = root.optJSONArray("photos") ?: JSONArray()
            val entriesArr = root.optJSONArray("entries") ?: JSONArray()

            val photos = mutableListOf<PhotoEntity>()
            val oldIdToFile = mutableMapOf<Long, File>()
            for (i in 0 until photosArr.length()) {
                val obj = photosArr.getJSONObject(i)
                val fileName = obj.optString("fileName")
                val restoredFile = restoredFiles[fileName]
                if (restoredFile != null && restoredFile.exists()) {
                    val oldId = obj.optLong("id")
                    oldIdToFile[oldId] = restoredFile
                    photos.add(
                        PhotoEntity(
                            id = 0,
                            filePath = restoredFile.absolutePath,
                            capturedAtMillis = obj.optLong("capturedAtMillis", System.currentTimeMillis()),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            width = obj.optInt("width", 0),
                            height = obj.optInt("height", 0),
                            savedToDeviceGallery = obj.optBoolean("savedToDeviceGallery", false),
                            isArchived = obj.optBoolean("isArchived", false),
                            archivedAtMillis = if (obj.has("archivedAtMillis")) obj.optLong("archivedAtMillis") else null
                        )
                    )
                }
            }

            val entries = mutableListOf<JournalEntryEntity>()
            for (i in 0 until entriesArr.length()) {
                val obj = entriesArr.getJSONObject(i)
                val photoFileName = obj.optString("photoFileName")
                val restoredFile = restoredFiles[photoFileName] ?: oldIdToFile[obj.optLong("photoId")]
                if (restoredFile != null && restoredFile.exists()) {
                    entries.add(
                        JournalEntryEntity(
                            id = 0,
                            photoId = obj.optLong("photoId"),
                            photoPath = restoredFile.absolutePath,
                            place = obj.optString("place"),
                            capturedAtMillis = obj.optLong("capturedAtMillis", System.currentTimeMillis()),
                            outfit = obj.optString("outfit"),
                            goodThingOne = obj.optString("goodThingOne"),
                            goodThingTwo = obj.optString("goodThingTwo"),
                            caption = obj.optString("caption"),
                            templateId = obj.optString("templateId", "pink_aesthetic"),
                            stickersCsv = obj.optString("stickersCsv", ""),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            createdAtMillis = obj.optLong("createdAtMillis", System.currentTimeMillis()),
                            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis()),
                            isArchived = obj.optBoolean("isArchived", false),
                            archivedAtMillis = if (obj.has("archivedAtMillis")) obj.optLong("archivedAtMillis") else null
                        )
                    )
                }
            }

            RestoredBackupData(photos = photos, entries = entries)
        }

    private fun extractVideoMetadata(videoFile: File): Pair<String, Long> {
        var thumbPath = ""
        var durationMs = 0L
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(videoFile.absolutePath)
            durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val frame = retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0L)
            if (frame != null) {
                val thumbFile = File(
                    videoThumbsDir,
                    "VTH_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
                )
                FileOutputStream(thumbFile).use { out ->
                    frame.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                frame.recycle()
                thumbPath = thumbFile.absolutePath
            }
        } catch (_: Exception) {
            // Gracefully handle unreadable frames on headless test environments
        } finally {
            runCatching { retriever.release() }
        }
        return thumbPath to durationMs
    }

    private fun readImageBounds(file: File): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        return options.outWidth.coerceAtLeast(0) to options.outHeight.coerceAtLeast(0)
    }
}
