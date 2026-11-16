package com.example.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.media.ExifInterface
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.CutoutBackdrop
import com.example.data.model.EditorFontOption
import com.example.data.model.PhotoAdjustments
import com.example.data.model.PhotoEditLayer
import com.example.data.model.PhotoEditState
import com.example.data.model.ResizePreset
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.roundToInt

data class BackgroundRemovalResult(
    val cutoutFilePath: String,
    val foregroundPixelRatio: Float
)

object PhotoEditEngine {

    fun decodeOrientedSampledBitmap(path: String, maxDimensionPx: Int): Bitmap? {
        val file = File(path)
        if (!file.exists()) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        var inSampleSize = 1
        if (maxDimensionPx > 0) {
            while ((bounds.outWidth / inSampleSize) > maxDimensionPx * 2 ||
                (bounds.outHeight / inSampleSize) > maxDimensionPx * 2
            ) {
                inSampleSize *= 2
            }
        }
        val opts = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val raw = BitmapFactory.decodeFile(file.absolutePath, opts) ?: return null
        val exifAngle = runCatching {
            when (ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        }.getOrDefault(0f)

        val oriented = if (exifAngle != 0f) {
            val m = Matrix().apply { postRotate(exifAngle) }
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, m, true).also {
                if (it !== raw) raw.recycle()
            }
        } else {
            raw
        }

        if (maxDimensionPx <= 0) return oriented
        val curMax = maxOf(oriented.width, oriented.height)
        if (curMax <= maxDimensionPx) return oriented
        val ratio = maxDimensionPx.toFloat() / curMax.toFloat()
        val targetW = (oriented.width * ratio).roundToInt().coerceAtLeast(1)
        val targetH = (oriented.height * ratio).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(oriented, targetW, targetH, true).also {
            if (it !== oriented) oriented.recycle()
        }
    }

