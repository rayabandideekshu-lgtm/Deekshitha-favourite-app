package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cameraswitch
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AlignmentGuidance
import com.example.data.model.PoseCategory
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseSuggestion
import com.example.ui.camera.MlKitPoseAnalyzer
import com.example.ui.components.ActivePoseDescriptionPill
import com.example.ui.components.AlignmentGuidanceChip
import com.example.ui.components.ChangePoseBottomSheet
import com.example.ui.components.OutlineCustomizeBottomSheet
import com.example.ui.components.WhiteLinePoseOverlay
import com.example.ui.viewmodel.PoseUiState
import kotlinx.coroutines.delay
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun VideoCameraScreen(
    selectedCategory: PoseCategory,
    activePose: PoseSuggestion,
    outlineConfig: PoseOutlineConfig,
    alignmentGuidance: AlignmentGuidance,
    poseUiState: PoseUiState,
    createTempVideoFile: () -> File,
    onSelectCategory: (PoseCategory) -> Unit,
    onGiveAnotherPose: () -> Unit,
    onNextPose: () -> Unit,
    onUseThisPose: (PoseSuggestion) -> Unit,
    onToggleVideoPoseGuide: (Boolean) -> Unit,
    onOutlineScaleChange: (Float) -> Unit,
    onOutlineOffsetChange: (Float) -> Unit,
    onOutlineAlphaChange: (Float) -> Unit,
    onResetOutline: () -> Unit,
    onToggleSaveVideoWithGuide: (Boolean) -> Unit,
    onAlignmentGuidance: (AlignmentGuidance) -> Unit,
    onVideoRecorded: (String, Long, Long) -> Unit,
    onImportPickedVideo: (Uri) -> Unit,
    onCloseCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasCameraPermission = results[Manifest.permission.CAMERA] == true
        hasAudioPermission = results[Manifest.permission.RECORD_AUDIO] == true
        permissionRequestedOnce = true
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImportPickedVideo(it) }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var torchEnabled by remember { mutableStateOf(false) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    var isRecording by remember { mutableStateOf(false) }
    var activeRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingSeconds by remember { mutableLongStateOf(0L) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    var showChangePoseSheet by remember { mutableStateOf(false) }
    var showSettingsSheet by remember { mutableStateOf(false) }

    val currentOutlineEnabled by rememberUpdatedState(outlineConfig.enabled)
    val currentLensFacing by rememberUpdatedState(lensFacing)
    val currentActivePose by rememberUpdatedState(activePose)
    val currentScale by rememberUpdatedState(outlineConfig.scale)

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val poseAnalyzer = remember {
        MlKitPoseAnalyzer(
            isEnabledProvider = { currentOutlineEnabled },
            isFrontCameraProvider = { currentLensFacing == CameraSelector.LENS_FACING_FRONT },
            outlineTypeProvider = { currentActivePose.outlineType },
            scaleProvider = { currentScale },
            onGuidance = onAlignmentGuidance
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            runCatching { activeRecording?.stop() }
            poseAnalyzer.close()
            analysisExecutor.shutdown()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0L
            while (isRecording) {
                delay(1000L)
                recordingSeconds++
            }
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val videoCapture = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(
                QualitySelector.from(
                    Quality.HIGHEST,
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                )
            )
            .build()
        VideoCapture.withOutput(recorder)
    }

    // Bind camera ONLY when lensFacing or lifecycleOwner changes; NEVER rebind when VIDEO POSE GUIDE toggles!
    if (hasCameraPermission) {
        DisposableEffect(lensFacing, lifecycleOwner) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)

            val listener = Runnable {
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(analysisExecutor, poseAnalyzer)
                        }
                    val selector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    cameraProvider.unbindAll()
                    val cam = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        videoCapture,
                        imageAnalysis
                    )
                    boundCamera = cam
                    hasFlashUnit = cam.cameraInfo.hasFlashUnit()
                    torchEnabled = false
                    cameraError = null
                } catch (_: Exception) {
                    cameraError = "Video camera preview unavailable on this device. You can still import a video."
                }
            }

            cameraProviderFuture.addListener(listener, executor)

            onDispose {
                runCatching {
                    cameraProviderFuture.get().unbindAll()
                }
            }
        }
    }

    fun startOrStopRecording() {
        val current = activeRecording
        if (current != null) {
            current.stop()
            activeRecording = null
            isRecording = false
            return
        }

        val tempFile = createTempVideoFile()
        val outputOptions = FileOutputOptions.Builder(tempFile).build()
        var pending = videoCapture.output.prepareRecording(context, outputOptions)
        if (hasAudioPermission) {
            pending = pending.withAudioEnabled()
        }

        val startedAt = System.currentTimeMillis()
        activeRecording = pending.start(ContextCompat.getMainExecutor(context)) { event ->
            when (event) {
                is VideoRecordEvent.Start -> {
                    isRecording = true
                }
                is VideoRecordEvent.Finalize -> {
                    isRecording = false
                    activeRecording = null
                    if (!event.hasError()) {
                        val recordedMs = event.recordingStats.recordedDurationNanos / 1_000_000L
                        onVideoRecorded(tempFile.absolutePath, recordedMs, startedAt)
                    } else {
                        cameraError = "Could not finish video recording (error ${event.error})."
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(84.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Videocam,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Video Camera Access Needed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Grant camera (and optional microphone) permission to record video memories with the Live White-Line Video Pose Guide.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("video_grant_permission_button")
                ) {
                    Text("Allow Camera & Mic Access")
                }
                if (permissionRequestedOnce) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Open App Settings")
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("video_pick_fallback_button")
                ) {
                    Icon(Icons.Rounded.VideoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Video from Device")
                }
            }
        } else {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            VideoCameraHudOverlay(
                activePose = activePose,
                outlineConfig = outlineConfig,
                alignmentGuidance = alignmentGuidance,
                isRecording = isRecording,
                recordingSeconds = recordingSeconds,
                hasFlashUnit = hasFlashUnit,
                torchEnabled = torchEnabled,
                cameraError = cameraError,
                onCloseCamera = onCloseCamera,
                onToggleTorch = {
                    val next = !torchEnabled
                    torchEnabled = next
                    boundCamera?.cameraControl?.enableTorch(next)
                },
                onFlipCamera = {
                    if (!isRecording) {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    }
                },
                onOpenPoseSettings = { showSettingsSheet = true },
                onOpenChangePose = { showChangePoseSheet = true },
                onToggleVideoPoseGuide = { onToggleVideoPoseGuide(!outlineConfig.enabled) },
                onPickVideo = {
                    videoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onToggleRecord = { startOrStopRecording() }
            )
        }
    }

    if (showChangePoseSheet) {
        ChangePoseBottomSheet(
            title = "Choose Video Pose Outline",
            selectedCategory = selectedCategory,
            activePoseId = activePose.id,
            poseUiState = poseUiState,
            onSelectCategory = onSelectCategory,
            onGiveAnotherPose = onGiveAnotherPose,
            onNextPose = onNextPose,
            onUseThisPose = onUseThisPose,
            onDismiss = { showChangePoseSheet = false }
        )
    }

    if (showSettingsSheet) {
        OutlineCustomizeBottomSheet(
            title = "Video Pose Settings",
            outlineToggleLabel = "Show Video Pose Outline",
            saveWithGuideLabel = "Save Video With Guide",
            config = outlineConfig,
            onToggleEnabled = onToggleVideoPoseGuide,
            onScaleChange = onOutlineScaleChange,
            onOffsetChange = onOutlineOffsetChange,
            onAlphaChange = onOutlineAlphaChange,
            onReset = onResetOutline,
            onToggleSaveWithGuide = onToggleSaveVideoWithGuide,
            onChangePoseClick = { showChangePoseSheet = true },
            onDismiss = { showSettingsSheet = false }
        )
    }
}

@Composable
fun VideoCameraHudOverlay(
    activePose: PoseSuggestion,
    outlineConfig: PoseOutlineConfig,
    alignmentGuidance: AlignmentGuidance,
    isRecording: Boolean,
    recordingSeconds: Long,
    hasFlashUnit: Boolean,
    torchEnabled: Boolean,
    cameraError: String?,
    onCloseCamera: () -> Unit,
    onToggleTorch: () -> Unit,
    onFlipCamera: () -> Unit,
    onOpenPoseSettings: () -> Unit,
    onOpenChangePose: () -> Unit,
    onToggleVideoPoseGuide: () -> Unit,
    onPickVideo: () -> Unit,
    onToggleRecord: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // White-line Pose Outline Overlay (toggles instantly without stopping video recording)
        AnimatedVisibility(
            visible = outlineConfig.enabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            WhiteLinePoseOverlay(
                outlineType = activePose.outlineType,
                scale = outlineConfig.scale,
                verticalOffsetFraction = outlineConfig.verticalOffsetFraction,
                alpha = outlineConfig.alpha,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("video_white_line_pose_overlay")
            )
        }

        // Top Bar: Close, Recording Timer Badge, Torch, Flip (disabled while recording), Pose Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onCloseCamera,
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.52f),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("video_camera_close")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close video camera", tint = Color.White)
                }
            }

            if (isRecording) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFFD32F2F).copy(alpha = 0.92f),
                    modifier = Modifier.testTag("video_recording_timer_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        val mins = recordingSeconds / 60
                        val secs = recordingSeconds % 60
                        Text(
                            text = String.format(Locale.US, "REC %02d:%02d", mins, secs),
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    onClick = onOpenPoseSettings,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.52f),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("video_pose_settings_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = "Video pose settings",
                            tint = Color.White
                        )
                    }
                }

                if (hasFlashUnit) {
                    Surface(
                        onClick = onToggleTorch,
                        shape = CircleShape,
                        color = if (torchEnabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        } else {
                            Color.Black.copy(alpha = 0.52f)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("video_torch_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (torchEnabled) Icons.Rounded.FlashOn else Icons.Rounded.FlashOff,
                                contentDescription = "Toggle flash light",
                                tint = Color.White
                            )
                        }
                    }
                }

                Surface(
                    onClick = onFlipCamera,
                    enabled = !isRecording,
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = if (isRecording) 0.28f else 0.52f),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("video_camera_flip")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Cameraswitch,
                            contentDescription = "Flip camera",
                            tint = Color.White.copy(alpha = if (isRecording) 0.5f else 1f)
                        )
                    }
                }
            }
        }

        if (outlineConfig.enabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 68.dp, start = 20.dp, end = 20.dp)
            ) {
                AlignmentGuidanceChip(guidance = alignmentGuidance)
            }
        }

        cameraError?.let { err ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.94f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(onClick = onPickVideo) {
                        Text("Import Video Instead")
                    }
                }
            }
        }

        // Bottom Controls: Active Pose Card + Prominent VIDEO POSE GUIDE toggle + Record button
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Color.Black.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ActivePoseDescriptionPill(pose = activePose)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onToggleVideoPoseGuide,
                    shape = RoundedCornerShape(50),
                    color = if (outlineConfig.enabled) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.94f)
                    } else {
                        Color.White.copy(alpha = 0.18f)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("video_pose_guide_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccessibilityNew,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (outlineConfig.enabled) {
                                "VIDEO POSE GUIDE: ON"
                            } else {
                                "VIDEO POSE GUIDE: OFF"
                            },
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    onClick = onOpenChangePose,
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("video_change_pose_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Change Pose",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPickVideo,
                    enabled = !isRecording,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                        .testTag("video_import_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VideoLibrary,
                        contentDescription = "Import video",
                        tint = Color.White
                    )
                }

                // Video Record / Stop Button
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (isRecording) Color(0xFFD32F2F) else Color(0xFFFF5252)
                        )
                        .clickable { onToggleRecord() }
                        .testTag("video_record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        Icon(
                            imageVector = Icons.Rounded.Stop,
                            contentDescription = "Stop recording",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenPoseSettings,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f))
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Adjust video pose guide",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
