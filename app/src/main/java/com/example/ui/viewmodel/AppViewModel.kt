package com.example.ui.viewmodel

import android.app.Application
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.transformer.Transformer
import com.example.data.ai.AiRepository
import com.example.data.ai.SampleAiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.EditProjectEntity
import com.example.data.local.JournalEntryEntity
import com.example.data.local.MediaStorageHelper
import com.example.data.local.PhotoEntity
import com.example.data.local.SettingsPreferences
import com.example.data.local.UserSettings
import com.example.data.local.VideoEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.data.model.AlignmentGuidance
import com.example.data.model.ArchivedMemoryItem
import com.example.data.model.AudioTrackKind
import com.example.data.model.CaptionResult
import com.example.data.model.CutoutBackdrop
import com.example.data.model.GallerySortOrder
import com.example.data.model.PhotoEditState
import com.example.data.model.PipOverlayItem
import com.example.data.model.PoseCategory
import com.example.data.model.PoseDifficulty
import com.example.data.model.PoseInspirationBatch
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseOutlineType
import com.example.data.model.PoseSuggestion
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePalette
import com.example.data.model.VideoAudioLayerItem
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoEditState
import com.example.data.model.VideoExportResolution
import com.example.data.model.VideoExportState
import com.example.data.model.VideoStickerOverlayItem
import com.example.data.model.VideoTextOverlayItem
import com.example.data.repository.MemoryRepository
import com.example.ui.editor.ExportResolutionSupport
import com.example.ui.editor.OfflineAudioLibrary
import com.example.ui.editor.PhotoEditEngine
import com.example.ui.editor.VideoExportEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

sealed interface PoseUiState {
    data object Loading : PoseUiState
    data class Success(val batch: PoseInspirationBatch) : PoseUiState
    data class Error(val message: String) : PoseUiState
}

sealed interface CaptionUiState {
    data object Idle : CaptionUiState
    data object Loading : CaptionUiState
    data class Success(val result: CaptionResult) : CaptionUiState
    data class Error(val message: String) : CaptionUiState
}

data class PendingCapture(
    val filePath: String,
    val capturedAtMillis: Long,
    val existingPhotoId: Long? = null
)

data class PendingVideoCapture(
    val filePath: String,
    val thumbnailPath: String? = null,
    val durationMillis: Long = 0L,
    val capturedAtMillis: Long = System.currentTimeMillis(),
    val existingVideoId: Long? = null
)

private val DEFAULT_PHOTO_POSE = PoseSuggestion(
    id = "default_cafe",
    title = "Relaxed Café Sitting",
    instruction = "Sit comfortably on the chair, keep your shoulders relaxed and place your hands naturally.",
    framingTip = "Angle your chair 45 degrees toward soft window light.",
    category = PoseCategory.SOLO_PORTRAITS,
    difficulty = PoseDifficulty.EASY,
    outlineType = PoseOutlineType.RELAXED_CAFE_SITTING
)

