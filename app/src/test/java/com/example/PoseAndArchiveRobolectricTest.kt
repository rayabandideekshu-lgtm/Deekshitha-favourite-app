package com.example

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.SampleAiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.MediaStorageHelper
import com.example.data.local.SettingsPreferences
import com.example.data.model.AlignmentGuidance
import com.example.data.model.ArchiveItemType
import com.example.data.model.GallerySortOrder
import com.example.data.model.PoseCategory
import com.example.data.model.PoseDifficulty
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseOutlineType
import com.example.data.model.PoseSuggestion
import com.example.data.repository.MemoryRepository
import com.example.ui.screens.VideoCameraHudOverlay
import com.example.ui.theme.PoseMemoriesTheme
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PoseAndArchiveRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

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
    fun `video pose guide toggles instantly during active recording without stopping recording`() {
        var outlineConfig by mutableStateOf(PoseOutlineConfig(enabled = true))
        var isRecording by mutableStateOf(true)

        val pose = PoseSuggestion(
            id = "test_walk",
            title = "Walking Pose",
            instruction = "Walk smoothly forward.",
            framingTip = "Keep shoulders relaxed.",
            category = PoseCategory.SOLO_PORTRAITS,
            difficulty = PoseDifficulty.EASY,
            outlineType = PoseOutlineType.WALKING_POSE
        )

        composeRule.setContent {
            PoseMemoriesTheme {
                VideoCameraHudOverlay(
                    activePose = pose,
                    outlineConfig = outlineConfig,
                    alignmentGuidance = AlignmentGuidance(),
                    isRecording = isRecording,
                    recordingSeconds = 12L,
                    hasFlashUnit = true,
                    torchEnabled = false,
                    cameraError = null,
                    onCloseCamera = {},
                    onToggleTorch = {},
                    onFlipCamera = {},
                    onOpenPoseSettings = {},
                    onOpenChangePose = {},
                    onToggleVideoPoseGuide = {
                        outlineConfig = outlineConfig.copy(enabled = !outlineConfig.enabled)
                    },
                    onPickVideo = {},
                    onToggleRecord = { isRecording = !isRecording }
                )
            }
        }

        // Recording timer and white-line pose overlay are visible initially
        composeRule.onNodeWithTag("video_recording_timer_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("video_white_line_pose_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("video_pose_guide_toggle").assertTextContains("VIDEO POSE GUIDE: ON")

        // Tap VIDEO POSE GUIDE toggle while recording
        composeRule.onNodeWithTag("video_pose_guide_toggle").performClick()
        composeRule.waitForIdle()

        // Recording must NOT stop or restart; guide flips to OFF and overlay hides
        assertTrue(isRecording)
        assertFalse(outlineConfig.enabled)
        composeRule.onNodeWithTag("video_recording_timer_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("video_pose_guide_toggle").assertTextContains("VIDEO POSE GUIDE: OFF")
        composeRule.onNodeWithTag("video_white_line_pose_overlay").assertDoesNotExist()

        // Toggle back ON mid-recording
        composeRule.onNodeWithTag("video_pose_guide_toggle").performClick()
        composeRule.waitForIdle()

        assertTrue(isRecording)
        assertTrue(outlineConfig.enabled)
        composeRule.onNodeWithTag("video_white_line_pose_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("video_pose_guide_toggle").assertTextContains("VIDEO POSE GUIDE: ON")
    }

    @Test
    fun `sample ai provides all 10 distinct pose outlines and viewmodel tracks active selection`() = runBlocking {
        val aiRepo = SampleAiRepository(app)
        val collectedOutlines = mutableSetOf<PoseOutlineType>()

        for (category in PoseCategory.entries) {
            val batch0 = aiRepo.suggestPoses(category, batchIndex = 0)
            val batch1 = aiRepo.suggestPoses(category, batchIndex = 1)
            (batch0.suggestions + batch1.suggestions).forEach {
                collectedOutlines.add(it.outlineType)
            }
        }

        assertEquals(PoseOutlineType.entries.toSet(), collectedOutlines)

        val vm = AppViewModel(app)
        val customPose = PoseSuggestion(
            id = "custom_cafe",
            title = "Relaxed Café Sitting",
            instruction = "Sit comfortably.",
            framingTip = "Keep shoulders relaxed.",
            category = PoseCategory.WOMEN,
            difficulty = PoseDifficulty.EASY,
            outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
        )
        vm.setPhotoPoseOutlineEnabled(false)
        assertFalse(vm.photoOutlineConfig.value.enabled)

        vm.selectPoseSuggestion(customPose)
        assertEquals(PoseOutlineType.RELAXED_CAFE_SITTING, vm.activePoseSuggestion.value.outlineType)
        assertTrue(vm.photoOutlineConfig.value.enabled)
    }

    @Test
    fun `archive restore and permanent delete work persistently across all memory types`() = runBlocking {
        val photosDir = File(app.filesDir, "photos").apply { mkdirs() }
        val videosDir = File(app.filesDir, "videos").apply { mkdirs() }

        val tempPhoto1 = storage.createTempCaptureFile().apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val tempPhoto2 = storage.createTempCaptureFile().apply { writeBytes(byteArrayOf(4, 5, 6)) }
        val tempVideo1 = storage.createTempVideoFile().apply { writeBytes(byteArrayOf(7, 8, 9)) }
        val tempVideo2 = storage.createTempVideoFile().apply { writeBytes(byteArrayOf(10, 11, 12)) }

        // 1) Standalone Photo & Photo Journal
        val standalonePhotoId = repo.saveStandalonePhoto(tempPhoto1.absolutePath, 1000L, exportToDeviceGallery = false)
        val photoJournalId = repo.saveNewJournalMemory(
            tempOrPrivatePath = tempPhoto2.absolutePath,
            existingPhotoId = null,
            place = "Rose Cafe",
            capturedAtMillis = 2000L,
            outfit = "Pastel cardigan",
            goodThingOne = "Warm tea",
            goodThingTwo = "Sunny table",
            caption = "Rose Cafe afternoon",
            templateId = "pink_aesthetic",
            stickersCsv = "flower",
            alsoSaveToDeviceGallery = false
        )

        // 2) Standalone Video & Video Journal
        val standaloneVideoId = repo.saveStandaloneVideo(tempVideo1.absolutePath, null, 4500L, 3000L, exportToDeviceGallery = false)
        val videoJournalId = repo.saveNewVideoJournalMemory(
            tempOrPrivatePath = tempVideo2.absolutePath,
            existingVideoId = null,
            existingThumbnailPath = null,
            durationMillis = 6000L,
            place = "Botanical Walk",
            capturedAtMillis = 4000L,
            outfit = "Cream coat",
            goodThingOne = "Fresh breeze",
            goodThingTwo = "Blooming tulips",
            caption = "Botanical stroll clip",
            templateId = "flower_garden",
            stickersCsv = "tulip",
            alsoSaveToDeviceGallery = false
        )

        // Verify all 4 active items exist
        assertEquals(2, repo.observePhotos(GallerySortOrder.NEWEST).first().size)
        assertEquals(2, repo.observeVideos(GallerySortOrder.NEWEST).first().size)
        assertEquals(1, repo.allJournalEntries.first().size)
        assertEquals(1, repo.allVideoJournals.first().size)
        assertTrue(repo.observeArchivedItems().first().isEmpty())

        // Archive all 4
        val photo1 = repo.getPhotoById(standalonePhotoId)!!
        val journal1 = repo.getJournalEntryById(photoJournalId)!!
        val video1 = repo.getVideoById(standaloneVideoId)!!
        val vJournal1 = repo.getVideoJournalById(videoJournalId)!!

        repo.archivePhotoAndRelatedJournal(photo1)
        repo.archiveJournalEntry(journal1)
        repo.archiveVideoAndRelatedJournal(video1)
        repo.archiveVideoJournalEntry(vJournal1)

        // Active lists must hide archived items
        assertEquals(1, repo.observePhotos(GallerySortOrder.NEWEST).first().size)
        assertEquals(1, repo.observeVideos(GallerySortOrder.NEWEST).first().size)
        assertEquals(0, repo.allJournalEntries.first().size)
        assertEquals(0, repo.allVideoJournals.first().size)

        // Archive screen receives all 4 items with original metadata
        val archived = repo.observeArchivedItems().first()
        assertEquals(4, archived.size)
        val archivedPhotoJournal = archived.first { it.type == ArchiveItemType.PHOTO_JOURNAL }
        assertEquals("Rose Cafe", archivedPhotoJournal.place)
        assertEquals("Rose Cafe afternoon", archivedPhotoJournal.caption)
        assertEquals(2000L, archivedPhotoJournal.originalTimestampMillis)

        // Restore the Photo Journal individually -> returns to active list with metadata
        repo.restoreArchivedItem(archivedPhotoJournal)
        assertEquals(1, repo.allJournalEntries.first().size)
        assertEquals("Rose Cafe", repo.allJournalEntries.first().first().place)
        assertEquals(3, repo.observeArchivedItems().first().size)

        // Permanently delete the standalone archived photo -> deletes row AND physical file
        val archivedPhoto = repo.observeArchivedItems().first().first { it.type == ArchiveItemType.PHOTO }
        val photoFile = File(archivedPhoto.mediaPath)
        assertTrue(photoFile.exists())
        repo.permanentlyDeleteArchivedItem(archivedPhoto)

        assertFalse(photoFile.exists())
        assertEquals(2, repo.observeArchivedItems().first().size)

        // Restore All remaining archived items (Video + Video Journal)
        repo.restoreAllArchived()
        assertTrue(repo.observeArchivedItems().first().isEmpty())
        assertEquals(2, repo.observeVideos(GallerySortOrder.NEWEST).first().size)
        assertEquals(1, repo.allVideoJournals.first().size)
        assertEquals("Botanical Walk", repo.allVideoJournals.first().first().place)
    }
}
