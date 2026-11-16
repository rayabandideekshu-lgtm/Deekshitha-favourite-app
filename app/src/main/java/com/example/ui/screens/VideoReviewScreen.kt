package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.SaveAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File

@Composable
fun VideoReviewScreen(
    videoPath: String,
    onRetake: () -> Unit,
    onEditInVideoEditor: () -> Unit = {},
    onCreateVideoJournal: () -> Unit,
    onSaveVideoOnly: () -> Unit
) {
    BackHandler(onBack = onRetake)
    var showJournalPrompt by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Preview Your Video Memory",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF141118)),
            contentAlignment = Alignment.Center
        ) {
            InlineVideoPlayer(
                videoPath = videoPath,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onEditInVideoEditor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("video_review_open_editor")
        ) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("EDIT IN PRO VIDEO EDITOR", color = Color.White)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("video_review_retake")
            ) {
                Icon(Icons.Rounded.Replay, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETAKE", color = Color.White)
            }

            Button(
                onClick = { showJournalPrompt = true },
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("video_review_continue")
            ) {
                Text("CONTINUE")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null)
            }
        }
    }

    if (showJournalPrompt) {
        AlertDialog(
            onDismissRequest = { showJournalPrompt = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = "Keep This Memory?",
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Would you like to create a video journal for this beautiful memory?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showJournalPrompt = false
                        onCreateVideoJournal()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("video_prompt_yes_journal")
                ) {
                    Text("YES, CREATE VIDEO JOURNAL")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showJournalPrompt = false
                        onSaveVideoOnly()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("video_prompt_no_save_only")
                ) {
                    Icon(Icons.Rounded.SaveAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("NO, SAVE VIDEO")
                }
            }
        )
    }
}

@Composable
fun InlineVideoPlayer(
    videoPath: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = true
) {
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                val controller = MediaController(ctx)
                controller.setAnchorView(this)
                setMediaController(controller)
                val file = File(videoPath)
                if (file.exists()) {
                    setVideoURI(Uri.fromFile(file))
                }
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    if (autoPlay) start()
                }
            }
        },
        update = { view ->
            val file = File(videoPath)
            if (file.exists() && view.tag != videoPath) {
                view.tag = videoPath
                view.setVideoURI(Uri.fromFile(file))
                view.start()
            }
        },
        modifier = modifier
    )
}
