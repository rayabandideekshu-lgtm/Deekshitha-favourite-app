package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.ArchiveFilterType
import com.example.data.model.ArchiveItemType
import com.example.data.model.ArchivedMemoryItem
import java.io.File
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveScreen(
    archivedItems: List<ArchivedMemoryItem>,
    onRestoreItem: (ArchivedMemoryItem) -> Unit,
    onRestoreAll: () -> Unit,
    onPermanentlyDeleteItem: (ArchivedMemoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ArchiveFilterType.ALL) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var deleteConfirmTarget by remember { mutableStateOf<ArchivedMemoryItem?>(null) }
    var showRestoreAllConfirm by remember { mutableStateOf(false) }

    val dateTimeFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    val availableYears = remember(archivedItems) {
        archivedItems.map {
            Calendar.getInstance().apply { timeInMillis = it.originalTimestampMillis }.get(Calendar.YEAR)
        }.distinct().sortedDescending()
    }

    val filteredItems = remember(archivedItems, selectedFilter, selectedYear, selectedMonth, searchQuery) {
        archivedItems.filter { item ->
            val matchesType = when (selectedFilter) {
                ArchiveFilterType.ALL -> true
                ArchiveFilterType.PHOTOS -> item.type == ArchiveItemType.PHOTO
                ArchiveFilterType.VIDEOS -> item.type == ArchiveItemType.VIDEO
                ArchiveFilterType.PHOTO_JOURNALS -> item.type == ArchiveItemType.PHOTO_JOURNAL
                ArchiveFilterType.VIDEO_JOURNALS -> item.type == ArchiveItemType.VIDEO_JOURNAL
            }
            val cal = Calendar.getInstance().apply { timeInMillis = item.originalTimestampMillis }
            val matchesYear = selectedYear == null || cal.get(Calendar.YEAR) == selectedYear
            val matchesMonth = selectedMonth == null || cal.get(Calendar.MONTH) == selectedMonth
            val matchesSearch = searchQuery.isBlank() ||
                item.caption.contains(searchQuery, ignoreCase = true) ||
                item.place.contains(searchQuery, ignoreCase = true) ||
                item.type.label.contains(searchQuery, ignoreCase = true)
            matchesType && matchesYear && matchesMonth && matchesSearch
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Archive",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Deleted photos, videos, and journals stay safely here until restored or permanently deleted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (archivedItems.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showRestoreAllConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("archive_restore_all_button")
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restore All", maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search archived items...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("archive_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ArchiveFilterType.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) },
                        modifier = Modifier.testTag("archive_filter_${filter.name.lowercase()}")
                    )
                }
                items(availableYears) { yr ->
                    FilterChip(
                        selected = selectedYear == yr,
                        onClick = { selectedYear = if (selectedYear == yr) null else yr },
                        label = { Text("$yr") }
                    )
                }
                items((0..11).toList()) { mIdx ->
                    val shortMonth = DateFormatSymbols.getInstance().shortMonths[mIdx]
                    FilterChip(
                        selected = selectedMonth == mIdx,
                        onClick = { selectedMonth = if (selectedMonth == mIdx) null else mIdx },
                        label = { Text(shortMonth) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredItems.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (archivedItems.isEmpty()) "Your Archive Is Empty" else "No Archived Items Match",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "When you delete any photo, video, photo journal, or video journal, it safely rests here so nothing is ever lost by mistake.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredItems, key = { it.uniqueKey }) { item ->
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("archive_card_${item.uniqueKey}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (item.thumbnailPath.isNotBlank() && File(item.thumbnailPath).exists()) {
                                            AsyncImage(
                                                model = File(item.thumbnailPath),
                                                contentDescription = item.type.label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (item.type == ArchiveItemType.VIDEO || item.type == ArchiveItemType.VIDEO_JOURNAL) {
                                                    Icons.Rounded.Videocam
                                                } else {
                                                    Icons.Rounded.Photo
                                                },
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = item.type.label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (item.place.isNotBlank()) {
                                            Text(
                                                text = item.place,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = item.caption.ifBlank { "No caption" },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = "Original: ${dateTimeFormatter.format(Date(item.originalTimestampMillis))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Archived: ${dateTimeFormatter.format(Date(item.archivedAtMillis))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { onRestoreItem(item) },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(44.dp)
                                            .testTag("archive_restore_${item.uniqueKey}")
                                    ) {
                                        Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("RESTORE", maxLines = 1)
                                    }

                                    OutlinedButton(
                                        onClick = { deleteConfirmTarget = item },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        ),
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .height(44.dp)
                                            .testTag("archive_perm_delete_${item.uniqueKey}")
                                    ) {
                                        Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PERMANENTLY DELETE", style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteConfirmTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { deleteConfirmTarget = null },
            title = { Text("Permanently Delete ${target.type.label}?") },
            text = {
                Text("This permanently deletes this ${target.type.label.lowercase()} and cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPermanentlyDeleteItem(target)
                        deleteConfirmTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("archive_confirm_perm_delete")
                ) {
                    Text("Permanently Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRestoreAllConfirm) {
        AlertDialog(
            onDismissRequest = { showRestoreAllConfirm = false },
            title = { Text("Restore All Archived Items?") },
            text = {
                Text("All ${archivedItems.size} archived items will be restored to their original galleries and journals.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreAllConfirm = false
                        onRestoreAll()
                    }
                ) {
                    Text("Restore All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreAllConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
