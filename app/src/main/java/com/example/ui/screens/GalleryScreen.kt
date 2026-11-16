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
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.SwapVert
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
import com.example.data.local.JournalEntryEntity
import com.example.data.local.PhotoEntity
import com.example.data.model.GallerySortOrder
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(
    photos: List<PhotoEntity>,
    journalEntries: List<JournalEntryEntity>,
    sortOrder: GallerySortOrder,
    onChangeSortOrder: (GallerySortOrder) -> Unit,
    onToggleFavorite: (PhotoEntity) -> Unit,
    onDownloadPhoto: (PhotoEntity) -> Unit,
    onDeletePhoto: (PhotoEntity) -> Unit,
    onOpenJournalEntry: (Long) -> Unit,
    onCreateJournalForPhoto: (PhotoEntity) -> Unit,
    onEditPhoto: (PhotoEntity) -> Unit = {},
    getShareUri: (String) -> android.net.Uri?,
    onOpenCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var favoritesOnly by remember { mutableStateOf(false) }
    var viewingPhoto by remember { mutableStateOf<PhotoEntity?>(null) }
    var deleteCandidate by remember { mutableStateOf<PhotoEntity?>(null) }

    val entryByPhotoId = remember(journalEntries) {
        journalEntries.associateBy { it.photoId }
    }

    val displayedPhotos = remember(photos, favoritesOnly) {
        if (favoritesOnly) photos.filter { it.isFavorite } else photos
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
                text = "My Gallery",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "All high-resolution photographs stored privately on your device",
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
                        val next = if (sortOrder == GallerySortOrder.NEWEST) {
                            GallerySortOrder.OLDEST
                        } else {
                            GallerySortOrder.NEWEST
                        }
                        onChangeSortOrder(next)
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.SwapVert,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = { Text(sortOrder.label) },
                    modifier = Modifier.testTag("gallery_sort_button")
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
                    label = { Text("Favorites") },
                    modifier = Modifier.testTag("gallery_favorites_chip")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (displayedPhotos.isEmpty()) {
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
                            text = if (photos.isEmpty()) "No Photographs Yet" else "No Favorite Photographs Yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (photos.isEmpty()) {
                                "Take a photograph with Pose Camera to build your private gallery."
                            } else {
                                "Tap the heart on any photograph to mark it as a favorite."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (photos.isEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onOpenCamera,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Icon(Icons.Rounded.CameraAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Open Pose Camera")
                            }
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 112.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedPhotos, key = { it.id }) { photo ->
                        val hasJournal = entryByPhotoId.containsKey(photo.id)
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewingPhoto = photo }
                                .testTag("gallery_photo_${photo.id}")
                        ) {
                            AsyncImage(
                                model = File(photo.filePath),
                                contentDescription = "Gallery photograph",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

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
                                            contentDescription = "Has journal entry",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { onToggleFavorite(photo) },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.36f))
                            ) {
                                Icon(
                                    imageVector = if (photo.isFavorite) {
                                        Icons.Rounded.Favorite
                                    } else {
                                        Icons.Rounded.FavoriteBorder
                                    },
                                    contentDescription = "Favorite photo",
                                    tint = if (photo.isFavorite) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        Color.White
                                    },
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Photo Viewer Dialog
    viewingPhoto?.let { activePhoto ->
        val latestPhoto = photos.firstOrNull { it.id == activePhoto.id } ?: activePhoto
        val linkedJournal = entryByPhotoId[latestPhoto.id]

        Dialog(
            onDismissRequest = { viewingPhoto = null },
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
                        IconButton(onClick = { viewingPhoto = null }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close viewer")
                        }
                        Text(
                            text = dateFormatter.format(Date(latestPhoto.capturedAtMillis)),
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(onClick = { onToggleFavorite(latestPhoto) }) {
                            Icon(
                                imageVector = if (latestPhoto.isFavorite) {
                                    Icons.Rounded.Favorite
                                } else {
                                    Icons.Rounded.FavoriteBorder
                                },
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    AsyncImage(
                        model = File(latestPhoto.filePath),
                        contentDescription = "Full photograph",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewingPhoto = null
                                    if (linkedJournal != null) {
                                        onOpenJournalEntry(linkedJournal.id)
                                    } else {
                                        onCreateJournalForPhoto(latestPhoto)
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
                                    viewingPhoto = null
                                    onEditPhoto(latestPhoto)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("gallery_edit_photo_button")
                            ) {
                                Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Edit Photo", maxLines = 1)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onDownloadPhoto(latestPhoto) },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = {
                                    val uri = getShareUri(latestPhoto.filePath) ?: return@OutlinedButton
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share photograph"))
                                },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = { deleteCandidate = latestPhoto },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }

    deleteCandidate?.let { victim ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Move Photograph to Archive?") },
            text = {
                Text("This moves the photograph (and any linked journal entry) to My Archive. You can restore it anytime.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteCandidate = null
                        viewingPhoto = null
                        onDeletePhoto(victim)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Move to Archive")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
