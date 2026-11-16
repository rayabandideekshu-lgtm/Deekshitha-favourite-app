package com.example.ui.editor

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.data.model.AestheticTemplate
import com.example.data.model.CreativeSticker
import com.example.data.model.EditorFontOption
import com.example.data.model.StickerBadgeStyle
import com.example.data.model.TemplateFrameStyle
import com.example.data.model.TemplatePatternType

fun EditorFontOption.toComposeFontFamily(): FontFamily =
    FontFamily(
        Font(fontResId, FontWeight.Normal),
        Font(fontResId, FontWeight.Bold)
    )

fun EditorFontOption.toAndroidTypeface(context: Context): Typeface =
    runCatching { ResourcesCompat.getFont(context, fontResId) }.getOrNull() ?: Typeface.SERIF

@Composable
fun AestheticTemplateBackdrop(
    template: AestheticTemplate,
    modifier: Modifier = Modifier,
    showPattern: Boolean = true
) {
    val start = Color(template.bgStartArgb)
    val end = Color(template.bgEndArgb)
    val accent = Color(template.accentArgb)
    val border = Color(template.borderArgb)

    Box(
        modifier = modifier
            .background(Brush.verticalGradient(listOf(start, end)))
            .border(1.5.dp, border, RoundedCornerShape(16.dp))
    ) {
        if (showPattern && template.patternType != TemplatePatternType.NONE) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val step = 28.dp.toPx()
                var y = step * 0.5f
                var row = 0
                while (y < size.height) {
                    var x = if (row % 2 == 0) step * 0.5f else step
                    while (x < size.width) {
                        when (template.patternType) {
                            TemplatePatternType.DOTS -> {
                                drawCircle(
                                    color = accent.copy(alpha = 0.14f),
                                    radius = 3.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                            TemplatePatternType.CHECKS -> {
                                drawRect(
                                    color = border.copy(alpha = 0.14f),
                                    topLeft = Offset(x - 6.dp.toPx(), y - 6.dp.toPx()),
                                    size = Size(12.dp.toPx(), 12.dp.toPx())
                                )
                            }
                            TemplatePatternType.STARS -> {
                                drawCircle(
                                    color = accent.copy(alpha = 0.16f),
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(x, y)
                                )
                                drawLine(
                                    color = accent.copy(alpha = 0.16f),
                                    start = Offset(x - 5.dp.toPx(), y),
                                    end = Offset(x + 5.dp.toPx(), y),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                                drawLine(
                                    color = accent.copy(alpha = 0.16f),
                                    start = Offset(x, y - 5.dp.toPx()),
                                    end = Offset(x, y + 5.dp.toPx()),
                                    strokeWidth = 1.2.dp.toPx()
                                )
                            }
                            TemplatePatternType.HEARTS, TemplatePatternType.PETALS -> {
                                drawCircle(
                                    color = accent.copy(alpha = 0.14f),
                                    radius = 4.dp.toPx(),
                                    center = Offset(x, y)
                                )
                            }
                            TemplatePatternType.NONE -> Unit
                        }
                        x += step * 1.4f
                    }
                    y += step * 1.3f
                    row++
                }
            }
        }
    }
}

@Composable
fun StickerVisual(
    sticker: CreativeSticker,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp,
    showLabel: Boolean = true
) {
    val primary = Color(sticker.primaryArgb)
    val secondary = Color(sticker.secondaryArgb)

    when (sticker.badgeStyle) {
        StickerBadgeStyle.CLEAN_VECTOR -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = sticker.glyph,
                    fontSize = (iconSize.value * 0.85f).sp
                )
            }
        }
        StickerBadgeStyle.PASTEL_PILL -> {
            Surface(
                shape = RoundedCornerShape(50),
                color = secondary.copy(alpha = 0.94f),
                shadowElevation = 2.dp,
                modifier = modifier.border(1.2.dp, primary.copy(alpha = 0.55f), RoundedCornerShape(50))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = sticker.glyph, fontSize = (iconSize.value * 0.58f).sp)
                    if (showLabel) {
                        Text(
                            text = sticker.subLabel,
                            color = primary,
                            fontSize = (iconSize.value * 0.34f).coerceIn(10f, 15f).sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        StickerBadgeStyle.STAMP_SEAL -> {
            Box(
                modifier = modifier
                    .clip(CircleShape)
                    .background(secondary.copy(alpha = 0.92f))
                    .border(2.dp, primary.copy(alpha = 0.78f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = sticker.glyph, fontSize = (iconSize.value * 0.54f).sp)
                    if (showLabel) {
                        Text(
                            text = sticker.subLabel.uppercase(),
                            color = primary,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
        StickerBadgeStyle.WASHI_TAG -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(secondary.copy(alpha = 0.88f))
                    .border(1.dp, primary.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = sticker.glyph, fontSize = (iconSize.value * 0.54f).sp)
                    if (showLabel) {
                        Text(
                            text = sticker.subLabel,
                            color = primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
        StickerBadgeStyle.SPARKLE_BADGE -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(secondary, Color.White.copy(alpha = 0.9f))
                        )
                    )
                    .border(1.5.dp, primary.copy(alpha = 0.65f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 9.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(text = sticker.glyph, fontSize = (iconSize.value * 0.60f).sp)
                    Text(text = "✦", color = primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun FrameOverlayPreview(
    frameStyle: TemplateFrameStyle,
    template: AestheticTemplate?,
    thicknessFraction: Float,
    modifier: Modifier = Modifier
) {
    if (frameStyle == TemplateFrameStyle.NONE && template == null) return
    val effectiveStyle = if (frameStyle != TemplateFrameStyle.NONE) frameStyle else (template?.frameStyle ?: TemplateFrameStyle.NONE)
    if (effectiveStyle == TemplateFrameStyle.NONE) return

    val borderColor = template?.let { Color(it.borderArgb) } ?: Color(0xFFF3BDD0)
    val accentColor = template?.let { Color(it.accentArgb) } ?: Color(0xFF9E3B5D)
    val washiColor = template?.let { Color(it.washiArgb) } ?: Color(0xFFF7B8CD)

    Canvas(modifier = modifier) {
        val pad = (size.minDimension * thicknessFraction).coerceIn(8f, size.minDimension * 0.14f)
        when (effectiveStyle) {
            TemplateFrameStyle.POLAROID -> {
                // White borders + bottom chin
                drawRect(Color.White.copy(alpha = 0.94f), topLeft = Offset.Zero, size = Size(size.width, pad))
                drawRect(Color.White.copy(alpha = 0.94f), topLeft = Offset.Zero, size = Size(pad, size.height))
                drawRect(Color.White.copy(alpha = 0.94f), topLeft = Offset(size.width - pad, 0f), size = Size(pad, size.height))
                drawRect(Color.White.copy(alpha = 0.94f), topLeft = Offset(0f, size.height - pad * 2.4f), size = Size(size.width, pad * 2.4f))
                drawRect(
                    color = borderColor.copy(alpha = 0.7f),
                    topLeft = Offset(pad, pad),
                    size = Size(size.width - pad * 2f, size.height - pad * 3.4f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            TemplateFrameStyle.WASHI_SCRAPBOOK -> {
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.85f),
                    topLeft = Offset(pad * 0.45f, pad * 0.45f),
                    size = Size(size.width - pad * 0.9f, size.height - pad * 0.9f),
                    cornerRadius = CornerRadius(18f, 18f),
                    style = Stroke(width = pad * 0.7f)
                )
                // Top & bottom washi strips
                drawRoundRect(
                    color = washiColor.copy(alpha = 0.88f),
                    topLeft = Offset(size.width * 0.34f, 4f),
                    size = Size(size.width * 0.32f, pad * 0.9f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
            TemplateFrameStyle.FILM_STRIP -> {
                val strip = pad * 1.15f
                drawRect(Color(0xFF231D20), topLeft = Offset.Zero, size = Size(strip, size.height))
                drawRect(Color(0xFF231D20), topLeft = Offset(size.width - strip, 0f), size = Size(strip, size.height))
                var y = strip * 0.4f
                while (y < size.height - strip * 0.5f) {
                    drawRoundRect(
                        color = Color(0xFFFFF5EE),
                        topLeft = Offset(strip * 0.25f, y),
                        size = Size(strip * 0.5f, strip * 0.65f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    drawRoundRect(
                        color = Color(0xFFFFF5EE),
                        topLeft = Offset(size.width - strip * 0.75f, y),
                        size = Size(strip * 0.5f, strip * 0.65f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    y += strip * 1.25f
                }
            }
            TemplateFrameStyle.LACE_BORDER -> {
                drawRect(
                    color = Color.White.copy(alpha = 0.9f),
                    topLeft = Offset.Zero,
                    size = size,
                    style = Stroke(width = pad * 1.3f)
                )
                val r = pad * 0.52f
                var x = r
                while (x < size.width) {
                    drawCircle(Color.White.copy(alpha = 0.92f), radius = r, center = Offset(x, pad * 0.7f))
                    drawCircle(Color.White.copy(alpha = 0.92f), radius = r, center = Offset(x, size.height - pad * 0.7f))
                    x += r * 1.7f
                }
            }
            TemplateFrameStyle.GOLD_FOIL -> {
                drawRoundRect(
                    color = Color(0xFFCFA268),
                    topLeft = Offset(pad * 0.45f, pad * 0.45f),
                    size = Size(size.width - pad * 0.9f, size.height - pad * 0.9f),
                    cornerRadius = CornerRadius(24f, 24f),
                    style = Stroke(width = 4.dp.toPx())
                )
                drawRoundRect(
                    color = Color(0xFFE7C58B),
                    topLeft = Offset(pad * 0.85f, pad * 0.85f),
                    size = Size(size.width - pad * 1.7f, size.height - pad * 1.7f),
                    cornerRadius = CornerRadius(18f, 18f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            TemplateFrameStyle.ARCH_FRAME, TemplateFrameStyle.HEART_LOCKET -> {
                drawRoundRect(
                    color = borderColor.copy(alpha = 0.88f),
                    topLeft = Offset(pad * 0.5f, pad * 0.5f),
                    size = Size(size.width - pad, size.height - pad),
                    cornerRadius = CornerRadius(size.width * 0.28f, size.width * 0.28f),
                    style = Stroke(width = pad * 0.8f)
                )
            }
            TemplateFrameStyle.POSTCARD, TemplateFrameStyle.DOUBLE_MINIMAL, TemplateFrameStyle.COLLAGE_SPLIT -> {
                drawRect(
                    color = accentColor.copy(alpha = 0.85f),
                    topLeft = Offset(pad * 0.45f, pad * 0.45f),
                    size = Size(size.width - pad * 0.9f, size.height - pad * 0.9f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawRect(
                    color = borderColor.copy(alpha = 0.75f),
                    topLeft = Offset(pad * 0.85f, pad * 0.85f),
                    size = Size(size.width - pad * 1.7f, size.height - pad * 1.7f),
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }
            TemplateFrameStyle.NONE -> Unit
        }
    }
}

fun drawTemplateBackdropOnCanvas(
    canvas: Canvas,
    width: Float,
    height: Float,
    template: AestheticTemplate
) {
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            0f,
            height,
            template.bgStartArgb.toInt(),
            template.bgEndArgb.toInt(),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width, height, bgPaint)
}

fun drawFrameOnCanvas(
    canvas: Canvas,
    width: Float,
    height: Float,
    frameStyle: TemplateFrameStyle,
    template: AestheticTemplate?,
    thicknessFraction: Float
) {
    val effectiveStyle = if (frameStyle != TemplateFrameStyle.NONE) frameStyle else (template?.frameStyle ?: TemplateFrameStyle.NONE)
    if (effectiveStyle == TemplateFrameStyle.NONE) return

    val minDim = minOf(width, height)
    val pad = (minDim * thicknessFraction).coerceIn(14f, minDim * 0.14f)
    val borderColor = (template?.borderArgb ?: 0xFFF3BDD0L).toInt()
    val accentColor = (template?.accentArgb ?: 0xFF9E3B5DL).toInt()
    val washiColor = (template?.washiArgb ?: 0xFFF7B8CDL).toInt()

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }

    when (effectiveStyle) {
        TemplateFrameStyle.POLAROID -> {
            fillPaint.color = android.graphics.Color.WHITE
            canvas.drawRect(0f, 0f, width, pad, fillPaint)
            canvas.drawRect(0f, 0f, pad, height, fillPaint)
            canvas.drawRect(width - pad, 0f, width, height, fillPaint)
            canvas.drawRect(0f, height - pad * 2.4f, width, height, fillPaint)
            strokePaint.color = borderColor
            strokePaint.strokeWidth = 3f
            canvas.drawRect(pad, pad, width - pad, height - pad * 2.4f, strokePaint)
            if (template != null) {
                val txtPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = accentColor
                    textSize = pad * 0.72f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText(
                    "${template.cornerGlyph} ${template.name}",
                    width / 2f,
                    height - pad * 0.9f,
                    txtPaint
                )
            }
        }
        TemplateFrameStyle.WASHI_SCRAPBOOK -> {
            strokePaint.color = borderColor
            strokePaint.strokeWidth = pad * 0.75f
            canvas.drawRoundRect(RectF(pad * 0.4f, pad * 0.4f, width - pad * 0.4f, height - pad * 0.4f), 24f, 24f, strokePaint)
            fillPaint.color = washiColor
            canvas.drawRoundRect(RectF(width * 0.34f, 6f, width * 0.66f, pad * 0.95f), 10f, 10f, fillPaint)
        }
        TemplateFrameStyle.FILM_STRIP -> {
            val strip = pad * 1.15f
            fillPaint.color = 0xFF231D20.toInt()
            canvas.drawRect(0f, 0f, strip, height, fillPaint)
            canvas.drawRect(width - strip, 0f, width, height, fillPaint)
            fillPaint.color = 0xFFFFF5EE.toInt()
            var y = strip * 0.4f
            while (y < height - strip * 0.5f) {
                canvas.drawRoundRect(RectF(strip * 0.25f, y, strip * 0.75f, y + strip * 0.65f), 6f, 6f, fillPaint)
                canvas.drawRoundRect(RectF(width - strip * 0.75f, y, width - strip * 0.25f, y + strip * 0.65f), 6f, 6f, fillPaint)
                y += strip * 1.25f
            }
        }
        TemplateFrameStyle.LACE_BORDER -> {
            strokePaint.color = android.graphics.Color.WHITE
            strokePaint.strokeWidth = pad * 1.25f
            canvas.drawRect(0f, 0f, width, height, strokePaint)
            fillPaint.color = android.graphics.Color.WHITE
            val r = pad * 0.52f
            var x = r
            while (x < width) {
                canvas.drawCircle(x, pad * 0.7f, r, fillPaint)
                canvas.drawCircle(x, height - pad * 0.7f, r, fillPaint)
                x += r * 1.7f
            }
        }
        TemplateFrameStyle.GOLD_FOIL -> {
            strokePaint.color = 0xFFCFA268.toInt()
            strokePaint.strokeWidth = pad * 0.35f
            canvas.drawRoundRect(RectF(pad * 0.45f, pad * 0.45f, width - pad * 0.45f, height - pad * 0.45f), 28f, 28f, strokePaint)
            strokePaint.color = 0xFFE7C58B.toInt()
            strokePaint.strokeWidth = pad * 0.14f
            canvas.drawRoundRect(RectF(pad * 0.85f, pad * 0.85f, width - pad * 0.85f, height - pad * 0.85f), 20f, 20f, strokePaint)
        }
        TemplateFrameStyle.ARCH_FRAME, TemplateFrameStyle.HEART_LOCKET -> {
            strokePaint.color = borderColor
            strokePaint.strokeWidth = pad * 0.8f
            val r = width * 0.26f
            canvas.drawRoundRect(RectF(pad * 0.5f, pad * 0.5f, width - pad * 0.5f, height - pad * 0.5f), r, r, strokePaint)
        }
        TemplateFrameStyle.POSTCARD, TemplateFrameStyle.DOUBLE_MINIMAL, TemplateFrameStyle.COLLAGE_SPLIT -> {
            strokePaint.color = accentColor
            strokePaint.strokeWidth = pad * 0.22f
            canvas.drawRect(pad * 0.45f, pad * 0.45f, width - pad * 0.45f, height - pad * 0.45f, strokePaint)
            strokePaint.color = borderColor
            strokePaint.strokeWidth = pad * 0.14f
            canvas.drawRect(pad * 0.85f, pad * 0.85f, width - pad * 0.85f, height - pad * 0.85f, strokePaint)
        }
        TemplateFrameStyle.NONE -> Unit
    }
}

fun drawStickerOnCanvas(
    canvas: Canvas,
    sticker: CreativeSticker,
    centerX: Float,
    centerY: Float,
    baseSizePx: Float,
    scale: Float,
    rotationDeg: Float,
    opacity: Float
) {
    canvas.save()
    canvas.translate(centerX, centerY)
    canvas.rotate(rotationDeg)
    canvas.scale(scale, scale)

    val alpha = (opacity.coerceIn(0f, 1f) * 255).toInt()
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = sticker.secondaryArgb.toInt()
        this.alpha = alpha
    }
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = (baseSizePx * 0.05f).coerceAtLeast(3f)
        color = sticker.primaryArgb.toInt()
        this.alpha = alpha
    }
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = baseSizePx * 0.65f
        this.alpha = alpha
    }

    when (sticker.badgeStyle) {
        StickerBadgeStyle.CLEAN_VECTOR -> {
            textPaint.textSize = baseSizePx * 0.9f
            canvas.drawText(sticker.glyph, 0f, baseSizePx * 0.32f, textPaint)
        }
        StickerBadgeStyle.PASTEL_PILL, StickerBadgeStyle.WASHI_TAG, StickerBadgeStyle.SPARKLE_BADGE -> {
            val w = baseSizePx * 1.9f
            val h = baseSizePx * 0.95f
            val rect = RectF(-w / 2f, -h / 2f, w / 2f, h / 2f)
            val rad = if (sticker.badgeStyle == StickerBadgeStyle.WASHI_TAG) h * 0.16f else h / 2f
            canvas.drawRoundRect(rect, rad, rad, bgPaint)
            canvas.drawRoundRect(rect, rad, rad, borderPaint)
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sticker.primaryArgb.toInt()
                this.alpha = alpha
                textSize = baseSizePx * 0.34f
                isFakeBoldText = true
                textAlign = Paint.Align.LEFT
            }
            textPaint.textSize = baseSizePx * 0.52f
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(sticker.glyph, -w * 0.06f, baseSizePx * 0.18f, textPaint)
            canvas.drawText(sticker.subLabel, w * 0.02f, baseSizePx * 0.13f, labelPaint)
        }
        StickerBadgeStyle.STAMP_SEAL -> {
            val r = baseSizePx * 0.72f
            canvas.drawCircle(0f, 0f, r, bgPaint)
            canvas.drawCircle(0f, 0f, r, borderPaint)
            textPaint.textSize = baseSizePx * 0.48f
            canvas.drawText(sticker.glyph, 0f, baseSizePx * 0.04f, textPaint)
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = sticker.primaryArgb.toInt()
                this.alpha = alpha
                textSize = baseSizePx * 0.22f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(sticker.subLabel.uppercase(), 0f, baseSizePx * 0.42f, labelPaint)
        }
    }
    canvas.restore()
}
