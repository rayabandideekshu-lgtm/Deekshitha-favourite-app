package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.local.JournalEntryEntity
import com.example.ui.components.ScrapbookJournalPreviewCard
import com.example.ui.components.stickersFromCsv
import com.example.ui.components.templateFromId
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnThisDayScreen(
    entries: List<JournalEntryEntity>,
    onOpenEntry: (Long) -> Unit,
    onOpenCamera: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val todayCal = remember { Calendar.getInstance() }
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    val pastMemoriesOnThisDay = remember(entries) {
        entries.filter { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.capturedAtMillis }
            cal.get(Calendar.DAY_OF_MONTH) == todayDay &&
                (cal.get(Calendar.MONTH) != todayMonth || cal.get(Calendar.YEAR) != todayYear)
        }
    }

    val todaysMemories = remember(entries) {
        entries.filter { entry ->
            val cal = Calendar.getInstance().apply { timeInMillis = entry.capturedAtMillis }
            cal.get(Calendar.DAY_OF_MONTH) == todayDay &&
                cal.get(Calendar.MONTH) == todayMonth &&
                cal.get(Calendar.YEAR) == todayYear
        }
    }

    val todayLabel = remember {
        SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Today's Memories", style = MaterialTheme.typography.headlineSmall) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("today_back_button")) {
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
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text(
                                text = "On This Day • $todayLabel",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (pastMemoriesOnThisDay.isNotEmpty()) {
                                    "On this day, you captured a beautiful memory!"
                                } else {
                                    "Memories captured on day $todayDay of earlier months or years will appear here as your journal grows."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                if (pastMemoriesOnThisDay.isNotEmpty()) {
                    item {
                        Text(
                            text = "From Previous Months & Years",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(pastMemoriesOnThisDay, key = { "past_${it.id}" }) { entry ->
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Card(
                                onClick = { onOpenEntry(entry.id) },
                                shape = RoundedCornerShape(28.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                ScrapbookJournalPreviewCard(
                                    photoPath = entry.photoPath,
                                    place = entry.place,
                                    capturedAtMillis = entry.capturedAtMillis,
                                    outfit = entry.outfit,
                                    goodThingOne = entry.goodThingOne,
                                    goodThingTwo = entry.goodThingTwo,
                                    caption = entry.caption,
                                    template = templateFromId(entry.templateId),
                                    stickers = stickersFromCsv(entry.stickersCsv)
                                )
                            }
                        }
                    }
                }

                if (todaysMemories.isNotEmpty()) {
                    item {
                        Text(
                            text = "Captured Today",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    items(todaysMemories, key = { "today_${it.id}" }) { entry ->
                        Card(
                            onClick = { onOpenEntry(entry.id) },
                            shape = RoundedCornerShape(28.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            ScrapbookJournalPreviewCard(
                                photoPath = entry.photoPath,
                                place = entry.place,
                                capturedAtMillis = entry.capturedAtMillis,
                                outfit = entry.outfit,
                                goodThingOne = entry.goodThingOne,
                                goodThingTwo = entry.goodThingTwo,
                                caption = entry.caption,
                                template = templateFromId(entry.templateId),
                                stickers = stickersFromCsv(entry.stickersCsv)
                            )
                        }
                    }
                }

                if (pastMemoriesOnThisDay.isEmpty() && todaysMemories.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Capture Today's Story",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Take a photograph today and create a memory so you can revisit it on future dates!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
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
                }
            }
        }
    }
}
