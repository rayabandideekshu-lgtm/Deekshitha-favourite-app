package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.JournalSticker
import com.example.data.model.JournalTemplate
import com.example.ui.theme.ButterCream
import com.example.ui.theme.CuteSkyPink
import com.example.ui.theme.DeepPlum
import com.example.ui.theme.DeepRose
import com.example.ui.theme.GardenBlossomBg
import com.example.ui.theme.GardenLeaf
import com.example.ui.theme.MintSage
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.SoftRose
import com.example.ui.theme.VintageGold
import com.example.ui.theme.VintageSepiaBg
import com.example.ui.theme.VintageSepiaInk
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TemplateVisualStyle(
    val bgBrush: Brush,
    val borderColor: Color,
    val accentColor: Color,
    val inkColor: Color,
    val subInkColor: Color,
    val washiColor: Color
)

fun styleForTemplate(template: JournalTemplate): TemplateVisualStyle {
    return when (template) {
        JournalTemplate.PINK_AESTHETIC -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFFFFF4F8), SoftRose.copy(alpha = 0.6f))),
            borderColor = Color(0xFFF3BDD0),
            accentColor = DeepRose,
            inkColor = Color(0xFF422330),
            subInkColor = Color(0xFF7D4E60),
            washiColor = Color(0xFFF7B8CD)
        )
        JournalTemplate.LAVENDER_DREAMS -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFFF9F6FF), SoftLavender)),
            borderColor = PastelLavender.copy(alpha = 0.65f),
            accentColor = DeepPlum,
            inkColor = Color(0xFF2E2246),
            subInkColor = Color(0xFF665685),
            washiColor = Color(0xFFD0BFEF)
        )
        JournalTemplate.MINIMAL_WHITE -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(Color.White, Color(0xFFFAF9FB))),
            borderColor = Color(0xFFE8E4EA),
            accentColor = Color(0xFF4A424E),
            inkColor = Color(0xFF242026),
            subInkColor = Color(0xFF726B75),
            washiColor = Color(0xFFE7E2EA)
        )
        JournalTemplate.VINTAGE_SCRAPBOOK -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(VintageSepiaBg, ButterCream)),
            borderColor = VintageGold.copy(alpha = 0.6f),
            accentColor = VintageSepiaInk,
            inkColor = Color(0xFF433124),
            subInkColor = Color(0xFF7A614E),
            washiColor = Color(0xFFE4C59B)
        )
        JournalTemplate.FLOWER_GARDEN -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(GardenBlossomBg, MintSage.copy(alpha = 0.5f))),
            borderColor = GardenLeaf.copy(alpha = 0.5f),
            accentColor = Color(0xFF3E6E52),
            inkColor = Color(0xFF283C31),
            subInkColor = Color(0xFF5C7566),
            washiColor = Color(0xFFC5E3CF)
        )
        JournalTemplate.CUTE_MEMORIES -> TemplateVisualStyle(
            bgBrush = Brush.verticalGradient(listOf(CuteSkyPink, Color(0xFFFFF8EB))),
            borderColor = Color(0xFFF6C2D9),
            accentColor = Color(0xFFB44472),
            inkColor = Color(0xFF452436),
            subInkColor = Color(0xFF7E5269),
            washiColor = Color(0xFFFFD6E6)
        )
    }
}

fun templateFromId(id: String): JournalTemplate =
    JournalTemplate.entries.firstOrNull { it.id == id } ?: JournalTemplate.PINK_AESTHETIC

fun stickersFromCsv(csv: String): List<JournalSticker> {
    if (csv.isBlank()) return emptyList()
    return csv.split(",").mapNotNull { token ->
        JournalSticker.entries.firstOrNull { it.id == token.trim() }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrapbookJournalPreviewCard(
    photoPath: String,
    place: String,
    capturedAtMillis: Long,
    outfit: String,
    goodThingOne: String,
    goodThingTwo: String,
    caption: String,
    template: JournalTemplate,
    stickers: List<JournalSticker>,
    modifier: Modifier = Modifier
) {
    val style = styleForTemplate(template)
    val dateFormatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
    val formattedDate = dateFormatter.format(Date(capturedAtMillis))

    Box(modifier = modifier.fillMaxWidth()) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            shadowElevation = 6.dp,
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(style.bgBrush)
                .border(1.5.dp, style.borderColor, RoundedCornerShape(26.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header tag
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = template.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.4.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = style.accentColor
                    )
                    if (stickers.isNotEmpty()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            stickers.take(4).forEach { sticker ->
                                Text(text = sticker.glyph, fontSize = 18.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Polaroid Photo Frame with sticker badge
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White)
                        .padding(8.dp)
                ) {
                    AsyncImage(
                        model = File(photoPath),
                        contentDescription = "Memory photograph at $place",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    )

                    if (stickers.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.92f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stickers.first().glyph,
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Location + Date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = style.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = place.ifBlank { "Sweet Memory" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = style.inkColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = style.subInkColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = style.subInkColor
                    )
                }

                if (caption.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White.copy(alpha = 0.66f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = caption,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic,
                                lineHeight = 22.sp
                            ),
                            color = style.inkColor
                        )
                    }
                }

                if (outfit.isNotBlank() || goodThingOne.isNotBlank() || goodThingTwo.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (outfit.isNotBlank()) {
                            ScrapbookPill(
                                icon = {
                                    Icon(
                                        Icons.Rounded.Checkroom,
                                        contentDescription = null,
                                        tint = style.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                text = outfit,
                                tint = style.accentColor,
                                bg = Color.White.copy(alpha = 0.72f)
                            )
                        }
                        if (goodThingOne.isNotBlank()) {
                            ScrapbookPill(
                                icon = {
                                    Icon(
                                        Icons.Rounded.Favorite,
                                        contentDescription = null,
                                        tint = style.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                text = goodThingOne,
                                tint = style.accentColor,
                                bg = Color.White.copy(alpha = 0.72f)
                            )
                        }
                        if (goodThingTwo.isNotBlank()) {
                            ScrapbookPill(
                                icon = {
                                    Icon(
                                        Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = style.accentColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                },
                                text = goodThingTwo,
                                tint = style.accentColor,
                                bg = Color.White.copy(alpha = 0.72f)
                            )
                        }
                    }
                }
            }
        }

        // Cute decorative washi tape strip at top center
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 0.dp)
                .rotate(-2.5f)
                .width(86.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(style.washiColor.copy(alpha = 0.88f))
        )
    }
}

@Composable
private fun ScrapbookPill(
    icon: @Composable () -> Unit,
    text: String,
    tint: Color,
    bg: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = tint
        )
    }
}
