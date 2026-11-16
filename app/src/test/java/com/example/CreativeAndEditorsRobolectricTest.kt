package com.example

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.EditProjectEntity
import com.example.data.local.MediaStorageHelper
import com.example.data.local.SettingsPreferences
import com.example.data.model.AudioTrackKind
import com.example.data.model.CaptionAnimationStyle
import com.example.data.model.CreativeCategory
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.CutoutBackdrop
import com.example.data.model.EditorFontOption
import com.example.data.model.NormalizedPoint
import com.example.data.model.PhotoAdjustments
import com.example.data.model.PhotoCropAspect
import com.example.data.model.PhotoEditLayer
import com.example.data.model.PhotoEditState
import com.example.data.model.PhotoFilterPreset
import com.example.data.model.PipOverlayItem
import com.example.data.model.ResizePreset
import com.example.data.model.TemplateFrameStyle
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoAudioLayerItem
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoColorFilter
import com.example.data.model.VideoEditState
import com.example.data.model.VideoStickerOverlayItem
import com.example.data.model.VideoTextOverlayItem
import com.example.data.model.VideoTransitionType
import com.example.data.repository.MemoryRepository
import com.example.ui.editor.OfflineAudioLibrary
import com.example.ui.editor.PhotoEditEngine
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CreativeAndEditorsRobolectricTest {

    private lateinit var app: Application
    private lateinit var db: AppDatabase
    private lateinit var storage: MediaStorageHelper
    private lateinit var prefs: SettingsPreferences
    private lateinit var repo: MemoryRepository

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        storage = MediaStorageHelper(app)
        prefs = SettingsPreferences(app)
        repo = MemoryRepository(db.memoryDao(), storage, prefs)
    }

    @After
    fun tearDown() {
        db.close()
        storage.clearAllStoredFiles()
    }

    @Test
    fun `creative library contains 240 templates and 120 stickers across all 10 categories`() {
        val templates = CreativeLibraryCatalog.templates
        val stickers = CreativeLibraryCatalog.stickers

        assertEquals(240, templates.size)
        assertEquals(120, stickers.size)
        assertEquals(10, CreativeCategory.entries.size)
        assertEquals(10, EditorFontOption.entries.size)

        // Every category is well-populated and IDs are globally unique
        assertEquals(240, templates.map { it.id }.toSet().size)
        assertEquals(120, stickers.map { it.id }.toSet().size)

        CreativeCategory.entries.forEach { cat ->
            val catTemplates = templates.filter { it.category == cat }
            val catStickers = stickers.filter { it.category == cat }
            assertEquals("Expected 24 templates in ${cat.label}", 24, catTemplates.size)
            assertEquals("Expected 12 stickers in ${cat.label}", 12, catStickers.size)
        }
    }

    @Test
    fun `template and sticker favorites persist in SettingsPreferences`() = runBlocking {
        val sampleTemplateId = CreativeLibraryCatalog.templates.first().id
        val sampleStickerId = CreativeLibraryCatalog.stickers.first().id

        prefs.toggleFavoriteTemplate(sampleTemplateId)
        prefs.toggleFavoriteSticker(sampleStickerId)

        val settingsAfterAdd = prefs.settingsFlow.first()
        assertTrue(sampleTemplateId in settingsAfterAdd.favoriteTemplateIds)
        assertTrue(sampleStickerId in settingsAfterAdd.favoriteStickerIds)

        prefs.toggleFavoriteTemplate(sampleTemplateId)
        val settingsAfterRemove = prefs.settingsFlow.first()
        assertFalse(sampleTemplateId in settingsAfterRemove.favoriteTemplateIds)
        assertTrue(sampleStickerId in settingsAfterRemove.favoriteStickerIds)
    }

    @Test
    fun `photo edit state serializes roundtrip cleanly and supports non-destructive save copy`() = runBlocking {
        // Create original JPEG file with distinct source pixels
        val origBmp = Bitmap.createBitmap(120, 160, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.rgb(245, 200, 215))
        }
        val tempSrc = storage.createTempCaptureFile()
        FileOutputStream(tempSrc).use { out ->
            origBmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        origBmp.recycle()

        val origPhotoId = repo.saveStandalonePhoto(
            tempOrPrivatePath = tempSrc.absolutePath,
            capturedAtMillis = 123456L,
            exportToDeviceGallery = false
        )
        val origEntity = repo.getPhotoById(origPhotoId)!!
        val originalBytes = File(origEntity.filePath).readBytes()

        val templateId = CreativeLibraryCatalog.templates.first().id
        val stickerId = CreativeLibraryCatalog.stickers.first().id

        val state = PhotoEditState(
            sourcePhotoId = origPhotoId,
            sourcePath = origEntity.filePath,
            adjustments = PhotoAdjustments(
                brightness = 0.12f,
                contrast = 1.15f,
                saturation = 1.2f,
                exposure = 0.08f,
                sharpness = 0.35f,
                blurRadius = 1.5f,
                filterPreset = PhotoFilterPreset.SOFT_ROSE,
                rotationQuarterTurns = 1,
                flipHorizontal = true,
                cropAspect = PhotoCropAspect.PORTRAIT_4_5,
                resizePreset = ResizePreset.SOCIAL_1080,
                frameStyle = TemplateFrameStyle.POLAROID,
                appliedTemplateId = templateId,
                cutoutBackdrop = CutoutBackdrop.BLUSH_PINK
            ),
            layers = listOf(
                PhotoEditLayer.StickerLayer(
                    name = "Floral Sticker",
                    stickerId = stickerId,
                    xFraction = 0.35f,
                    yFraction = 0.42f,
                    scale = 1.3f,
                    rotationDeg = 12f
                ),
                PhotoEditLayer.TextLayer(
                    name = "Caption",
                    text = "Coffee Date",
                    fontId = EditorFontOption.SACRAMENTO.id
                ),
                PhotoEditLayer.DoodleStrokeLayer(
                    name = "Eraser",
                    points = listOf(NormalizedPoint(0.2f, 0.2f), NormalizedPoint(0.8f, 0.8f)),
                    isEraser = true
                )
            )
        )

        val decoded = PhotoEditState.fromJsonString(state.toJsonString())
        assertNotNull(decoded)
        assertEquals(PhotoFilterPreset.SOFT_ROSE, decoded!!.adjustments.filterPreset)
        assertEquals(TemplateFrameStyle.POLAROID, decoded.adjustments.frameStyle)
        assertEquals(templateId, decoded.adjustments.appliedTemplateId)
        assertEquals(3, decoded.layers.size)
        assertTrue(decoded.layers[2] is PhotoEditLayer.DoodleStrokeLayer)
        assertTrue((decoded.layers[2] as PhotoEditLayer.DoodleStrokeLayer).isEraser)

        // Render and save non-destructive copy
        val rendered = PhotoEditEngine.renderFinalPhotoBitmap(app, state, previewModeMaxDim = 600)
        val (copyId, copyPath) = repo.saveEditedPhotoCopy(rendered, asPng = false, exportToDeviceGallery = false)
        rendered.recycle()

        assertNotEquals(origPhotoId, copyId)
        assertNotEquals(origEntity.filePath, copyPath)
        assertTrue(File(copyPath).exists())
        // Original file must remain byte-for-byte untouched
        assertArrayEquals(originalBytes, File(origEntity.filePath).readBytes())
    }

    @Test
    fun `photo and video editor undo redo stacks work deterministically`() {
        val vm = AppViewModel(app)

        vm.openPhotoEditor("/tmp/sample_photo.jpg", sourcePhotoId = 1L)
        assertFalse(vm.canUndoPhoto.value)
        assertFalse(vm.canRedoPhoto.value)

        vm.updatePhotoEdit(recordUndo = true) {
            it.copy(adjustments = it.adjustments.copy(filterPreset = PhotoFilterPreset.DREAMY_LAVENDER))
        }
        assertTrue(vm.canUndoPhoto.value)
        assertEquals(PhotoFilterPreset.DREAMY_LAVENDER, vm.photoEditState.value!!.adjustments.filterPreset)

        vm.undoPhotoEdit()
        assertFalse(vm.canUndoPhoto.value)
        assertTrue(vm.canRedoPhoto.value)
        assertEquals(PhotoFilterPreset.ORIGINAL, vm.photoEditState.value!!.adjustments.filterPreset)

        vm.redoPhotoEdit()
        assertTrue(vm.canUndoPhoto.value)
        assertEquals(PhotoFilterPreset.DREAMY_LAVENDER, vm.photoEditState.value!!.adjustments.filterPreset)

        // Video Editor timeline split, speed, reverse, undo & redo
        vm.openVideoInProEditor("/tmp/sample_clip.mp4", durationMillis = 10_000L, title = "Weekend Vlog")
        assertEquals(1, vm.videoEditState.value.clips.size)
        assertEquals(10_000L, vm.videoEditState.value.totalDurationMs)

        val splitOk = vm.splitClipAtTimelinePosition(4_000L)
        assertTrue(splitOk)
        assertEquals(2, vm.videoEditState.value.clips.size)
        assertEquals(4_000L, vm.videoEditState.value.clips[0].effectiveDurationMs)
        assertEquals(6_000L, vm.videoEditState.value.clips[1].effectiveDurationMs)

        vm.updateVideoEdit(recordUndo = true) { st ->
            st.copy(
                clips = st.clips.mapIndexed { idx, c ->
                    if (idx == 0) c.copy(speed = 2f, isReversed = true) else c
                }
            )
        }
        assertEquals(2_000L, vm.videoEditState.value.clips[0].effectiveDurationMs)
        assertEquals(8_000L, vm.videoEditState.value.totalDurationMs)
        assertTrue(vm.videoEditState.value.clips[0].isReversed)

        vm.undoVideoEdit()
        assertEquals(10_000L, vm.videoEditState.value.totalDurationMs)
        assertFalse(vm.videoEditState.value.clips[0].isReversed)

        vm.redoVideoEdit()
        assertEquals(8_000L, vm.videoEditState.value.totalDurationMs)
    }

    @Test
    fun `video edit state roundtrip, offline wav audio synthesis, and Room EditProjectEntity persistence work`() = runBlocking {
        val state = VideoEditState(
            title = "Cherry Blossom Reel",
            aspectRatio = VideoAspectRatio.PORTRAIT_9_16,
            clips = listOf(
                VideoClipItem(
                    sourcePath = "/tmp/c1.mp4",
                    sourceDurationMs = 6000L,
                    trimStartMs = 500L,
                    trimEndMs = 4500L,
                    speed = 0.5f,
                    isReversed = true,
                    filter = VideoColorFilter.PASTEL_FILM,
                    transitionAfter = VideoTransitionType.HEART_WIPE
                )
            ),
            pipOverlays = listOf(
                PipOverlayItem(
                    sourcePath = "/tmp/pip.png",
                    isVideo = false,
                    startMs = 0L,
                    endMs = 4000L,
                    startX = 0.2f,
                    endX = 0.8f,
                    chromaKeyEnabled = true
                )
            ),
            textOverlays = listOf(
                VideoTextOverlayItem(
                    text = "Spring Walk",
                    isSubtitle = true,
                    animationStyle = CaptionAnimationStyle.TYPEWRITER
                )
            ),
            stickerOverlays = listOf(
                VideoStickerOverlayItem(stickerId = CreativeLibraryCatalog.stickers.first().id)
            ),
            audioTracks = listOf(
                VideoAudioLayerItem(
                    title = "Pastel Morning Lofi",
                    kind = AudioTrackKind.MUSIC,
                    filePath = "/tmp/bgm.wav",
                    volume = 0.65f,
                    fadeInMs = 400L,
                    fadeOutMs = 600L
                )
            )
        )

        val restored = VideoEditState.fromJsonString(state.toJsonString(), projectId = 42L)
        assertNotNull(restored)
        assertEquals("Cherry Blossom Reel", restored!!.title)
        assertEquals(1, restored.clips.size)
        assertTrue(restored.clips.first().isReversed)
        assertEquals(VideoTransitionType.HEART_WIPE, restored.clips.first().transitionAfter)
        assertEquals(8000L, restored.clips.first().effectiveDurationMs)
        assertTrue(restored.pipOverlays.first().chromaKeyEnabled)
        assertEquals(CaptionAnimationStyle.TYPEWRITER, restored.textOverlays.first().animationStyle)

        // Verify offline WAV synthesis creates valid RIFF/WAVE headers
        val audioDir = storage.getAudioDir()
        val (wavFile, durMs) = OfflineAudioLibrary.ensureAudioAssetFile(audioDir, "bgm_lofi")
        assertTrue(wavFile.exists() && wavFile.length() > 44L)
        assertEquals(12_000L, durMs)
        val header = wavFile.inputStream().use { inp -> ByteArray(12).also { inp.read(it) } }
        assertEquals("RIFF", String(header, 0, 4))
        assertEquals("WAVE", String(header, 8, 4))

        // Verify Room EditProjectEntity DAO persistence
        val projId = repo.saveEditProject(
            EditProjectEntity(
                projectType = "VIDEO",
                title = state.title,
                sourcePath = "/tmp/c1.mp4",
                previewPath = "/tmp/thumb.jpg",
                stateJson = state.toJsonString()
            )
        )
        val storedProjects = repo.observeVideoEditProjects().first()
        assertEquals(1, storedProjects.size)
        assertEquals(projId, storedProjects.first().id)
        assertEquals("Cherry Blossom Reel", storedProjects.first().title)

        repo.deleteEditProject(projId)
        assertTrue(repo.observeVideoEditProjects().first().isEmpty())
    }
}
