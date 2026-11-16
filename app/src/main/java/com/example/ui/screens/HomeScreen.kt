package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MovieCreation
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.JournalEntryEntity
import com.example.data.local.PhotoEntity
import com.example.data.local.VideoEntity
import com.example.data.local.VideoJournalEntryEntity
import com.example.ui.components.PastelFloralHeaderAccent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class HomeShortcut(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val containerColor: @Composable () -> Color,
    val iconTint: @Composable () -> Color,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun HomeScreen(
    recentPhotos: List<PhotoEntity>,
    recentVideos: List<VideoEntity>,
    journalEntries: List<JournalEntryEntity>,
    videoJournals: List<VideoJournalEntryEntity>,
    archivedCount: Int,
    onOpenDrawer: () -> Unit,
    onOpenCamera: () -> Unit,
    onOpenVideoCamera: () -> Unit,
    onOpenPoseInspiration: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenVideoJournal: () -> Unit,
    onOpenAllMemories: () -> Unit,
    onOpenGallery: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenTodayMemories: () -> Unit,
    onOpenVideoEditor: () -> Unit = {},
    onOpenTemplateLibrary: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenMemoryDetail: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val todayCal = Calendar.getInstance()
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    val onThisDayPast = journalEntries.firstOrNull { entry ->
        val c = Calendar.getInstance().apply { timeInMillis = entry.capturedAtMillis }
        c.get(Calendar.DAY_OF_MONTH) == todayDay &&
            (c.get(Calendar.MONTH) != todayMonth || c.get(Calendar.YEAR) != todayYear)
    }

    val shortcuts = listOf(
        HomeShortcut(
            title = "Photo Camera",
            subtitle = "White-line pose outline",
            icon = Icons.Rounded.CameraAlt,
            containerColor = { MaterialTheme.colorScheme.primaryContainer },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer },
            testTag = "home_tile_camera",
            onClick = onOpenCamera
        ),
        HomeShortcut(
            title = "Video Camera",
            subtitle = "Live video pose guide",
            icon = Icons.Rounded.Videocam,
            containerColor = { MaterialTheme.colorScheme.secondaryContainer },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer },
            testTag = "home_tile_video_camera",
            onClick = onOpenVideoCamera
        ),
        HomeShortcut(
            title = "AI Pose Inspiration",
            subtitle = "10 pose outline guides",
            icon = Icons.Rounded.AutoAwesome,
            containerColor = { MaterialTheme.colorScheme.tertiaryContainer },
            iconTint = { MaterialTheme.colorScheme.onTertiaryContainer },
            testTag = "home_tile_poses",
            onClick = onOpenPoseInspiration
        ),
        HomeShortcut(
            title = "Photo Journal",
            subtitle = "${journalEntries.size} memories",
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            containerColor = { MaterialTheme.colorScheme.surfaceVariant },
            iconTint = { MaterialTheme.colorScheme.primary },
            testTag = "home_tile_journal",
            onClick = onOpenJournal
        ),
        HomeShortcut(
            title = "Video Journal",
            subtitle = "${videoJournals.size} video stories",
            icon = Icons.Rounded.MovieCreation,
            containerColor = { MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f) },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer },
            testTag = "home_tile_video_journal",
            onClick = onOpenVideoJournal
        ),
        HomeShortcut(
            title = "All Memories",
            subtitle = "${recentPhotos.size + recentVideos.size} items",
            icon = Icons.Rounded.CollectionsBookmark,
            containerColor = { MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f) },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer },
            testTag = "home_tile_all_memories",
            onClick = onOpenAllMemories
        ),
        HomeShortcut(
            title = "My Gallery",
            subtitle = "${recentPhotos.size} photos",
            icon = Icons.Rounded.PhotoLibrary,
            containerColor = { MaterialTheme.colorScheme.surfaceVariant },
            iconTint = { MaterialTheme.colorScheme.primary },
            testTag = "home_tile_gallery",
            onClick = onOpenGallery
        ),
        HomeShortcut(
            title = "My Archive",
            subtitle = "$archivedCount archived",
            icon = Icons.Rounded.Archive,
            containerColor = { MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.75f) },
            iconTint = { MaterialTheme.colorScheme.onTertiaryContainer },
            testTag = "home_tile_archive",
            onClick = onOpenArchive
        ),
        HomeShortcut(
            title = "Memory Calendar",
            subtitle = "Browse by date",
            icon = Icons.Rounded.CalendarMonth,
            containerColor = { MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f) },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer },
            testTag = "home_tile_calendar",
            onClick = onOpenCalendar
        ),
        HomeShortcut(
            title = "Today's Memories",
            subtitle = "On This Day",
            icon = Icons.Rounded.Today,
            containerColor = { MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f) },
            iconTint = { MaterialTheme.colorScheme.onPrimaryContainer },
            testTag = "home_tile_today",
            onClick = onOpenTodayMemories
        ),
        HomeShortcut(
            title = "Pro Video Editor",
            subtitle = "Multi-track timeline",
            icon = Icons.Rounded.MovieCreation,
            containerColor = { MaterialTheme.colorScheme.secondaryContainer },
            iconTint = { MaterialTheme.colorScheme.onSecondaryContainer },
            testTag = "home_tile_video_editor",
            onClick = onOpenVideoEditor
        ),
        HomeShortcut(
            title = "Templates & Stickers",
            subtitle = "240 frames • 120 stickers",
            icon = Icons.Rounded.AutoAwesome,
            containerColor = { MaterialTheme.colorScheme.tertiaryContainer },
            iconTint = { MaterialTheme.colorScheme.onTertiaryContainer },
            testTag = "home_tile_template_library",
            onClick = onOpenTemplateLibrary
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onOpenDrawer,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_menu_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Menu,
                                contentDescription = "Navigation menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = stringResource(R.string.app_tagline),
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    PastelFloralHeaderAccent()
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        onClick = onOpenSettings,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 2.dp,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("home_settings_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Hero Scrapbook Welcome Card
            item {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_home_banner),
                                contentDescription = "Scrapbook floral banner",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.home_greeting),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onOpenCamera,
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("home_primary_camera_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photo Camera", maxLines = 1)
                            }

                            OutlinedButton(
                                onClick = onOpenVideoCamera,
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                                    .testTag("home_primary_video_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Videocam,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Video Camera", maxLines = 1)
                            }
                        }
                    }
                }
            }

            // On This Day Spotlight
            item {
                AnimatedVisibility(
                    visible = onThisDayPast != null,
                    enter = fadeIn() + slideInVertically()
                ) {
                    if (onThisDayPast != null) {
                        Card(
                            onClick = { onOpenMemoryDetail(onThisDayPast.id) },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("home_on_this_day_card")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = File(onThisDayPast.photoPath),
                                    contentDescription = "On this day memory",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "On This Day ✨",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "On this day, you captured a beautiful memory!",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = onThisDayPast.place.ifBlank { "Tap to revisit" },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2-Column Scrapbook Shortcuts Grid
            item {
                Text(
                    text = "Your Studio & Scrapbook",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    shortcuts.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { item ->
                                ShortcutTile(
                                    item = item,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Recently Captured Photos
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Captured Photos",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (recentPhotos.isNotEmpty()) {
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenGallery)
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (recentPhotos.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "No photographs captured yet",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Open Photo Camera or Video Camera to capture memories with live pose outlines.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    val dateFmt = SimpleDateFormat("MMM d", Locale.getDefault())
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentPhotos.take(10), key = { it.id }) { photo ->
                            Card(
                                onClick = onOpenGallery,
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier.width(136.dp)
                            ) {
                                Column(modifier = Modifier.padding(6.dp)) {
                                    Box {
                                        AsyncImage(
                                            model = File(photo.filePath),
                                            contentDescription = "Recent photo",
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(15.dp))
                                        )
                                        if (photo.isFavorite) {
                                            Icon(
                                                imageVector = Icons.Rounded.Favorite,
                                                contentDescription = "Favorite photo",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = dateFmt.format(Date(photo.capturedAtMillis)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Settings quick link footer card
            item {
                Card(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_tile_settings")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Settings & Privacy",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Theme mode, camera permissions, local backup & export",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun ShortcutTile(
    item: HomeShortcut,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = item.onClick,
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier.testTag(item.testTag)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(item.containerColor()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
