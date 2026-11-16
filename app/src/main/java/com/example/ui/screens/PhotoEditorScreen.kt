package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.BorderAll
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Compare
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Crop
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.Flip
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.RotateRight
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.CutoutBackdrop
import com.example.data.model.EditorFontOption
import com.example.data.model.NormalizedPoint
import com.example.data.model.PhotoCropAspect
import com.example.data.model.PhotoEditLayer
import com.example.data.model.PhotoEditState
import com.example.data.model.PhotoFilterPreset
import com.example.data.model.ResizePreset
import com.example.data.model.TemplateFrameStyle
import com.example.ui.editor.PhotoEditEngine
import com.example.ui.editor.StickerVisual
import com.example.ui.editor.toComposeFontFamily
import java.io.File
import kotlin.math.roundToInt

private enum class PhotoToolTab(val label: String, val icon: ImageVector) {
    TEMPLATES("Templates", Icons.Rounded.Style),
    STICKERS("Stickers", Icons.Rounded.EmojiEmotions),
    TEXT("Text", Icons.Rounded.TextFields),
    FILTERS("Filters", Icons.Rounded.FilterVintage),
    ADJUST("Adjust", Icons.Rounded.Tune),
    CROP("Crop", Icons.Rounded.Crop),
    CUTOUT("Cutout", Icons.Rounded.ContentCut),
    DRAW("Draw/Erase", Icons.Rounded.Brush),
    FRAMES("Frames", Icons.Rounded.BorderAll),
    LAYERS("Layers", Icons.Rounded.Layers)
}