    fun buildCombinedColorMatrix(adjustments: PhotoAdjustments): ColorMatrix {
        val filter = adjustments.filterPreset
        val totalContrast = (adjustments.contrast * filter.contrast).coerceIn(0.35f, 2.2f)
        val totalSaturation = (adjustments.saturation * filter.saturation).coerceIn(0f, 2.4f)
        val totalBrightness = ((adjustments.brightness + adjustments.exposure * 0.45f + filter.brightness) * 140f)
            .coerceIn(-180f, 180f)

        val matrix = ColorMatrix()

        val satMatrix = ColorMatrix().apply { setSaturation(totalSaturation) }
        matrix.postConcat(satMatrix)

        val translate = (1f - totalContrast) * 128f + totalBrightness
        val contrastBrightnessMatrix = ColorMatrix(
            floatArrayOf(
                totalContrast, 0f, 0f, 0f, translate,
                0f, totalContrast, 0f, 0f, translate,
                0f, 0f, totalContrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        matrix.postConcat(contrastBrightnessMatrix)

        if (filter.tintAlpha > 0f) {
            val a = filter.tintAlpha.coerceIn(0f, 0.4f)
            val inv = 1f - a
            val tr = Color.red(filter.tintArgb.toInt()).toFloat() * a
            val tg = Color.green(filter.tintArgb.toInt()).toFloat() * a
            val tb = Color.blue(filter.tintArgb.toInt()).toFloat() * a
            val tintMatrix = ColorMatrix(
                floatArrayOf(
                    inv, 0f, 0f, 0f, tr,
                    0f, inv, 0f, 0f, tg,
                    0f, 0f, inv, 0f, tb,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(tintMatrix)
        }
        return matrix
    }

    suspend fun removeBackgroundOnDevice(
        sourcePath: String,
        cutoutOutputFile: File
    ): BackgroundRemovalResult = withContext(Dispatchers.Default) {
        val bitmap = decodeOrientedSampledBitmap(sourcePath, 1280)
            ?: error("Could not decode photograph for background removal.")

        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .build()
        val segmenter = Segmentation.getClient(options)

        val mask = suspendCancellableCoroutine { cont ->
            segmenter.process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { result -> cont.resume(result) }
                .addOnFailureListener { cont.resume(null) }
        }
        runCatching { segmenter.close() }

        val outBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        var foregroundCount = 0
        if (mask != null && mask.width > 0 && mask.height > 0) {
            val buffer = mask.buffer
            buffer.rewind()
            val maskW = mask.width
            val maskH = mask.height
            val confidences = FloatArray(maskW * maskH)
            for (i in confidences.indices) {
                confidences[i] = if (buffer.remaining() >= 4) buffer.float else 0f
            }

            for (y in 0 until bitmap.height) {
                val my = (y * maskH / bitmap.height).coerceIn(0, maskH - 1)
                for (x in 0 until bitmap.width) {
                    val mx = (x * maskW / bitmap.width).coerceIn(0, maskW - 1)
                    val conf = confidences[my * maskW + mx]
                    val alphaFactor = ((conf - 0.32f) / 0.44f).coerceIn(0f, 1f)
                    if (alphaFactor > 0.3f) foregroundCount++
                    val idx = y * bitmap.width + x
                    val orig = pixels[idx]
                    val newAlpha = (Color.alpha(orig) * alphaFactor).roundToInt().coerceIn(0, 255)
                    pixels[idx] = Color.argb(newAlpha, Color.red(orig), Color.green(orig), Color.blue(orig))
                }
            }
        }

        // Fallback radial subject preservation if no human figure was detected
        val fgRatio = foregroundCount.toFloat() / pixels.size.coerceAtLeast(1)
        if (fgRatio < 0.03f) {
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            // Soft border edge chroma distance matte from corners
            val corner = pixels[0]
            val cr = Color.red(corner)
            val cg = Color.green(corner)
            val cb = Color.blue(corner)
            var kept = 0
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    val idx = y * bitmap.width + x
                    val p = pixels[idx]
                    val dist = abs(Color.red(p) - cr) + abs(Color.green(p) - cg) + abs(Color.blue(p) - cb)
                    val nx = (x.toFloat() / bitmap.width) - 0.5f
                    val ny = (y.toFloat() / bitmap.height) - 0.5f
                    val inCenter = (nx * nx + ny * ny) < 0.18f
                    val alphaFactor = if (dist > 78 || inCenter) 1f else ((dist - 28f) / 50f).coerceIn(0f, 1f)
                    if (alphaFactor > 0.3f) kept++
                    pixels[idx] = Color.argb(
                        (Color.alpha(p) * alphaFactor).roundToInt(),
                        Color.red(p),
                        Color.green(p),
                        Color.blue(p)
                    )
                }
            }
            outBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            FileOutputStream(cutoutOutputFile).use { out ->
                outBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            bitmap.recycle()
            outBitmap.recycle()
            return@withContext BackgroundRemovalResult(
                cutoutFilePath = cutoutOutputFile.absolutePath,
                foregroundPixelRatio = kept.toFloat() / pixels.size.coerceAtLeast(1)
            )
        }

        outBitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        FileOutputStream(cutoutOutputFile).use { out ->
            outBitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
        }
        bitmap.recycle()
        outBitmap.recycle()
        BackgroundRemovalResult(
            cutoutFilePath = cutoutOutputFile.absolutePath,
            foregroundPixelRatio = fgRatio
        )
    }

    suspend fun renderFinalPhotoBitmap(
        context: Context,
        state: PhotoEditState,
        previewModeMaxDim: Int = 0
    ): Bitmap = withContext(Dispatchers.Default) {
        val adj = state.adjustments
        val targetMax = when {
            previewModeMaxDim > 0 -> previewModeMaxDim
            adj.resizePreset != ResizePreset.ORIGINAL -> adj.resizePreset.maxDimensionPx
            else -> 2400
        }

        val activePath = if (adj.backgroundRemoved && !state.cutoutMaskPath.isNullOrBlank() && File(state.cutoutMaskPath).exists()) {
            state.cutoutMaskPath
        } else {
            state.sourcePath
        }
        val base = decodeOrientedSampledBitmap(activePath, targetMax)
            ?: error("Source photo could not be opened.")

        // 1. Apply Crop + Quarter Rotation + Flip + Straighten
        val cropL = adj.cropLeft.coerceIn(0f, 0.9f)
        val cropT = adj.cropTop.coerceIn(0f, 0.9f)
        val cropR = adj.cropRight.coerceIn(cropL + 0.08f, 1f)
        val cropB = adj.cropBottom.coerceIn(cropT + 0.08f, 1f)

        val sx = (cropL * base.width).roundToInt().coerceIn(0, base.width - 2)
        val sy = (cropT * base.height).roundToInt().coerceIn(0, base.height - 2)
        val sw = ((cropR - cropL) * base.width).roundToInt().coerceIn(2, base.width - sx)
        val sh = ((cropB - cropT) * base.height).roundToInt().coerceIn(2, base.height - sy)

        val cropped = Bitmap.createBitmap(base, sx, sy, sw, sh)
        if (cropped !== base) base.recycle()

        val transformMatrix = Matrix().apply {
            val totalAngle = (adj.rotationQuarterTurns * 90f) + adj.straightenDegrees
            if (totalAngle != 0f) postRotate(totalAngle)
            val fx = if (adj.flipHorizontal) -1f else 1f
            val fy = if (adj.flipVertical) -1f else 1f
            if (fx != 1f || fy != 1f) postScale(fx, fy)
        }
        val transformed = if (!transformMatrix.isIdentity) {
            Bitmap.createBitmap(cropped, 0, 0, cropped.width, cropped.height, transformMatrix, true).also {
                if (it !== cropped) cropped.recycle()
            }
        } else {
            cropped
        }

        // 2. Color Matrix (Brightness, Contrast, Saturation, Exposure, Filter)
        val colored = Bitmap.createBitmap(transformed.width, transformed.height, Bitmap.Config.ARGB_8888)
        val colorCanvas = Canvas(colored)
        val colorPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
            colorFilter = ColorMatrixColorFilter(buildCombinedColorMatrix(adj))
        }
        colorCanvas.drawBitmap(transformed, 0f, 0f, colorPaint)
        if (transformed !== colored) transformed.recycle()

        // 3. Sharpness & Blur
        val detailed = applyBlurAndSharpen(
            src = colored,
            blurRadius = adj.blurRadius,
            sharpness = adj.sharpness
        )
        if (detailed !== colored) colored.recycle()

        // 4. Final Output Canvas with Template Backdrop, Cutout Backdrop, Frames, Layers & Eraser Strokes
        val finalBitmap = Bitmap.createBitmap(detailed.width, detailed.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(finalBitmap)
        val w = finalBitmap.width.toFloat()
        val h = finalBitmap.height.toFloat()

        val template = CreativeLibraryCatalog.templateById(adj.appliedTemplateId)
        if (adj.backgroundRemoved) {
            when (adj.cutoutBackdrop) {
                CutoutBackdrop.NONE, CutoutBackdrop.TRANSPARENT -> Unit
                else -> canvas.drawColor(adj.cutoutBackdrop.argb.toInt())
            }
        }
        if (template != null && (!adj.backgroundRemoved || adj.cutoutBackdrop == CutoutBackdrop.NONE)) {
            drawTemplateBackdropOnCanvas(canvas, w, h, template)
        }

        // Photo drawn inside template margin if template is active
        val photoLayerBitmap = Bitmap.createBitmap(detailed.width, detailed.height, Bitmap.Config.ARGB_8888)
        val photoLayerCanvas = Canvas(photoLayerBitmap)
        if (template != null) {
            val inset = minOf(w, h) * 0.055f
            photoLayerCanvas.drawBitmap(
                detailed,
                null,
                RectF(inset, inset, w - inset, h - inset),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        } else {
            photoLayerCanvas.drawBitmap(detailed, 0f, 0f, null)
        }
        detailed.recycle()

        // Apply eraser strokes onto photoLayerBitmap before compositing over backdrop
        val eraserPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        state.layers.filterIsInstance<PhotoEditLayer.DoodleStrokeLayer>()
            .filter { it.isVisible && it.isEraser && it.points.size >= 2 }
            .forEach { eraser ->
                eraserPaint.strokeWidth = (minOf(w, h) * eraser.strokeWidthFraction).coerceAtLeast(6f)
                val path = Path()
                eraser.points.forEachIndexed { idx, pt ->
                    val px = pt.x * w
                    val py = pt.y * h
                    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                }
                photoLayerCanvas.drawPath(path, eraserPaint)
            }

        canvas.drawBitmap(photoLayerBitmap, 0f, 0f, null)
        photoLayerBitmap.recycle()

        // Frame / Border
        drawFrameOnCanvas(
            canvas = canvas,
            width = w,
            height = h,
            frameStyle = adj.frameStyle,
            template = template,
            thicknessFraction = adj.frameThickness
        )

        // Render Non-Eraser Layers in Z-Order
        state.layers.filter { it.isVisible }.forEach { layer ->
            when (layer) {
                is PhotoEditLayer.StickerLayer -> {
                    val sticker = CreativeLibraryCatalog.stickerById(layer.stickerId) ?: return@forEach
                    drawStickerOnCanvas(
                        canvas = canvas,
                        sticker = sticker,
                        centerX = layer.xFraction * w,
                        centerY = layer.yFraction * h,
                        baseSizePx = minOf(w, h) * 0.13f,
                        scale = layer.scale,
                        rotationDeg = layer.rotationDeg,
                        opacity = layer.opacity
                    )
                }
                is PhotoEditLayer.TextLayer -> {
                    if (layer.text.isNotBlank()) {
                        canvas.save()
                        canvas.translate(layer.xFraction * w, layer.yFraction * h)
                        canvas.rotate(layer.rotationDeg)
                        canvas.scale(layer.scale, layer.scale)

                        val font = EditorFontOption.fromId(layer.fontId)
                        val scaledTextPx = (minOf(w, h) * (layer.fontSizeSp / 380f)).coerceAtLeast(18f)
                        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = layer.textColorArgb.toInt()
                            alpha = (layer.opacity * 255).toInt()
                            textSize = scaledTextPx
                            typeface = font.toAndroidTypeface(context)
                            textAlign = Paint.Align.CENTER
                        }
                        val lines = layer.text.split("\n")
                        val lineH = scaledTextPx * 1.25f
                        val maxLineW = lines.maxOfOrNull { textPaint.measureText(it) } ?: 40f
                        val totalH = lines.size * lineH

                        if (layer.hasPillBackground) {
                            val padX = scaledTextPx * 0.65f
                            val padY = scaledTextPx * 0.35f
                            val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                                color = layer.pillColorArgb.toInt()
                                alpha = (layer.opacity * 225).toInt()
                            }
                            canvas.drawRoundRect(
                                RectF(-maxLineW / 2f - padX, -totalH / 2f - padY, maxLineW / 2f + padX, totalH / 2f + padY),
                                scaledTextPx * 0.6f,
                                scaledTextPx * 0.6f,
                                pillPaint
                            )
                        }

                        var yCursor = -totalH / 2f + scaledTextPx * 0.9f
                        lines.forEach { line ->
                            canvas.drawText(line, 0f, yCursor, textPaint)
                            yCursor += lineH
                        }
                        canvas.restore()
                    }
                }
                is PhotoEditLayer.DoodleStrokeLayer -> {
                    if (!layer.isEraser && layer.points.size >= 2) {
                        val doodlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = layer.colorArgb.toInt()
                            alpha = (layer.opacity * 255).toInt()
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                            strokeWidth = (minOf(w, h) * layer.strokeWidthFraction).coerceAtLeast(4f)
                        }
                        val path = Path()
                        layer.points.forEachIndexed { idx, pt ->
                            val px = pt.x * w
                            val py = pt.y * h
                            if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                        }
                        canvas.drawPath(path, doodlePaint)
                    }
                }
            }
        }

        // Resize preset enforcement
        val maxFinal = adj.resizePreset.maxDimensionPx
        if (previewModeMaxDim <= 0 && maxFinal > 0 && maxOf(finalBitmap.width, finalBitmap.height) > maxFinal) {
            val scale = maxFinal.toFloat() / maxOf(finalBitmap.width, finalBitmap.height).toFloat()
            val rw = (finalBitmap.width * scale).roundToInt().coerceAtLeast(1)
            val rh = (finalBitmap.height * scale).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(finalBitmap, rw, rh, true).also {
                if (it !== finalBitmap) finalBitmap.recycle()
            }
        } else {
            finalBitmap
        }
    }

    private fun applyBlurAndSharpen(
        src: Bitmap,
        blurRadius: Float,
        sharpness: Float
    ): Bitmap {
        var current = src
        if (blurRadius >= 0.8f) {
            val rad = blurRadius.roundToInt().coerceIn(1, 22)
            current = fastBoxBlur(current, rad)
        }
        if (sharpness >= 0.05f) {
            val sharpened = applyUnsharpMask(current, sharpness.coerceIn(0f, 1f))
            if (current !== src && sharpened !== current) current.recycle()
            current = sharpened
        }
        return current
    }

    private fun fastBoxBlur(src: Bitmap, radius: Int): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)
        val tmp = IntArray(w * h)
        val out = IntArray(w * h)
        val r = radius.coerceIn(1, minOf(w, h) / 4).coerceAtLeast(1)
        val div = r * 2 + 1

