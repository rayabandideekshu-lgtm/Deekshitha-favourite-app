package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.JournalEntryEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.ui.screens.AllMemoriesScreen
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JournalEditorScreen
import com.example.ui.screens.JournalListScreen
import com.example.ui.screens.MemoryDetailScreen
import com.example.ui.screens.OnThisDayScreen
import com.example.ui.screens.PhotoCaptureReviewScreen
import com.example.ui.screens.PoseInspirationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VideoCameraScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.screens.VideoGalleryScreen
import com.example.ui.screens.VideoJournalEditorScreen
import com.example.ui.screens.VideoJournalListScreen
import com.example.ui.screens.VideoReviewScreen
import androidx.compose.material.icons.rounded.VideoSettings
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.PhotoEditLayer
import com.example.ui.screens.PhotoEditorScreen
import com.example.ui.screens.TemplateStickerLibraryDialog
import com.example.ui.screens.VideoEditorScreen
import com.example.ui.theme.PoseMemoriesTheme
import com.example.ui.viewmodel.AppViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object AppRoutes {
    const val HOME = "home"
    const val CAMERA = "camera"
    const val VIDEO_CAMERA = "video_camera"
    const val JOURNAL = "journal"
    const val VIDEO_JOURNAL = "video_journal"
    const val ALL_MEMORIES = "all_memories"
    const val ARCHIVE = "archive"
    const val GALLERY = "gallery"
    const val VIDEO_GALLERY = "video_gallery"
    const val PROFILE = "profile"
    const val POSE_INSPIRATION = "pose_inspiration"
    const val PHOTO_REVIEW = "photo_review"
    const val VIDEO_REVIEW = "video_review"
    const val JOURNAL_EDITOR = "journal_editor"
    const val VIDEO_JOURNAL_EDITOR = "video_journal_editor"
    const val MEMORY_DETAIL = "memory_detail"
    const val VIDEO_DETAIL = "video_detail"
    const val CALENDAR = "calendar"
    const val ON_THIS_DAY = "on_this_day"
    const val PHOTO_EDITOR = "photo_editor"
    const val VIDEO_EDITOR = "video_editor"
}

private data class TopNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val BOTTOM_NAV_ITEMS = listOf(
    TopNavItem(
        route = AppRoutes.HOME,
        label = "Home",
        selectedIcon = Icons.Rounded.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_home"
    ),
    TopNavItem(
        route = AppRoutes.CAMERA,
        label = "Photo",
        selectedIcon = Icons.Rounded.CameraAlt,
        unselectedIcon = Icons.Outlined.CameraAlt,
        testTag = "nav_camera"
    ),
    TopNavItem(
        route = AppRoutes.VIDEO_CAMERA,
        label = "Video",
        selectedIcon = Icons.Rounded.Videocam,
        unselectedIcon = Icons.Outlined.Videocam,
        testTag = "nav_video_camera"
    ),
    TopNavItem(
        route = AppRoutes.VIDEO_EDITOR,
        label = "VIDEO EDITOR",
        selectedIcon = Icons.Rounded.VideoSettings,
        unselectedIcon = Icons.Outlined.VideoSettings,
        testTag = "nav_video_editor"
    ),
    TopNavItem(
        route = AppRoutes.ALL_MEMORIES,
        label = "Memories",
        selectedIcon = Icons.Rounded.CollectionsBookmark,
        unselectedIcon = Icons.Outlined.CollectionsBookmark,
        testTag = "nav_all_memories"
    ),
    TopNavItem(
        route = AppRoutes.ARCHIVE,
        label = "Archive",
        selectedIcon = Icons.Rounded.Archive,
        unselectedIcon = Icons.Outlined.Archive,
        testTag = "nav_archive"
    )
)

private val DRAWER_NAV_ITEMS = listOf(
    TopNavItem(AppRoutes.HOME, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "drawer_home"),
    TopNavItem(AppRoutes.CAMERA, "Photo Camera", Icons.Rounded.CameraAlt, Icons.Outlined.CameraAlt, "drawer_photo_camera"),
    TopNavItem(AppRoutes.VIDEO_CAMERA, "Video Camera", Icons.Rounded.Videocam, Icons.Outlined.Videocam, "drawer_video_camera"),
    TopNavItem(AppRoutes.VIDEO_EDITOR, "Video Editor", Icons.Rounded.VideoSettings, Icons.Outlined.VideoSettings, "drawer_video_editor"),
    TopNavItem(AppRoutes.JOURNAL, "Photo Journal", Icons.AutoMirrored.Rounded.MenuBook, Icons.AutoMirrored.Outlined.MenuBook, "drawer_photo_journal"),
    TopNavItem(AppRoutes.VIDEO_JOURNAL, "Video Journal", Icons.Rounded.MovieCreation, Icons.Rounded.MovieCreation, "drawer_video_journal"),
    TopNavItem(AppRoutes.ALL_MEMORIES, "All Memories", Icons.Rounded.CollectionsBookmark, Icons.Outlined.CollectionsBookmark, "drawer_all_memories"),
    TopNavItem(AppRoutes.ARCHIVE, "Archive", Icons.Rounded.Archive, Icons.Outlined.Archive, "drawer_archive"),
    TopNavItem(AppRoutes.PROFILE, "Settings", Icons.Rounded.Settings, Icons.Rounded.Settings, "drawer_settings")
)