private val BRUSH_COLORS = listOf(
    0xFFE88AA7L,
    0xFFFFFFFFL,
    0xFF422330L,
    0xFFB39DDBL,
    0xFFFFC8A2L,
    0xFF75A488L,
    0xFF6B9AC4L,
    0xFFCFA268L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditorScreen(
    state: PhotoEditState,
    canUndo: Boolean,
    canRedo: Boolean,
    isProcessingCutout: Boolean,
    isSaving: Boolean,
    favoriteTemplateIds: Set<String>,
    favoriteStickerIds: Set<String>,
    onToggleFavoriteTemplate: (String) -> Unit,
    onToggleFavoriteSticker: (String) -> Unit,
    onUpdateState: (recordUndo: Boolean, (PhotoEditState) -> PhotoEditState) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onRemoveBackground: () -> Unit,
    onSaveDraft: () -> Unit,
    onSaveCopy: (exportToGallery: Boolean, onSaved: (Long, String) -> Unit) -> Unit,
    onOpenSavedResultInGallery: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(PhotoToolTab.TEMPLATES) }
    var selectedLayerId by remember { mutableStateOf<String?>(null) }
    var compareOriginal by remember { mutableStateOf(false) }

    var showLibraryModal by remember { mutableStateOf(false) }
    var libraryInitialTab by remember { mutableStateOf(0) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var exportToDeviceGallery by remember { mutableStateOf(true) }

    // Text editor modal state
    var showTextDialog by remember { mutableStateOf(false) }
    var editingTextLayerId by remember { mutableStateOf<String?>(null) }
    var textDraft by remember { mutableStateOf("") }
    var textFontId by remember { mutableStateOf(EditorFontOption.PLAYFAIR.id) }
    var textColorArgb by remember { mutableLongStateOf(0xFF422330L) }
    var textHasPill by remember { mutableStateOf(true) }
    var textPillColorArgb by remember { mutableLongStateOf(0xFFFFF4F8L) }

    // Doodle / Erase live stroke state
    var brushIsEraser by remember { mutableStateOf(false) }
    var brushColorArgb by remember { mutableLongStateOf(0xFFE88AA7L) }
    var brushWidthFrac by remember { mutableFloatStateOf(0.014f) }
    val activeStrokePoints = remember { mutableStateListOf<NormalizedPoint>() }

    // Render base bitmap (crop, rotate, adjustments, filters, cutout, template backdrop, frames, eraser & doodle strokes)
    // Interactive Sticker & Text layers are composited smoothly on top in Compose during editing, and baked by PhotoEditEngine on Save!
    val baseRenderState = remember(state.sourcePath, state.cutoutMaskPath, state.adjustments, state.layers) {
        state.copy(
            layers = state.layers.filterIsInstance<PhotoEditLayer.DoodleStrokeLayer>()
        )
    }

    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isRenderingPreview by remember { mutableStateOf(false) }

    LaunchedEffect(baseRenderState) {
        isRenderingPreview = true
        val bmp = runCatching {
            PhotoEditEngine.renderFinalPhotoBitmap(context, baseRenderState, previewModeMaxDim = 960)
        }.getOrNull()
        previewBitmap?.let { old -> if (old !== bmp && !old.isRecycled) old.recycle() }
        previewBitmap = bmp
        isRenderingPreview = false
    }

    val selectedLayer = state.layers.firstOrNull { it.id == selectedLayerId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Photo Studio", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (state.adjustments.appliedTemplateId != null) {
                                CreativeLibraryCatalog.templateById(state.adjustments.appliedTemplateId)?.name
                                    ?: "Non-destructive edit"
                            } else {
                                "Non-destructive edit"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("photo_editor_back")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = onUndo,
                        enabled = canUndo,
                        modifier = Modifier.testTag("photo_editor_undo")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Undo, contentDescription = "Undo")
                    }
                    IconButton(
                        onClick = onRedo,
                        enabled = canRedo,
                        modifier = Modifier.testTag("photo_editor_redo")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.Redo, contentDescription = "Redo")
                    }
                    IconButton(
                        onClick = { compareOriginal = !compareOriginal },
                        modifier = Modifier.testTag("photo_editor_compare")
                    ) {
                        Icon(
                            Icons.Rounded.Compare,
                            contentDescription = "Compare with original",
                            tint = if (compareOriginal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showLayersSheet = true },
                        modifier = Modifier.testTag("photo_editor_layers_btn")
                    ) {
                        Icon(Icons.Rounded.Layers, contentDescription = "Layers")
                    }
                    Button(
                        onClick = { showSaveDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("photo_editor_save_button")
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Copy", style = MaterialTheme.typography.labelLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // 1. Central Interactive Canvas Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                if (compareOriginal) {
                    AsyncImage(
                        model = File(state.sourcePath),
                        contentDescription = "Original unedited photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "ORIGINAL (Preserved)",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    val bmp = previewBitmap
                    val aspect = if (bmp != null && bmp.height > 0) bmp.width.toFloat() / bmp.height.toFloat() else 0.75f

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val density = LocalDensity.current
                        val maxWpx = with(density) { maxWidth.toPx() }
                        val maxHpx = with(density) { maxHeight.toPx() }
                        val boxWpx: Float
                        val boxHpx: Float
                        if (maxWpx / maxHpx > aspect) {
                            boxHpx = maxHpx
                            boxWpx = maxHpx * aspect
                        } else {
                            boxWpx = maxWpx
                            boxHpx = maxWpx / aspect
                        }
                        val boxWdp = with(density) { boxWpx.toDp() }
                        val boxHdp = with(density) { boxHpx.toDp() }

                        Box(
                            modifier = Modifier
                                .size(boxWdp, boxHdp)
                                .clip(RoundedCornerShape(14.dp))
                                .pointerInput(activeTab, brushIsEraser, brushColorArgb, brushWidthFrac) {
                                    if (activeTab == PhotoToolTab.DRAW) {
                                        detectDragGestures(
                                            onDragStart = { offset ->
                                                activeStrokePoints.clear()
                                                activeStrokePoints.add(
                                                    NormalizedPoint(
                                                        (offset.x / boxWpx).coerceIn(0f, 1f),
                                                        (offset.y / boxHpx).coerceIn(0f, 1f)
                                                    )
                                                )
                                            },
                                            onDrag = { change, _ ->
                                                change.consume()
                                                activeStrokePoints.add(
                                                    NormalizedPoint(
                                                        (change.position.x / boxWpx).coerceIn(0f, 1f),
                                                        (change.position.y / boxHpx).coerceIn(0f, 1f)
                                                    )
                                                )
                                            },
                                            onDragEnd = {
                                                if (activeStrokePoints.size >= 2) {
                                                    val pts = activeStrokePoints.toList()
                                                    val newLayer = PhotoEditLayer.DoodleStrokeLayer(
                                                        name = if (brushIsEraser) "Eraser Stroke" else "Brush Doodle",
                                                        points = pts,
                                                        colorArgb = brushColorArgb,
                                                        strokeWidthFraction = brushWidthFrac,
                                                        isEraser = brushIsEraser
                                                    )
                                                    onUpdateState(true) { st -> st.copy(layers = st.layers + newLayer) }
                                                }
                                                activeStrokePoints.clear()
                                            },
                                            onDragCancel = { activeStrokePoints.clear() }
                                        )
                                    } else {
                                        detectTapGestures { selectedLayerId = null }
                                    }
                                }
                        ) {
                            // Checkerboard hint under transparent cutouts
                            if (state.adjustments.backgroundRemoved &&
                                state.adjustments.cutoutBackdrop == CutoutBackdrop.TRANSPARENT
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val cell = 14.dp.toPx()
                                    var row = 0
                                    var y = 0f
                                    while (y < size.height) {
                                        var col = 0
                                        var x = 0f
                                        while (x < size.width) {
                                            drawRect(
                                                color = if ((row + col) % 2 == 0) Color(0xFFF3EDF4) else Color.White,
                                                topLeft = Offset(x, y),
                                                size = androidx.compose.ui.geometry.Size(cell, cell)
                                            )
                                            x += cell
                                            col++
                                        }
                                        y += cell
                                        row++
                                    }
                                }
                            }

                            if (bmp != null && !bmp.isRecycled) {
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Edited photo canvas",
                                    contentScale = ContentScale.FillBounds,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                AsyncImage(
                                    model = File(state.sourcePath),
                                    contentDescription = "Photo preview",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            // Interactive Sticker & Text Overlays
                            state.layers.filter { it.isVisible }.forEach { layer ->
                                when (layer) {
                                    is PhotoEditLayer.StickerLayer -> {
                                        val sticker = CreativeLibraryCatalog.stickerById(layer.stickerId)
                                        if (sticker != null) {
                                            val isSelected = layer.id == selectedLayerId
                                            val cx = layer.xFraction * boxWpx
                                            val cy = layer.yFraction * boxHpx
                                            Box(
                                                modifier = Modifier
                                                    .offset {
                                                        IntOffset(
                                                            (cx - 48.dp.toPx()).roundToInt(),
                                                            (cy - 32.dp.toPx()).roundToInt()
                                                        )
                                                    }
                                                    .scale(layer.scale)
                                                    .rotate(layer.rotationDeg)
                                                    .alpha(layer.opacity)
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier
                                                                .border(
                                                                    1.5.dp,
                                                                    MaterialTheme.colorScheme.primary,
                                                                    RoundedCornerShape(12.dp)
                                                                )
                                                                .padding(4.dp)
                                                        } else Modifier
                                                    )
                                                    .pointerInput(layer.id, layer.isLocked) {
                                                        detectTapGestures { selectedLayerId = layer.id }
                                                    }
                                                    .pointerInput(layer.id, layer.isLocked, boxWpx, boxHpx) {
                                                        if (!layer.isLocked) {
                                                            detectDragGestures(
                                                                onDragStart = { selectedLayerId = layer.id },
                                                                onDrag = { change, drag ->
                                                                    change.consume()
                                                                    onUpdateState(false) { st ->
                                                                        st.copy(
                                                                            layers = st.layers.map { item ->
                                                                                if (item.id == layer.id && item is PhotoEditLayer.StickerLayer) {
                                                                                    item.copy(
                                                                                        xFraction = (item.xFraction + drag.x / boxWpx).coerceIn(0.05f, 0.95f),
                                                                                        yFraction = (item.yFraction + drag.y / boxHpx).coerceIn(0.05f, 0.95f)
                                                                                    )
                                                                                } else item
                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            )
                                                        }
                                                    }
                                            ) {
                                                StickerVisual(sticker = sticker, iconSize = 36.dp)
                                            }
                                        }
                                    }
                                    is PhotoEditLayer.TextLayer -> {
                                        val isSelected = layer.id == selectedLayerId
                                        val font = remember(layer.fontId) {
                                            EditorFontOption.fromId(layer.fontId).toComposeFontFamily()
                                        }
                                        val cx = layer.xFraction * boxWpx
                                        val cy = layer.yFraction * boxHpx
                                        Box(
                                            modifier = Modifier
                                                .offset {
                                                    IntOffset(
                                                        (cx - 90.dp.toPx()).roundToInt(),
                                                        (cy - 24.dp.toPx()).roundToInt()
                                                    )
                                                }
                                                .width(180.dp)
                                                .scale(layer.scale)
                                                .rotate(layer.rotationDeg)
                                                .alpha(layer.opacity)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier
                                                            .border(
                                                                1.5.dp,
                                                                MaterialTheme.colorScheme.primary,
                                                                RoundedCornerShape(12.dp)
                                                            )
                                                            .padding(2.dp)
                                                    } else Modifier
                                                )
                                                .pointerInput(layer.id) {
                                                    detectTapGestures(
                                                        onTap = { selectedLayerId = layer.id },
                                                        onDoubleTap = {
                                                            selectedLayerId = layer.id
                                                            editingTextLayerId = layer.id
                                                            textDraft = layer.text
                                                            textFontId = layer.fontId
                                                            textColorArgb = layer.textColorArgb
                                                            textHasPill = layer.hasPillBackground
                                                            textPillColorArgb = layer.pillColorArgb
                                                            showTextDialog = true
                                                        }
                                                    )
                                                }
                                                .pointerInput(layer.id, layer.isLocked, boxWpx, boxHpx) {
                                                    if (!layer.isLocked) {
                                                        detectDragGestures(
                                                            onDragStart = { selectedLayerId = layer.id },
                                                            onDrag = { change, drag ->
                                                                change.consume()
                                                                onUpdateState(false) { st ->
                                                                    st.copy(
                                                                        layers = st.layers.map { item ->
                                                                            if (item.id == layer.id && item is PhotoEditLayer.TextLayer) {
                                                                                item.copy(
                                                                                    xFraction = (item.xFraction + drag.x / boxWpx).coerceIn(0.05f, 0.95f),
                                                                                    yFraction = (item.yFraction + drag.y / boxHpx).coerceIn(0.05f, 0.95f)
                                                                                )
                                                                            } else item
                                                                        }
                                                                    )
                                                                }
                                                            }
                                                        )
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .then(
                                                        if (layer.hasPillBackground) {
                                                            Modifier
                                                                .clip(RoundedCornerShape(14.dp))
                                                                .background(Color(layer.pillColorArgb).copy(alpha = 0.88f))
                                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                                        } else Modifier
                                                    )
                                            ) {
                                                Text(
                                                    text = layer.text,
                                                    fontFamily = font,
                                                    fontSize = (layer.fontSizeSp * 0.78f).sp,
                                                    color = Color(layer.textColorArgb),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                    is PhotoEditLayer.DoodleStrokeLayer -> Unit
                                }
                            }

                            // Live Brush / Eraser Stroke Preview
                            if (activeStrokePoints.size >= 2) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val path = Path()
                                    activeStrokePoints.forEachIndexed { i, pt ->
                                        val px = pt.x * size.width
                                        val py = pt.y * size.height
                                        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    }
                                    drawPath(
                                        path = path,
                                        color = if (brushIsEraser) Color.White.copy(alpha = 0.85f) else Color(brushColorArgb),
                                        style = Stroke(
                                            width = (size.minDimension * brushWidthFrac).coerceAtLeast(4f),
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (isRenderingPreview || isProcessingCutout || isSaving) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        shadowElevation = 4.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Text(
                                text = when {
                                    isSaving -> "Saving edited copy..."
                                    isProcessingCutout -> "Removing background..."
                                    else -> "Updating canvas..."
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            // Selected layer quick transform bar (Scale, Rotate, Opacity, Delete)
            if (selectedLayer != null && (selectedLayer is PhotoEditLayer.StickerLayer || selectedLayer is PhotoEditLayer.TextLayer)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedLayer.name,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.width(72.dp)
                        )
                        Text("Size", style = MaterialTheme.typography.labelSmall)
                        Slider(
                            value = selectedLayer.scale,
                            onValueChange = { nextScale ->
                                onUpdateState(false) { st ->
                                    st.copy(
                                        layers = st.layers.map { item ->
                                            if (item.id != selectedLayer.id) item
                                            else when (item) {
                                                is PhotoEditLayer.StickerLayer -> item.copy(scale = nextScale)
                                                is PhotoEditLayer.TextLayer -> item.copy(scale = nextScale)
                                                else -> item
                                            }
                                        }
                                    )
                                }
                            },
                            valueRange = 0.4f..2.6f,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                onUpdateState(true) { st ->
                                    st.copy(
                                        layers = st.layers.map { item ->
                                            if (item.id != selectedLayer.id) item
                                            else when (item) {
                                                is PhotoEditLayer.StickerLayer -> item.copy(rotationDeg = (item.rotationDeg + 15f) % 360f)
                                                is PhotoEditLayer.TextLayer -> item.copy(rotationDeg = (item.rotationDeg + 15f) % 360f)
                                                else -> item
                                            }
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.RotateRight, contentDescription = "Rotate layer", modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = {
                                onUpdateState(true) { st ->
                                    st.copy(layers = st.layers.filterNot { it.id == selectedLayer.id })
                                }
                                selectedLayerId = null
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete layer",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Active Tool Controls Panel
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    when (activeTab) {
                        PhotoToolTab.TEMPLATES -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "240 Aesthetic Templates",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    if (state.adjustments.appliedTemplateId != null) {
                                        TextButton(
                                            onClick = {
                                                onUpdateState(true) { st ->
                                                    st.copy(adjustments = st.adjustments.copy(appliedTemplateId = null))
                                                }
                                            }
                                        ) { Text("Clear") }
                                    }
                                    Button(
                                        onClick = {
                                            libraryInitialTab = 0
                                            showLibraryModal = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                        modifier = Modifier.testTag("photo_browse_all_templates")
                                    ) {
                                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Browse All 240")
                                    }
                                }
                            }
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CreativeLibraryCatalog.templates.take(16), key = { it.id }) { tmpl ->
                                    val selected = state.adjustments.appliedTemplateId == tmpl.id
                                    Card(
                                        onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(
                                                    adjustments = st.adjustments.copy(
                                                        appliedTemplateId = tmpl.id,
                                                        frameStyle = tmpl.frameStyle
                                                    )
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(tmpl.bgStartArgb)),
                                        modifier = Modifier
                                            .width(106.dp)
                                            .border(
                                                width = if (selected) 2.5.dp else 1.dp,
                                                color = if (selected) MaterialTheme.colorScheme.primary else Color(tmpl.borderArgb),
                                                shape = RoundedCornerShape(14.dp)
                                            )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(tmpl.cornerGlyph, fontSize = 18.sp)
                                            Text(
                                                text = tmpl.name,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(tmpl.inkArgb),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        PhotoToolTab.STICKERS -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("120 Cute Stickers", style = MaterialTheme.typography.titleSmall)
                                Button(
                                    onClick = {
                                        libraryInitialTab = 1
                                        showLibraryModal = true
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("photo_browse_all_stickers")
                                ) {
                                    Text("Browse All 120")
                                }
                            }
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(CreativeLibraryCatalog.stickers.take(20), key = { it.id }) { stk ->
                                    Surface(
                                        onClick = {
                                            val layer = PhotoEditLayer.StickerLayer(
                                                name = stk.name,
                                                stickerId = stk.id,
                                                xFraction = 0.5f,
                                                yFraction = 0.45f
                                            )
                                            onUpdateState(true) { st -> st.copy(layers = st.layers + layer) }
                                            selectedLayerId = layer.id
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Box(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                            StickerVisual(sticker = stk, iconSize = 28.dp, showLabel = true)
                                        }
                                    }
                                }
                            }
                        }

                        PhotoToolTab.TEXT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Custom Fonts & Captions",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Button(
                                        onClick = {
                                            editingTextLayerId = null
                                            textDraft = ""
                                            textFontId = EditorFontOption.PLAYFAIR.id
                                            textColorArgb = 0xFF422330L
                                            textHasPill = true
                                            textPillColorArgb = 0xFFFFF4F8L
                                            showTextDialog = true
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("photo_add_text_button")
                                    ) {
                                        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Caption")
                                    }
                                }
                                Text(
                                    text = "Tap a text layer to drag/resize, or double-tap it on the photo to edit font & wording.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        PhotoToolTab.FILTERS -> {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(PhotoFilterPreset.entries) { preset ->
                                    val selected = state.adjustments.filterPreset == preset
                                    FilterChip(
                                        selected = selected,
                                        onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(adjustments = st.adjustments.copy(filterPreset = preset))
                                            }
                                        },
                                        label = { Text(preset.displayName) },
                                        modifier = Modifier.testTag("photo_filter_${preset.name.lowercase()}")
                                    )
                                }
                            }
                        }

                        PhotoToolTab.ADJUST -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Light, Color & Detail", style = MaterialTheme.typography.titleSmall)
                                    TextButton(onClick = {
                                        onUpdateState(true) { st ->
                                            st.copy(
                                                adjustments = st.adjustments.copy(
                                                    brightness = 0f,
                                                    contrast = 1f,
                                                    saturation = 1f,
                                                    exposure = 0f,
                                                    sharpness = 0f,
                                                    blurRadius = 0f
                                                )
                                            )
                                        }
                                    }) { Text("Reset") }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                    item {
                                        AdjustmentSliderChip("Brightness", state.adjustments.brightness, -0.5f..0.5f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(brightness = v)) }
                                        }
                                    }
                                    item {
                                        AdjustmentSliderChip("Contrast", state.adjustments.contrast, 0.5f..1.8f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(contrast = v)) }
                                        }
                                    }
                                    item {
                                        AdjustmentSliderChip("Saturation", state.adjustments.saturation, 0f..2f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(saturation = v)) }
                                        }
                                    }
                                    item {
                                        AdjustmentSliderChip("Exposure", state.adjustments.exposure, -0.5f..0.5f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(exposure = v)) }
                                        }
                                    }
                                    item {
                                        AdjustmentSliderChip("Sharpness", state.adjustments.sharpness, 0f..1f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(sharpness = v)) }
                                        }
                                    }
                                    item {
                                        AdjustmentSliderChip("Blur", state.adjustments.blurRadius, 0f..16f) { v ->
                                            onUpdateState(false) { it.copy(adjustments = it.adjustments.copy(blurRadius = v)) }
                                        }
                                    }
                                }
                            }
                        }

                        PhotoToolTab.CROP -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(PhotoCropAspect.entries) { aspect ->
                                        FilterChip(
                                            selected = state.adjustments.cropAspect == aspect,
                                            onClick = {
                                                val box = computeCenteredAspectBox(aspect.ratio)
                                                onUpdateState(true) { st ->
                                                    st.copy(
                                                        adjustments = st.adjustments.copy(
                                                            cropAspect = aspect,
                                                            cropLeft = box[0],
                                                            cropTop = box[1],
                                                            cropRight = box[2],
                                                            cropBottom = box[3]
                                                        )
                                                    )
                                                }
                                            },
                                            label = { Text(aspect.label) }
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(
                                                    adjustments = st.adjustments.copy(
                                                        rotationQuarterTurns = (st.adjustments.rotationQuarterTurns + 1) % 4
                                                    )
                                                )
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.RotateRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Rotate 90°")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(adjustments = st.adjustments.copy(flipHorizontal = !st.adjustments.flipHorizontal))
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Rounded.Flip, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Flip H")
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Resize:", style = MaterialTheme.typography.labelMedium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        items(ResizePreset.entries) { rp ->
                                            FilterChip(
                                                selected = state.adjustments.resizePreset == rp,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(adjustments = st.adjustments.copy(resizePreset = rp))
                                                    }
                                                },
                                                label = { Text(rp.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        PhotoToolTab.CUTOUT -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = onRemoveBackground,
                                        enabled = !isProcessingCutout,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("photo_remove_bg_button")
                                    ) {
                                        Icon(Icons.Rounded.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (state.adjustments.backgroundRemoved) "Re-run BG Cutout" else "Remove Background (AI)")
                                    }
                                    if (state.adjustments.backgroundRemoved) {
                                        OutlinedButton(onClick = {
                                            onUpdateState(true) { st ->
                                                st.copy(
                                                    adjustments = st.adjustments.copy(
                                                        backgroundRemoved = false,
                                                        cutoutBackdrop = CutoutBackdrop.NONE
                                                    )
                                                )
                                            }
                                        }) {
                                            Text("Restore BG")
                                        }
                                    }
                                }
                                if (state.adjustments.backgroundRemoved) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(CutoutBackdrop.entries.filter { it != CutoutBackdrop.NONE }) { bd ->
                                            FilterChip(
                                                selected = state.adjustments.cutoutBackdrop == bd,
                                                onClick = {
                                                    onUpdateState(true) { st ->
                                                        st.copy(adjustments = st.adjustments.copy(cutoutBackdrop = bd))
                                                    }
                                                },
                                                label = { Text(bd.label) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        PhotoToolTab.DRAW -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    FilterChip(
                                        selected = !brushIsEraser,
                                        onClick = { brushIsEraser = false },
                                        label = { Text("Pastel Brush") }
                                    )
                                    FilterChip(
                                        selected = brushIsEraser,
                                        onClick = { brushIsEraser = true },
                                        label = { Text("Eraser Tool") }
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text("Drag on photo", style = MaterialTheme.typography.labelSmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!brushIsEraser) {
                                        BRUSH_COLORS.forEach { hex ->
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(hex))
                                                    .border(
                                                        width = if (brushColorArgb == hex) 2.5.dp else 1.dp,
                                                        color = if (brushColorArgb == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                                        shape = CircleShape
                                                    )
                                                    .clickable { brushColorArgb = hex }
                                            )
                                        }
                                    } else {
                                        Text("Eraser Size", style = MaterialTheme.typography.labelMedium)
                                    }
                                    Slider(
                                        value = brushWidthFrac,
                                        onValueChange = { brushWidthFrac = it },
                                        valueRange = 0.005f..0.045f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        PhotoToolTab.FRAMES -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(TemplateFrameStyle.entries) { fs ->
                                        FilterChip(
                                            selected = state.adjustments.frameStyle == fs,
                                            onClick = {
                                                onUpdateState(true) { st ->
                                                    st.copy(adjustments = st.adjustments.copy(frameStyle = fs))
                                                }
                                            },
                                            label = { Text(fs.label) }
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Border Thickness", style = MaterialTheme.typography.labelSmall)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Slider(
                                        value = state.adjustments.frameThickness,
                                        onValueChange = { v ->
                                            onUpdateState(false) { st ->
                                                st.copy(adjustments = st.adjustments.copy(frameThickness = v))
                                            }
                                        },
                                        valueRange = 0.02f..0.11f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        PhotoToolTab.LAYERS -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${state.layers.size} layer(s) in stack",
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Button(
                                    onClick = { showLayersSheet = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Manage Layers")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Bottom Tool Selector Strip
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(PhotoToolTab.entries) { tab ->
                            val selected = activeTab == tab
                            Surface(
                                onClick = { activeTab = tab },
                                shape = RoundedCornerShape(14.dp),
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                },
                                modifier = Modifier.testTag("photo_tab_${tab.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
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

    // Template & Sticker Library Browser Dialog
    if (showLibraryModal) {
        TemplateStickerLibraryDialog(
            initialTabIndex = libraryInitialTab,
            showTemplatesTab = true,
            favoriteTemplateIds = favoriteTemplateIds,
            favoriteStickerIds = favoriteStickerIds,
            onToggleFavoriteTemplate = onToggleFavoriteTemplate,
            onToggleFavoriteSticker = onToggleFavoriteSticker,
            onSelectTemplate = { tmpl ->
                onUpdateState(true) { st ->
                    st.copy(
                        adjustments = st.adjustments.copy(
                            appliedTemplateId = tmpl.id,
                            frameStyle = tmpl.frameStyle
                        )
                    )
                }
            },
            onSelectSticker = { stk ->
                val layer = PhotoEditLayer.StickerLayer(
                    name = stk.name,
                    stickerId = stk.id,
                    xFraction = 0.5f,
                    yFraction = 0.45f
                )
                onUpdateState(true) { st -> st.copy(layers = st.layers + layer) }
                selectedLayerId = layer.id
            },
            onDismiss = { showLibraryModal = false }
        )
    }

    // Custom Font & Text Layer Dialog
    if (showTextDialog) {
        AlertDialog(
            onDismissRequest = { showTextDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text(if (editingTextLayerId != null) "Edit Caption" else "Add Text & Custom Font") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = textDraft,
                        onValueChange = { textDraft = it },
                        label = { Text("Caption Text") },
                        placeholder = { Text("e.g., Golden Hour Memories ✨") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("photo_text_dialog_input")
                    )

                    Text("Font Family (10 Bundled Fonts)", style = MaterialTheme.typography.labelMedium)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(EditorFontOption.entries) { fontOpt ->
                            val family = remember(fontOpt.id) { fontOpt.toComposeFontFamily() }
                            FilterChip(
                                selected = textFontId == fontOpt.id,
                                onClick = { textFontId = fontOpt.id },
                                label = {
                                    Text(
                                        text = fontOpt.displayName,
                                        fontFamily = family,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }

                    Text("Ink Color", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BRUSH_COLORS.take(7).forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (textColorArgb == hex) 2.5.dp else 1.dp,
                                        color = if (textColorArgb == hex) MaterialTheme.colorScheme.primary else Color.LightGray,
                                        shape = CircleShape
                                    )
                                    .clickable { textColorArgb = hex }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Pastel Label Pill Backdrop", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = textHasPill, onCheckedChange = { textHasPill = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (textDraft.isNotBlank()) {
                            val existingId = editingTextLayerId
                            if (existingId != null) {
                                onUpdateState(true) { st ->
                                    st.copy(
                                        layers = st.layers.map { l ->
                                            if (l.id == existingId && l is PhotoEditLayer.TextLayer) {
                                                l.copy(
                                                    name = textDraft.take(18),
                                                    text = textDraft,
                                                    fontId = textFontId,
                                                    textColorArgb = textColorArgb,
                                                    hasPillBackground = textHasPill,
                                                    pillColorArgb = textPillColorArgb
                                                )
                                            } else l
                                        }
                                    )
                                }
                            } else {
                                val newText = PhotoEditLayer.TextLayer(
                                    name = textDraft.take(18),
                                    text = textDraft,
                                    fontId = textFontId,
                                    textColorArgb = textColorArgb,
                                    hasPillBackground = textHasPill,
                                    pillColorArgb = textPillColorArgb
                                )
                                onUpdateState(true) { st -> st.copy(layers = st.layers + newText) }
                                selectedLayerId = newText.id
                            }
                        }
                        showTextDialog = false
                    }
                ) { Text("Apply") }
            },
            dismissButton = {
                TextButton(onClick = { showTextDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Layers Manager Sheet
    if (showLayersSheet) {
        ModalBottomSheet(onDismissRequest = { showLayersSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Layers (${state.layers.size})", style = MaterialTheme.typography.headlineSmall)
                if (state.layers.isEmpty()) {
                    Text(
                        text = "Add stickers, captions, or doodle/eraser strokes to manage layer order here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(state.layers.reversed(), key = { _, l -> l.id }) { revIdx, layer ->
                            val actualIdx = state.layers.lastIndex - revIdx
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (layer.id == selectedLayerId) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedLayerId = layer.id }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = layer.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            if (actualIdx < state.layers.lastIndex) {
                                                onUpdateState(true) { st ->
                                                    val list = st.layers.toMutableList()
                                                    val item = list.removeAt(actualIdx)
                                                    list.add(actualIdx + 1, item)
                                                    st.copy(layers = list)
                                                }
                                            }
                                        },
                                        enabled = actualIdx < state.layers.lastIndex
                                    ) {
                                        Icon(Icons.Rounded.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            if (actualIdx > 0) {
                                                onUpdateState(true) { st ->
                                                    val list = st.layers.toMutableList()
                                                    val item = list.removeAt(actualIdx)
                                                    list.add(actualIdx - 1, item)
                                                    st.copy(layers = list)
                                                }
                                            }
                                        },
                                        enabled = actualIdx > 0
                                    ) {
                                        Icon(Icons.Rounded.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = {
                                        onUpdateState(true) { st ->
                                            st.copy(
                                                layers = st.layers.map { item ->
                                                    if (item.id != layer.id) item else when (item) {
                                                        is PhotoEditLayer.StickerLayer -> item.copy(isVisible = !item.isVisible)
                                                        is PhotoEditLayer.TextLayer -> item.copy(isVisible = !item.isVisible)
                                                        is PhotoEditLayer.DoodleStrokeLayer -> item.copy(isVisible = !item.isVisible)
                                                    }
                                                }
                                            )
                                        }
                                    }) {
                                        Icon(
                                            if (layer.isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = "Toggle visibility",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        onUpdateState(true) { st ->
                                            st.copy(
                                                layers = st.layers.map { item ->
                                                    if (item.id != layer.id) item else when (item) {
                                                        is PhotoEditLayer.StickerLayer -> item.copy(isLocked = !item.isLocked)
                                                        is PhotoEditLayer.TextLayer -> item.copy(isLocked = !item.isLocked)
                                                        is PhotoEditLayer.DoodleStrokeLayer -> item.copy(isLocked = !item.isLocked)
                                                    }
                                                }
                                            )
                                        }
                                    }) {
                                        Icon(
                                            if (layer.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                            contentDescription = "Toggle lock",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    IconButton(onClick = {
                                        onUpdateState(true) { st ->
                                            st.copy(layers = st.layers.filterNot { it.id == layer.id })
                                        }
                                    }) {
                                        Icon(
                                            Icons.Rounded.DeleteOutline,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Non-Destructive Save Copy Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Save Edited Photograph") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Your original photograph stays completely untouched. This saves a new high-resolution edited copy.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Also save to Device Pictures", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = exportToDeviceGallery, onCheckedChange = { exportToDeviceGallery = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveDialog = false
                        onSaveCopy(exportToDeviceGallery) { _, _ ->
                            onOpenSavedResultInGallery()
                        }
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Copy")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSaveDialog = false
                        onSaveDraft()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Save Draft")
                }
            }
        )
    }
}

@Composable
private fun AdjustmentSliderChip(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.width(156.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(String.format("%.2f", value), style = MaterialTheme.typography.labelSmall)
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onChange,
            valueRange = range
        )
    }
}

private fun computeCenteredAspectBox(aspect: Float?): FloatArray {
    if (aspect == null) return floatArrayOf(0f, 0f, 1f, 1f)
    return if (aspect >= 1f) {
        val h = (1f / aspect).coerceIn(0.25f, 1f)
        val top = (1f - h) / 2f
        floatArrayOf(0f, top, 1f, top + h)
    } else {
        val w = aspect.coerceIn(0.25f, 1f)
        val left = (1f - w) / 2f
        floatArrayOf(left, 0f, left + w, 1f)
    }
}