        // Horizontal pass with sliding sum
        for (y in 0 until h) {
            var sr = 0; var sg = 0; var sb = 0; var sa = 0
            val rowOff = y * w
            for (k in -r..r) {
                val c = pix[rowOff + k.coerceIn(0, w - 1)]
                sa += Color.alpha(c); sr += Color.red(c); sg += Color.green(c); sb += Color.blue(c)
            }
            for (x in 0 until w) {
                tmp[rowOff + x] = Color.argb(sa / div, sr / div, sg / div, sb / div)
                val removeC = pix[rowOff + (x - r).coerceIn(0, w - 1)]
                val addC = pix[rowOff + (x + r + 1).coerceIn(0, w - 1)]
                sa += Color.alpha(addC) - Color.alpha(removeC)
                sr += Color.red(addC) - Color.red(removeC)
                sg += Color.green(addC) - Color.green(removeC)
                sb += Color.blue(addC) - Color.blue(removeC)
            }
        }
        // Vertical pass
        for (x in 0 until w) {
            var sr = 0; var sg = 0; var sb = 0; var sa = 0
            for (k in -r..r) {
                val c = tmp[k.coerceIn(0, h - 1) * w + x]
                sa += Color.alpha(c); sr += Color.red(c); sg += Color.green(c); sb += Color.blue(c)
            }
            for (y in 0 until h) {
                out[y * w + x] = Color.argb(sa / div, sr / div, sg / div, sb / div)
                val removeC = tmp[(y - r).coerceIn(0, h - 1) * w + x]
                val addC = tmp[(y + r + 1).coerceIn(0, h - 1) * w + x]
                sa += Color.alpha(addC) - Color.alpha(removeC)
                sr += Color.red(addC) - Color.red(removeC)
                sg += Color.green(addC) - Color.green(removeC)
                sb += Color.blue(addC) - Color.blue(removeC)
            }
        }
        return Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun applyUnsharpMask(src: Bitmap, amount: Float): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)
        val out = pix.clone()
        val strength = amount * 0.9f
        for (y in 1 until h - 1) {
            val row = y * w
            for (x in 1 until w - 1) {
                val c = pix[row + x]
                val top = pix[row - w + x]
                val bot = pix[row + w + x]
                val left = pix[row + x - 1]
                val right = pix[row + x + 1]

                val a = Color.alpha(c)
                val r = (Color.red(c) * (1f + 4f * strength) - (Color.red(top) + Color.red(bot) + Color.red(left) + Color.red(right)) * strength).roundToInt().coerceIn(0, 255)
                val g = (Color.green(c) * (1f + 4f * strength) - (Color.green(top) + Color.green(bot) + Color.green(left) + Color.green(right)) * strength).roundToInt().coerceIn(0, 255)
                val b = (Color.blue(c) * (1f + 4f * strength) - (Color.blue(top) + Color.blue(bot) + Color.blue(left) + Color.blue(right)) * strength).roundToInt().coerceIn(0, 255)
                out[row + x] = Color.argb(a, r, g, b)
            }
        }
        return Bitmap.createBitmap(out, w, h, Bitmap.Config.ARGB_8888)
    }
}
