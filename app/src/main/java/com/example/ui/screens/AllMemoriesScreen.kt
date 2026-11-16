package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.PlayCircle
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.JournalEntryEntity
import com.example.data.local.PhotoEntity
import com.example.data.local.VideoEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.data.model.ArchiveFilterType
import com.example.data.model.ArchiveItemType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class UnifiedMemoryRow(
    val key: String,
    val type: ArchiveItemType,
    val title: String,
    val subtitle: String,
    val thumbnailPath: String,
    val timestampMillis: Long,
    val isFavorite: Boolean,
    val photo: PhotoEntity? = null,
    val video: VideoEntity? = null,
    val photoJournal: JournalEntryEntity? = null,
    val videoJournal: VideoJournalEntryEntity? = null
)

@Composable
fun AllMemoriesScreen(
    photos: List<PhotoEntity>,
    videos: List<VideoEntity>,
    photoJournals: List<JournalEntryEntity>,
    videoJournals: List<VideoJournalEntryEntity>,
    onOpenPhotoGallery: () -> Unit,
    onOpenVideoGallery: () -> Unit,
    onOpenPhotoJournalDetail: (Long) -> Unit,
    onOpenVideoJournalDetail: (Long) -> Unit,
    onCreateJournalForPhoto: (PhotoEntity) -> Unit,
    onCreateJournalForVideo: (VideoEntity) -> Unit,
    onArchivePhoto: (PhotoEntity) -> Unit,
    onArchiveVideo: (VideoEntity) -> Unit,
    onArchivePhotoJournal: (JournalEntryEntity) -> Unit,
    onArchiveVideoJournal: (VideoJournalEntryEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ArchiveFilterType.ALL) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var archiveTarget by remember { mutableStateOf<UnifiedMemoryRow?>(null) }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

    val allRows = remember(photos, videos, photoJournals, videoJournals) {
        buildList {
            photoJournals.forEach { e ->
                add(
                    UnifiedMemoryRow(
                        key = "pj_${e.id}",
                        type = ArchiveItemType.PHOTO_JOURNAL,
                        title = e.place.ifBlank { "Photo Journal" },
                        subtitle = e.caption,
                        thumbnailPath = e.photoPath,
                        timestampMillis = e.capturedAtMillis,
                        isFavorite = e.isFavorite,
                        photoJournal = e
                    )
                )
            }
            videoJournals.forEach { vj ->
                add(
                    UnifiedMemoryRow(
                        key = "vj_${vj.id}",
                        type = ArchiveItemType.VIDEO_JOURNAL,
                        title = vj.place.ifBlank { "Video Journal" },
                        subtitle = vj.caption,
                        thumbnailPath = vj.thumbnailPath,
                        timestampMillis = vj.capturedAtMillis,
                        isFavorite = vj.isFavorite,
                        videoJournal = vj
                    )
                )
            }
            photos.forEach { p ->
                add(
                    UnifiedMemoryRow(
                        key = "p_${p.id}",
                        type = ArchiveItemType.PHOTO,
                        title = "Photograph",
                        subtitle = dateFormatter.format(Date(p.capturedAtMillis)),
                        thumbnailPath = p.filePath,
                        timestampMillis = p.capturedAtMillis,
                        isFavorite = p.isFavorite,
                        photo = p
                    )
                )
            }
            videos.forEach { v ->
                add(
                    UnifiedMemoryRow(
                        key = "v_${v.id}",
                        type = ArchiveItemType.VIDEO,
                        title = "Video Clip",
                        subtitle = dateFormatter.format(Date(v.capturedAtMillis)),
                        thumbnailPath = v.thumbnailPath,
                        timestampMillis = v.capturedAtMillis,
                        isFavorite = v.isFavorite,
                        video = v
                    )
                )
            }
        }.sortedByDescending { it.timestampMillis }
    }

    val filteredRows = remember(allRows, selectedFilter, favoritesOnly, searchQuery) {
        allRows.filter { row ->
            val matchesType = when (selectedFilter) {
                ArchiveFilterType.ALL -> true
                ArchiveFilterType.PHOTOS -> row.type == ArchiveItemType.PHOTO
                ArchiveFilterType.VIDEOS -> row.type == ArchiveItemType.VIDEO
                ArchiveFilterType.PHOTO_JOURNALS -> row.type == ArchiveItemType.PHOTO_JOURNAL
                ArchiveFilterType.VIDEO_JOURNALS -> row.type == ArchiveItemType.VIDEO_JOURNAL
            }
            val matchesFav = !favoritesOnly || row.isFavorite
            val matchesSearch = searchQuery.isBlank() ||
                row.title.contains(searchQuery, ignoreCase = true) ||
                row.subtitle.contains(searchQuery, ignoreCase = true)
            matchesType && matchesFav && matchesSearch
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
                text = "All Memories",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${photos.size} photos • ${videos.size} videos • ${photoJournals.size + videoJournals.size} journals",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onOpenPhotoGallery,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("all_memories_photo_gallery_btn")
                ) {
                    Icon(Icons.Rounded.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Photo Gallery (${photos.size})", maxLines = 1)
                }
                OutlinedButton(
                    onClick = onOpenVideoGallery,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("all_memories_video_gallery_btn")
                ) {
                    Icon(Icons.Rounded.Videocam, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Video Gallery (${videos.size})", maxLines = 1)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search all photos, videos & journals...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ArchiveFilterType.entries) { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label) },
                        modifier = Modifier.testTag("all_memories_filter_${filter.name.lowercase()}")
                    )
                }
                item {
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
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (filteredRows.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No memories match this filter yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRows, key = { it.key }) { row ->
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    when (row.type) {
                                        ArchiveItemType.PHOTO_JOURNAL -> row.photoJournal?.let { onOpenPhotoJournalDetail(it.id) }
                                        ArchiveItemType.VIDEO_JOURNAL -> row.videoJournal?.let { onOpenVideoJournalDetail(it.id) }
                                        ArchiveItemType.PHOTO -> row.photo?.let { onCreateJournalForPhoto(it) }
                                        ArchiveItemType.VIDEO -> row.video?.let { onCreateJournalForVideo(it) }
                                    }
                                }
                                .testTag("all_memory_row_${row.key}")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (row.thumbnailPath.isNotBlank() && File(row.thumbnailPath).exists()) {
                                        AsyncImage(
                                            model = File(row.thumbnailPath),
                                            contentDescription = row.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Icon(
                                            imageVector = when (row.type) {
                                                ArchiveItemType.VIDEO, ArchiveItemType.VIDEO_JOURNAL -> Icons.Rounded.Videocam
                                                else -> Icons.Rounded.CameraAlt
                                            },
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (row.type == ArchiveItemType.VIDEO || row.type == ArchiveItemType.VIDEO_JOURNAL) {
                                        Icon(
                                            Icons.Rounded.PlayCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = row.type.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = row.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = row.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = dateFormatter.format(Date(row.timestampMillis)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = { archiveTarget = row },
                                    modifier = Modifier.testTag("all_memory_delete_${row.key}")
                                ) {
                                    Icon(
                                        Icons.Rounded.DeleteOutline,
                                        contentDescription = "Move to Archive",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    archiveTarget?.let { target ->
        AlertDialog(
            onDismissRequest = { archiveTarget = null },
            title = { Text("Move to My Archive?") },
            text = { Text("This ${target.type.label.lowercase()} will be moved to My Archive instead of being permanently deleted. You can restore it anytime.") },
            confirmButton = {
                Button(
                    onClick = {
                        when (target.type) {
                            ArchiveItemType.PHOTO -> target.photo?.let(onArchivePhoto)
                            ArchiveItemType.VIDEO -> target.video?.let(onArchiveVideo)
                            ArchiveItemType.PHOTO_JOURNAL -> target.photoJournal?.let(onArchivePhotoJournal)
                            ArchiveItemType.VIDEO_JOURNAL -> target.videoJournal?.let(onArchiveVideoJournal)
                        }
                        archiveTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Move to Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { archiveTarget = null }) { Text("Cancel") }
            }
        )
    }
}