@Composable
fun PoseMemoriesApp(
    viewModel: AppViewModel = viewModel()
) {
    val settings by viewModel.userSettings.collectAsStateWithLifecycle()
    val journalEntries by viewModel.journalEntries.collectAsStateWithLifecycle()
    val videoJournals by viewModel.videoJournals.collectAsStateWithLifecycle()
    val galleryPhotos by viewModel.galleryPhotos.collectAsStateWithLifecycle()
    val galleryVideos by viewModel.galleryVideos.collectAsStateWithLifecycle()
    val archivedItems by viewModel.archivedItems.collectAsStateWithLifecycle()
    val sortOrder by viewModel.gallerySortOrder.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedPoseCategory.collectAsStateWithLifecycle()
    val poseUiState by viewModel.poseUiState.collectAsStateWithLifecycle()
    val videoPoseUiState by viewModel.videoPoseUiState.collectAsStateWithLifecycle()
    val activePose by viewModel.activePoseSuggestion.collectAsStateWithLifecycle()
    val activeVideoPose by viewModel.activeVideoPoseSuggestion.collectAsStateWithLifecycle()
    val photoOutlineConfig by viewModel.photoOutlineConfig.collectAsStateWithLifecycle()
    val videoOutlineConfig by viewModel.videoOutlineConfig.collectAsStateWithLifecycle()
    val alignmentGuidance by viewModel.alignmentGuidance.collectAsStateWithLifecycle()
    val captionUiState by viewModel.captionUiState.collectAsStateWithLifecycle()
    val pendingCapture by viewModel.pendingCapture.collectAsStateWithLifecycle()
    val pendingVideoCapture by viewModel.pendingVideoCapture.collectAsStateWithLifecycle()
    val isBusyBackup by viewModel.isBusyBackup.collectAsStateWithLifecycle()
    val videoEditProjects by viewModel.videoEditProjects.collectAsStateWithLifecycle()
    val photoEditState by viewModel.photoEditState.collectAsStateWithLifecycle()
    val canUndoPhoto by viewModel.canUndoPhoto.collectAsStateWithLifecycle()
    val canRedoPhoto by viewModel.canRedoPhoto.collectAsStateWithLifecycle()
    val isProcessingCutout by viewModel.isProcessingCutout.collectAsStateWithLifecycle()
    val isSavingPhotoEdit by viewModel.isSavingPhotoEdit.collectAsStateWithLifecycle()
    val videoEditState by viewModel.videoEditState.collectAsStateWithLifecycle()
    val canUndoVideo by viewModel.canUndoVideo.collectAsStateWithLifecycle()
    val canRedoVideo by viewModel.canRedoVideo.collectAsStateWithLifecycle()
    val videoExportState by viewModel.videoExportState.collectAsStateWithLifecycle()

    var showStudioLibraryModal by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    PoseMemoriesTheme(
        themeMode = settings.themeMode,
        themePalette = settings.themePalette
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showTopLevelChrome = currentRoute in setOf(
            AppRoutes.HOME,
            AppRoutes.JOURNAL,
            AppRoutes.VIDEO_JOURNAL,
            AppRoutes.VIDEO_EDITOR,
            AppRoutes.ALL_MEMORIES,
            AppRoutes.ARCHIVE,
            AppRoutes.GALLERY,
            AppRoutes.VIDEO_GALLERY,
            AppRoutes.PROFILE
        )

        fun navigateTopLevel(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = showTopLevelChrome,
            drawerContent = {
                ModalDrawerSheet {
                    Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
                        Text(
                            text = "Pose & Memories",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "Every pose tells a story.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        DRAWER_NAV_ITEMS.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationDrawerItem(
                                label = { Text(item.label) },
                                selected = selected,
                                onClick = {
                                    drawerScope.launch { drawerState.close() }
                                    if (item.route == AppRoutes.CAMERA || item.route == AppRoutes.VIDEO_CAMERA) {
                                        navController.navigate(item.route)
                                    } else {
                                        navigateTopLevel(item.route)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.testTag(item.testTag)
                            )
                        }
                    }
                }
            }
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val useNavigationRail = maxWidth >= 700.dp

                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    bottomBar = {
                        if (showTopLevelChrome && !useNavigationRail) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 4.dp,
                                windowInsets = WindowInsets.navigationBars
                            ) {
                                BOTTOM_NAV_ITEMS.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            if (item.route == AppRoutes.CAMERA || item.route == AppRoutes.VIDEO_CAMERA) {
                                                navController.navigate(item.route)
                                            } else {
                                                navigateTopLevel(item.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                maxLines = 1,
                                                softWrap = false,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                        ),
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (showTopLevelChrome && useNavigationRail) {
                            NavigationRail(
                                containerColor = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                            ) {
                                BOTTOM_NAV_ITEMS.forEach { item ->
                                    val selected = currentRoute == item.route
                                    NavigationRailItem(
                                        selected = selected,
                                        onClick = {
                                            if (item.route == AppRoutes.CAMERA || item.route == AppRoutes.VIDEO_CAMERA) {
                                                navController.navigate(item.route)
                                            } else {
                                                navigateTopLevel(item.route)
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                                contentDescription = item.label
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        },
                                        modifier = Modifier.testTag(item.testTag)
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = AppRoutes.HOME
                            ) {
                                composable(AppRoutes.HOME) {
                                    HomeScreen(
                                        recentPhotos = galleryPhotos,
                                        recentVideos = galleryVideos,
                                        journalEntries = journalEntries,
                                        videoJournals = videoJournals,
                                        archivedCount = archivedItems.size,
                                        onOpenDrawer = { drawerScope.launch { drawerState.open() } },
                                        onOpenCamera = { navController.navigate(AppRoutes.CAMERA) },
                                        onOpenVideoCamera = { navController.navigate(AppRoutes.VIDEO_CAMERA) },
                                        onOpenPoseInspiration = { navController.navigate(AppRoutes.POSE_INSPIRATION) },
                                        onOpenJournal = { navigateTopLevel(AppRoutes.JOURNAL) },
                                        onOpenVideoJournal = { navigateTopLevel(AppRoutes.VIDEO_JOURNAL) },
                                        onOpenAllMemories = { navigateTopLevel(AppRoutes.ALL_MEMORIES) },
                                        onOpenGallery = { navigateTopLevel(AppRoutes.GALLERY) },
                                        onOpenArchive = { navigateTopLevel(AppRoutes.ARCHIVE) },
                                        onOpenCalendar = { navController.navigate(AppRoutes.CALENDAR) },
                                        onOpenTodayMemories = { navController.navigate(AppRoutes.ON_THIS_DAY) },
                                        onOpenVideoEditor = { navigateTopLevel(AppRoutes.VIDEO_EDITOR) },
                                        onOpenTemplateLibrary = { showStudioLibraryModal = true },
                                        onOpenSettings = { navigateTopLevel(AppRoutes.PROFILE) },
                                        onOpenMemoryDetail = { entryId ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$entryId")
                                        },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.POSE_INSPIRATION) {
                                    PoseInspirationScreen(
                                        selectedCategory = selectedCategory,
                                        activePoseId = activePose.id,
                                        poseUiState = poseUiState,
                                        onSelectCategory = { cat ->
                                            viewModel.selectPoseCategory(cat, hasCameraFrame = false)
                                        },
                                        onGiveAnotherPose = {
                                            viewModel.loadPoseSuggestions(advanceBatch = true, hasCameraFrame = false)
                                        },
                                        onNextPose = {
                                            viewModel.loadPoseSuggestions(advanceBatch = true, hasCameraFrame = false)
                                        },
                                        onUseThisPose = { pose ->
                                            viewModel.selectPoseSuggestion(pose)
                                            navController.navigate(AppRoutes.CAMERA)
                                        },
                                        onTryInCamera = {
                                            navController.navigate(AppRoutes.CAMERA)
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(AppRoutes.CAMERA) {
                                    CameraScreen(
                                        selectedCategory = selectedCategory,
                                        activePose = activePose,
                                        outlineConfig = photoOutlineConfig,
                                        alignmentGuidance = alignmentGuidance,
                                        poseUiState = poseUiState,
                                        defaultTimerSeconds = settings.defaultTimerSeconds,
                                        createTempFile = { viewModel.createTempCaptureFile() },
                                        onSelectCategory = { cat, hasFrame ->
                                            viewModel.selectPoseCategory(cat, hasCameraFrame = hasFrame)
                                        },
                                        onGiveAnotherPose = { hasFrame ->
                                            viewModel.loadPoseSuggestions(advanceBatch = true, hasCameraFrame = hasFrame)
                                        },
                                        onNextPose = { hasFrame ->
                                            viewModel.loadPoseSuggestions(advanceBatch = true, hasCameraFrame = hasFrame)
                                        },
                                        onUseThisPose = { pose ->
                                            viewModel.selectPoseSuggestion(pose)
                                        },
                                        onToggleOutlineEnabled = { viewModel.setPhotoPoseOutlineEnabled(it) },
                                        onOutlineScaleChange = { viewModel.setOutlineScale(it) },
                                        onOutlineOffsetChange = { viewModel.setOutlineVerticalOffset(it) },
                                        onOutlineAlphaChange = { viewModel.setOutlineAlpha(it) },
                                        onResetOutline = { viewModel.resetOutlineStyle() },
                                        onToggleSavePhotoWithGuide = { viewModel.setSavePhotoWithGuide(it) },
                                        onAlignmentGuidance = { viewModel.updateAlignmentGuidance(it) },
                                        onPhotoCaptured = { path, timestamp ->
                                            viewModel.onPhotoCapturedWithGuideOption(path, timestamp) {
                                                navController.navigate(AppRoutes.PHOTO_REVIEW)
                                            }
                                        },
                                        onImportPickedPhoto = { uri ->
                                            viewModel.importPickedPhoto(uri) {
                                                navController.navigate(AppRoutes.PHOTO_REVIEW)
                                            }
                                        },
                                        onCloseCamera = {
                                            if (!navController.popBackStack()) {
                                                navigateTopLevel(AppRoutes.HOME)
                                            }
                                        }
                                    )
                                }

                                composable(AppRoutes.VIDEO_CAMERA) {
                                    VideoCameraScreen(
                                        selectedCategory = selectedCategory,
                                        activePose = activeVideoPose,
                                        outlineConfig = videoOutlineConfig,
                                        alignmentGuidance = alignmentGuidance,
                                        poseUiState = videoPoseUiState,
                                        createTempVideoFile = { viewModel.createTempVideoFile() },
                                        onSelectCategory = { cat ->
                                            viewModel.selectPoseCategory(cat, hasCameraFrame = false)
                                        },
                                        onGiveAnotherPose = {
                                            viewModel.loadVideoPoseSuggestions(advanceBatch = true)
                                        },
                                        onNextPose = {
                                            viewModel.loadVideoPoseSuggestions(advanceBatch = true)
                                        },
                                        onUseThisPose = { pose ->
                                            viewModel.selectVideoPoseSuggestion(pose)
                                        },
                                        onToggleVideoPoseGuide = { viewModel.setVideoPoseGuideEnabled(it) },
                                        onOutlineScaleChange = { viewModel.setOutlineScale(it) },
                                        onOutlineOffsetChange = { viewModel.setOutlineVerticalOffset(it) },
                                        onOutlineAlphaChange = { viewModel.setOutlineAlpha(it) },
                                        onResetOutline = { viewModel.resetOutlineStyle() },
                                        onToggleSaveVideoWithGuide = { viewModel.setSaveVideoWithGuide(it) },
                                        onAlignmentGuidance = { viewModel.updateAlignmentGuidance(it) },
                                        onVideoRecorded = { path, durationMs, capturedAt ->
                                            viewModel.setPendingVideoCapture(path, durationMs, capturedAt)
                                            navController.navigate(AppRoutes.VIDEO_REVIEW)
                                        },
                                        onImportPickedVideo = { uri ->
                                            viewModel.importPickedVideo(uri) {
                                                navController.navigate(AppRoutes.VIDEO_REVIEW)
                                            }
                                        },
                                        onCloseCamera = {
                                            if (!navController.popBackStack()) {
                                                navigateTopLevel(AppRoutes.HOME)
                                            }
                                        }
                                    )
                                }

                                composable(AppRoutes.PHOTO_REVIEW) {
                                    val capture = pendingCapture
                                    if (capture == null) {
                                        LaunchedEffect(Unit) { navController.popBackStack() }
                                    } else {
                                        PhotoCaptureReviewScreen(
                                            photoPath = capture.filePath,
                                            onRetake = {
                                                viewModel.clearPendingCapture()
                                                navController.popBackStack()
                                            },
                                            onEditPhoto = {
                                                viewModel.openPhotoEditor(capture.filePath, capture.existingPhotoId)
                                                navController.navigate(AppRoutes.PHOTO_EDITOR)
                                            },
                                            onCreateJournal = {
                                                viewModel.resetCaptionState()
                                                navController.navigate("${AppRoutes.JOURNAL_EDITOR}?entryId=-1")
                                            },
                                            onSavePhotoOnly = {
                                                viewModel.savePhotoOnly {
                                                    navController.popBackStack(
                                                        route = AppRoutes.HOME,
                                                        inclusive = false
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }

                                composable(AppRoutes.VIDEO_REVIEW) {
                                    val capture = pendingVideoCapture
                                    if (capture == null) {
                                        LaunchedEffect(Unit) { navController.popBackStack() }
                                    } else {
                                        VideoReviewScreen(
                                            videoPath = capture.filePath,
                                            onRetake = {
                                                viewModel.clearPendingVideoCapture()
                                                navController.popBackStack()
                                            },
                                            onEditInVideoEditor = {
                                                viewModel.openVideoInProEditor(
                                                    videoPath = capture.filePath,
                                                    durationMillis = capture.durationMillis,
                                                    thumbnailPath = capture.thumbnailPath.orEmpty()
                                                )
                                                navigateTopLevel(AppRoutes.VIDEO_EDITOR)
                                            },
                                            onCreateVideoJournal = {
                                                viewModel.resetCaptionState()
                                                navController.navigate("${AppRoutes.VIDEO_JOURNAL_EDITOR}?entryId=-1")
                                            },
                                            onSaveVideoOnly = {
                                                viewModel.saveVideoOnly {
                                                    navController.popBackStack(
                                                        route = AppRoutes.HOME,
                                                        inclusive = false
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }

                                composable(
                                    route = "${AppRoutes.JOURNAL_EDITOR}?entryId={entryId}",
                                    arguments = listOf(
                                        navArgument("entryId") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                    )
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                                    var editingEntry by remember(entryId) { mutableStateOf<JournalEntryEntity?>(null) }

                                    LaunchedEffect(entryId) {
                                        if (entryId > 0L) {
                                            editingEntry = viewModel.getJournalEntryById(entryId)
                                        }
                                    }

                                    val capture = pendingCapture
                                    val resolvedPhotoPath = editingEntry?.photoPath ?: capture?.filePath.orEmpty()
                                    var stablePhotoPath by remember(entryId) { mutableStateOf(resolvedPhotoPath) }
                                    if (resolvedPhotoPath.isNotBlank()) {
                                        stablePhotoPath = resolvedPhotoPath
                                    }
                                    val activePhotoPath = stablePhotoPath
                                    val activeTimestamp = editingEntry?.capturedAtMillis
                                        ?: capture?.capturedAtMillis
                                        ?: System.currentTimeMillis()

                                    if (activePhotoPath.isNotBlank()) {
                                        JournalEditorScreen(
                                            photoPath = activePhotoPath,
                                            initialTimestampMillis = activeTimestamp,
                                            editingEntry = editingEntry,
                                            captionUiState = captionUiState,
                                            onGenerateAiCaption = { place, formattedDate, outfit, g1, g2, onDone ->
                                                viewModel.generateCaption(place, formattedDate, outfit, g1, g2, onDone)
                                            },
                                            onSaveJournal = { editId, place, ts, outfit, g1, g2, caption, tmpl, stickers, saveGallery ->
                                                viewModel.saveJournalEntry(
                                                    editingEntryId = editId,
                                                    place = place,
                                                    capturedAtMillis = ts,
                                                    outfit = outfit,
                                                    goodThingOne = g1,
                                                    goodThingTwo = g2,
                                                    caption = caption,
                                                    templateId = tmpl,
                                                    stickersCsv = stickers,
                                                    alsoSaveToGallery = saveGallery
                                                ) { savedEntryId ->
                                                    navController.navigate("${AppRoutes.MEMORY_DETAIL}/$savedEntryId") {
                                                        popUpTo(AppRoutes.HOME) { inclusive = false }
                                                    }
                                                }
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }

                                composable(
                                    route = "${AppRoutes.VIDEO_JOURNAL_EDITOR}?entryId={entryId}",
                                    arguments = listOf(
                                        navArgument("entryId") {
                                            type = NavType.LongType
                                            defaultValue = -1L
                                        }
                                    )
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                                    var editingEntry by remember(entryId) { mutableStateOf<VideoJournalEntryEntity?>(null) }

                                    LaunchedEffect(entryId) {
                                        if (entryId > 0L) {
                                            editingEntry = viewModel.getVideoJournalById(entryId)
                                        }
                                    }

                                    val capture = pendingVideoCapture
                                    val resolvedVideoPath = editingEntry?.videoPath ?: capture?.filePath.orEmpty()
                                    var stableVideoPath by remember(entryId) { mutableStateOf(resolvedVideoPath) }
                                    if (resolvedVideoPath.isNotBlank()) {
                                        stableVideoPath = resolvedVideoPath
                                    }
                                    val activeVideoPath = stableVideoPath
                                    val activeTimestamp = editingEntry?.capturedAtMillis
                                        ?: capture?.capturedAtMillis
                                        ?: System.currentTimeMillis()

                                    if (activeVideoPath.isNotBlank()) {
                                        VideoJournalEditorScreen(
                                            videoPath = activeVideoPath,
                                            initialTimestampMillis = activeTimestamp,
                                            editingEntry = editingEntry,
                                            captionUiState = captionUiState,
                                            onGenerateAiCaption = { place, formattedDate, outfit, g1, g2, onDone ->
                                                viewModel.generateCaption(place, formattedDate, outfit, g1, g2, onDone)
                                            },
                                            onSaveVideoJournal = { editId, place, ts, outfit, g1, g2, caption, tmpl, stickers, saveGallery ->
                                                viewModel.saveVideoJournalEntry(
                                                    editingEntryId = editId,
                                                    place = place,
                                                    capturedAtMillis = ts,
                                                    outfit = outfit,
                                                    goodThingOne = g1,
                                                    goodThingTwo = g2,
                                                    caption = caption,
                                                    templateId = tmpl,
                                                    stickersCsv = stickers,
                                                    alsoSaveToGallery = saveGallery
                                                ) { savedEntryId ->
                                                    navController.navigate("${AppRoutes.VIDEO_DETAIL}/$savedEntryId") {
                                                        popUpTo(AppRoutes.HOME) { inclusive = false }
                                                    }
                                                }
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }

                                composable(AppRoutes.JOURNAL) {
                                    JournalListScreen(
                                        entries = journalEntries,
                                        onOpenEntry = { id ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$id")
                                        },
                                        onToggleFavorite = { viewModel.toggleJournalFavorite(it) },
                                        onOpenCamera = { navController.navigate(AppRoutes.CAMERA) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.VIDEO_JOURNAL) {
                                    VideoJournalListScreen(
                                        entries = videoJournals,
                                        onOpenEntry = { id ->
                                            navController.navigate("${AppRoutes.VIDEO_DETAIL}/$id")
                                        },
                                        onToggleFavorite = { viewModel.toggleVideoJournalFavorite(it) },
                                        onOpenVideoCamera = { navController.navigate(AppRoutes.VIDEO_CAMERA) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.ALL_MEMORIES) {
                                    AllMemoriesScreen(
                                        photos = galleryPhotos,
                                        videos = galleryVideos,
                                        photoJournals = journalEntries,
                                        videoJournals = videoJournals,
                                        onOpenPhotoGallery = { navController.navigate(AppRoutes.GALLERY) },
                                        onOpenVideoGallery = { navController.navigate(AppRoutes.VIDEO_GALLERY) },
                                        onOpenPhotoJournalDetail = { id ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$id")
                                        },
                                        onOpenVideoJournalDetail = { id ->
                                            navController.navigate("${AppRoutes.VIDEO_DETAIL}/$id")
                                        },
                                        onCreateJournalForPhoto = { photo ->
                                            viewModel.resetCaptionState()
                                            viewModel.prepareEditorFromGalleryPhoto(photo) {
                                                navController.navigate("${AppRoutes.JOURNAL_EDITOR}?entryId=-1")
                                            }
                                        },
                                        onCreateJournalForVideo = { video ->
                                            viewModel.resetCaptionState()
                                            viewModel.prepareVideoEditorFromGallery(video) {
                                                navController.navigate("${AppRoutes.VIDEO_JOURNAL_EDITOR}?entryId=-1")
                                            }
                                        },
                                        onArchivePhoto = { viewModel.archivePhoto(it) },
                                        onArchiveVideo = { viewModel.archiveVideo(it) },
                                        onArchivePhotoJournal = { viewModel.archiveJournalEntry(it) },
                                        onArchiveVideoJournal = { viewModel.archiveVideoJournalEntry(it) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.ARCHIVE) {
                                    ArchiveScreen(
                                        archivedItems = archivedItems,
                                        onRestoreItem = { viewModel.restoreArchivedItem(it) },
                                        onRestoreAll = { viewModel.restoreAllArchived() },
                                        onPermanentlyDeleteItem = { viewModel.permanentlyDeleteArchivedItem(it) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(
                                    route = "${AppRoutes.MEMORY_DETAIL}/{entryId}",
                                    arguments = listOf(navArgument("entryId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                                    val entry = journalEntries.firstOrNull { it.id == entryId }
                                    MemoryDetailScreen(
                                        entry = entry,
                                        onBack = { navController.popBackStack() },
                                        onEdit = { id ->
                                            viewModel.resetCaptionState()
                                            navController.navigate("${AppRoutes.JOURNAL_EDITOR}?entryId=$id")
                                        },
                                        onEditPhoto = { path, photoId ->
                                            viewModel.openPhotoEditor(path, photoId)
                                            navController.navigate(AppRoutes.PHOTO_EDITOR)
                                        },
                                        onToggleFavorite = { viewModel.toggleJournalFavorite(it) },
                                        onDownloadPhoto = { path, photoId ->
                                            viewModel.downloadPhotoToGallery(path, photoId)
                                        },
                                        getShareUri = { path -> viewModel.getShareableUri(path) },
                                        onDelete = { victim ->
                                            viewModel.archiveJournalEntry(victim) {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }

                                composable(
                                    route = "${AppRoutes.VIDEO_DETAIL}/{entryId}",
                                    arguments = listOf(navArgument("entryId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val entryId = backStackEntry.arguments?.getLong("entryId") ?: -1L
                                    val entry = videoJournals.firstOrNull { it.id == entryId }
                                    VideoDetailScreen(
                                        entry = entry,
                                        onBack = { navController.popBackStack() },
                                        onEdit = { id ->
                                            viewModel.resetCaptionState()
                                            navController.navigate("${AppRoutes.VIDEO_JOURNAL_EDITOR}?entryId=$id")
                                        },
                                        onEditVideoInStudio = { vj ->
                                            viewModel.openVideoInProEditor(
                                                videoPath = vj.videoPath,
                                                durationMillis = vj.durationMillis,
                                                thumbnailPath = vj.thumbnailPath,
                                                title = vj.place.ifBlank { "Video Memory" }
                                            )
                                            navigateTopLevel(AppRoutes.VIDEO_EDITOR)
                                        },
                                        onToggleFavorite = { viewModel.toggleVideoJournalFavorite(it) },
                                        onDownloadVideo = { path, videoId ->
                                            viewModel.downloadVideoToGallery(path, videoId)
                                        },
                                        getShareUri = { path -> viewModel.getShareableUri(path) },
                                        onArchive = { victim ->
                                            viewModel.archiveVideoJournalEntry(victim) {
                                                navController.popBackStack()
                                            }
                                        }
                                    )
                                }

                                composable(AppRoutes.CALENDAR) {
                                    CalendarScreen(
                                        photos = galleryPhotos,
                                        journalEntries = journalEntries,
                                        onOpenJournalEntry = { id ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$id")
                                        },
                                        onCreateJournalForPhoto = { photo ->
                                            viewModel.resetCaptionState()
                                            viewModel.prepareEditorFromGalleryPhoto(photo) {
                                                navController.navigate("${AppRoutes.JOURNAL_EDITOR}?entryId=-1")
                                            }
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(AppRoutes.ON_THIS_DAY) {
                                    OnThisDayScreen(
                                        entries = journalEntries,
                                        onOpenEntry = { id ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$id")
                                        },
                                        onOpenCamera = { navController.navigate(AppRoutes.CAMERA) },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(AppRoutes.GALLERY) {
                                    GalleryScreen(
                                        photos = galleryPhotos,
                                        journalEntries = journalEntries,
                                        sortOrder = sortOrder,
                                        onChangeSortOrder = { viewModel.setGallerySortOrder(it) },
                                        onToggleFavorite = { viewModel.togglePhotoFavorite(it) },
                                        onDownloadPhoto = { photo ->
                                            viewModel.downloadPhotoToGallery(photo.filePath, photo.id)
                                        },
                                        onDeletePhoto = { viewModel.archivePhoto(it) },
                                        onOpenJournalEntry = { id ->
                                            navController.navigate("${AppRoutes.MEMORY_DETAIL}/$id")
                                        },
                                        onCreateJournalForPhoto = { photo ->
                                            viewModel.resetCaptionState()
                                            viewModel.prepareEditorFromGalleryPhoto(photo) {
                                                navController.navigate("${AppRoutes.JOURNAL_EDITOR}?entryId=-1")
                                            }
                                        },
                                        onEditPhoto = { photo ->
                                            viewModel.openPhotoEditor(photo.filePath, photo.id)
                                            navController.navigate(AppRoutes.PHOTO_EDITOR)
                                        },
                                        getShareUri = { path -> viewModel.getShareableUri(path) },
                                        onOpenCamera = { navController.navigate(AppRoutes.CAMERA) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.VIDEO_GALLERY) {
                                    VideoGalleryScreen(
                                        videos = galleryVideos,
                                        videoJournals = videoJournals,
                                        sortOrder = sortOrder,
                                        onChangeSortOrder = { viewModel.setGallerySortOrder(it) },
                                        onToggleFavorite = { viewModel.toggleVideoFavorite(it) },
                                        onDownloadVideo = { video ->
                                            viewModel.downloadVideoToGallery(video.filePath, video.id)
                                        },
                                        onArchiveVideo = { viewModel.archiveVideo(it) },
                                        onOpenVideoJournal = { id ->
                                            navController.navigate("${AppRoutes.VIDEO_DETAIL}/$id")
                                        },
                                        onCreateJournalForVideo = { video ->
                                            viewModel.resetCaptionState()
                                            viewModel.prepareVideoEditorFromGallery(video) {
                                                navController.navigate("${AppRoutes.VIDEO_JOURNAL_EDITOR}?entryId=-1")
                                            }
                                        },
                                        onEditVideoInStudio = { video ->
                                            viewModel.openVideoInProEditor(
                                                videoPath = video.filePath,
                                                durationMillis = video.durationMillis,
                                                thumbnailPath = video.thumbnailPath
                                            )
                                            navigateTopLevel(AppRoutes.VIDEO_EDITOR)
                                        },
                                        getShareUri = { path -> viewModel.getShareableUri(path) },
                                        onOpenVideoCamera = { navController.navigate(AppRoutes.VIDEO_CAMERA) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.PHOTO_EDITOR) {
                                    val pState = photoEditState
                                    if (pState == null) {
                                        LaunchedEffect(Unit) { navController.popBackStack() }
                                    } else {
                                        PhotoEditorScreen(
                                            state = pState,
                                            canUndo = canUndoPhoto,
                                            canRedo = canRedoPhoto,
                                            isProcessingCutout = isProcessingCutout,
                                            isSaving = isSavingPhotoEdit,
                                            favoriteTemplateIds = settings.favoriteTemplateIds,
                                            favoriteStickerIds = settings.favoriteStickerIds,
                                            onToggleFavoriteTemplate = { viewModel.toggleFavoriteTemplate(it) },
                                            onToggleFavoriteSticker = { viewModel.toggleFavoriteSticker(it) },
                                            onUpdateState = { rec, fn -> viewModel.updatePhotoEdit(rec, fn) },
                                            onUndo = { viewModel.undoPhotoEdit() },
                                            onRedo = { viewModel.redoPhotoEdit() },
                                            onRemoveBackground = { viewModel.removePhotoBackground() },
                                            onSaveDraft = { viewModel.savePhotoEditDraft() },
                                            onSaveCopy = { exportGallery, onDone ->
                                                viewModel.saveEditedPhotoCopy(exportGallery, onDone)
                                            },
                                            onOpenSavedResultInGallery = {
                                                navController.navigate(AppRoutes.GALLERY) {
                                                    popUpTo(AppRoutes.HOME) { inclusive = false }
                                                }
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }

                                composable(AppRoutes.VIDEO_EDITOR) {
                                    VideoEditorScreen(
                                        state = videoEditState,
                                        canUndo = canUndoVideo,
                                        canRedo = canRedoVideo,
                                        exportState = videoExportState,
                                        supportedResolutions = viewModel.supportedResolutions,
                                        galleryVideos = galleryVideos,
                                        galleryPhotos = galleryPhotos,
                                        savedProjects = videoEditProjects,
                                        favoriteStickerIds = settings.favoriteStickerIds,
                                        audioDir = viewModel.getAudioDir(),
                                        onToggleFavoriteSticker = { viewModel.toggleFavoriteSticker(it) },
                                        onUpdateState = { rec, fn -> viewModel.updateVideoEdit(rec, fn) },
                                        onUndo = { viewModel.undoVideoEdit() },
                                        onRedo = { viewModel.redoVideoEdit() },
                                        onStartFresh = { viewModel.startFreshVideoProject() },
                                        onOpenProject = { viewModel.openVideoProject(it) },
                                        onDeleteProject = { viewModel.deleteEditProject(it) },
                                        onAddExistingVideo = { viewModel.addExistingVideoClipToTimeline(it) },
                                        onAddExistingPhoto = { viewModel.addExistingPhotoSlideToTimeline(it) },
                                        onImportVideoUri = { viewModel.importPickedVideoIntoTimeline(it) },
                                        onImportPhotoUri = { viewModel.importPickedPhotoIntoTimeline(it) },
                                        onSplitAtPlayhead = { viewModel.splitClipAtTimelinePosition(it) },
                                        onMoveClip = { from, to -> viewModel.moveClipInTimeline(from, to) },
                                        onAddBuiltInAudio = { assetId, ms -> viewModel.addBuiltInAudioTrack(assetId, ms) },
                                        onImportAudioUri = { uri, ms -> viewModel.importCustomAudioTrack(uri, ms) },
                                        onAddVoiceover = { file, dur, ms -> viewModel.addVoiceoverAudioTrack(file, dur, ms) },
                                        onImportPipImageUri = { uri, ms -> viewModel.importPipImageFromUri(uri, ms) },
                                        onImportPipVideoUri = { uri, ms -> viewModel.importPipVideoFromUri(uri, ms) },
                                        onSaveDraft = { viewModel.saveVideoProjectDraft() },
                                        onExportVideo = { res, saveGal -> viewModel.exportVideoProject(res, saveGal) },
                                        onCancelExport = { viewModel.cancelVideoExport() },
                                        onResetExportState = { viewModel.resetVideoExportState() },
                                        onOpenVideoGallery = { navController.navigate(AppRoutes.VIDEO_GALLERY) },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }

                                composable(AppRoutes.PROFILE) {
                                    SettingsScreen(
                                        settings = settings,
                                        photoCount = galleryPhotos.size,
                                        journalCount = journalEntries.size,
                                        isBusyBackup = isBusyBackup,
                                        onSetThemeMode = { viewModel.setThemeMode(it) },
                                        onSetThemePalette = { viewModel.setThemePalette(it) },
                                        onSetDefaultTimer = { viewModel.setDefaultTimerSeconds(it) },
                                        onExportBackupZip = { viewModel.exportBackupZip(it) },
                                        onExportJournalText = { viewModel.exportReadableJournal(it) },
                                        onImportBackupZip = { viewModel.importBackupZip(it) },
                                        onDeleteAllData = { viewModel.deleteAllAppData() },
                                        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showStudioLibraryModal) {
            TemplateStickerLibraryDialog(
                initialTabIndex = 0,
                showTemplatesTab = true,
                favoriteTemplateIds = settings.favoriteTemplateIds,
                favoriteStickerIds = settings.favoriteStickerIds,
                onToggleFavoriteTemplate = { viewModel.toggleFavoriteTemplate(it) },
                onToggleFavoriteSticker = { viewModel.toggleFavoriteSticker(it) },
                onSelectTemplate = { template ->
                    showStudioLibraryModal = false
                    val targetPhoto = galleryPhotos.firstOrNull()
                    if (targetPhoto != null) {
                        viewModel.openPhotoEditor(targetPhoto.filePath, targetPhoto.id)
                        viewModel.updatePhotoEdit(recordUndo = false) { st ->
                            st.copy(
                                adjustments = st.adjustments.copy(
                                    appliedTemplateId = template.id,
                                    frameStyle = template.frameStyle
                                )
                            )
                        }
                        navController.navigate(AppRoutes.PHOTO_EDITOR)
                    } else {
                        drawerScope.launch {
                            snackbarHostState.showSnackbar("Capture or import a photo first to apply \"${template.name}\"!")
                        }
                        navController.navigate(AppRoutes.CAMERA)
                    }
                },
                onSelectSticker = { sticker ->
                    showStudioLibraryModal = false
                    val targetPhoto = galleryPhotos.firstOrNull()
                    if (targetPhoto != null) {
                        viewModel.openPhotoEditor(targetPhoto.filePath, targetPhoto.id)
                        viewModel.updatePhotoEdit(recordUndo = false) { st ->
                            st.copy(
                                layers = st.layers + PhotoEditLayer.StickerLayer(
                                    name = sticker.name,
                                    stickerId = sticker.id
                                )
                            )
                        }
                        navController.navigate(AppRoutes.PHOTO_EDITOR)
                    } else {
                        drawerScope.launch {
                            snackbarHostState.showSnackbar("Capture a photo first to place \"${sticker.name}\"!")
                        }
                        navController.navigate(AppRoutes.CAMERA)
                    }
                },
                onDismiss = { showStudioLibraryModal = false }
            )
        }
    }
}
