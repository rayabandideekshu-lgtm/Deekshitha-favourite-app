package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.JournalEntryEntity
import com.example.ui.components.stickersFromCsv
import com.example.ui.components.styleForTemplate
import com.example.ui.components.templateFromId
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalListScreen(
    entries: List<JournalEntryEntity>,
    onOpenEntry: (Long) -> Unit,
    onToggleFavorite: (JournalEntryEntity) -> Unit,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchLocation by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) } // 0..11
    var selectedSpecificDayKey by remember { mutableStateOf<String?>(null) }
    var showDateFilterPicker by remember { mutableStateOf(false) }

    val dayKeyFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }
    val cardDateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    val availableYears = remember(entries) {
        entries.map {
            Calendar.getInstance().apply { timeInMillis = it.capturedAtMillis }.get(Calendar.YEAR)
        }.distinct().sortedDescending()
    }

    val filteredEntries = remember(
        entries,
        searchLocation,
        favoritesOnly,
        selectedYear,
        selectedMonth,
        selectedSpecificDayKey
    ) {
        entries.filter { entry ->
            val matchesQuery = searchLocation.isBlank() ||
                entry.place.contains(searchLocation, ignoreCase = true) ||
                entry.caption.contains(searchLocation, ignoreCase = true)
            val matchesFav = !favoritesOnly || entry.isFavorite
            val cal = Calendar.getInstance().apply { timeInMillis = entry.capturedAtMillis }
            val matchesYear = selectedYear == null || cal.get(Calendar.YEAR) == selectedYear
            val matchesMonth = selectedMonth == null || cal.get(Calendar.MONTH) == selectedMonth
            val matchesDay = selectedSpecificDayKey == null ||
                dayKeyFormatter.format(Date(entry.capturedAtMillis)) == selectedSpecificDayKey
            matchesQuery && matchesFav && matchesYear && matchesMonth && matchesDay
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
                text = "My Photo Journal",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your private digital scrapbook of places, outfits & happy moments",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = searchLocation,
                onValueChange = { searchLocation = it },
                placeholder = { Text("Search by location or memory...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchLocation.isNotEmpty()) {
                        IconButton(onClick = { searchLocation = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("journal_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips Row (Favorites, Date picker, Years, Months)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = favoritesOnly,
                        onClick = { favoritesOnly = !favoritesOnly },
                        leadingIcon = {
                            Icon(
                                imageVector = if (favoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("Favorites") },
                        modifier = Modifier.testTag("journal_filter_favorites")
                    )
                }

                item {
                    FilterChip(
                        selected = selectedSpecificDayKey != null,
                        onClick = {
                            if (selectedSpecificDayKey != null) {
                                selectedSpecificDayKey = null
                            } else {
                                showDateFilterPicker = true
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(selectedSpecificDayKey ?: "Pick Date") },
                        modifier = Modifier.testTag("journal_filter_date")
                    )
                }

                items(availableYears) { yr ->
                    FilterChip(
                        selected = selectedYear == yr,
                        onClick = {
                            selectedYear = if (selectedYear == yr) null else yr
                        },
                        label = { Text("$yr") }
                    )
                }

                items((0..11).toList()) { mIdx ->
                    val shortMonth = DateFormatSymbols.getInstance().shortMonths[mIdx]
                    FilterChip(
                        selected = selectedMonth == mIdx,
                        onClick = {
                            selectedMonth = if (selectedMonth == mIdx) null else mIdx
                        },
                        label = { Text(shortMonth) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (entries.isEmpty()) {
                // First-time Scrapbook Empty State
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_empty_journal),
                            contentDescription = "Empty journal illustration",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Your Scrapbook Awaits",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Capture a photograph, write two good things about your day, and keep it beautifully framed here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onOpenCamera,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("journal_empty_open_camera")
                        ) {
                            Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Capture First Memory")
                        }
                    }
                }
            } else if (filteredEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching memories found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                searchLocation = ""
                                favoritesOnly = false
                                selectedYear = null
                                selectedMonth = null
                                selectedSpecificDayKey = null
                            }
                        ) {
                            Text("Clear Filters")
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
                    items(filteredEntries, key = { it.id }) { entry ->
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
                                .testTag("journal_card_${entry.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White)
                                    .padding(5.dp)
                            ) {
                                AsyncImage(
                                    model = File(entry.photoPath),
                                    contentDescription = entry.place,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(12.dp))
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
                                        imageVector = if (entry.isFavorite) {
                                            Icons.Rounded.Favorite
                                        } else {
                                            Icons.Rounded.FavoriteBorder
                                        },
                                        contentDescription = "Favorite memory",
                                        tint = visual.accentColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                if (stickers.isNotEmpty()) {
                                    Text(
                                        text = stickers.first().glyph,
                                        fontSize = 16.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = entry.place.ifBlank { "Sweet Memory" },
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

    if (showDateFilterPicker) {
        val pickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateFilterPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { utcMillis ->
                            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                .apply { timeInMillis = utcMillis }
                            val localCal = Calendar.getInstance().apply {
                                set(
                                    utcCal.get(Calendar.YEAR),
                                    utcCal.get(Calendar.MONTH),
                                    utcCal.get(Calendar.DAY_OF_MONTH)
                                )
                            }
                            selectedSpecificDayKey = dayKeyFormatter.format(localCal.time)
                        }
                        showDateFilterPicker = false
                    }
                ) {
                    Text("Filter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDateFilterPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}
