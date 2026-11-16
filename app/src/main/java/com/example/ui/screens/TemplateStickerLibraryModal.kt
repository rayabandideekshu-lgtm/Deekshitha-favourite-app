package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.AestheticTemplate
import com.example.data.model.CreativeCategory
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.CreativeSticker
import com.example.data.model.EditorFontOption
import com.example.ui.editor.AestheticTemplateBackdrop
import com.example.ui.editor.FrameOverlayPreview
import com.example.ui.editor.StickerVisual
import com.example.ui.editor.toComposeFontFamily

@Composable
fun TemplateStickerLibraryDialog(
    initialTabIndex: Int = 0, // 0 = Templates (240), 1 = Stickers (120)
    showTemplatesTab: Boolean = true,
    favoriteTemplateIds: Set<String>,
    favoriteStickerIds: Set<String>,
    onToggleFavoriteTemplate: (String) -> Unit,
    onToggleFavoriteSticker: (String) -> Unit,
    onSelectTemplate: (AestheticTemplate) -> Unit = {},
    onSelectSticker: (CreativeSticker) -> Unit,
    onDismiss: () -> Unit
) {
    var tabIndex by remember(initialTabIndex, showTemplatesTab) {
        mutableIntStateOf(if (!showTemplatesTab) 1 else initialTabIndex.coerceIn(0, 1))
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CreativeCategory?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }

    var previewTemplate by remember { mutableStateOf<AestheticTemplate?>(null) }
    var previewSticker by remember { mutableStateOf<CreativeSticker?>(null) }

    val filteredTemplates = remember(searchQuery, selectedCategory, favoritesOnly, favoriteTemplateIds) {
        CreativeLibraryCatalog.templates.filter { t ->
            val catOk = selectedCategory == null || t.category == selectedCategory
            val favOk = !favoritesOnly || favoriteTemplateIds.contains(t.id)
            val qOk = searchQuery.isBlank() ||
                t.name.contains(searchQuery, ignoreCase = true) ||
                t.tagline.contains(searchQuery, ignoreCase = true) ||
                t.category.label.contains(searchQuery, ignoreCase = true) ||
                t.badgeText.contains(searchQuery, ignoreCase = true)
            catOk && favOk && qOk
        }
    }

    val filteredStickers = remember(searchQuery, selectedCategory, favoritesOnly, favoriteStickerIds) {
        CreativeLibraryCatalog.stickers.filter { s ->
            val catOk = selectedCategory == null || s.category == selectedCategory
            val favOk = !favoritesOnly || favoriteStickerIds.contains(s.id)
            val qOk = searchQuery.isBlank() ||
                s.name.contains(searchQuery, ignoreCase = true) ||
                s.subLabel.contains(searchQuery, ignoreCase = true) ||
                s.category.label.contains(searchQuery, ignoreCase = true) ||
                s.tags.any { it.contains(searchQuery, ignoreCase = true) }
            catOk && favOk && qOk
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Aesthetic Studio Library",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${CreativeLibraryCatalog.templates.size} templates • ${CreativeLibraryCatalog.stickers.size} stickers",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("library_close_button")
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close library")
                    }
                }

                if (showTemplatesTab) {
                    TabRow(selectedTabIndex = tabIndex) {
                        Tab(
                            selected = tabIndex == 0,
                            onClick = { tabIndex = 0 },
                            text = { Text("Templates (${filteredTemplates.size})") },
                            modifier = Modifier.testTag("library_tab_templates")
                        )
                        Tab(
                            selected = tabIndex == 1,
                            onClick = { tabIndex = 1 },
                            text = { Text("Stickers (${filteredStickers.size})") },
                            modifier = Modifier.testTag("library_tab_stickers")
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Search + Favorites row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    if (tabIndex == 0) "Search pastel, floral, retro, polaroid..."
                                    else "Search flowers, hearts, stars, bows..."
                                )
                            },
                            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Clear, contentDescription = "Clear search")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("library_search_input")
                        )

                        FilterChip(
                            selected = favoritesOnly,
                            onClick = { favoritesOnly = !favoritesOnly },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (favoritesOnly) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text("Favs") },
                            modifier = Modifier.testTag("library_favorites_filter")
                        )
                    }

                    // 10 Categories Horizontal Filter
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All (10)") }
                            )
                        }
                        items(CreativeCategory.entries) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text("${cat.iconGlyph} ${cat.label}") },
                                modifier = Modifier.testTag("library_cat_${cat.id}")
                            )
                        }
                    }

                    // Grid Content
                    if (tabIndex == 0 && showTemplatesTab) {
                        if (filteredTemplates.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No matching templates found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 152.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredTemplates, key = { it.id }) { template ->
                                    val isFav = favoriteTemplateIds.contains(template.id)
                                    val fontFamily = remember(template.fontId) {
                                        EditorFontOption.fromId(template.fontId).toComposeFontFamily()
                                    }
                                    Card(
                                        onClick = { previewTemplate = template },
                                        shape = RoundedCornerShape(18.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("template_card_${template.id}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(0.84f)
                                        ) {
                                            AestheticTemplateBackdrop(
                                                template = template,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            FrameOverlayPreview(
                                                frameStyle = template.frameStyle,
                                                template = template,
                                                thicknessFraction = 0.055f,
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            // Inner photo frame mock window
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(18.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(Color.White.copy(alpha = 0.72f))
                                                    .border(
                                                        1.dp,
                                                        Color(template.borderArgb),
                                                        RoundedCornerShape(10.dp)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    modifier = Modifier.padding(8.dp)
                                                ) {
                                                    Text(template.cornerGlyph, fontSize = 22.sp)
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = template.name,
                                                        fontFamily = fontFamily,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color(template.inkArgb),
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = template.frameStyle.label,
                                                        fontSize = 10.sp,
                                                        color = Color(template.accentArgb),
                                                        maxLines = 1
                                                    )
                                                }
                                            }

                                            // Category badge
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(template.washiArgb).copy(alpha = 0.92f),
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(8.dp)
                                            ) {
                                                Text(
                                                    text = template.category.label,
                                                    fontSize = 10.sp,
                                                    color = Color(template.inkArgb),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                )
                                            }

                                            // Favorite button
                                            IconButton(
                                                onClick = { onToggleFavoriteTemplate(template.id) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.85f))
                                                    .testTag("template_fav_${template.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                    contentDescription = "Favorite template",
                                                    tint = Color(template.accentArgb),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredStickers.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No matching stickers found.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 112.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(bottom = 24.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(filteredStickers, key = { it.id }) { sticker ->
                                    val isFav = favoriteStickerIds.contains(sticker.id)
                                    Card(
                                        onClick = { previewSticker = sticker },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("sticker_card_${sticker.id}")
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onToggleFavoriteSticker(sticker.id) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                                    contentDescription = "Favorite sticker",
                                                    tint = Color(sticker.primaryArgb),
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }

                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 14.dp, bottom = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                StickerVisual(
                                                    sticker = sticker,
                                                    iconSize = 30.dp,
                                                    showLabel = true
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = sticker.name,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Template Preview & Apply Dialog
    previewTemplate?.let { tmpl ->
        val isFav = favoriteTemplateIds.contains(tmpl.id)
        val fontFamily = remember(tmpl.fontId) {
            EditorFontOption.fromId(tmpl.fontId).toComposeFontFamily()
        }
        AlertDialog(
            onDismissRequest = { previewTemplate = null },
            shape = RoundedCornerShape(26.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${tmpl.cornerGlyph} ${tmpl.name}",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { onToggleFavoriteTemplate(tmpl.id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = Color(tmpl.accentArgb)
                        )
                    }
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(18.dp))
                    ) {
                        AestheticTemplateBackdrop(template = tmpl, modifier = Modifier.fillMaxSize())
                        FrameOverlayPreview(
                            frameStyle = tmpl.frameStyle,
                            template = tmpl,
                            thicknessFraction = 0.055f,
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(tmpl.cornerGlyph, fontSize = 32.sp)
                            Text(
                                text = tmpl.badgeText,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color(tmpl.inkArgb),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = tmpl.tagline,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(tmpl.accentArgb),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = tmpl.category.label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = tmpl.frameStyle.label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectTemplate(tmpl)
                        previewTemplate = null
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Apply Template")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { previewTemplate = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Sticker Preview & Add Dialog
    previewSticker?.let { stk ->
        val isFav = favoriteStickerIds.contains(stk.id)
        AlertDialog(
            onDismissRequest = { previewSticker = null },
            shape = RoundedCornerShape(26.dp),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(stk.name, style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { onToggleFavoriteSticker(stk.id) }) {
                        Icon(
                            imageVector = if (isFav) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite sticker",
                            tint = Color(stk.primaryArgb)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(stk.secondaryArgb).copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        StickerVisual(sticker = stk, iconSize = 52.dp, showLabel = true)
                    }
                    Text(
                        text = "Category: ${stk.category.iconGlyph} ${stk.category.label}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSelectSticker(stk)
                        previewSticker = null
                        onDismiss()
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Sticker")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { previewSticker = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
