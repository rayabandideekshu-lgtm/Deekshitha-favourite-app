package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Preview
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.JournalEntryEntity
import com.example.data.model.JournalSticker
import com.example.data.model.JournalTemplate
import com.example.ui.components.SampleAiBanner
import com.example.ui.components.ScrapbookJournalPreviewCard
import com.example.ui.components.stickersFromCsv
import com.example.ui.components.styleForTemplate
import com.example.ui.components.templateFromId
import com.example.ui.viewmodel.CaptionUiState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val QUICK_EMOJIS = listOf("🌸", "✨", "💖", "📸", "🎀", "🌷", "☁️", "🦋", "☕", "🌿")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun JournalEditorScreen(
    photoPath: String,
    initialTimestampMillis: Long,
    editingEntry: JournalEntryEntity?,
    captionUiState: CaptionUiState,
    onGenerateAiCaption: (place: String, formattedDate: String, outfit: String, goodOne: String, goodTwo: String, onDone: (String) -> Unit) -> Unit,
    onSaveJournal: (
        editingEntryId: Long?,
        place: String,
        capturedAtMillis: Long,
        outfit: String,
        goodOne: String,
        goodTwo: String,
        caption: String,
        templateId: String,
        stickersCsv: String,
        alsoSaveToGallery: Boolean
    ) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var place by remember(editingEntry) { mutableStateOf(editingEntry?.place ?: "") }
    var capturedAtMillis by remember(editingEntry, initialTimestampMillis) {
        mutableLongStateOf(editingEntry?.capturedAtMillis ?: initialTimestampMillis)
    }
    var outfit by remember(editingEntry) { mutableStateOf(editingEntry?.outfit ?: "") }
    var goodThingOne by remember(editingEntry) { mutableStateOf(editingEntry?.goodThingOne ?: "") }
    var goodThingTwo by remember(editingEntry) { mutableStateOf(editingEntry?.goodThingTwo ?: "") }
    var caption by remember(editingEntry) { mutableStateOf(editingEntry?.caption ?: "") }
    var selectedTemplate by remember(editingEntry) {
        mutableStateOf(
            editingEntry?.let { templateFromId(it.templateId) } ?: JournalTemplate.PINK_AESTHETIC
        )
    }
    val selectedStickers = remember(editingEntry) {
        mutableStateListOf<JournalSticker>().apply {
            editingEntry?.let { addAll(stickersFromCsv(it.stickersCsv)) }
        }
    }
    var alsoSaveToGallery by remember { mutableStateOf(editingEntry == null) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showPreviewModal by remember { mutableStateOf(false) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val captionBringIntoViewRequester = remember { BringIntoViewRequester() }

    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val fullDateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    LaunchedEffect(editingEntry) {
        if (editingEntry != null) {
            place = editingEntry.place
            capturedAtMillis = editingEntry.capturedAtMillis
            outfit = editingEntry.outfit
            goodThingOne = editingEntry.goodThingOne
            goodThingTwo = editingEntry.goodThingTwo
            caption = editingEntry.caption
            selectedTemplate = templateFromId(editingEntry.templateId)
            selectedStickers.clear()
            selectedStickers.addAll(stickersFromCsv(editingEntry.stickersCsv))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingEntry != null) "Edit Memory Journal" else "Create Photo Journal",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("editor_back_button")) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LaunchedEffect(captionUiState) {
            if (captionUiState is CaptionUiState.Success) {
                caption = captionUiState.result.threeLineCaption
                captionBringIntoViewRequester.bringIntoView()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
                .imePadding(),
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
                // Photograph at the top
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        AsyncImage(
                            model = File(editingEntry?.photoPath ?: photoPath),
                            contentDescription = "Journal photograph",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .clip(RoundedCornerShape(18.dp))
                        )
                    }
                }

                // 1. PLACE
                OutlinedTextField(
                    value = place,
                    onValueChange = {
                        place = it
                        validationMessage = null
                    },
                    label = { Text("1. Place — Where was this photograph taken?") },
                    placeholder = { Text("e.g., Botanical Garden, Grandma's Porch, Sunset Beach") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_place_input")
                )

                // 2. DATE AND TIME
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "2. Date and Time",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDatePicker = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("editor_date_button")
                        ) {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(dateFormatter.format(Date(capturedAtMillis)), maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = { showTimePicker = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.75f)
                                .height(48.dp)
                                .testTag("editor_time_button")
                        ) {
                            Icon(
                                Icons.Rounded.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(timeFormatter.format(Date(capturedAtMillis)), maxLines = 1)
                        }
                    }
                }

                // 3. OUTFIT / DRESS
                OutlinedTextField(
                    value = outfit,
                    onValueChange = { outfit = it },
                    label = { Text("3. Outfit / Dress — What outfit are you wearing?") },
                    placeholder = { Text("e.g., Lavender linen sundress and white sneakers") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("editor_outfit_input")
                )

                // 4. TWO GOOD THINGS
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "4. Two Good Things — Happy moments from your day",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = goodThingOne,
                        onValueChange = { goodThingOne = it },
                        label = { Text("First good thing") },
                        placeholder = { Text("e.g., Morning iced latte in the sun") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_good_one_input")
                    )
                    OutlinedTextField(
                        value = goodThingTwo,
                        onValueChange = { goodThingTwo = it },
                        label = { Text("Second good thing") },
                        placeholder = { Text("e.g., Long walk and laughs with loved ones") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("editor_good_two_input")
                    )
                }

                // AI 3-Line Caption Section
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                        validationMessage = null
                        onGenerateAiCaption(
                            place,
                            fullDateFormatter.format(Date(capturedAtMillis)),
                            outfit,
                            goodThingOne,
                            goodThingTwo
                        ) { generated ->
                            caption = generated
                        }
                    },
                    enabled = captionUiState !is CaptionUiState.Loading,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("editor_generate_caption_button")
                ) {
                    if (captionUiState is CaptionUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crafting Three-Line Caption...")
                    } else {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Three-Line AI Caption")
                    }
                }

                when (captionUiState) {
                    is CaptionUiState.Success -> {
                        captionUiState.result.notice?.let { notice ->
                            SampleAiBanner(message = notice)
                        }
                    }
                    is CaptionUiState.Error -> {
                        Text(
                            text = captionUiState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    else -> Unit
                }

                // Editable Caption
                OutlinedTextField(
                    value = caption,
                    onValueChange = {
                        caption = it
                        validationMessage = null
                    },
                    label = { Text("Journal Caption (3 lines, editable)") },
                    placeholder = {
                        Text("Line 1: Place & date/time\nLine 2: Outfit\nLine 3: Two good things")
                    },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .bringIntoViewRequester(captionBringIntoViewRequester)
                        .testTag("editor_caption_input")
                )

                // Quick Emoji Picker
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Add Emojis to Caption",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(QUICK_EMOJIS) { emoji ->
                            Card(
                                onClick = {
                                    caption = if (caption.isBlank()) emoji else "$caption $emoji"
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text(
                                    text = emoji,
                                    fontSize = 20.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Choose Journal Theme (6 templates)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose Journal Template",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(JournalTemplate.entries) { template ->
                            val visual = styleForTemplate(template)
                            val isSelected = template == selectedTemplate
                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(visual.bgBrush)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else visual.borderColor,
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { selectedTemplate = template }
                                    .padding(12.dp)
                                    .testTag("template_${template.id}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = template.displayName,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = visual.inkColor,
                                        maxLines = 1
                                    )
                                    if (isSelected) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = "Selected",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = template.tagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = visual.subInkColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // Add Cute Stickers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Add Cute Stickers",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JournalSticker.entries.forEach { sticker ->
                            val active = sticker in selectedStickers
                            FilterChip(
                                selected = active,
                                onClick = {
                                    if (active) selectedStickers.remove(sticker)
                                    else selectedStickers.add(sticker)
                                },
                                label = { Text("${sticker.glyph} ${sticker.label}") },
                                modifier = Modifier.testTag("sticker_${sticker.id}")
                            )
                        }
                    }
                }

                if (editingEntry == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Also save photo to device gallery",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Saves a copy in Pictures/PoseAndMemories",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alsoSaveToGallery,
                            onCheckedChange = { alsoSaveToGallery = it }
                        )
                    }
                }

                validationMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Preview & Save Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showPreviewModal = true },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .height(52.dp)
                            .testTag("editor_preview_button")
                    ) {
                        Icon(Icons.Rounded.Preview, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Preview")
                    }

                    Button(
                        onClick = {
                            if (place.isBlank() && caption.isBlank()) {
                                validationMessage = "Please add at least a place or a journal caption before saving."
                                return@Button
                            }
                            val finalCaption = if (caption.isBlank()) {
                                val line1 = "Captured at ${place.ifBlank { "a special spot" }} on ${fullDateFormatter.format(Date(capturedAtMillis))}."
                                val line2 = if (outfit.isNotBlank()) "Wearing $outfit." else ""
                                val line3 = listOf(goodThingOne, goodThingTwo)
                                    .filter { it.isNotBlank() }
                                    .joinToString(", ")
                                    .let { if (it.isNotBlank()) "Good things today: $it." else "" }
                                listOf(line1, line2, line3).filter { it.isNotBlank() }.joinToString("\n")
                            } else {
                                caption
                            }
                            onSaveJournal(
                                editingEntry?.id,
                                place,
                                capturedAtMillis,
                                outfit,
                                goodThingOne,
                                goodThingTwo,
                                finalCaption,
                                selectedTemplate.id,
                                selectedStickers.joinToString(",") { it.id },
                                alsoSaveToGallery
                            )
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(52.dp)
                            .testTag("editor_save_button")
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (editingEntry != null) "Save Changes" else "Save Journal")
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = capturedAtMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { pickedUtcMillis ->
                            val currentCal = Calendar.getInstance().apply { timeInMillis = capturedAtMillis }
                            val pickedCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                .apply { timeInMillis = pickedUtcMillis }
                            currentCal.set(
                                pickedCal.get(Calendar.YEAR),
                                pickedCal.get(Calendar.MONTH),
                                pickedCal.get(Calendar.DAY_OF_MONTH)
                            )
                            capturedAtMillis = currentCal.timeInMillis
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val currentCal = remember(capturedAtMillis) {
            Calendar.getInstance().apply { timeInMillis = capturedAtMillis }
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
                        val updatedCal = Calendar.getInstance().apply {
                            timeInMillis = capturedAtMillis
                            set(Calendar.HOUR_OF_DAY, timeState.hour)
                            set(Calendar.MINUTE, timeState.minute)
                        }
                        capturedAtMillis = updatedCal.timeInMillis
                        showTimePicker = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPreviewModal) {
        AlertDialog(
            onDismissRequest = { showPreviewModal = false },
            title = { Text("Journal Scrapbook Preview") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ScrapbookJournalPreviewCard(
                        photoPath = editingEntry?.photoPath ?: photoPath,
                        place = place,
                        capturedAtMillis = capturedAtMillis,
                        outfit = outfit,
                        goodThingOne = goodThingOne,
                        goodThingTwo = goodThingTwo,
                        caption = caption,
                        template = selectedTemplate,
                        stickers = selectedStickers.toList()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPreviewModal = false }) {
                    Text("Looks Lovely")
                }
            }
        )
    }
}
