package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AlignmentGuidance
import com.example.data.model.PoseCategory
import com.example.data.model.PoseOutlineConfig
import com.example.data.model.PoseSuggestion
import com.example.ui.viewmodel.PoseUiState

@Composable
fun AlignmentGuidanceChip(
    guidance: AlignmentGuidance,
    modifier: Modifier = Modifier
) {
    val bgColor = if (guidance.isAligned) {
        Color(0xFF1F7A4C).copy(alpha = 0.86f)
    } else {
        Color.Black.copy(alpha = 0.62f)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(
            width = 1.dp,
            color = if (guidance.isAligned) Color(0xFF8FF0BA) else Color.White.copy(alpha = 0.45f)
        ),
        modifier = modifier.testTag("camera_alignment_guidance")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (guidance.isAligned) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF8FF0BA),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = guidance.message,
                color = Color.White,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
fun ActivePoseDescriptionPill(
    pose: PoseSuggestion,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.Black.copy(alpha = 0.56f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("camera_active_pose_info")
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "POSE: ${pose.title}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = pose.difficulty.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = pose.instruction,
                color = Color.White.copy(alpha = 0.9f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlineCustomizeBottomSheet(
    title: String,
    outlineToggleLabel: String,
    saveWithGuideLabel: String,
    config: PoseOutlineConfig,
    onToggleEnabled: (Boolean) -> Unit,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Float) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onReset: () -> Unit,
    onToggleSaveWithGuide: (Boolean) -> Unit,
    onChangePoseClick: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
                TextButton(
                    onClick = onReset,
                    modifier = Modifier.testTag("outline_reset_button")
                ) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Outline")
                }
            }

            if (onChangePoseClick != null) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onChangePoseClick()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Change Pose")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(outlineToggleLabel, style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = config.enabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.testTag("sheet_outline_enable_switch")
                )
            }

            Column {
                Text(
                    text = "Outline Size (${(config.scale * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = config.scale,
                    onValueChange = onScaleChange,
                    valueRange = 0.6f..1.4f,
                    modifier = Modifier.testTag("outline_size_slider")
                )
            }

            Column {
                Text(
                    text = "Move Up / Down",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = config.verticalOffsetFraction,
                    onValueChange = onOffsetChange,
                    valueRange = -0.22f..0.22f,
                    modifier = Modifier.testTag("outline_position_slider")
                )
            }

            Column {
                Text(
                    text = "Outline Transparency (${(config.alpha * 100).toInt()}%)",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = config.alpha,
                    onValueChange = onAlphaChange,
                    valueRange = 0.25f..1.0f,
                    modifier = Modifier.testTag("outline_alpha_slider")
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                    Text(
                        text = saveWithGuideLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "Keep OFF to save natural photos/videos without white guide lines.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = config.saveWithGuide,
                    onCheckedChange = onToggleSaveWithGuide,
                    modifier = Modifier.testTag("save_with_guide_switch")
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePoseBottomSheet(
    title: String,
    selectedCategory: PoseCategory,
    activePoseId: String,
    poseUiState: PoseUiState,
    onSelectCategory: (PoseCategory) -> Unit,
    onGiveAnotherPose: () -> Unit,
    onNextPose: () -> Unit,
    onUseThisPose: (PoseSuggestion) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(PoseCategory.entries) { cat ->
                    FilterChip(
                        selected = cat == selectedCategory,
                        onClick = { onSelectCategory(cat) },
                        label = { Text(cat.label) }
                    )
                }
            }

            when (poseUiState) {
                PoseUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is PoseUiState.Error -> {
                    Text(
                        text = poseUiState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                is PoseUiState.Success -> {
                    poseUiState.batch.notice?.let { notice ->
                        SampleAiBanner(message = notice)
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                    ) {
                        items(poseUiState.batch.suggestions, key = { it.id }) { suggestion ->
                            val isSelected = suggestion.id == activePoseId
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PoseOutlineCardPreview(
                                        outlineType = suggestion.outlineType,
                                        modifier = Modifier.size(66.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = suggestion.title,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.secondaryContainer
                                            ) {
                                                Text(
                                                    text = suggestion.difficulty.label,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = suggestion.instruction,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 3
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                onUseThisPose(suggestion)
                                                onDismiss()
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .height(38.dp)
                                                .testTag("use_this_pose_${suggestion.id}")
                                        ) {
                                            Text(if (isSelected) "Active Outline" else "Use This Pose")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onGiveAnotherPose,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Give Me Another Pose", maxLines = 1)
                        }
                        Button(
                            onClick = onNextPose,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(0.8f)
                                .height(46.dp)
                        ) {
                            Text("Next Pose")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