private val DEFAULT_VIDEO_POSE = PoseSuggestion(
    id = "default_vid_walk",
    title = "Walking Pose (Video)",
    instruction = "Start standing inside the outline, then take 4 smooth, natural steps forward while smiling.",
    framingTip = "Toggle VIDEO POSE GUIDE off anytime mid-recording without stopping the clip.",
    category = PoseCategory.SOLO_PORTRAITS,
    difficulty = PoseDifficulty.EASY,
    outlineType = PoseOutlineType.WALKING_POSE
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val storageHelper = MediaStorageHelper(application)
    private val settingsPrefs = SettingsPreferences(application)
    val repository = MemoryRepository(database.memoryDao(), storageHelper, settingsPrefs)
    private val aiRepository: AiRepository = SampleAiRepository(application)

    val userSettings: StateFlow<UserSettings> = repository.userSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    val journalEntries: StateFlow<List<JournalEntryEntity>> = repository.allJournalEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoJournals: StateFlow<List<VideoJournalEntryEntity>> = repository.allVideoJournals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _gallerySortOrder = MutableStateFlow(GallerySortOrder.NEWEST)
    val gallerySortOrder: StateFlow<GallerySortOrder> = _gallerySortOrder.asStateFlow()

    val galleryPhotos: StateFlow<List<PhotoEntity>> = _gallerySortOrder
        .flatMapLatest { sortOrder -> repository.observePhotos(sortOrder) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val galleryVideos: StateFlow<List<VideoEntity>> = _gallerySortOrder
        .flatMapLatest { sortOrder -> repository.observeVideos(sortOrder) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedItems: StateFlow<List<ArchivedMemoryItem>> = repository.observeArchivedItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedPoseCategory = MutableStateFlow(PoseCategory.SOLO_PORTRAITS)
    val selectedPoseCategory: StateFlow<PoseCategory> = _selectedPoseCategory.asStateFlow()

    private var currentPoseBatchIndex = 0
    private var currentVideoPoseBatchIndex = 0

    private val _poseUiState = MutableStateFlow<PoseUiState>(PoseUiState.Loading)
    val poseUiState: StateFlow<PoseUiState> = _poseUiState.asStateFlow()

    private val _videoPoseUiState = MutableStateFlow<PoseUiState>(PoseUiState.Loading)
    val videoPoseUiState: StateFlow<PoseUiState> = _videoPoseUiState.asStateFlow()

    private val _activePoseSuggestion = MutableStateFlow(DEFAULT_PHOTO_POSE)
    val activePoseSuggestion: StateFlow<PoseSuggestion> = _activePoseSuggestion.asStateFlow()

    private val _activeVideoPoseSuggestion = MutableStateFlow(DEFAULT_VIDEO_POSE)
    val activeVideoPoseSuggestion: StateFlow<PoseSuggestion> = _activeVideoPoseSuggestion.asStateFlow()

    private val _photoOutlineConfig = MutableStateFlow(PoseOutlineConfig())
    val photoOutlineConfig: StateFlow<PoseOutlineConfig> = _photoOutlineConfig.asStateFlow()

    private val _videoOutlineConfig = MutableStateFlow(PoseOutlineConfig())
    val videoOutlineConfig: StateFlow<PoseOutlineConfig> = _videoOutlineConfig.asStateFlow()

    private val _alignmentGuidance = MutableStateFlow(AlignmentGuidance())
    val alignmentGuidance: StateFlow<AlignmentGuidance> = _alignmentGuidance.asStateFlow()

    private val _captionUiState = MutableStateFlow<CaptionUiState>(CaptionUiState.Idle)
    val captionUiState: StateFlow<CaptionUiState> = _captionUiState.asStateFlow()

    private val _pendingCapture = MutableStateFlow<PendingCapture?>(null)
    val pendingCapture: StateFlow<PendingCapture?> = _pendingCapture.asStateFlow()

    private val _pendingVideoCapture = MutableStateFlow<PendingVideoCapture?>(null)
    val pendingVideoCapture: StateFlow<PendingVideoCapture?> = _pendingVideoCapture.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    private val _isBusyBackup = MutableStateFlow(false)
    val isBusyBackup: StateFlow<Boolean> = _isBusyBackup.asStateFlow()

    // Photo & Video Studio Projects
    val photoEditProjects: StateFlow<List<EditProjectEntity>> = repository.observePhotoEditProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videoEditProjects: StateFlow<List<EditProjectEntity>> = repository.observeVideoEditProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Photo Editor State & Undo/Redo Stacks
    private val _photoEditState = MutableStateFlow<PhotoEditState?>(null)
    val photoEditState: StateFlow<PhotoEditState?> = _photoEditState.asStateFlow()

    private val photoUndoStack = ArrayDeque<PhotoEditState>()
    private val photoRedoStack = ArrayDeque<PhotoEditState>()

    private val _canUndoPhoto = MutableStateFlow(false)
    val canUndoPhoto: StateFlow<Boolean> = _canUndoPhoto.asStateFlow()

    private val _canRedoPhoto = MutableStateFlow(false)
    val canRedoPhoto: StateFlow<Boolean> = _canRedoPhoto.asStateFlow()

    private val _isProcessingCutout = MutableStateFlow(false)
    val isProcessingCutout: StateFlow<Boolean> = _isProcessingCutout.asStateFlow()

    private val _isSavingPhotoEdit = MutableStateFlow(false)
    val isSavingPhotoEdit: StateFlow<Boolean> = _isSavingPhotoEdit.asStateFlow()

    // Video Editor State & Undo/Redo Stacks
    private val _videoEditState = MutableStateFlow(VideoEditState())
    val videoEditState: StateFlow<VideoEditState> = _videoEditState.asStateFlow()

    private val videoUndoStack = ArrayDeque<VideoEditState>()
    private val videoRedoStack = ArrayDeque<VideoEditState>()

    private val _canUndoVideo = MutableStateFlow(false)
    val canUndoVideo: StateFlow<Boolean> = _canUndoVideo.asStateFlow()

    private val _canRedoVideo = MutableStateFlow(false)
    val canRedoVideo: StateFlow<Boolean> = _canRedoVideo.asStateFlow()

    private val _videoExportState = MutableStateFlow<VideoExportState>(VideoExportState.Idle)
    val videoExportState: StateFlow<VideoExportState> = _videoExportState.asStateFlow()

    val supportedResolutions: List<ExportResolutionSupport> by lazy {
        VideoExportEngine.probeSupportedResolutions()
    }

    private var activeExportJob: Job? = null
    @OptIn(UnstableApi::class)
    private var activeTransformer: Transformer? = null

    init {
        viewModelScope.launch {
            val saved = repository.userSettings.first()
            _photoOutlineConfig.value = PoseOutlineConfig(
                enabled = saved.photoPoseOutlineEnabled,
                scale = saved.outlineScale,
                verticalOffsetFraction = saved.outlineVerticalOffset,
                alpha = saved.outlineAlpha,
                saveWithGuide = saved.savePhotoWithGuide
            )
            _videoOutlineConfig.value = PoseOutlineConfig(
                enabled = saved.videoPoseGuideEnabled,
                scale = saved.outlineScale,
                verticalOffsetFraction = saved.outlineVerticalOffset,
                alpha = saved.outlineAlpha,
                saveWithGuide = saved.saveVideoWithGuide
            )
        }
        loadPoseSuggestions(PoseCategory.SOLO_PORTRAITS, advanceBatch = false, hasCameraFrame = false)
        loadVideoPoseSuggestions(PoseCategory.SOLO_PORTRAITS, advanceBatch = false)
    }

    fun selectPoseCategory(category: PoseCategory, hasCameraFrame: Boolean = false) {
        _selectedPoseCategory.value = category
        currentPoseBatchIndex = 0
        currentVideoPoseBatchIndex = 0
        loadPoseSuggestions(category, advanceBatch = false, hasCameraFrame = hasCameraFrame)
        loadVideoPoseSuggestions(category, advanceBatch = false)
    }

    fun loadPoseSuggestions(
        category: PoseCategory = _selectedPoseCategory.value,
        advanceBatch: Boolean = true,
        hasCameraFrame: Boolean = false
    ) {
        if (advanceBatch) currentPoseBatchIndex++
        viewModelScope.launch {
            _poseUiState.value = PoseUiState.Loading
            try {
                val batch = aiRepository.suggestPoses(
                    category = category,
                    batchIndex = currentPoseBatchIndex,
                    hasCameraFrame = hasCameraFrame
                )
                _poseUiState.value = PoseUiState.Success(batch)
                if (advanceBatch && batch.suggestions.isNotEmpty()) {
                    _activePoseSuggestion.value = batch.suggestions.first()
                }
            } catch (e: Exception) {
                _poseUiState.value = PoseUiState.Error(
                    e.localizedMessage ?: "Unable to load pose suggestions."
                )
            }
        }
    }

    fun loadVideoPoseSuggestions(
        category: PoseCategory = _selectedPoseCategory.value,
        advanceBatch: Boolean = true
    ) {
        if (advanceBatch) currentVideoPoseBatchIndex++
        viewModelScope.launch {
            _videoPoseUiState.value = PoseUiState.Loading
            try {
                val batch = aiRepository.suggestVideoPoses(
                    category = category,
                    batchIndex = currentVideoPoseBatchIndex
                )
                _videoPoseUiState.value = PoseUiState.Success(batch)
                if (advanceBatch && batch.suggestions.isNotEmpty()) {
                    _activeVideoPoseSuggestion.value = batch.suggestions.first()
                }
            } catch (e: Exception) {
                _videoPoseUiState.value = PoseUiState.Error(
                    e.localizedMessage ?: "Unable to load video pose suggestions."
                )
            }
        }
    }

    fun selectPoseSuggestion(pose: PoseSuggestion) {
        _activePoseSuggestion.value = pose
        if (!_photoOutlineConfig.value.enabled) {
            setPhotoPoseOutlineEnabled(true)
        }
    }

    fun selectVideoPoseSuggestion(pose: PoseSuggestion) {
        _activeVideoPoseSuggestion.value = pose
        if (!_videoOutlineConfig.value.enabled) {
            setVideoPoseGuideEnabled(true)
        }
    }

    fun setPhotoPoseOutlineEnabled(enabled: Boolean) {
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(enabled = enabled)
        viewModelScope.launch { repository.setPhotoPoseOutlineEnabled(enabled) }
    }

    fun setVideoPoseGuideEnabled(enabled: Boolean) {
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(enabled = enabled)
        viewModelScope.launch { repository.setVideoPoseGuideEnabled(enabled) }
    }

    fun setOutlineScale(scale: Float) {
        val clamped = scale.coerceIn(0.6f, 1.4f)
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(scale = clamped)
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(scale = clamped)
        viewModelScope.launch { repository.setOutlineScale(clamped) }
    }

    fun setOutlineVerticalOffset(offsetFraction: Float) {
        val clamped = offsetFraction.coerceIn(-0.22f, 0.22f)
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(verticalOffsetFraction = clamped)
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(verticalOffsetFraction = clamped)
        viewModelScope.launch { repository.setOutlineVerticalOffset(clamped) }
    }

    fun setOutlineAlpha(alpha: Float) {
        val clamped = alpha.coerceIn(0.25f, 1.0f)
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(alpha = clamped)
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(alpha = clamped)
        viewModelScope.launch { repository.setOutlineAlpha(clamped) }
    }

    fun resetOutlineStyle() {
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(
            scale = 1.0f,
            verticalOffsetFraction = 0f,
            alpha = 0.88f
        )
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(
            scale = 1.0f,
            verticalOffsetFraction = 0f,
            alpha = 0.88f
        )
        viewModelScope.launch { repository.resetOutlineStyle() }
    }

    fun setSavePhotoWithGuide(enabled: Boolean) {
        _photoOutlineConfig.value = _photoOutlineConfig.value.copy(saveWithGuide = enabled)
        viewModelScope.launch { repository.setSavePhotoWithGuide(enabled) }
    }

    fun setSaveVideoWithGuide(enabled: Boolean) {
        _videoOutlineConfig.value = _videoOutlineConfig.value.copy(saveWithGuide = enabled)
        viewModelScope.launch { repository.setSaveVideoWithGuide(enabled) }
    }

    fun updateAlignmentGuidance(guidance: AlignmentGuidance) {
        _alignmentGuidance.value = guidance
    }

    fun generateCaption(
        place: String,
        dateTimeFormatted: String,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        onGenerated: (String) -> Unit
    ) {
        viewModelScope.launch {
            _captionUiState.value = CaptionUiState.Loading
            try {
                val result = aiRepository.generateThreeLineCaption(
                    place = place,
                    dateTimeFormatted = dateTimeFormatted,
                    outfit = outfit,
                    goodThingOne = goodThingOne,
                    goodThingTwo = goodThingTwo
                )
                _captionUiState.value = CaptionUiState.Success(result)
                onGenerated(result.threeLineCaption)
                _toastEvents.tryEmit("Three-line caption ready!")
            } catch (e: Exception) {
                _captionUiState.value = CaptionUiState.Error(
                    e.localizedMessage ?: "Could not generate caption."
                )
            }
        }
    }

    fun resetCaptionState() {
        _captionUiState.value = CaptionUiState.Idle
    }

    fun createTempCaptureFile(): File = repository.createTempCaptureFile()
    fun createTempVideoFile(): File = repository.createTempVideoFile()

    fun onPhotoCapturedWithGuideOption(
        filePath: String,
        capturedAtMillis: Long,
        onReady: () -> Unit
    ) {
        viewModelScope.launch {
            val cfg = _photoOutlineConfig.value
            val outline = _activePoseSuggestion.value.outlineType
            repository.maybeApplyGuideOverlayToTempPhoto(filePath, outline, cfg)
            _pendingCapture.value = PendingCapture(filePath, capturedAtMillis, null)
            onReady()
        }
    }

    fun setPendingCapture(filePath: String, capturedAtMillis: Long, existingPhotoId: Long? = null) {
        _pendingCapture.value = PendingCapture(filePath, capturedAtMillis, existingPhotoId)
    }

    fun clearPendingCapture() {
        _pendingCapture.value = null
    }

    fun setPendingVideoCapture(
        filePath: String,
        durationMillis: Long,
        capturedAtMillis: Long = System.currentTimeMillis()
    ) {
        _pendingVideoCapture.value = PendingVideoCapture(
            filePath = filePath,
            thumbnailPath = null,
            durationMillis = durationMillis,
            capturedAtMillis = capturedAtMillis
        )
    }

    fun clearPendingVideoCapture() {
        val pending = _pendingVideoCapture.value
        if (pending != null && pending.existingVideoId == null && pending.filePath.contains("/video_captures/")) {
            runCatching { File(pending.filePath).delete() }
        }
        _pendingVideoCapture.value = null
    }

    fun importPickedPhoto(uri: Uri, onReady: () -> Unit) {
        viewModelScope.launch {
            try {
                val (path, timestamp) = repository.importPickedPhoto(uri)
                _pendingCapture.value = PendingCapture(path, timestamp, null)
                onReady()
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not import selected photo.")
            }
        }
    }

    fun importPickedVideo(uri: Uri, onReady: () -> Unit) {
        viewModelScope.launch {
            try {
                val result = repository.importPickedVideo(uri)
                _pendingVideoCapture.value = PendingVideoCapture(
                    filePath = result.filePath,
                    thumbnailPath = result.thumbnailPath,
                    durationMillis = result.durationMillis,
                    capturedAtMillis = System.currentTimeMillis()
                )
                onReady()
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not import selected video.")
            }
        }
    }

    fun savePhotoOnly(onSaved: () -> Unit) {
        val capture = _pendingCapture.value ?: return
        viewModelScope.launch {
            try {
                repository.saveStandalonePhoto(
                    tempOrPrivatePath = capture.filePath,
                    capturedAtMillis = capture.capturedAtMillis,
                    exportToDeviceGallery = true
                )
                _pendingCapture.value = null
                _toastEvents.tryEmit("Your beautiful photo has been saved!")
                onSaved()
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not save photo.")
            }
        }
    }

    fun saveVideoOnly(onSaved: () -> Unit) {
        val capture = _pendingVideoCapture.value ?: return
        viewModelScope.launch {
            try {
                repository.saveStandaloneVideo(
                    tempOrPrivatePath = capture.filePath,
                    thumbnailPath = capture.thumbnailPath,
                    durationMillis = capture.durationMillis,
                    capturedAtMillis = capture.capturedAtMillis,
                    exportToDeviceGallery = true
                )
                _pendingVideoCapture.value = null
                _toastEvents.tryEmit("Your beautiful video memory has been saved!")
                onSaved()
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not save video.")
            }
        }
    }

    fun saveJournalEntry(
        editingEntryId: Long?,
        place: String,
        capturedAtMillis: Long,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToGallery: Boolean,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (editingEntryId != null && editingEntryId > 0L) {
                    val existing = repository.getJournalEntryById(editingEntryId)
                    if (existing != null) {
                        val updated = existing.copy(
                            place = place.trim(),
                            capturedAtMillis = capturedAtMillis,
                            outfit = outfit.trim(),
                            goodThingOne = goodThingOne.trim(),
                            goodThingTwo = goodThingTwo.trim(),
                            caption = caption.trim(),
                            templateId = templateId,
                            stickersCsv = stickersCsv
                        )
                        repository.updateExistingJournalEntry(updated)
                        _toastEvents.tryEmit("Journal entry updated!")
                        onSaved(updated.id)
                        return@launch
                    }
                }

                val capture = _pendingCapture.value
                    ?: throw IllegalStateException("No photograph selected for journal entry.")
                val newId = repository.saveNewJournalMemory(
                    tempOrPrivatePath = capture.filePath,
                    existingPhotoId = capture.existingPhotoId,
                    place = place,
                    capturedAtMillis = capturedAtMillis,
                    outfit = outfit,
                    goodThingOne = goodThingOne,
                    goodThingTwo = goodThingTwo,
                    caption = caption,
                    templateId = templateId,
                    stickersCsv = stickersCsv,
                    alsoSaveToDeviceGallery = alsoSaveToGallery
                )
                _toastEvents.tryEmit("Saved to My Photo Journal!")
                onSaved(newId)
                _pendingCapture.value = null
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to save journal entry.")
            }
        }
    }

    fun saveVideoJournalEntry(
        editingEntryId: Long?,
        place: String,
        capturedAtMillis: Long,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToGallery: Boolean,
        onSaved: (Long) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (editingEntryId != null && editingEntryId > 0L) {
                    val existing = repository.getVideoJournalById(editingEntryId)
                    if (existing != null) {
                        val updated = existing.copy(
                            place = place.trim(),
                            capturedAtMillis = capturedAtMillis,
                            outfit = outfit.trim(),
                            goodThingOne = goodThingOne.trim(),
                            goodThingTwo = goodThingTwo.trim(),
                            caption = caption.trim(),
                            templateId = templateId,
                            stickersCsv = stickersCsv
                        )
                        repository.updateExistingVideoJournalEntry(updated)
                        _toastEvents.tryEmit("Video journal updated!")
                        onSaved(updated.id)
                        return@launch
                    }
                }

                val capture = _pendingVideoCapture.value
                    ?: throw IllegalStateException("No video selected for video journal entry.")
                val newId = repository.saveNewVideoJournalMemory(
                    tempOrPrivatePath = capture.filePath,
                    existingVideoId = capture.existingVideoId,
                    existingThumbnailPath = capture.thumbnailPath,
                    durationMillis = capture.durationMillis,
                    place = place,
                    capturedAtMillis = capturedAtMillis,
                    outfit = outfit,
                    goodThingOne = goodThingOne,
                    goodThingTwo = goodThingTwo,
                    caption = caption,
                    templateId = templateId,
                    stickersCsv = stickersCsv,
                    alsoSaveToDeviceGallery = alsoSaveToGallery
                )
                _toastEvents.tryEmit("Saved to Video Journal!")
                onSaved(newId)
                _pendingVideoCapture.value = null
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to save video journal.")
            }
        }
    }

    fun prepareEditorFromGalleryPhoto(photo: PhotoEntity, onReady: () -> Unit) {
        _pendingCapture.value = PendingCapture(
            filePath = photo.filePath,
            capturedAtMillis = photo.capturedAtMillis,
            existingPhotoId = photo.id
        )
        onReady()
    }

    fun prepareVideoEditorFromGallery(video: VideoEntity, onReady: () -> Unit) {
        _pendingVideoCapture.value = PendingVideoCapture(
            filePath = video.filePath,
            thumbnailPath = video.thumbnailPath,
            durationMillis = video.durationMillis,
            capturedAtMillis = video.capturedAtMillis,
            existingVideoId = video.id
        )
        onReady()
    }

    fun toggleJournalFavorite(entry: JournalEntryEntity) {
        viewModelScope.launch { repository.toggleJournalFavorite(entry) }
    }

    fun togglePhotoFavorite(photo: PhotoEntity) {
        viewModelScope.launch { repository.togglePhotoFavorite(photo) }
    }

    fun toggleVideoFavorite(video: VideoEntity) {
        viewModelScope.launch { repository.toggleVideoFavorite(video) }
    }

    fun toggleVideoJournalFavorite(entry: VideoJournalEntryEntity) {
        viewModelScope.launch { repository.toggleVideoJournalFavorite(entry) }
    }

    // Soft-delete actions -> Move to MY ARCHIVE
    fun archiveJournalEntry(entry: JournalEntryEntity, onArchived: () -> Unit = {}) {
        viewModelScope.launch {
            repository.archiveJournalEntry(entry)
            _toastEvents.tryEmit("Moved journal to My Archive.")
            onArchived()
        }
    }

    fun archivePhoto(photo: PhotoEntity) {
        viewModelScope.launch {
            repository.archivePhotoAndRelatedJournal(photo)
            _toastEvents.tryEmit("Moved photograph to My Archive.")
        }
    }

    fun archiveVideo(video: VideoEntity) {
        viewModelScope.launch {
            repository.archiveVideoAndRelatedJournal(video)
            _toastEvents.tryEmit("Moved video to My Archive.")
        }
    }

    fun archiveVideoJournalEntry(entry: VideoJournalEntryEntity, onArchived: () -> Unit = {}) {
        viewModelScope.launch {
            repository.archiveVideoJournalEntry(entry)
            _toastEvents.tryEmit("Moved video journal to My Archive.")
            onArchived()
        }
    }

    fun restoreArchivedItem(item: ArchivedMemoryItem) {
        viewModelScope.launch {
            repository.restoreArchivedItem(item)
            _toastEvents.tryEmit("${item.type.label} restored!")
        }
    }

    fun restoreAllArchived() {
        viewModelScope.launch {
            repository.restoreAllArchived()
            _toastEvents.tryEmit("All archived memories restored!")
        }
    }

    fun permanentlyDeleteArchivedItem(item: ArchivedMemoryItem) {
        viewModelScope.launch {
            repository.permanentlyDeleteArchivedItem(item)
            _toastEvents.tryEmit("Permanently deleted.")
        }
    }

    fun downloadPhotoToGallery(filePath: String, photoId: Long? = null) {
        viewModelScope.launch {
            val ok = repository.exportPhotoToDeviceGallery(filePath, photoId)
            if (ok) {
                _toastEvents.tryEmit("Your beautiful photo has been saved!")
            } else {
                _toastEvents.tryEmit("Unable to save photo to device gallery.")
            }
        }
    }

    fun downloadVideoToGallery(filePath: String, videoId: Long? = null) {
        viewModelScope.launch {
            val ok = repository.exportVideoToDeviceGallery(filePath, videoId)
            if (ok) {
                _toastEvents.tryEmit("Your video has been saved to Movies!")
            } else {
                _toastEvents.tryEmit("Unable to save video to device gallery.")
            }
        }
    }

    fun getShareableUri(filePath: String): Uri? = repository.getShareableUri(filePath)

    fun setGallerySortOrder(order: GallerySortOrder) {
        _gallerySortOrder.value = order
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setThemePalette(palette: ThemePalette) {
        viewModelScope.launch { repository.setThemePalette(palette) }
    }

    fun setDefaultTimerSeconds(seconds: Int) {
        viewModelScope.launch { repository.setDefaultTimerSeconds(seconds) }
    }

    fun exportBackupZip(outputStream: OutputStream?) {
        if (outputStream == null) return
        viewModelScope.launch {
            _isBusyBackup.value = true
            try {
                repository.exportBackupZip(outputStream)
                _toastEvents.tryEmit("Backup exported successfully!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Backup export failed.")
            } finally {
                _isBusyBackup.value = false
            }
        }
    }

    fun exportReadableJournal(outputStream: OutputStream?) {
        if (outputStream == null) return
        viewModelScope.launch {
            _isBusyBackup.value = true
            try {
                repository.exportReadableJournal(outputStream)
                _toastEvents.tryEmit("Journal entries exported!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Journal export failed.")
            } finally {
                _isBusyBackup.value = false
            }
        }
    }

    fun importBackupZip(inputStream: InputStream?) {
        if (inputStream == null) return
        viewModelScope.launch {
            _isBusyBackup.value = true
            try {
                val restoredCount = repository.importBackupZip(inputStream)
                _toastEvents.tryEmit("Restored $restoredCount memories from backup!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not restore backup.")
            } finally {
                _isBusyBackup.value = false
            }
        }
    }

    fun deleteAllAppData() {
        viewModelScope.launch {
            repository.deleteAllAppData()
            _pendingCapture.value = null
            _pendingVideoCapture.value = null
            _toastEvents.tryEmit("All app data has been deleted.")
        }
    }

    suspend fun findJournalIdForPhoto(photoId: Long): Long? =
        repository.getJournalEntryByPhotoId(photoId)?.id

    suspend fun getJournalEntryById(entryId: Long): JournalEntryEntity? =
        repository.getJournalEntryById(entryId)

    suspend fun getVideoJournalById(entryId: Long): VideoJournalEntryEntity? =
        repository.getVideoJournalById(entryId)

    // ==================== TEMPLATE & STICKER LIBRARY FAVORITES ====================

    fun toggleFavoriteTemplate(templateId: String) {
        viewModelScope.launch { repository.toggleFavoriteTemplate(templateId) }
    }

    fun toggleFavoriteSticker(stickerId: String) {
        viewModelScope.launch { repository.toggleFavoriteSticker(stickerId) }
    }

    // ==================== PHOTO EDITOR STUDIO ====================

    fun openPhotoEditor(filePath: String, sourcePhotoId: Long? = null) {
        photoUndoStack.clear()
        photoRedoStack.clear()
        _canUndoPhoto.value = false
        _canRedoPhoto.value = false
        _photoEditState.value = PhotoEditState(
            projectId = 0L,
            sourcePhotoId = sourcePhotoId,
            sourcePath = filePath
        )
    }

    fun openPhotoEditorFromProject(project: EditProjectEntity) {
        val decoded = PhotoEditState.fromJsonString(project.stateJson, projectId = project.id)
        if (decoded != null) {
            photoUndoStack.clear()
            photoRedoStack.clear()
            _canUndoPhoto.value = false
            _canRedoPhoto.value = false
            _photoEditState.value = decoded
        } else {
            _toastEvents.tryEmit("Could not open saved photo project.")
        }
    }

    fun updatePhotoEdit(recordUndo: Boolean = true, transform: (PhotoEditState) -> PhotoEditState) {
        val current = _photoEditState.value ?: return
        val next = transform(current)
        if (next == current) return
        if (recordUndo) {
            photoUndoStack.addLast(current)
            if (photoUndoStack.size > 40) photoUndoStack.removeFirst()
            photoRedoStack.clear()
            _canUndoPhoto.value = photoUndoStack.isNotEmpty()
            _canRedoPhoto.value = false
        }
        _photoEditState.value = next
    }

    fun undoPhotoEdit() {
        val current = _photoEditState.value ?: return
        val prev = photoUndoStack.removeLastOrNull() ?: return
        photoRedoStack.addLast(current)
        _photoEditState.value = prev
        _canUndoPhoto.value = photoUndoStack.isNotEmpty()
        _canRedoPhoto.value = photoRedoStack.isNotEmpty()
    }

    fun redoPhotoEdit() {
        val current = _photoEditState.value ?: return
        val next = photoRedoStack.removeLastOrNull() ?: return
        photoUndoStack.addLast(current)
        _photoEditState.value = next
        _canUndoPhoto.value = photoUndoStack.isNotEmpty()
        _canRedoPhoto.value = photoRedoStack.isNotEmpty()
    }

    fun removePhotoBackground() {
        val current = _photoEditState.value ?: return
        if (_isProcessingCutout.value) return
        viewModelScope.launch {
            _isProcessingCutout.value = true
            try {
                val outFile = repository.createCutoutFile()
                val res = PhotoEditEngine.removeBackgroundOnDevice(current.sourcePath, outFile)
                updatePhotoEdit(recordUndo = true) { st ->
                    st.copy(
                        cutoutMaskPath = res.cutoutFilePath,
                        adjustments = st.adjustments.copy(
                            backgroundRemoved = true,
                            cutoutBackdrop = if (st.adjustments.cutoutBackdrop == CutoutBackdrop.NONE) {
                                CutoutBackdrop.BLUSH_PINK
                            } else {
                                st.adjustments.cutoutBackdrop
                            }
                        )
                    )
                }
                _toastEvents.tryEmit("Subject cutout ready! Choose any pastel or transparent backdrop.")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not remove background on this image.")
            } finally {
                _isProcessingCutout.value = false
            }
        }
    }

    fun savePhotoEditDraft() {
        val current = _photoEditState.value ?: return
        viewModelScope.launch {
            val savedId = repository.saveEditProject(
                EditProjectEntity(
                    id = current.projectId,
                    projectType = "PHOTO",
                    title = "Photo Edit",
                    sourcePath = current.sourcePath,
                    previewPath = current.sourcePath,
                    stateJson = current.toJsonString(),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            _photoEditState.value = current.copy(projectId = savedId)
            _toastEvents.tryEmit("Photo project draft saved!")
        }
    }

    fun saveEditedPhotoCopy(
        exportToDeviceGallery: Boolean = true,
        onSaved: (photoId: Long, savedPath: String) -> Unit
    ) {
        val current = _photoEditState.value ?: return
        if (_isSavingPhotoEdit.value) return
        viewModelScope.launch {
            _isSavingPhotoEdit.value = true
            try {
                val bmp = PhotoEditEngine.renderFinalPhotoBitmap(getApplication(), current, previewModeMaxDim = 0)
                val asPng = current.adjustments.backgroundRemoved &&
                    current.adjustments.cutoutBackdrop == CutoutBackdrop.TRANSPARENT
                val (newId, newPath) = repository.saveEditedPhotoCopy(
                    bitmap = bmp,
                    asPng = asPng,
                    exportToDeviceGallery = exportToDeviceGallery
                )
                bmp.recycle()
                val savedProjId = repository.saveEditProject(
                    EditProjectEntity(
                        id = current.projectId,
                        projectType = "PHOTO",
                        title = "Edited Photo",
                        sourcePath = current.sourcePath,
                        previewPath = newPath,
                        stateJson = current.toJsonString(),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                _photoEditState.value = current.copy(projectId = savedProjId)
                _toastEvents.tryEmit("Edited copy saved without altering your original!")
                onSaved(newId, newPath)
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to save edited photo.")
            } finally {
                _isSavingPhotoEdit.value = false
            }
        }
    }

    // ==================== PRO VIDEO EDITOR ====================

    fun startFreshVideoProject(title: String = "My Pastel Film") {
        videoUndoStack.clear()
        videoRedoStack.clear()
        _canUndoVideo.value = false
        _canRedoVideo.value = false
        _videoEditState.value = VideoEditState(title = title)
    }

    fun openVideoInProEditor(
        videoPath: String,
        durationMillis: Long = 0L,
        thumbnailPath: String = "",
        title: String = "Edited Video"
    ) {
        val measuredDuration = if (durationMillis > 0L) durationMillis else probeMediaDurationMs(videoPath)
        val initialClip = VideoClipItem(
            sourcePath = videoPath,
            thumbnailPath = thumbnailPath,
            isImageClip = false,
            sourceDurationMs = measuredDuration.coerceAtLeast(1000L),
            trimStartMs = 0L,
            trimEndMs = measuredDuration.coerceAtLeast(1000L)
        )
        videoUndoStack.clear()
        videoRedoStack.clear()
        _canUndoVideo.value = false
        _canRedoVideo.value = false
        _videoEditState.value = VideoEditState(
            title = title,
            clips = listOf(initialClip)
        )
    }

    fun openVideoProject(project: EditProjectEntity) {
        val decoded = VideoEditState.fromJsonString(project.stateJson, projectId = project.id)
        if (decoded != null) {
            videoUndoStack.clear()
            videoRedoStack.clear()
            _canUndoVideo.value = false
            _canRedoVideo.value = false
            _videoEditState.value = decoded
        } else {
            _toastEvents.tryEmit("Could not open saved video project.")
        }
    }

    fun updateVideoEdit(recordUndo: Boolean = true, transform: (VideoEditState) -> VideoEditState) {
        val current = _videoEditState.value
        val next = transform(current)
        if (next == current) return
        if (recordUndo) {
            videoUndoStack.addLast(current)
            if (videoUndoStack.size > 40) videoUndoStack.removeFirst()
            videoRedoStack.clear()
            _canUndoVideo.value = videoUndoStack.isNotEmpty()
            _canRedoVideo.value = false
        }
        _videoEditState.value = next
    }

    fun undoVideoEdit() {
        val current = _videoEditState.value
        val prev = videoUndoStack.removeLastOrNull() ?: return
        videoRedoStack.addLast(current)
        _videoEditState.value = prev
        _canUndoVideo.value = videoUndoStack.isNotEmpty()
        _canRedoVideo.value = videoRedoStack.isNotEmpty()
    }

    fun redoVideoEdit() {
        val current = _videoEditState.value
        val next = videoRedoStack.removeLastOrNull() ?: return
        videoUndoStack.addLast(current)
        _videoEditState.value = next
        _canUndoVideo.value = videoUndoStack.isNotEmpty()
        _canRedoVideo.value = videoRedoStack.isNotEmpty()
    }

    fun addExistingVideoClipToTimeline(video: VideoEntity) {
        val dur = if (video.durationMillis > 0L) video.durationMillis else probeMediaDurationMs(video.filePath)
        val clip = VideoClipItem(
            sourcePath = video.filePath,
            thumbnailPath = video.thumbnailPath,
            isImageClip = false,
            sourceDurationMs = dur.coerceAtLeast(1000L),
            trimStartMs = 0L,
            trimEndMs = dur.coerceAtLeast(1000L)
        )
        updateVideoEdit { it.copy(clips = it.clips + clip) }
    }

    fun addExistingPhotoSlideToTimeline(photo: PhotoEntity, durationMs: Long = 3500L) {
        val clip = VideoClipItem(
            sourcePath = photo.filePath,
            thumbnailPath = photo.filePath,
            isImageClip = true,
            sourceDurationMs = durationMs,
            trimStartMs = 0L,
            trimEndMs = durationMs
        )
        updateVideoEdit { it.copy(clips = it.clips + clip) }
    }

    fun importPickedVideoIntoTimeline(uri: Uri) {
        viewModelScope.launch {
            try {
                val res = repository.importPickedVideo(uri)
                val clip = VideoClipItem(
                    sourcePath = res.filePath,
                    thumbnailPath = res.thumbnailPath,
                    isImageClip = false,
                    sourceDurationMs = res.durationMillis.coerceAtLeast(1000L),
                    trimStartMs = 0L,
                    trimEndMs = res.durationMillis.coerceAtLeast(1000L)
                )
                updateVideoEdit { it.copy(clips = it.clips + clip) }
                _toastEvents.tryEmit("Added clip to timeline!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to import video clip.")
            }
        }
    }

    fun importPickedPhotoIntoTimeline(uri: Uri) {
        viewModelScope.launch {
            try {
                val (path, _) = repository.importPickedPhoto(uri)
                val clip = VideoClipItem(
                    sourcePath = path,
                    thumbnailPath = path,
                    isImageClip = true,
                    sourceDurationMs = 3500L,
                    trimStartMs = 0L,
                    trimEndMs = 3500L
                )
                updateVideoEdit { it.copy(clips = it.clips + clip) }
                _toastEvents.tryEmit("Added photo slide to timeline!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to import photo.")
            }
        }
    }

    fun splitClipAtTimelinePosition(timelineMs: Long): Boolean {
        val current = _videoEditState.value
        var cursor = 0L
        current.clips.forEachIndexed { idx, clip ->
            val clipEffDur = clip.effectiveDurationMs
            val start = cursor
            val end = cursor + clipEffDur
            if (timelineMs in (start + 250L)..(end - 250L)) {
                val localOffsetEff = timelineMs - start
                val localSourceOffset = (localOffsetEff * clip.speed).toLong()
                val splitSourceMs = (clip.trimStartMs + localSourceOffset)
                    .coerceIn(clip.trimStartMs + 250L, clip.trimEndMs - 250L)
                val firstHalf = clip.copy(trimEndMs = splitSourceMs)
                val secondHalf = clip.copy(
                    id = UUID.randomUUID().toString(),
                    trimStartMs = splitSourceMs
                )
                val nextClips = current.clips.toMutableList().apply {
                    removeAt(idx)
                    add(idx, firstHalf)
                    add(idx + 1, secondHalf)
                }
                updateVideoEdit { it.copy(clips = nextClips) }
                return true
            }
            cursor = end
        }
        return false
    }

    fun moveClipInTimeline(fromIndex: Int, toIndex: Int) {
        val current = _videoEditState.value
        if (fromIndex !in current.clips.indices || toIndex !in current.clips.indices || fromIndex == toIndex) return
        val list = current.clips.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        updateVideoEdit { it.copy(clips = list) }
    }

    fun addBuiltInAudioTrack(assetId: String, atTimelineMs: Long) {
        viewModelScope.launch {
            val asset = OfflineAudioLibrary.catalog.firstOrNull { it.id == assetId } ?: return@launch
            val (file, durMs) = OfflineAudioLibrary.ensureAudioAssetFile(repository.getAudioDir(), assetId)
            val layer = VideoAudioLayerItem(
                title = asset.title,
                kind = asset.kind,
                filePath = file.absolutePath,
                startMs = atTimelineMs.coerceAtLeast(0L),
                durationMs = durMs,
                volume = if (asset.kind == AudioTrackKind.MUSIC) 0.55f else 0.9f
            )
            updateVideoEdit { it.copy(audioTracks = it.audioTracks + layer) }
            _toastEvents.tryEmit("Added ${asset.title} to audio track!")
        }
    }

    fun importCustomAudioTrack(uri: Uri, atTimelineMs: Long) {
        viewModelScope.launch {
            try {
                val (path, durMs) = repository.importAudioUri(uri)
                val layer = VideoAudioLayerItem(
                    title = "Custom Audio",
                    kind = AudioTrackKind.MUSIC,
                    filePath = path,
                    startMs = atTimelineMs.coerceAtLeast(0L),
                    durationMs = durMs,
                    volume = 0.7f
                )
                updateVideoEdit { it.copy(audioTracks = it.audioTracks + layer) }
                _toastEvents.tryEmit("Audio track added!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Could not import audio.")
            }
        }
    }

    fun addVoiceoverAudioTrack(file: File, durationMs: Long, atTimelineMs: Long) {
        val layer = VideoAudioLayerItem(
            title = "Voiceover",
            kind = AudioTrackKind.VOICEOVER,
            filePath = file.absolutePath,
            startMs = atTimelineMs.coerceAtLeast(0L),
            durationMs = durationMs.coerceAtLeast(400L),
            volume = 1.0f,
            fadeInMs = 150L,
            fadeOutMs = 200L
        )
        updateVideoEdit { it.copy(audioTracks = it.audioTracks + layer) }
        _toastEvents.tryEmit("Voiceover recorded & placed on timeline!")
    }

    fun importPipImageFromUri(uri: Uri, atTimelineMs: Long) {
        viewModelScope.launch {
            try {
                val (path, _) = repository.importPickedPhoto(uri)
                val total = _videoEditState.value.totalDurationMs.coerceAtLeast(4000L)
                val end = (atTimelineMs + 4000L).coerceAtMost(total)
                val pip = PipOverlayItem(
                    title = "Photo Overlay",
                    sourcePath = path,
                    isVideo = false,
                    startMs = atTimelineMs.coerceAtLeast(0L),
                    endMs = end.coerceAtLeast(atTimelineMs + 800L)
                )
                updateVideoEdit { it.copy(pipOverlays = it.pipOverlays + pip) }
                _toastEvents.tryEmit("Added PiP photo layer!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to add PiP photo.")
            }
        }
    }

    fun importPipVideoFromUri(uri: Uri, atTimelineMs: Long) {
        viewModelScope.launch {
            try {
                val res = repository.importPickedVideo(uri)
                val total = _videoEditState.value.totalDurationMs.coerceAtLeast(4000L)
                val end = (atTimelineMs + res.durationMillis.coerceAtLeast(1000L)).coerceAtMost(total)
                val pip = PipOverlayItem(
                    title = "Video Overlay",
                    sourcePath = res.filePath,
                    isVideo = true,
                    startMs = atTimelineMs.coerceAtLeast(0L),
                    endMs = end.coerceAtLeast(atTimelineMs + 800L)
                )
                updateVideoEdit { it.copy(pipOverlays = it.pipOverlays + pip) }
                _toastEvents.tryEmit("Added PiP video layer!")
            } catch (e: Exception) {
                _toastEvents.tryEmit(e.localizedMessage ?: "Failed to add PiP video.")
            }
        }
    }

    fun saveVideoProjectDraft() {
        val current = _videoEditState.value
        viewModelScope.launch {
            val sourcePath = current.clips.firstOrNull()?.sourcePath.orEmpty()
            val preview = current.clips.firstOrNull()?.thumbnailPath?.ifBlank { sourcePath }.orEmpty()
            val savedId = repository.saveEditProject(
                EditProjectEntity(
                    id = current.projectId,
                    projectType = "VIDEO",
                    title = current.title.ifBlank { "My Pastel Film" },
                    sourcePath = sourcePath,
                    previewPath = preview,
                    stateJson = current.toJsonString(),
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
            _videoEditState.value = current.copy(projectId = savedId)
            _toastEvents.tryEmit("Video project draft saved!")
        }
    }

    fun deleteEditProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteEditProject(projectId)
            _toastEvents.tryEmit("Draft project removed.")
        }
    }

    @OptIn(UnstableApi::class)
    fun exportVideoProject(
        resolution: VideoExportResolution,
        saveToDeviceGallery: Boolean = true
    ) {
        val state = _videoEditState.value
        if (state.clips.isEmpty()) {
            _toastEvents.tryEmit("Add at least one clip to the timeline before exporting.")
            return
        }
        activeExportJob?.cancel()
        activeExportJob = viewModelScope.launch {
            _videoExportState.value = VideoExportState.Preparing("Preparing timeline & assets...")
            val tempOut = repository.createTempVideoFile()
            try {
                VideoExportEngine.exportVideo(
                    context = getApplication(),
                    state = state,
                    resolution = resolution,
                    outputFile = tempOut,
                    onStageAndProgress = { stage, pct ->
                        _videoExportState.value = VideoExportState.Exporting(pct, stage)
                    },
                    onTransformerCreated = { t -> activeTransformer = t }
                )
                val (videoId, savedPath) = repository.saveExportedVideoCopy(
                    tempVideoFile = tempOut,
                    exportToDeviceGallery = saveToDeviceGallery
                )
                val sourcePath = state.clips.firstOrNull()?.sourcePath.orEmpty()
                val preview = state.clips.firstOrNull()?.thumbnailPath?.ifBlank { sourcePath }.orEmpty()
                val projId = repository.saveEditProject(
                    EditProjectEntity(
                        id = state.projectId,
                        projectType = "VIDEO",
                        title = state.title.ifBlank { "My Pastel Film" },
                        sourcePath = sourcePath,
                        previewPath = preview,
                        stateJson = state.toJsonString(),
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
                _videoEditState.value = state.copy(projectId = projId)
                _videoExportState.value = VideoExportState.Completed(
                    videoId = videoId,
                    outputPath = savedPath,
                    savedToGallery = saveToDeviceGallery
                )
            } catch (e: Exception) {
                runCatching { tempOut.delete() }
                _videoExportState.value = VideoExportState.Error(
                    e.localizedMessage ?: "Export could not be completed on this device."
                )
            } finally {
                activeTransformer = null
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun cancelVideoExport() {
        runCatching { activeTransformer?.cancel() }
        activeTransformer = null
        activeExportJob?.cancel()
        activeExportJob = null
        _videoExportState.value = VideoExportState.Idle
    }

    fun resetVideoExportState() {
        _videoExportState.value = VideoExportState.Idle
    }

    fun getAudioDir(): File = repository.getAudioDir()

    private fun probeMediaDurationMs(filePath: String): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 5000L
        } catch (_: Exception) {
            5000L
        } finally {
            runCatching { retriever.release() }
        }
    }
}
