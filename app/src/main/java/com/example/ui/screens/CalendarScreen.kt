package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.JournalEntryEntity
import com.example.data.local.PhotoEntity
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    photos: List<PhotoEntity>,
    journalEntries: List<JournalEntryEntity>,
    onOpenJournalEntry: (Long) -> Unit,
    onCreateJournalForPhoto: (PhotoEntity) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val now = remember { Calendar.getInstance() }
    var currentYear by remember { mutableIntStateOf(now.get(Calendar.YEAR)) }
    var currentMonth by remember { mutableIntStateOf(now.get(Calendar.MONTH)) } // 0..11
    var selectedDayOfMonth by remember { mutableIntStateOf(now.get(Calendar.DAY_OF_MONTH)) }

    val dayKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val monthNames = remember { DateFormatSymbols.getInstance().months }

    val entriesByDay = remember(journalEntries) {
        journalEntries.groupBy { dayKeyFormatter.format(Date(it.capturedAtMillis)) }
    }
    val photosByDay = remember(photos) {
        photos.groupBy { dayKeyFormatter.format(Date(it.capturedAtMillis)) }
    }

    val monthCalendar = remember(currentYear, currentMonth) {
        Calendar.getInstance().apply {
            clear()
            set(currentYear, currentMonth, 1)
        }
    }
    val firstDayOffset = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1 // Sun = 0
    val daysInMonth = monthCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val selectedDayKey = remember(currentYear, currentMonth, selectedDayOfMonth) {
        val c = Calendar.getInstance().apply {
            clear()
            set(currentYear, currentMonth, selectedDayOfMonth.coerceAtMost(daysInMonth))
        }
        dayKeyFormatter.format(c.time)
    }
    val selectedEntries = entriesByDay[selectedDayKey].orEmpty()
    val selectedPhotos = photosByDay[selectedDayKey].orEmpty()
    val journalPhotoIds = remember(selectedEntries) { selectedEntries.map { it.photoId }.toSet() }
    val standaloneDayPhotos = selectedPhotos.filter { it.id !in journalPhotoIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Calendar", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calendar_back_button")) {
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
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Month & Year Navigation Bar
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (currentMonth == 0) {
                                            currentMonth = 11
                                            currentYear--
                                        } else {
                                            currentMonth--
                                        }
                                        selectedDayOfMonth = 1
                                    },
                                    modifier = Modifier.testTag("calendar_prev_month")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${monthNames[currentMonth]} $currentYear",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (currentMonth == 11) {
                                            currentMonth = 0
                                            currentYear++
                                        } else {
                                            currentMonth++
                                        }
                                        selectedDayOfMonth = 1
                                    },
                                    modifier = Modifier.testTag("calendar_next_month")
                                ) {
                                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Weekday Headers
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Calendar Cells
                            val totalCells = firstDayOffset + daysInMonth
                            val rows = (totalCells + 6) / 7
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                for (rowIdx in 0 until rows) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        for (colIdx in 0 until 7) {
                                            val cellNumber = rowIdx * 7 + colIdx - firstDayOffset + 1
                                            if (cellNumber in 1..daysInMonth) {
                                                val c = Calendar.getInstance().apply {
                                                    clear()
                                                    set(currentYear, currentMonth, cellNumber)
                                                }
                                                val key = dayKeyFormatter.format(c.time)
                                                val dayPhotos = photosByDay[key].orEmpty()
                                                val dayEntries = entriesByDay[key].orEmpty()
                                                val hasMemory = dayPhotos.isNotEmpty() || dayEntries.isNotEmpty()
                                                val thumbPath = dayEntries.firstOrNull()?.photoPath
                                                    ?: dayPhotos.firstOrNull()?.filePath
                                                val isSelected = cellNumber == selectedDayOfMonth.coerceAtMost(daysInMonth)

                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(0.92f)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(
                                                            when {
                                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                                hasMemory -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                                                else -> Color.Transparent
                                                            }
                                                        )
                                                        .border(
                                                            width = if (isSelected) 1.8.dp else 0.dp,
                                                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                            shape = RoundedCornerShape(12.dp)
                                                        )
                                                        .clickable { selectedDayOfMonth = cellNumber }
                                                        .padding(3.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center
                                                    ) {
                                                        Text(
                                                            text = "$cellNumber",
                                                            style = MaterialTheme.typography.labelMedium.copy(
                                                                fontWeight = if (hasMemory || isSelected) FontWeight.Bold else FontWeight.Normal
                                                            ),
                                                            color = if (isSelected) {
                                                                MaterialTheme.colorScheme.onPrimaryContainer
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurface
                                                            }
                                                        )
                                                        if (thumbPath != null) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            AsyncImage(
                                                                model = File(thumbPath),
                                                                contentDescription = "Memory thumbnail",
                                                                contentScale = ContentScale.Crop,
                                                                modifier = Modifier
                                                                    .size(20.dp)
                                                                    .clip(CircleShape)
                                                            )
                                                        }
                                                    }
                                                }
                                            } else {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Day Memories Header
                item {
                    Text(
                        text = "${monthNames[currentMonth]} ${selectedDayOfMonth.coerceAtMost(daysInMonth)}, $currentYear",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (selectedEntries.isEmpty() && standaloneDayPhotos.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No memories recorded on this date.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                } else {
                    items(selectedEntries, key = { "entry_${it.id}" }) { entry ->
                        Card(
                            onClick = { onOpenJournalEntry(entry.id) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = File(entry.photoPath),
                                    contentDescription = entry.place,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.place.ifBlank { "Journal Memory" },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = entry.caption,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    items(standaloneDayPhotos, key = { "photo_${it.id}" }) { photo ->
                        Card(
                            onClick = { onCreateJournalForPhoto(photo) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = File(photo.filePath),
                                    contentDescription = "Saved photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Saved Photograph",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = "Tap to write a photo journal for this snapshot",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
