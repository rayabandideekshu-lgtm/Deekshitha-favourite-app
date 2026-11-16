package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PictureInPictureAlt
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.local.EditProjectEntity
import com.example.data.local.PhotoEntity
import com.example.data.local.VideoEntity
import com.example.data.model.AudioTrackKind
import com.example.data.model.CaptionAnimationStyle
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.EditorFontOption
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoColorFilter
import com.example.data.model.VideoEditState
import com.example.data.model.VideoExportResolution
import com.example.data.model.VideoExportState
import com.example.data.model.VideoStickerOverlayItem
import com.example.data.model.VideoTextOverlayItem
import com.example.data.model.VideoTransitionType
import com.example.ui.editor.ExportResolutionSupport
import com.example.ui.editor.OfflineAudioLibrary
import com.example.ui.editor.StickerVisual
import com.example.ui.editor.VoiceoverRecorder
import com.example.ui.editor.toComposeFontFamily
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import kotlin.math.sin

private enum class VideoToolTab(val label: String, val icon: ImageVector) {
    CLIP("Clip", Icons.Rounded.ContentCut),
    SPEED("Speed/Rev", Icons.Rounded.Speed),
    COLOR("Color", Icons.Rounded.ColorLens),
    TRANSFORM("Transform", Icons.Rounded.AspectRatio),
    TEXT("Text/Sub", Icons.Rounded.TextFields),
    STICKERS("Stickers", Icons.Rounded.EmojiEmotions),
    AUDIO("Audio/VO", Icons.Rounded.MusicNote),
    PIP("PiP/Key", Icons.Rounded.PictureInPictureAlt),
    TRANSITIONS("Transitions", Icons.Rounded.Animation)
}

