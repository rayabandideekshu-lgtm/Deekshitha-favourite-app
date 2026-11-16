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
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
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
import androidx.compose.material.icons.rounded.FlashAuto
import androidx.compose.material.icons.rounded.FlashOff
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    selectedCategory: PoseCategory,
    activePose: PoseSuggestion,
    outlineConfig: PoseOutlineConfig,
    alignmentGuidance: AlignmentGuidance,
    poseUiState: PoseUiState,
    defaultTimerSeconds: Int,
    createTempFile: () -> File,
    onSelectCategory: (PoseCategory, Boolean) -> Unit,
    onGiveAnotherPose: (Boolean) -> Unit,
    onNextPose: (Boolean) -> Unit,
    onUseThisPose: (PoseSuggestion) -> Unit,
    onToggleOutlineEnabled: (Boolean) -> Unit,
    onOutlineScaleChange: (Float) -> Unit,
    onOutlineOffsetChange: (Float) -> Unit,
    onOutlineAlphaChange: (Float) -> Unit,
    onResetOutline: () -> Unit,
    onToggleSavePhotoWithGuide: (Boolean) -> Unit,
    onAlignmentGuidance: (AlignmentGuidance) -> Unit,
    onPhotoCaptured: (String, Long) -> Unit,
    onImportPickedPhoto: (Uri) -> Unit,
    onCloseCamera: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var permissionRequestedOnce by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        permissionRequestedOnce = true
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onImportPickedPhoto(it) }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var hasFlashUnit by remember { mutableStateOf(false) }
    var timerSeconds by remember(defaultTimerSeconds) { mutableIntStateOf(defaultTimerSeconds) }
    var countdownRemaining by remember { mutableIntStateOf(0) }
    var isCapturing by remember { mutableStateOf(false) }
    var cameraError by remember { mutableStateOf<String?>(null) }

    var showChangePoseSheet by remember { mutableStateOf(false) }
    var showCustomizeSheet by remember { mutableStateOf(false) }

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
            poseAnalyzer.close()
            analysisExecutor.shutdown()
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }

    fun executeCapture() {
        if (isCapturing) return
        isCapturing = true
        cameraError = null
        val outputFile = createTempFile()
        val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()
        imageCapture.flashMode = if (hasFlashUnit) flashMode else ImageCapture.FLASH_MODE_OFF

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    onPhotoCaptured(outputFile.absolutePath, System.currentTimeMillis())
                }

                override fun onError(exception: ImageCaptureException) {
                    isCapturing = false
                    cameraError = exception.localizedMessage ?: "Failed to capture photograph."
                }
            }
        )
    }

    fun triggerShutterWithTimer() {
        if (isCapturing || countdownRemaining > 0) return
        if (timerSeconds <= 0) {
            executeCapture()
        } else {
            scope.launch {
                for (sec in timerSeconds downTo 1) {
                    countdownRemaining = sec
                    delay(1000L)
                }
                countdownRemaining = 0
                executeCapture()
            }
        }
    }

    if (hasCameraPermission) {
        DisposableEffect(lensFacing, lifecycleOwner) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            val executor = ContextCompat.getMainExecutor(context)
            var boundCamera: Camera? = null

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
                    boundCamera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageCapture,
                        imageAnalysis
                    )
                    hasFlashUnit = boundCamera?.cameraInfo?.hasFlashUnit() == true
                    cameraError = null
                } catch (_: Exception) {
                    cameraError = "Camera preview unavailable on this device. You can still import a photo from your library."
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
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Camera Access Needed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Pose & Memories needs camera permission to show the Live Pose Outline Guide and capture high-resolution photographs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(22.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("camera_grant_permission_button")
                ) {
                    Text("Allow Camera Access")
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
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("camera_pick_photo_fallback")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Photo from Device")
                }
            }
        } else {
            // MIDDLE LAYER: Live CameraX Preview + Live White-Line Pose Outline Guide
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

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
                        .testTag("photo_white_line_pose_overlay")
                )
            }

            // TOP BAR: Close, Flash, Flip, Customize Outline
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
                        .testTag("camera_close_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Close camera",
                            tint = Color.White
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        onClick = { showCustomizeSheet = true },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.52f),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("camera_customize_outline_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = "Customize pose outline",
                                tint = Color.White
                            )
                        }
                    }

                    if (hasFlashUnit) {
                        Surface(
                            onClick = {
                                flashMode = when (flashMode) {
                                    ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                    ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                                    else -> ImageCapture.FLASH_MODE_AUTO
                                }
                            },
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.52f),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("camera_flash_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val flashIcon = when (flashMode) {
                                    ImageCapture.FLASH_MODE_ON -> Icons.Rounded.FlashOn
                                    ImageCapture.FLASH_MODE_OFF -> Icons.Rounded.FlashOff
                                    else -> Icons.Rounded.FlashAuto
                                }
                                Icon(
                                    imageVector = flashIcon,
                                    contentDescription = "Toggle flash",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Surface(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.52f),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("camera_flip_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Cameraswitch,
                                contentDescription = "Flip camera",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Alignment Guidance right under Top Bar when Pose Outline is ON
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

            // Countdown Number Overlay
            AnimatedVisibility(
                visible = countdownRemaining > 0,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f))
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$countdownRemaining",
                        color = Color.White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Hardware fallback banner
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
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Text("Choose Photo Instead")
                        }
                    }
                }
            }

            // BOTTOM: Pose Description + Visible POSE OUTLINE: ON/OFF Toggle + Change Pose + Capture + Timer
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

                // Instant POSE OUTLINE: ON / OFF toggle + Change Pose
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { onToggleOutlineEnabled(!outlineConfig.enabled) },
                        shape = RoundedCornerShape(50),
                        color = if (outlineConfig.enabled) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                        } else {
                            Color.White.copy(alpha = 0.18f)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("photo_pose_outline_toggle")
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
                                    "POSE OUTLINE: ON"
                                } else {
                                    "POSE OUTLINE: OFF"
                                },
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        onClick = { showChangePoseSheet = true },
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.22f),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("photo_change_pose_button")
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

                // Shutter Row: Import Photo | Capture | Timer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.18f))
                            .testTag("camera_import_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhotoLibrary,
                            contentDescription = "Import photo",
                            tint = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (isCapturing) MaterialTheme.colorScheme.primary else Color.White
                            )
                            .clickable(enabled = !isCapturing && countdownRemaining == 0) {
                                triggerShutterWithTimer()
                            }
                            .testTag("camera_capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            timerSeconds = when (timerSeconds) {
                                0 -> 3
                                3 -> 5
                                5 -> 10
                                else -> 0
                            }
                        },
                        shape = CircleShape,
                        color = if (timerSeconds > 0) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                        } else {
                            Color.White.copy(alpha = 0.18f)
                        },
                        modifier = Modifier
                            .size(52.dp)
                            .testTag("camera_timer_button")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Rounded.Timer,
                                contentDescription = "Timer",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (timerSeconds == 0) "Off" else "${timerSeconds}s",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }

    if (showChangePoseSheet) {
        ChangePoseBottomSheet(
            title = "Choose Photo Pose Outline",
            selectedCategory = selectedCategory,
            activePoseId = activePose.id,
            poseUiState = poseUiState,
            onSelectCategory = { cat -> onSelectCategory(cat, true) },
            onGiveAnotherPose = { onGiveAnotherPose(true) },
            onNextPose = { onNextPose(true) },
            onUseThisPose = onUseThisPose,
            onDismiss = { showChangePoseSheet = false }
        )
    }

    if (showCustomizeSheet) {
        OutlineCustomizeBottomSheet(
            title = "Pose Outline Settings",
            outlineToggleLabel = "Show Pose Outline",
            saveWithGuideLabel = "Save Photo With Guide",
            config = outlineConfig,
            onToggleEnabled = onToggleOutlineEnabled,
            onScaleChange = onOutlineScaleChange,
            onOffsetChange = onOutlineOffsetChange,
            onAlphaChange = onOutlineAlphaChange,
            onReset = onResetOutline,
            onToggleSaveWithGuide = onToggleSavePhotoWithGuide,
            onChangePoseClick = { showChangePoseSheet = true },
            onDismiss = { showCustomizeSheet = false }
        )
    }
}
