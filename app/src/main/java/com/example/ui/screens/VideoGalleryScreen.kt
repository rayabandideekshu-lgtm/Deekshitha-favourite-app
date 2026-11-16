package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.VideoEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.data.model.GallerySortOrder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VideoGalleryScreen(
    videos: List<VideoEntity>,
    videoJournals: List<VideoJournalEntryEntity>,
    sortOrder: GallerySortOrder,
    onChangeSortOrder: (GallerySortOrder) -> Unit,
    onToggleFavorite: (VideoEntity) -> Unit,
    onDownloadVideo: (VideoEntity) -> Unit,
    onArchiveVideo: (VideoEntity) -> Unit,
    onOpenVideoJournal: (Long) -> Unit,
    onCreateJournalForVideo: (VideoEntity) -> Unit,
    onEditVideoInStudio: (VideoEntity) -> Unit = {},
    getShareUri: (String) -> android.net.Uri?,
    onOpenVideoCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var favoritesOnly by remember { mutableStateOf(false) }
    var viewingVideo by remember { mutableStateOf<VideoEntity?>(null) }
    var archiveCandidate by remember { mutableStateOf<VideoEntity?>(null) }

    val journalByVideoId = remember(videoJournals) {
        videoJournals.associateBy { it.videoId }
    }

    val displayedVideos = remember(videos, favoritesOnly) {
        if (favoritesOnly) videos.filter { it.isFavorite } else videos
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }

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
                text = "Video Gallery",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Personal video memories saved privately on your device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = sortOrder == GallerySortOrder.NEWEST,
                    onClick = {
                        onChangeSortOrder(
                            if (sortOrder == GallerySortOrder.NEWEST) GallerySortOrder.OLDEST
                            else GallerySortOrder.NEWEST
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.SwapVert, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    label = { Text(sortOrder.label) }
                )

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
                    label = { Text("Favorites") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayedVideos.isEmpty()) {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (videos.isEmpty()) "No Videos Yet" else "No Favorite Videos Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Use Video Camera with the live White-Line Video Pose Guide to record a keepsake.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (videos.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenVideoCamera,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Videocam, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Video Camera")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 136.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedVideos, key = { it.id }) { video ->
                        val hasJournal = journalByVideoId.containsKey(video.id)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { viewingVideo = video }
                                .testTag("video_gallery_item_${video.id}")
                        ) {
                            if (video.thumbnailPath.isNotBlank() && File(video.thumbnailPath).exists()) {
                                AsyncImage(
                                    model = File(video.thumbnailPath),
                                    contentDescription = "Video memory thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Videocam,
                                        contentDescription = "Video",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Rounded.PlayCircle,
                                contentDescription = "Play video",
                                tint = Color.White.copy(alpha = 0.92f),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(40.dp)
                            )

                            if (video.durationMillis > 0L) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.Black.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(6.dp)
                                ) {
                                    Text(
                                        text = formatDurationMs(video.durationMillis),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (hasJournal) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.88f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(6.dp)
                                        .size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.MenuBook,
                                            contentDescription = "Has video journal",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavorite(video) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.36f))
                            ) {
                                Icon(
                                    imageVector = if (video.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorite video",
                                    tint = if (video.isFavorite) MaterialTheme.colorScheme.primaryContainer else Color.White,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    viewingVideo?.let { activeVideo ->
        val latest = videos.firstOrNull { it.id == activeVideo.id } ?: activeVideo
        val linkedJournal = journalByVideoId[latest.id]

        Dialog(
            onDismissRequest = { viewingVideo = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewingVideo = null }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close player")
                        }
                        Text(
                            text = dateFormatter.format(Date(latest.capturedAtMillis)),
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(onClick = { onToggleFavorite(latest) }) {
                            Icon(
                                imageVector = if (latest.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        InlineVideoPlayer(
                            videoPath = latest.filePath,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewingVideo = null
                                    if (linkedJournal != null) {
                                        onOpenVideoJournal(linkedJournal.id)
                                    } else {
                                        onCreateJournalForVideo(latest)
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (linkedJournal != null) "Journal" else "Create Journal",
                                    maxLines = 1
                                )
                            }

                            Button(
                                onClick = {
                                    viewingVideo = null
                                    onEditVideoInStudio(latest)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("video_gallery_edit_studio_btn")
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Video Studio", maxLines = 1)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDownloadVideo(latest) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    val uri = getShareUri(latest.filePath) ?: return@OutlinedButton
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "video/mp4"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share video"))
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { archiveCandidate = latest },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("video_gallery_delete_button")
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }

    archiveCandidate?.let { victim ->
        AlertDialog(
            onDismissRequest = { archiveCandidate = null },
            title = { Text("Move Video to Archive?") },
            text = {
                Text("This video (and any linked video journal) will move to My Archive. You can restore it anytime.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        archiveCandidate = null
                        viewingVideo = null
                        onArchiveVideo(victim)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Move to Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { archiveCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun formatDurationMs(durationMs: Long): String {
    val totalSec = (durationMs / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return String.format(Locale.US, "%d:%02d", mins, secs)
}