@androidx.annotation.OptIn(UnstableApi::class)
@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    state: VideoEditState,
    canUndo: Boolean,
    canRedo: Boolean,
    exportState: VideoExportState,
    supportedResolutions: List<ExportResolutionSupport>,
    galleryVideos: List<VideoEntity>,
    galleryPhotos: List<PhotoEntity>,
    savedProjects: List<EditProjectEntity>,
    favoriteStickerIds: Set<String>,
    audioDir: File,
    onToggleFavoriteSticker: (String) -> Unit,
    onUpdateState: (recordUndo: Boolean, (VideoEditState) -> VideoEditState) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onStartFresh: () -> Unit,
    onOpenProject: (EditProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onAddExistingVideo: (VideoEntity) -> Unit,
    onAddExistingPhoto: (PhotoEntity) -> Unit,
    onImportVideoUri: (Uri) -> Unit,
    onImportPhotoUri: (Uri) -> Unit,
    onSplitAtPlayhead: (Long) -> Boolean,
    onMoveClip: (Int, Int) -> Unit,
    onAddBuiltInAudio: (String, Long) -> Unit,
    onImportAudioUri: (Uri, Long) -> Unit,
    onAddVoiceover: (File, Long, Long) -> Unit,
    onImportPipImageUri: (Uri, Long) -> Unit,
    onImportPipVideoUri: (Uri, Long) -> Unit,
    onSaveDraft: () -> Unit,
    onExportVideo: (VideoExportResolution, Boolean) -> Unit,
    onCancelExport: () -> Unit,
    onResetExportState: () -> Unit,
    onOpenVideoGallery: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(VideoToolTab.CLIP) }
    var selectedClipId by remember(state.clips) {
        mutableStateOf(state.clips.firstOrNull()?.id)
    }
    var selectedTextId by remember { mutableStateOf<String?>(null) }
    var selectedAudioId by remember { mutableStateOf<String?>(null) }
    var selectedPipId by remember { mutableStateOf<String?>(null) }

    var playheadMs by remember { mutableLongStateOf(0L) }
    var isPlaying by remember { mutableStateOf(false) }

    var showGalleryPickerDialog by remember { mutableStateOf(false) }
    var showStickerLibrary by remember { mutableStateOf(false) }
    var showTextDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedExportRes by remember { mutableStateOf(VideoExportResolution.FHD_1080P) }
    var saveExportToGallery by remember { mutableStateOf(true) }

    // Text/Subtitle editor dialog state
    var textDraft by remember { mutableStateOf("") }
    var textIsSubtitle by remember { mutableStateOf(false) }
    var textFontId by remember { mutableStateOf(EditorFontOption.PLAYFAIR.id) }
    var textAnimStyle by remember { mutableStateOf(CaptionAnimationStyle.POP_SCALE) }

    // Voiceover Recorder state
    val recorder = remember(context) { VoiceoverRecorder(context) }
    var isRecordingVoiceover by remember { mutableStateOf(false) }
    var voiceoverSeconds by remember { mutableLongStateOf(0L) }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    val micPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            runCatching {
                recorder.startRecording(audioDir)
                isRecordingVoiceover = true
                voiceoverSeconds = 0L
            }
        }
    }

    LaunchedEffect(isRecordingVoiceover) {
        while (isRecordingVoiceover) {
            delay(1000L)
            voiceoverSeconds++
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isRecordingVoiceover) recorder.cancel()
        }
    }

    // Media Pickers
    val pickVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onImportVideoUri(uri) }

    val pickPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onImportPhotoUri(uri) }

    val pickPipPhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onImportPipImageUri(uri, playheadMs) }

    val pickPipVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) onImportPipVideoUri(uri, playheadMs) }

    val pickAudioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onImportAudioUri(uri, playheadMs) }

    // Clamp playhead if timeline shrinks
    val totalDurationMs = state.totalDurationMs.coerceAtLeast(1L)
    LaunchedEffect(totalDurationMs) {
        if (playheadMs > totalDurationMs) playheadMs = 0L
    }

    // Resolve active clip at playhead
    val activeClipInfo = remember(state.clips, playheadMs) {
        var cursor = 0L
        var found: Triple<Int, VideoClipItem, Long>? = null
        state.clips.forEachIndexed { idx, clip ->
            val end = cursor + clip.effectiveDurationMs
            if (found == null && playheadMs <= end) {
                found = Triple(idx, clip, playheadMs - cursor)
            }
            cursor = end
        }
        found ?: state.clips.firstOrNull()?.let { Triple(0, it, 0L) }
    }
    val currentClip = activeClipInfo?.second
    val selectedClip = state.clips.firstOrNull { it.id == selectedClipId } ?: currentClip

    // Real ExoPlayer Preview
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }
    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer.release() }
    }

    // Sync active video clip to ExoPlayer when clip changes
    LaunchedEffect(currentClip?.id, currentClip?.sourcePath, currentClip?.trimStartMs, currentClip?.trimEndMs, currentClip?.isImageClip) {
        val clip = currentClip
        if (clip != null && !clip.isImageClip && File(clip.sourcePath).exists()) {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.fromFile(File(clip.sourcePath)))
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(clip.trimStartMs.coerceAtLeast(0L))
                        .setEndPositionMs(clip.trimEndMs.coerceAtLeast(clip.trimStartMs + 300L))
                        .build()
                )
                .build()
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        } else {
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
        }
    }

    // Live Playback Timer Loop across multi-clip timeline
    LaunchedEffect(isPlaying, state.clips) {
        while (isPlaying && state.clips.isNotEmpty()) {
            val clip = activeClipInfo?.second
            if (clip != null && !clip.isImageClip) {
                exoPlayer.setPlaybackSpeed(clip.speed.coerceIn(0.25f, 4f))
                exoPlayer.volume = clip.volume.coerceIn(0f, 1f)
                if (!exoPlayer.isPlaying) exoPlayer.play()
            } else {
                if (exoPlayer.isPlaying) exoPlayer.pause()
            }
            delay(50L)
            val next = playheadMs + 50L
            if (next >= totalDurationMs) {
                playheadMs = 0L
                isPlaying = false
                exoPlayer.pause()
                exoPlayer.seekTo(0L)
            } else {
                playheadMs = next
            }
        }
        if (!isPlaying && exoPlayer.isPlaying) {
            exoPlayer.pause()
        }
    }

    if (state.clips.isEmpty()) {
        // Pro Video Studio Start Hub
        VideoStudioLauncherHub(
            galleryVideos = galleryVideos,
            galleryPhotos = galleryPhotos,
            savedProjects = savedProjects,
            onPickVideo = { pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
            onPickPhoto = { pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onAddGalleryVideo = onAddExistingVideo,
            onAddGalleryPhoto = onAddExistingPhoto,
            onOpenProject = onOpenProject,
            onDeleteProject = onDeleteProject,
            modifier = modifier
        )
        return
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(
                        onClick = onStartFresh,
                        modifier = Modifier.testTag("video_editor_hub_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Studio Hub"
                        )
                    }
                },
                title = {
                    Column {
                        Text(
                            text = state.title.ifBlank { "Video Studio" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${state.clips.size} clip(s) • ${formatMillisPrecise(totalDurationMs)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onUndo, enabled = canUndo, modifier = Modifier.testTag("video_editor_undo")) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo")
                    }
                    IconButton(onClick = onRedo, enabled = canRedo, modifier = Modifier.testTag("video_editor_redo")) {
                        Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo")
                    }
                    IconButton(onClick = onSaveDraft, modifier = Modifier.testTag("video_editor_save_draft")) {
                        Icon(Icons.Rounded.Save, contentDescription = "Save Draft")
                    }
                    Button(
                        onClick = { showExportDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("video_editor_export_btn")
                    ) {
                        Icon(Icons.Rounded.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // 1. Live Multi-Layer Video Stage
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF19141C))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                val previewRatio = state.aspectRatio.ratio ?: (9f / 16f)
                val clipColorMatrix = remember(
                    currentClip?.filter,
                    currentClip?.brightness,
                    currentClip?.contrast,
                    currentClip?.saturation
                ) {
                    buildComposeVideoColorMatrix(currentClip)
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(previewRatio)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    // Main Video / Photo Clip Canvas
                    if (currentClip != null) {
                        val zoomScale = currentClip.cropZoom * if (currentClip.stabilize) 1.08f else 1f
                        val graphicsMod = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = zoomScale * if (currentClip.flipHorizontal) -1f else 1f
                                scaleY = zoomScale
                                rotationZ = currentClip.rotationDeg.toFloat()
                            }

                        if (currentClip.isImageClip) {
                            AsyncImage(
                                model = File(currentClip.sourcePath),
                                contentDescription = "Photo slide",
                                contentScale = ContentScale.Crop,
                                colorFilter = ColorFilter.colorMatrix(clipColorMatrix),
                                modifier = graphicsMod
                            )
                        } else {
                            Box(modifier = graphicsMod) {
                                AndroidView(
                                    factory = { ctx ->
                                        PlayerView(ctx).apply {
                                            useController = false
                                            player = exoPlayer
                                            layoutParams = FrameLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                ViewGroup.LayoutParams.MATCH_PARENT
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Live tint badge for color-grade preview over SurfaceView
                                if (currentClip.filter.tintAlpha > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(currentClip.filter.tintArgb).copy(alpha = currentClip.filter.tintAlpha))
                                    )
                                }
                            }
                        }
                    }

                    // V2: Live Keyframed Picture-in-Picture Overlays
                    state.pipOverlays.forEach { pip ->
                        if (playheadMs in pip.startMs..pip.endMs) {
                            val span = (pip.endMs - pip.startMs).coerceAtLeast(1L).toFloat()
                            val p = ((playheadMs - pip.startMs) / span).coerceIn(0f, 1f)
                            val scaleVal = pip.startScale + (pip.endScale - pip.startScale) * p
                            val rotVal = pip.startRotationDeg + (pip.endRotationDeg - pip.startRotationDeg) * p
                            val alphaVal = (pip.startOpacity + (pip.endOpacity - pip.startOpacity) * p).coerceIn(0f, 1f)
                            val cxFrac = pip.startX + (pip.endX - pip.startX) * p
                            val cyFrac = pip.startY + (pip.endY - pip.startY) * p

                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .graphicsLayer {
                                        translationX = cxFrac * 180f
                                        translationY = cyFrac * 260f
                                        rotationZ = rotVal
                                        alpha = alphaVal
                                    }
                                    .size((150f * scaleVal.coerceIn(0.2f, 0.85f)).dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        1.5.dp,
                                        if (pip.chromaKeyEnabled) Color(0xFF66BB6A) else Color.White,
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = File(pip.sourcePath),
                                    contentDescription = pip.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (pip.chromaKeyEnabled) {
                                    Surface(
                                        color = Color(0xCC2E7D32),
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    ) {
                                        Text("Chroma Key", color = Color.White, fontSize = 8.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // T1: Stickers
                    state.stickerOverlays.forEach { stk ->
                        if (playheadMs in stk.startMs..stk.endMs) {
                            val model = CreativeLibraryCatalog.stickerById(stk.stickerId)
                            if (model != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 18.dp)
                                        .scale(stk.scale)
                                        .rotate(stk.rotationDeg)
                                        .alpha(stk.opacity)
                                ) {
                                    StickerVisual(sticker = model, iconSize = 28.dp)
                                }
                            }
                        }
                    }

                    // T1: Text, Animated Captions & Subtitles
                    state.textOverlays.forEach { txt ->
                        if (playheadMs in txt.startMs..txt.endMs && txt.text.isNotBlank()) {
                            val font = remember(txt.fontId) {
                                EditorFontOption.fromId(txt.fontId).toComposeFontFamily()
                            }
                            val localMs = (playheadMs - txt.startMs).coerceAtLeast(0L)
                            val animP = (localMs / 420f).coerceIn(0f, 1f)
                            val shownText = if (txt.animationStyle == CaptionAnimationStyle.TYPEWRITER) {
                                val chars = (txt.text.length * animP).toInt().coerceIn(1, txt.text.length)
                                txt.text.take(chars)
                            } else txt.text

                            val animAlpha = if (txt.animationStyle == CaptionAnimationStyle.FADE_IN) animP else 1f
                            val animScale = when (txt.animationStyle) {
                                CaptionAnimationStyle.POP_SCALE -> 0.7f + 0.3f * animP
                                CaptionAnimationStyle.PULSE -> 1f + 0.05f * sin(localMs / 160.0).toFloat()
                                else -> 1f
                            }

                            Box(
                                modifier = Modifier
                                    .align(if (txt.isSubtitle) Alignment.BottomCenter else Alignment.Center)
                                    .padding(14.dp)
                                    .scale(animScale)
                                    .alpha(animAlpha)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(txt.bgColorArgb))
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = shownText,
                                    fontFamily = font,
                                    fontSize = (txt.fontSizeSp * 0.65f).sp,
                                    color = Color(txt.colorArgb),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // 2. Transport Controls & Playhead Scrubber
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.testTag("video_editor_play_pause")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "${formatMillisPrecise(playheadMs)} / ${formatMillisPrecise(totalDurationMs)}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Slider(
                        value = playheadMs.toFloat(),
                        onValueChange = { v ->
                            isPlaying = false
                            playheadMs = v.toLong().coerceIn(0L, totalDurationMs)
                            val clipOffset = activeClipInfo?.third ?: 0L
                            if (currentClip != null && !currentClip.isImageClip) {
                                exoPlayer.seekTo((clipOffset * currentClip.speed).toLong().coerceAtLeast(0L))
                            }
                        },
                        valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(100f),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onSplitAtPlayhead(playheadMs) },
                        modifier = Modifier.testTag("video_editor_split_quick")
                    ) {
                        Icon(Icons.Rounded.ContentCut, contentDescription = "Split at playhead")
                    }
                    IconButton(
                        onClick = { showGalleryPickerDialog = true },
                        modifier = Modifier.testTag("video_editor_add_clip_quick")
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add clip or photo")
                    }
                }
            }

            // 3. Multi-Track Timeline (V2 PiP, T1 Text/Stickers, V1 Clips + Transitions, A1 Audio)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        // Track V2: PiP Overlays
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TrackBadge("V2")
                            if (state.pipOverlays.isEmpty()) {
                                Text("No PiP overlays", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                state.pipOverlays.forEach { pip ->
                                    FilterChip(
                                        selected = selectedPipId == pip.id,
                                        onClick = { selectedPipId = pip.id; activeTab = VideoToolTab.PIP },
                                        label = { Text("${pip.title} (${formatMillisPrecise(pip.startMs)})", fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Track T1: Captions, Subtitles & Stickers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TrackBadge("T1")
                            if (state.textOverlays.isEmpty() && state.stickerOverlays.isEmpty()) {
                                Text("Add text, subtitles & stickers", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                state.textOverlays.forEach { txt ->
                                    FilterChip(
                                        selected = selectedTextId == txt.id,
                                        onClick = { selectedTextId = txt.id; activeTab = VideoToolTab.TEXT },
                                        label = {
                                            Text(
                                                "${if (txt.isSubtitle) "SUB" else "TXT"}: ${txt.text.take(12)}",
                                                fontSize = 11.sp
                                            )
                                        }
                                    )
                                }
                                state.stickerOverlays.forEach { stk ->
                                    val model = CreativeLibraryCatalog.stickerById(stk.stickerId)
                                    Surface(
                                        onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(stickerOverlays = st.stickerOverlays.filterNot { it.id == stk.id })
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = "${model?.glyph ?: "✨"} ×",
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Track V1: Video Clips & Transitions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TrackBadge("V1")
                            state.clips.forEachIndexed { idx, clip ->
                                val isSel = selectedClip?.id == clip.id
                                val blockWidth = ((clip.effectiveDurationMs / 1000f) * 24f).coerceIn(72f, 220f).dp
                                Card(
                                    onClick = { selectedClipId = clip.id },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    ),
                                    modifier = Modifier
                                        .width(blockWidth)
                                        .height(48.dp)
                                        .border(
                                            width = if (isSel) 2.dp else 1.dp,
                                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        .testTag("timeline_clip_${idx}")
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (clip.thumbnailPath.isNotBlank() && File(clip.thumbnailPath).exists()) {
                                            AsyncImage(
                                                model = File(clip.thumbnailPath),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(38.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                            )
                                        } else {
                                            Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(22.dp))
                                        }
                                        Column {
                                            Text(
                                                text = formatMillisPrecise(clip.effectiveDurationMs),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = buildString {
                                                    if (clip.speed != 1f) append("${clip.speed}x ")
                                                    if (clip.isReversed) append("REV")
                                                    if (isEmpty()) append(if (clip.isImageClip) "Photo" else "Clip ${idx + 1}")
                                                },
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                if (idx < state.clips.lastIndex) {
                                    Surface(
                                        onClick = {
                                            selectedClipId = clip.id
                                            activeTab = VideoToolTab.TRANSITIONS
                                        },
                                        shape = CircleShape,
                                        color = if (clip.transitionAfter != VideoTransitionType.NONE) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Rounded.SwapHoriz,
                                                contentDescription = "Transition",
                                                tint = if (clip.transitionAfter != VideoTransitionType.NONE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Track A1: Music, Sound FX & Voiceovers
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            TrackBadge("A1")
                            if (state.audioTracks.isEmpty()) {
                                Text("Add music, sound effects or voiceover", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            } else {
                                state.audioTracks.forEach { aud ->
                                    FilterChip(
                                        selected = selectedAudioId == aud.id,
                                        onClick = { selectedAudioId = aud.id; activeTab = VideoToolTab.AUDIO },
                                        label = {
                                            Text(
                                                text = "${if (aud.isMuted) "🔇" else "🎵"} ${aud.title}",
                                                fontSize = 11.sp
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Active Tool Controls Panel
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    when (activeTab) {
                        VideoToolTab.CLIP -> {
                            val clip = selectedClip
                            val clipIdx = state.clips.indexOfFirst { it.id == clip?.id }
                            if (clip != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { onSplitAtPlayhead(playheadMs) },
                                            modifier = Modifier.weight(1f).testTag("video_split_button")
                                        ) {
                                            Icon(Icons.Rounded.ContentCut, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Split", maxLines = 1)
                                        }
                                        OutlinedButton(
                                            onClick = { if (clipIdx > 0) onMoveClip(clipIdx, clipIdx - 1) },
                                            enabled = clipIdx > 0
                                        ) {
                                            Icon(Icons.Rounded.ArrowBack, contentDescription = "Move Left", modifier = Modifier.size(16.dp))
                                        }
                                        OutlinedButton(
                                            onClick = { if (clipIdx < state.clips.lastIndex) onMoveClip(clipIdx, clipIdx + 1) },
                                            enabled = clipIdx in 0 until state.clips.lastIndex
                                        ) {
                                            Icon(Icons.Rounded.ArrowForward, contentDescription = "Move Right", modifier = Modifier.size(16.dp))
                                        }
                                        OutlinedButton(
                                            onClick = { showGalleryPickerDialog = true },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Merge", maxLines = 1)
                                        }
                                        IconButton(
                                            onClick = {
                                                if (state.clips.size > 1) {
                                                    onUpdateState(true) { st -> st.copy(clips = st.clips.filterNot { it.id == clip.id }) }
                                                }
                                            },
                                            enabled = state.clips.size > 1
                                        ) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete clip", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }

                                    if (!clip.isImageClip) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Trim In", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(54.dp))
                                            Slider(
                                                value = clip.trimStartMs.toFloat(),
                                                onValueChange = { v ->
                                                    val nextStart = v.toLong().coerceIn(0L, (clip.trimEndMs - 400L).coerceAtLeast(0L))
                                                    onUpdateState(false) { st ->
                                                        st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(trimStartMs = nextStart) else it })
                                                    }
                                                },
                                                valueRange = 0f..clip.sourceDurationMs.toFloat().coerceAtLeast(500f),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(formatMillisPrecise(clip.trimStartMs), style = MaterialTheme.typography.labelSmall)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Trim Out", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(54.dp))
                                            Slider(
                                                value = clip.trimEndMs.toFloat(),
                                                onValueChange = { v ->
                                                    val nextEnd = v.toLong().coerceIn((clip.trimStartMs + 400L).coerceAtMost(clip.sourceDurationMs), clip.sourceDurationMs)
                                                    onUpdateState(false) { st ->
                                                        st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(trimEndMs = nextEnd) else it })
                                                    }
                                                },
                                                valueRange = 0f..clip.sourceDurationMs.toFloat().coerceAtLeast(500f),
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(formatMillisPrecise(clip.trimEndMs), style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.SPEED -> {
                            val clip = selectedClip
                            if (clip != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 3.0f, 4.0f)) { spd ->
                                            FilterChip(
                                                selected = kotlin.math.abs(clip.speed - spd) < 0.04f,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(speed = spd) else it })
                                                    }
                                                },
                                                label = { Text("${spd}x") },
                                                modifier = Modifier.testTag("video_speed_${spd}")
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Reverse Video Clip", style = MaterialTheme.typography.titleSmall)
                                            Text("Plays selected clip backwards cleanly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Switch(
                                            checked = clip.isReversed,
                                            enabled = !clip.isImageClip,
                                            onCheckedChange = { rev ->
                                                onUpdateState(true) { st ->
                                                    st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(isReversed = rev) else it })
                                                }
                                            },
                                            modifier = Modifier.testTag("video_reverse_switch")
                                        )
                                    }
                                }
                            }
                        }

                        VideoToolTab.COLOR -> {
                            val clip = selectedClip
                            if (clip != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(VideoColorFilter.entries) { flt ->
                                            FilterChip(
                                                selected = clip.filter == flt,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(filter = flt) else it })
                                                    }
                                                },
                                                label = { Text(flt.label) }
                                            )
                                        }
                                    }
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        item {
                                            VideoSliderChip("Brightness", clip.brightness, -0.4f..0.4f) { v ->
                                                onUpdateState(false) { st ->
                                                    st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(brightness = v) else it })
                                                }
                                            }
                                        }
                                        item {
                                            VideoSliderChip("Contrast", clip.contrast, 0.5f..1.8f) { v ->
                                                onUpdateState(false) { st ->
                                                    st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(contrast = v) else it })
                                                }
                                            }
                                        }
                                        item {
                                            VideoSliderChip("Saturation", clip.saturation, 0f..2f) { v ->
                                                onUpdateState(false) { st ->
                                                    st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(saturation = v) else it })
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.TRANSFORM -> {
                            val clip = selectedClip
                            if (clip != null) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(VideoAspectRatio.entries) { ar ->
                                            FilterChip(
                                                selected = state.aspectRatio == ar,
                                                onClick = { onUpdateState(true) { it.copy(aspectRatio = ar) } },
                                                label = { Text(ar.label) }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(rotationDeg = (it.rotationDeg + 90) % 360) else it })
                                            }
                                        }) {
                                            Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Rotate")
                                        }
                                        OutlinedButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(flipHorizontal = !it.flipHorizontal) else it })
                                            }
                                        }) {
                                            Icon(Icons.Rounded.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Flip")
                                        }
                                        FilterChip(
                                            selected = clip.stabilize,
                                            onClick = {
                                                onUpdateState(true) { st ->
                                                    st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(stabilize = !it.stabilize) else it })
                                                }
                                            },
                                            label = { Text("Stabilize") }
                                        )
                                    }
                                }
                            }
                        }

                        VideoToolTab.TEXT -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            textDraft = ""
                                            textIsSubtitle = false
                                            showTextDialog = true
                                        },
                                        modifier = Modifier.weight(1f).testTag("video_add_caption_btn")
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Animated Caption")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            textDraft = ""
                                            textIsSubtitle = true
                                            showTextDialog = true
                                        },
                                        modifier = Modifier.weight(1f).testTag("video_add_subtitle_btn")
                                    ) {
                                        Text("Add Subtitle")
                                    }
                                }
                                val selText = state.textOverlays.firstOrNull { it.id == selectedTextId }
                                if (selText != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Selected: \"${selText.text.take(18)}\"", style = MaterialTheme.typography.labelMedium)
                                        TextButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(textOverlays = st.textOverlays.filterNot { it.id == selText.id })
                                            }
                                            selectedTextId = null
                                        }) {
                                            Text("Remove", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.STICKERS -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Stickers & Overlays", style = MaterialTheme.typography.titleSmall)
                                    Button(
                                        onClick = { showStickerLibrary = true },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Browse 120 Stickers")
                                    }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(CreativeLibraryCatalog.stickers.take(16), key = { it.id }) { stk ->
                                        Surface(
                                            onClick = {
                                                val end = (playheadMs + 3500L).coerceAtMost(totalDurationMs)
                                                val item = VideoStickerOverlayItem(
                                                    stickerId = stk.id,
                                                    startMs = playheadMs,
                                                    endMs = end.coerceAtLeast(playheadMs + 500L)
                                                )
                                                onUpdateState(true) { st -> st.copy(stickerOverlays = st.stickerOverlays + item) }
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                        ) {
                                            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                                StickerVisual(sticker = stk, iconSize = 24.dp, showLabel = true)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.AUDIO -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { pickAudioLauncher.launch(arrayOf("audio/*")) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.MusicNote, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Import Audio", maxLines = 1)
                                    }

                                    Button(
                                        onClick = {
                                            if (isRecordingVoiceover) {
                                                val res = recorder.stopRecording()
                                                isRecordingVoiceover = false
                                                if (res != null) {
                                                    onAddVoiceover(res.first, res.second, playheadMs)
                                                }
                                            } else {
                                                if (hasMicPermission) {
                                                    runCatching {
                                                        recorder.startRecording(audioDir)
                                                        isRecordingVoiceover = true
                                                        voiceoverSeconds = 0L
                                                    }
                                                } else {
                                                    micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                                }
                                            }
                                        },
                                        colors = if (isRecordingVoiceover) ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)) else ButtonDefaults.buttonColors(),
                                        modifier = Modifier.weight(1f).testTag("video_voiceover_button")
                                    ) {
                                        Icon(
                                            if (isRecordingVoiceover) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (isRecordingVoiceover) "Stop (${voiceoverSeconds}s)" else "Voiceover")
                                    }
                                }

                                // Offline royalty-free Music & SFX
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(OfflineAudioLibrary.catalog, key = { it.id }) { asset ->
                                        FilterChip(
                                            selected = false,
                                            onClick = { onAddBuiltInAudio(asset.id, playheadMs) },
                                            label = {
                                                Text(
                                                    text = if (asset.kind == AudioTrackKind.MUSIC) "🎵 ${asset.title}" else "✨ ${asset.title}",
                                                    fontSize = 11.sp
                                                )
                                            }
                                        )
                                    }
                                }

                                val selAudio = state.audioTracks.firstOrNull { it.id == selectedAudioId } ?: state.audioTracks.lastOrNull()
                                if (selAudio != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("${selAudio.title} Vol", style = MaterialTheme.typography.labelSmall)
                                        Slider(
                                            value = selAudio.volume,
                                            onValueChange = { vol ->
                                                onUpdateState(false) { st ->
                                                    st.copy(audioTracks = st.audioTracks.map { if (it.id == selAudio.id) it.copy(volume = vol) else it })
                                                }
                                            },
                                            valueRange = 0f..1.5f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(audioTracks = st.audioTracks.map { if (it.id == selAudio.id) it.copy(isMuted = !it.isMuted) else it })
                                            }
                                        }) {
                                            Icon(
                                                if (selAudio.isMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                                contentDescription = "Mute toggle",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(audioTracks = st.audioTracks.filterNot { it.id == selAudio.id })
                                            }
                                            selectedAudioId = null
                                        }) {
                                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Remove audio", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.PIP -> {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            pickPipPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+ PiP Photo")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            pickPipVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("+ PiP Video")
                                    }
                                }

                                val selPip = state.pipOverlays.firstOrNull { it.id == selectedPipId } ?: state.pipOverlays.lastOrNull()
                                if (selPip != null) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text("Chroma Key (Green Screen)", style = MaterialTheme.typography.labelMedium)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Switch(
                                                checked = selPip.chromaKeyEnabled,
                                                onCheckedChange = { ck ->
                                                    onUpdateState(true) { st ->
                                                        st.copy(pipOverlays = st.pipOverlays.map { if (it.id == selPip.id) it.copy(chromaKeyEnabled = ck) else it })
                                                    }
                                                }
                                            )
                                        }
                                        TextButton(onClick = {
                                            onUpdateState(true) { st -> st.copy(pipOverlays = st.pipOverlays.filterNot { it.id == selPip.id }) }
                                            selectedPipId = null
                                        }) {
                                            Text("Delete PiP", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                    // Keyframe Animation Presets + End Scale slider
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        item {
                                            FilterChip(
                                                selected = false,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(
                                                            pipOverlays = st.pipOverlays.map {
                                                                if (it.id == selPip.id) {
                                                                    it.copy(startX = 0.25f, startY = 0.3f, endX = 0.78f, endY = 0.3f, startScale = 0.32f, endScale = 0.38f)
                                                                } else it
                                                            }
                                                        )
                                                    }
                                                },
                                                label = { Text("Keyframe Slide L→R") }
                                            )
                                        }
                                        item {
                                            FilterChip(
                                                selected = false,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(
                                                            pipOverlays = st.pipOverlays.map {
                                                                if (it.id == selPip.id) {
                                                                    it.copy(startScale = 0.22f, endScale = 0.50f, startOpacity = 0.3f, endOpacity = 1f)
                                                                } else it
                                                            }
                                                        )
                                                    }
                                                },
                                                label = { Text("Keyframe Zoom-In") }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        VideoToolTab.TRANSITIONS -> {
                            val clip = selectedClip
                            if (clip != null) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)) {
                                    Text(
                                        "Transition After Selected Clip",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(VideoTransitionType.entries) { tr ->
                                            FilterChip(
                                                selected = clip.transitionAfter == tr,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(clips = st.clips.map { if (it.id == clip.id) it.copy(transitionAfter = tr) else it })
                                                    }
                                                },
                                                label = { Text(tr.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Tool Selector Bar
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(VideoToolTab.entries) { tab ->
                            val selected = activeTab == tab
                            Surface(
                                onClick = { activeTab = tab },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                modifier = Modifier.testTag("video_tab_${tab.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = tab.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Clip / Photo Merge Picker Dialog
    if (showGalleryPickerDialog) {
        AlertDialog(
            onDismissRequest = { showGalleryPickerDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Add Media to Timeline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                showGalleryPickerDialog = false
                                pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Device Video") }
                        OutlinedButton(
                            onClick = {
                                showGalleryPickerDialog = false
                                pickPhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Device Photo") }
                    }
                    if (galleryVideos.isNotEmpty() || galleryPhotos.isNotEmpty()) {
                        Text("Or tap from your Pose & Memories library:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(galleryVideos.take(10), key = { "gv_${it.id}" }) { vid ->
                                Card(
                                    onClick = {
                                        onAddExistingVideo(vid)
                                        showGalleryPickerDialog = false
                                    },
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (vid.thumbnailPath.isNotBlank() && File(vid.thumbnailPath).exists()) {
                                            AsyncImage(model = File(vid.thumbnailPath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        }
                                        Icon(Icons.Rounded.Videocam, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                            items(galleryPhotos.take(10), key = { "gp_${it.id}" }) { ph ->
                                Card(
                                    onClick = {
                                        onAddExistingPhoto(ph)
                                        showGalleryPickerDialog = false
                                    },
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    AsyncImage(model = File(ph.filePath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showGalleryPickerDialog = false }) { Text("Close") }
            }
        )
    }

    // Sticker Library Browser Dialog
    if (showStickerLibrary) {
        TemplateStickerLibraryDialog(
            initialTabIndex = 1,
            showTemplatesTab = false,
            favoriteTemplateIds = emptySet(),
            favoriteStickerIds = favoriteStickerIds,
            onToggleFavoriteTemplate = {},
            onToggleFavoriteSticker = onToggleFavoriteSticker,
            onSelectSticker = { stk ->
                val end = (playheadMs + 3500L).coerceAtMost(totalDurationMs)
                val item = VideoStickerOverlayItem(
                    stickerId = stk.id,
                    startMs = playheadMs,
                    endMs = end.coerceAtLeast(playheadMs + 500L)
                )
                onUpdateState(true) { st -> st.copy(stickerOverlays = st.stickerOverlays + item) }
            },
            onDismiss = { showStickerLibrary = false }
        )
    }

    // Animated Caption & Subtitle Dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(if (textIsSubtitle) "Add Timed Subtitle" else "Add Animated Caption") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = textDraft,
                        onValueChange = { textDraft = it },
                        label = { Text(if (textIsSubtitle) "Subtitle line" else "Caption text") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Custom Font", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(EditorFontOption.entries) { fontOpt ->
                            val family = remember(fontOpt.id) { fontOpt.toComposeFontFamily() }
                            FilterChip(
                                selected = textFontId == fontOpt.id,
                                onClick = { textFontId = fontOpt.id },
                                label = { Text(fontOpt.displayName, fontFamily = family) }
                            )
                        }
                    }
                    Text("Animation Style", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(CaptionAnimationStyle.entries) { anim ->
                            FilterChip(
                                selected = textAnimStyle == anim,
                                onClick = { textAnimStyle = anim },
                                label = { Text(anim.label) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textDraft.isNotBlank()) {
                            val end = (playheadMs + 3500L).coerceAtMost(totalDurationMs)
                            val overlay = VideoTextOverlayItem(
                                text = textDraft,
                                isSubtitle = textIsSubtitle,
                                startMs = playheadMs,
                                endMs = end.coerceAtLeast(playheadMs + 600L),
                                yFraction = if (textIsSubtitle) 0.84f else 0.52f,
                                fontId = textFontId,
                                animationStyle = textAnimStyle
                            )
                            onUpdateState(true) { st -> st.copy(textOverlays = st.textOverlays + overlay) }
                        }
                        showTextDialog = false
                    }
                ) { Text("Add to Timeline") }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Export Resolution & Progress Dialog
    if (showExportDialog || exportState !is VideoExportState.Idle) {
        AlertDialog(
            onDismissRequest = {
                if (exportState is VideoExportState.Idle || exportState is VideoExportState.Completed || exportState is VideoExportState.Error) {
                    showExportDialog = false
                    onResetExportState()
                }
            },
            shape = RoundedCornerShape(26.dp),
            title = {
                Text(
                    when (exportState) {
                        is VideoExportState.Idle -> "Export Video Copy"
                        is VideoExportState.Preparing -> "Preparing Studio Export"
                        is VideoExportState.Exporting -> "Rendering ${selectedExportRes.shortLabel} MP4"
                        is VideoExportState.Completed -> "Export Complete!"
                        is VideoExportState.Error -> "Export Failed"
                    }
                )
            },
            text = {
                when (exportState) {
                    is VideoExportState.Idle -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Your original videos are untouched. Choose export resolution:",
                                style = MaterialTheme.typography.bodySmall
                            )
                            supportedResolutions.forEach { info ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable(enabled = info.supported) {
                                            selectedExportRes = info.resolution
                                        }
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = selectedExportRes == info.resolution,
                                        enabled = info.supported,
                                        onClick = { if (info.supported) selectedExportRes = info.resolution }
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(info.resolution.label, style = MaterialTheme.typography.titleSmall)
                                        Text(info.note, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Save to Device Movies", style = MaterialTheme.typography.bodyMedium)
                                Switch(checked = saveExportToGallery, onCheckedChange = { saveExportToGallery = it })
                            }
                        }
                    }
                    is VideoExportState.Preparing -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(exportState.stage, style = MaterialTheme.typography.bodyMedium)
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                    is VideoExportState.Exporting -> {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(exportState.stage, style = MaterialTheme.typography.bodyMedium)
                                Text("${exportState.progressPercent}%", fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { exportState.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    is VideoExportState.Completed -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Saved a brand-new copy to your Video Gallery" +
                                    if (exportState.savedToGallery) " & Movies folder!" else "!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is VideoExportState.Error -> {
                        Text(exportState.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                when (exportState) {
                    is VideoExportState.Idle -> {
                        Button(
                            onClick = {
                                showExportDialog = false
                                onExportVideo(selectedExportRes, saveExportToGallery)
                            },
                            modifier = Modifier.testTag("video_confirm_export_btn")
                        ) {
                            Text("Start Export")
                        }
                    }
                    is VideoExportState.Completed -> {
                        Button(onClick = {
                            showExportDialog = false
                            onResetExportState()
                            onOpenVideoGallery()
                        }) {
                            Text("Open Video Gallery")
                        }
                    }
                    is VideoExportState.Error -> {
                        Button(onClick = {
                            showExportDialog = false
                            onResetExportState()
                        }) { Text("Close") }
                    }
                    else -> Unit
                }
            },
            dismissButton = {
                when (exportState) {
                    is VideoExportState.Preparing, is VideoExportState.Exporting -> {
                        TextButton(onClick = {
                            onCancelExport()
                            showExportDialog = false
                        }) { Text("Cancel Export") }
                    }
                    is VideoExportState.Completed -> {
                        TextButton(onClick = {
                            showExportDialog = false
                            onResetExportState()
                        }) { Text("Keep Editing") }
                    }
                    is VideoExportState.Idle -> {
                        TextButton(onClick = { showExportDialog = false }) { Text("Close") }
                    }
                    else -> Unit
                }
            }
        )
    }
}

@Composable
private fun VideoStudioLauncherHub(
    galleryVideos: List<VideoEntity>,
    galleryPhotos: List<PhotoEntity>,
    savedProjects: List<EditProjectEntity>,
    onPickVideo: () -> Unit,
    onPickPhoto: () -> Unit,
    onAddGalleryVideo: (VideoEntity) -> Unit,
    onAddGalleryPhoto: (PhotoEntity) -> Unit,
    onOpenProject: (EditProjectEntity) -> Unit,
    onDeleteProject: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Pro Video Editor",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Multi-track timeline, speed & reverse, keyframes, chroma key, voiceovers & 4K export",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Rounded.MovieCreation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Start a New Film Project", style = MaterialTheme.typography.titleMedium)
                        Text("Non-destructive editing — originals always stay safe", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPickVideo,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("video_hub_import_video")
                    ) {
                        Icon(Icons.Rounded.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pick Video")
                    }
                    OutlinedButton(
                        onClick = onPickPhoto,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f).height(48.dp).testTag("video_hub_import_photo")
                    ) {
                        Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photo Slide")
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (savedProjects.isNotEmpty()) {
                item {
                    Text("Saved Draft Projects", style = MaterialTheme.typography.titleMedium)
                }
                items(savedProjects, key = { it.id }) { proj ->
                    Card(
                        onClick = { onOpenProject(proj) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.MovieCreation, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proj.title, style = MaterialTheme.typography.titleSmall)
                                Text("Tap to resume editing", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDeleteProject(proj.id) }) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete draft", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (galleryVideos.isNotEmpty()) {
                item {
                    Text("Edit From Your Video Gallery", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(galleryVideos, key = { it.id }) { vid ->
                            Card(
                                onClick = { onAddGalleryVideo(vid) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(110.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    if (vid.thumbnailPath.isNotBlank() && File(vid.thumbnailPath).exists()) {
                                        AsyncImage(model = File(vid.thumbnailPath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    }
                                    Surface(color = Color.Black.copy(alpha = 0.55f), shape = CircleShape, modifier = Modifier.size(36.dp)) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Rounded.PlayArrow, contentDescription = "Edit video", tint = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (galleryPhotos.isNotEmpty()) {
                item {
                    Text("Create Photo Slideshow Video", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(galleryPhotos.take(12), key = { it.id }) { ph ->
                            Card(
                                onClick = { onAddGalleryPhoto(ph) },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.size(96.dp)
                            ) {
                                AsyncImage(model = File(ph.filePath), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackBadge(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.width(30.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
}

@Composable
private fun VideoSliderChip(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.width(140.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(String.format(Locale.US, "%.2f", value), style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range
        )
    }
}

private fun buildComposeVideoColorMatrix(clip: VideoClipItem?): ColorMatrix {
    if (clip == null) return ColorMatrix()
    val c = (clip.contrast * clip.filter.contrast).coerceIn(0.4f, 2f)
    val s = (clip.saturation * clip.filter.saturation).coerceIn(0f, 2f)
    val b = ((clip.brightness + clip.filter.brightness) * 140f).coerceIn(-160f, 160f)

    val mat = ColorMatrix()
    mat.setToSaturation(s)
    val t = (1f - c) * 128f + b
    val cb = ColorMatrix(
        floatArrayOf(
            c, 0f, 0f, 0f, t,
            0f, c, 0f, 0f, t,
            0f, 0f, c, 0f, t,
            0f, 0f, 0f, 1f, 0f
        )
    )
    mat.timesAssign(cb)
    return mat
}

private fun formatMillisPrecise(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format(Locale.US, "%d:%02d", mins, secs)
}
