package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.VideoJournalEntryEntity
import com.example.data.model.JournalSticker
import com.example.data.model.JournalTemplate
import com.example.ui.components.SampleAiBanner
import com.example.ui.components.ScrapbookJournalPreviewCard
import com.example.ui.components.stickersFromCsv
import com.example.ui.components.styleForTemplate
import com.example.ui.components.templateFromId
import com.example.ui.viewmodel.CaptionUiState
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun VideoJournalEditorScreen(
    videoPath: String,
    initialTimestampMillis: Long,
    editingEntry: VideoJournalEntryEntity?,
    captionUiState: CaptionUiState,
    onGenerateAiCaption: (String, String, String, String, String, (String) -> Unit) -> Unit,
    onSaveVideoJournal: (
        editingId: Long?,
        place: String,
        timestampMillis: Long,
        outfit: String,
        goodThingOne: String,
        goodThingTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToGallery: Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var place by remember(editingEntry) { mutableStateOf(editingEntry?.place.orEmpty()) }
    var timestampMillis by remember(editingEntry, initialTimestampMillis) {
        mutableLongStateOf(editingEntry?.capturedAtMillis ?: initialTimestampMillis)
    }
    var outfit by remember(editingEntry) { mutableStateOf(editingEntry?.outfit.orEmpty()) }
    var goodThingOne by remember(editingEntry) { mutableStateOf(editingEntry?.goodThingOne.orEmpty()) }
    var goodThingTwo by remember(editingEntry) { mutableStateOf(editingEntry?.goodThingTwo.orEmpty()) }
    var caption by remember(editingEntry) { mutableStateOf(editingEntry?.caption.orEmpty()) }
    var selectedTemplate by remember(editingEntry) {
        mutableStateOf(
            JournalTemplate.entries.firstOrNull { it.id == editingEntry?.templateId }
                ?: JournalTemplate.PINK_AESTHETIC
        )
    }
    val selectedStickers = remember(editingEntry) {
        mutableStateListOf<JournalSticker>().apply {
            editingEntry?.stickersCsv?.split(",")?.forEach { token ->
                JournalSticker.entries.firstOrNull { it.id == token.trim() }?.let { add(it) }
            }
        }
    }
    var alsoSaveToGallery by remember { mutableStateOf(editingEntry == null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val captionBringIntoViewRequester = remember { BringIntoViewRequester() }
    val dateTimeFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDateTime = remember(timestampMillis) { dateTimeFormatter.format(Date(timestampMillis)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingEntry != null) "Edit Video Journal" else "Create Video Journal",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    InlineVideoPlayer(videoPath = videoPath, modifier = Modifier.fillMaxSize())
                }

                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it; validationError = null },
                    label = { Text("1. PLACE — Where was this video recorded?") },
                    placeholder = { Text("e.g., Sunset boardwalk, Botanical Garden") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("video_journal_place_input")
                )

                Text(text = "2. DATE AND TIME", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestampMillis)))
                    }
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(0.85f).height(48.dp)
                    ) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestampMillis)))
                    }
                }

                OutlinedTextField(
                    value = outfit,
                    onValueChange = { outfit = it },
                    label = { Text("3. OUTFIT / DRESS — What outfit are you wearing?") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = goodThingOne,
                    onValueChange = { goodThingOne = it },
                    label = { Text("4. TWO GOOD THINGS — Happy thing #1") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = goodThingTwo,
                    onValueChange = { goodThingTwo = it },
                    label = { Text("Happy thing #2") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        focusManager.clearFocus(force = true)
                        onGenerateAiCaption(
                            place,
                            formattedDateTime,
                            outfit,
                            goodThingOne,
                            goodThingTwo
                        ) { generated ->
                            caption = generated
                            coroutineScope.launch {
                                captionBringIntoViewRequester.bringIntoView()
                            }
                        }
                    },
                    enabled = captionUiState !is CaptionUiState.Loading,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (captionUiState is CaptionUiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Writing Three-Line Caption...")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Three-Line AI Caption")
                    }
                }

                if (captionUiState is CaptionUiState.Success) {
                    captionUiState.result.notice?.let { notice ->
                        SampleAiBanner(message = notice)
                    }
                }

                OutlinedTextField(
                    value = caption,
                    onValueChange = { caption = it; validationError = null },
                    label = { Text("5. THREE-LINE CAPTION (Editable)") },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(captionBringIntoViewRequester)
                        .testTag("video_journal_caption_input")
                )

                Text(text = "Choose Scrapbook Template", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(JournalTemplate.entries) { template ->
                        val visual = styleForTemplate(template)
                        val selected = selectedTemplate == template
                        Card(
                            onClick = { selectedTemplate = template },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = visual.washiColor.copy(alpha = 0.35f)),
                            modifier = Modifier.border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) MaterialTheme.colorScheme.primary else visual.borderColor,
                                shape = RoundedCornerShape(16.dp)
                            )
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                Text(template.displayName, style = MaterialTheme.typography.labelLarge, color = visual.inkColor)
                                Text(template.tagline, style = MaterialTheme.typography.bodySmall, color = visual.subInkColor)
                            }
                        }
                    }
                }

                Text(text = "Decorative Stickers", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(JournalSticker.entries) { sticker ->
                        val selected = selectedStickers.contains(sticker)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (selected) selectedStickers.remove(sticker)
                                else if (selectedStickers.size < 4) selectedStickers.add(sticker)
                            },
                            label = { Text("${sticker.glyph} ${sticker.label}") }
                        )
                    }
                }

                if (editingEntry == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Also Save Video to Movies Folder", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Always stored privately inside Pose & Memories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = alsoSaveToGallery, onCheckedChange = { alsoSaveToGallery = it })
                    }
                }

                validationError?.let { err ->
                    Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    onClick = {
                        if (place.isBlank() && caption.isBlank()) {
                            validationError = "Please add at least a place or a caption for your video memory."
                            return@Button
                        }
                        onSaveVideoJournal(
                            editingEntry?.id,
                            place.ifBlank { "Special Moment" },
                            timestampMillis,
                            outfit,
                            goodThingOne,
                            goodThingTwo,
                            caption.ifBlank { "A happy video memory saved on $formattedDateTime." },
                            selectedTemplate.id,
                            selectedStickers.joinToString(",") { it.id },
                            alsoSaveToGallery
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("video_journal_save_button")
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editingEntry != null) "Update Video Journal" else "Save Video Memory")
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = timestampMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { pickedUtc ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                .apply { timeInMillis = pickedUtc }
                            val localCal = Calendar.getInstance().apply { timeInMillis = timestampMillis }
                            localCal.set(
                                utcCal.get(Calendar.YEAR),
                                utcCal.get(Calendar.MONTH),
                                utcCal.get(Calendar.DAY_OF_MONTH)
                            )
                            timestampMillis = localCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) { Text("Set Date") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = pickerState) }
    }

    if (showTimePicker) {
        val currentCal = remember(timestampMillis) {
            Calendar.getInstance().apply { timeInMillis = timestampMillis }
        }
        val timeState = rememberTimePickerState(
            initialHour = currentCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = currentCal.get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val updated = Calendar.getInstance().apply {
                            timeInMillis = timestampMillis
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                        }
                        timestampMillis = updated.timeInMillis
                        showTimePicker = false
                    }
                ) { Text("Set Time") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun VideoJournalListScreen(
    entries: List<VideoJournalEntryEntity>,
    onOpenEntry: (Long) -> Unit,
    onToggleFavorite: (VideoJournalEntryEntity) -> Unit,
    onOpenVideoCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchLocation by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }

    val cardDateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val filtered = remember(entries, searchLocation, favoritesOnly) {
        entries.filter { e ->
            val q = searchLocation.isBlank() ||
                e.place.contains(searchLocation, ignoreCase = true) ||
                e.caption.contains(searchLocation, ignoreCase = true)
            val fav = !favoritesOnly || e.isFavorite
            q && fav
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Video Journal",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your motion memories paired with outfits, locations & heartfelt notes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchLocation,
                onValueChange = { searchLocation = it },
                placeholder = { Text("Search video journals...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchLocation.isNotEmpty()) {
                        IconButton(onClick = { searchLocation = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterChip(
                selected = favoritesOnly,
                onClick = { favoritesOnly = !favoritesOnly },
                leadingIcon = {
                    Icon(
                        if (favoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                label = { Text("Favorites") }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (filtered.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (entries.isEmpty()) "No Video Journals Yet" else "No Matching Video Journals",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Record a guided video pose and turn the clip into a scrapbook journal entry.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (entries.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenVideoCamera,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Videocam, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Record Video Memory")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 162.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered, key = { it.id }) { entry ->
                        val template = templateFromId(entry.templateId)
                        val visual = styleForTemplate(template)
                        val stickers = stickersFromCsv(entry.stickersCsv)

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .background(visual.bgBrush)
                                .border(1.dp, visual.borderColor, RoundedCornerShape(22.dp))
                                .clickable { onOpenEntry(entry.id) }
                                .padding(10.dp)
                                .testTag("video_journal_card_${entry.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(5.dp)
                            ) {
                                if (entry.thumbnailPath.isNotBlank() && File(entry.thumbnailPath).exists()) {
                                    AsyncImage(
                                        model = File(entry.thumbnailPath),
                                        contentDescription = entry.place,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(visual.washiColor.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Videocam,
                                            contentDescription = null,
                                            tint = visual.accentColor,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Rounded.PlayCircle,
                                    contentDescription = "Video",
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.align(Alignment.Center).size(34.dp)
                                )

                                IconButton(
                                    onClick = { onToggleFavorite(entry) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.85f))
                                ) {
                                    Icon(
                                        imageVector = if (entry.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = "Favorite",
                                        tint = visual.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (stickers.isNotEmpty()) {
                                    Text(
                                        text = stickers.first().glyph,
                                        fontSize = 16.sp,
                                        modifier = Modifier.align(Alignment.BottomStart).padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = entry.place.ifBlank { "Video Memory" },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = visual.inkColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = cardDateFormatter.format(Date(entry.capturedAtMillis)),
                                style = MaterialTheme.typography.bodySmall,
                                color = visual.subInkColor,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = entry.caption,
                                style = MaterialTheme.typography.bodySmall,
                                color = visual.inkColor.copy(alpha = 0.85f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailScreen(
    entry: VideoJournalEntryEntity?,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onEditVideoInStudio: (VideoJournalEntryEntity) -> Unit = {},
    onToggleFavorite: (VideoJournalEntryEntity) -> Unit,
    onDownloadVideo: (String, Long) -> Unit,
    getShareUri: (String) -> android.net.Uri?,
    onArchive: (VideoJournalEntryEntity) -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var showArchiveConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = entry?.place?.ifBlank { "Video Memory" } ?: "Video Memory",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (entry != null) {
                        IconButton(onClick = { onToggleFavorite(entry) }) {
                            Icon(
                                imageVector = if (entry.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Toggle favorite",
                                tint = if (entry.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onEdit(entry.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Edit video journal")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        if (entry == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("This video memory could not be found.")
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        InlineVideoPlayer(videoPath = entry.videoPath, modifier = Modifier.fillMaxSize())
                    }

                    ScrapbookJournalPreviewCard(
                        photoPath = entry.thumbnailPath,
                        place = entry.place,
                        capturedAtMillis = entry.capturedAtMillis,
                        outfit = entry.outfit,
                        goodThingOne = entry.goodThingOne,
                        goodThingTwo = entry.goodThingTwo,
                        caption = entry.caption,
                        template = templateFromId(entry.templateId),
                        stickers = stickersFromCsv(entry.stickersCsv)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onDownloadVideo(entry.videoPath, entry.videoId) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Video")
                        }

                        OutlinedButton(
                            onClick = {
                                val uri = getShareUri(entry.videoPath) ?: return@OutlinedButton
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "video/mp4"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_TEXT, entry.caption)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share memory video"))
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }

                    OutlinedButton(
                        onClick = { onEditVideoInStudio(entry) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("video_detail_studio_button")
                    ) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit in Pro Video Studio")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEdit(entry.id) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Entry")
                        }

                        OutlinedButton(
                            onClick = { showArchiveConfirm = true },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("video_detail_delete_button")
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showArchiveConfirm && entry != null) {
        AlertDialog(
            onDismissRequest = { showArchiveConfirm = false },
            title = { Text("Move to Archive?") },
            text = { Text("This video journal entry will be moved to My Archive. You can restore it anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        showArchiveConfirm = false
                        onArchive(entry)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Move to Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { showArchiveConfirm = false }) { Text("Cancel") }
            }
        )
    }
}
