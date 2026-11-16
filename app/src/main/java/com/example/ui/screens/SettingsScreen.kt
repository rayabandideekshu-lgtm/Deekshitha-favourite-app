package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.local.UserSettings
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePalette
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    photoCount: Int,
    journalCount: Int,
    isBusyBackup: Boolean,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetThemePalette: (ThemePalette) -> Unit,
    onSetDefaultTimer: (Int) -> Unit,
    onExportBackupZip: (OutputStream?) -> Unit,
    onExportJournalText: (OutputStream?) -> Unit,
    onImportBackupZip: (InputStream?) -> Unit,
    onDeleteAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val backupCreateLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        uri?.let {
            onExportBackupZip(context.contentResolver.openOutputStream(it))
        }
    }

    val journalExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let {
            onExportJournalText(context.contentResolver.openOutputStream(it))
        }
    }

    val restoreOpenLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            onImportBackupZip(context.contentResolver.openInputStream(it))
        }
    }

    var deleteStep by remember { mutableStateOf(0) } // 0 = closed, 1 = confirm, 2 = final confirm

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Settings & Privacy",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$journalCount journal memories • $photoCount saved photographs stored locally",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isBusyBackup) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // Camera Permission Card
            item {
                SettingsSectionCard(
                    title = "Camera Permission",
                    icon = Icons.Rounded.CameraAlt
                ) {
                    Text(
                        text = if (hasCameraPermission) {
                            "Camera access is granted for live pose previews and photography."
                        } else {
                            "Camera permission is not yet granted."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!hasCameraPermission) {
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Grant Camera")
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                val intent = Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("settings_system_permissions")
                        ) {
                            Text("System Permissions")
                        }
                    }
                }
            }

            // Theme & Dark Mode Settings
            item {
                SettingsSectionCard(
                    title = "Theme & Appearance",
                    icon = Icons.Rounded.Palette
                ) {
                    Text(
                        text = "Display Mode",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = settings.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(mode.label) },
                                modifier = Modifier.testTag("settings_theme_${mode.name.lowercase()}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Light Mode Pastel Palette",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemePalette.entries.forEach { palette ->
                            FilterChip(
                                selected = settings.themePalette == palette,
                                onClick = { onSetThemePalette(palette) },
                                label = { Text(palette.label) },
                                modifier = Modifier.testTag("settings_palette_${palette.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Default Camera Timer
            item {
                SettingsSectionCard(
                    title = "Default Camera Timer",
                    icon = Icons.Rounded.Timer
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0, 3, 5, 10).forEach { sec ->
                            FilterChip(
                                selected = settings.defaultTimerSeconds == sec,
                                onClick = { onSetDefaultTimer(sec) },
                                label = { Text(if (sec == 0) "Off" else "${sec}s") }
                            )
                        }
                    }
                }
            }

            // Data Backup, Restore & Journal Export
            item {
                SettingsSectionCard(
                    title = "Data Backup, Restore & Export",
                    icon = Icons.Rounded.Backup
                ) {
                    Text(
                        text = "Back up your photographs and journal entries into a private ZIP archive on your device, or export a readable journal file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val stamp = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                    Button(
                        onClick = { backupCreateLauncher.launch("PoseMemories_Backup_$stamp.zip") },
                        enabled = !isBusyBackup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_export_backup_zip")
                    ) {
                        Icon(Icons.Rounded.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Photos & Journals (.zip)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { restoreOpenLauncher.launch(arrayOf("application/zip", "*/*")) },
                        enabled = !isBusyBackup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_restore_backup_zip")
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Restore from Backup (.zip)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { journalExportLauncher.launch("PoseMemories_Journal_$stamp.txt") },
                        enabled = !isBusyBackup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_export_journal_txt")
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Journal Entries (.txt)")
                    }
                }
            }

            // Privacy Info
            item {
                SettingsSectionCard(
                    title = "Privacy Information",
                    icon = Icons.Rounded.Lock
                ) {
                    Text(
                        text = "• All photographs and journal entries are stored privately on this device.\n" +
                            "• Nothing is uploaded to external cloud servers or social networks.\n" +
                            "• Photographs are shared only when you explicitly tap Share.\n" +
                            "• AI pose suggestions and captions currently run in offline Sample AI mode because Firebase AI Logic was not enabled.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete All App Data
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Danger Zone",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Delete all saved photographs, journal entries, and preferences from Pose & Memories.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { deleteStep = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("settings_delete_all_button")
                        ) {
                            Icon(Icons.Rounded.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete All App Data")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    if (deleteStep == 1) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            title = { Text("Delete All App Data?") },
            text = {
                Text("Are you sure you want to delete all $journalCount journal entries and $photoCount app photos? Consider creating a ZIP backup first.")
            },
            confirmButton = {
                Button(
                    onClick = { deleteStep = 2 },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Continue")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (deleteStep == 2) {
        AlertDialog(
            onDismissRequest = { deleteStep = 0 },
            title = { Text("Final Confirmation") },
            text = {
                Text("This permanently wipes all local photos, journal captions, and settings in Pose & Memories. Copies previously saved to your device's Pictures folder remain untouched.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleteStep = 0
                        onDeleteAllData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteStep = 0 }) {
                    Text("Keep My Memories")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
