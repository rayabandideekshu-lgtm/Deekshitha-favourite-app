package com.example.ui.editor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.AudioFormat
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.audio.SpeedProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.Brightness
import androidx.media3.effect.Contrast
import androidx.media3.effect.Crop
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.Presentation
import androidx.media3.effect.ScaleAndRotateTransformation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.data.model.CaptionAnimationStyle
import com.example.data.model.CreativeLibraryCatalog
import com.example.data.model.EditorFontOption
import com.example.data.model.PipOverlayItem
import com.example.data.model.VideoAspectRatio
import com.example.data.model.VideoClipItem
import com.example.data.model.VideoColorFilter
import com.example.data.model.VideoEditState
import com.example.data.model.VideoExportResolution
import com.example.data.model.VideoTransitionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

data class ExportResolutionSupport(
    val resolution: VideoExportResolution,
    val supported: Boolean,
    val note: String
)

object VideoExportEngine {

    fun probeSupportedResolutions(): List<ExportResolutionSupport> {
        val supports4K = runCatching {
            val list = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            list.codecInfos.any { info ->
                info.isEncoder && info.supportedTypes.any { type ->
                    if (!type.equals(MediaFormatMimeTypeH264, true) && !type.equals(MediaFormatMimeTypeHevc, true)) {
                        false
                    } else {
                        val caps = info.getCapabilitiesForType(type)?.videoCapabilities
                        caps != null && (caps.isSizeSupported(3840, 2160) || caps.isSizeSupported(2160, 3840))
                    }
                }
            }
        }.getOrDefault(false)

        return listOf(
            ExportResolutionSupport(VideoExportResolution.HD_720P, true, "Fast & crisp"),
            ExportResolutionSupport(VideoExportResolution.FHD_1080P, true, "Recommended HD"),
            ExportResolutionSupport(
                VideoExportResolution.UHD_4K,
                supports4K,
                if (supports4K) "Ultra HD supported" else "Not supported by device encoder"
            )
        )
    }

    private const val MediaFormatMimeTypeH264 = "video/avc"
    private const val MediaFormatMimeTypeHevc = "video/hevc"

    fun computeOutputDimensions(
        state: VideoEditState,
        resolution: VideoExportResolution
    ): Pair<Int, Int> {
        val shortEdge = when (resolution) {
            VideoExportResolution.HD_720P -> 720
            VideoExportResolution.FHD_1080P -> 1080
            VideoExportResolution.UHD_4K -> 2160
        }
        val longEdge = when (resolution) {
            VideoExportResolution.HD_720P -> 1280
            VideoExportResolution.FHD_1080P -> 1920
            VideoExportResolution.UHD_4K -> 3840
        }

        fun align16(v: Int): Int = ((v / 16).coerceAtLeast(2)) * 16

        return when (state.aspectRatio) {
            VideoAspectRatio.PORTRAIT_9_16 -> align16(shortEdge) to align16(longEdge)
            VideoAspectRatio.LANDSCAPE_16_9 -> align16(longEdge) to align16(shortEdge)
            VideoAspectRatio.SQUARE_1_1 -> align16(shortEdge) to align16(shortEdge)
            VideoAspectRatio.PORTRAIT_4_5 -> align16(shortEdge) to align16((shortEdge * 5f / 4f).roundToInt())
            VideoAspectRatio.CLASSIC_4_3 -> align16((shortEdge * 4f / 3f).roundToInt()) to align16(shortEdge)
            VideoAspectRatio.ORIGINAL -> {
                val first = state.clips.firstOrNull { File(it.sourcePath).exists() }
                if (first != null && !first.isImageClip) {
                    val retriever = MediaMetadataRetriever()
                    val dims = runCatching {
                        retriever.setDataSource(first.sourcePath)
                        val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 720
                        val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1280
                        val rot = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0
                        if (rot == 90 || rot == 270) h to w else w to h
                    }.getOrNull()
                    runCatching { retriever.release() }
                    if (dims != null && dims.first > 0 && dims.second > 0) {
                        val aspect = dims.first.toFloat() / dims.second.toFloat()
                        if (aspect >= 1f) {
                            align16((shortEdge * aspect).roundToInt()) to align16(shortEdge)
                        } else {
                            align16(shortEdge) to align16((shortEdge / aspect).roundToInt())
                        }
                    } else {
                        align16(shortEdge) to align16(longEdge)
                    }
                } else {
                    align16(shortEdge) to align16(longEdge)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    suspend fun exportVideo(
        context: Context,
        state: VideoEditState,
        resolution: VideoExportResolution,
        outputFile: File,
        onStageAndProgress: (stage: String, progressPercent: Int) -> Unit,
        onTransformerCreated: (Transformer) -> Unit = {}
    ): File {
        require(state.clips.isNotEmpty()) { "Add at least one video clip or photo before exporting." }
        if (outputFile.exists()) outputFile.delete()

        val tempReversedFiles = mutableListOf<File>()
        val clipEffectiveSources = mutableListOf<Pair<VideoClipItem, String>>()

        // 1. Pre-process reversed video clips
        val totalReverseClips = state.clips.count { it.isReversed && !it.isImageClip }
        var reversedDone = 0
        state.clips.forEach { clip ->
            if (clip.isReversed && !clip.isImageClip && File(clip.sourcePath).exists()) {
                onStageAndProgress("Reversing clip ${reversedDone + 1}/$totalReverseClips...", 4 + (reversedDone * 10 / totalReverseClips.coerceAtLeast(1)))
                val revFile = File(context.cacheDir, "REV_${UUID.randomUUID().toString().take(8)}.mp4")
                VideoReverseProcessor.createReversedClip(
                    sourcePath = clip.sourcePath,
                    trimStartMs = clip.trimStartMs,
                    trimEndMs = clip.trimEndMs,
                    outputFile = revFile
                ) { p ->
                    val overall = ((reversedDone + p) / totalReverseClips.coerceAtLeast(1)) * 18f
                    onStageAndProgress("Reversing video clip...", overall.roundToInt().coerceIn(2, 20))
                }
                tempReversedFiles += revFile
                clipEffectiveSources += clip.copy(trimStartMs = 0L, trimEndMs = (clip.trimEndMs - clip.trimStartMs).coerceAtLeast(400L)) to revFile.absolutePath
                reversedDone++
            } else {
                clipEffectiveSources += clip to clip.sourcePath
            }
        }

        val (outW, outH) = computeOutputDimensions(state, resolution)
        val overlay = DynamicTimelineBitmapOverlay(
            context = context,
            state = state,
            canvasWidth = outW.coerceAtMost(1080),
            canvasHeight = ((outH.toFloat() / outW.toFloat()) * outW.coerceAtMost(1080)).roundToInt().coerceAtLeast(2)
        )

        onStageAndProgress("Rendering multi-track timeline...", if (totalReverseClips > 0) 22 else 5)

        return try {
            val primaryItems = clipEffectiveSources.map { (clip, path) ->
                val uri = Uri.fromFile(File(path))
                val mediaItem = if (clip.isImageClip) {
                    MediaItem.Builder()
                        .setUri(uri)
                        .setImageDurationMs(clip.effectiveDurationMs)
                        .build()
                } else {
                    val clipStart = clip.trimStartMs.coerceAtLeast(0L)
                    val clipEnd = clip.trimEndMs.coerceAtLeast(clipStart + 300L)
                    MediaItem.Builder()
                        .setUri(uri)
                        .setClippingConfiguration(
                            MediaItem.ClippingConfiguration.Builder()
                                .setStartPositionMs(clipStart)
                                .setEndPositionMs(clipEnd)
                                .build()
                        )
                        .build()
                }

                val speedPair = if (!clip.isImageClip && abs(clip.speed - 1f) > 0.02f) {
                    val speedValue = clip.speed.coerceIn(0.25f, 4f)
                    Effects.createExperimentalSpeedChangingEffect(
                        object : SpeedProvider {
                            override fun getSpeed(timeUs: Long): Float = speedValue
                            override fun getNextSpeedChangeTimeUs(timeUs: Long): Long = C.TIME_UNSET
                        }
                    )
                } else {
                    null
                }

                val videoEffects = buildList<Effect> {
                    // Stabilization micro-crop + user zoom/pan
                    val effectiveZoom = (clip.cropZoom * if (clip.stabilize) 1.08f else 1f).coerceIn(1f, 3f)
                    if (effectiveZoom > 1.01f || clip.cropPanX != 0f || clip.cropPanY != 0f) {
                        val halfSpan = 1f / effectiveZoom
                        val cx = clip.cropPanX.coerceIn(-(1f - halfSpan), 1f - halfSpan)
                        val cy = clip.cropPanY.coerceIn(-(1f - halfSpan), 1f - halfSpan)
                        add(Crop(cx - halfSpan, cx + halfSpan, cy - halfSpan, cy + halfSpan))
                    }

                    // Rotate & horizontal flip
                    val rot = ((clip.rotationDeg % 360) + 360) % 360
                    if (rot != 0 || clip.flipHorizontal) {
                        add(
                            ScaleAndRotateTransformation.Builder()
                                .setRotationDegrees(rot.toFloat())
                                .setScale(if (clip.flipHorizontal) -1f else 1f, 1f)
                                .build()
                        )
                    }

                    // Color grading & brightness / contrast / saturation
                    val filter: VideoColorFilter = clip.filter
                    val totalBrightness = (clip.brightness + filter.brightness).coerceIn(-0.6f, 0.6f)
                    if (abs(totalBrightness) > 0.01f) {
                        add(Brightness(totalBrightness))
                    }
                    val totalContrast = ((clip.contrast * filter.contrast) - 1f).coerceIn(-0.7f, 0.9f)
                    if (abs(totalContrast) > 0.01f) {
                        add(Contrast(totalContrast))
                    }
                    val totalSatDelta = ((clip.saturation * filter.saturation) - 1f) * 100f
                    if (abs(totalSatDelta) > 1f) {
                        add(
                            HslAdjustment.Builder()
                                .adjustSaturation(totalSatDelta.coerceIn(-100f, 100f))
                                .build()
                        )
                    }

                    speedPair?.second?.let { add(it) }

                    add(Presentation.createForWidthAndHeight(outW, outH, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
                }

                val audioProcessors = buildList<AudioProcessor> {
                    speedPair?.first?.let { add(it) }
                    if (!clip.isImageClip && (abs(clip.volume - 1f) > 0.01f || clip.fadeInMs > 0L || clip.fadeOutMs > 0L)) {
                        add(
                            VolumeFadeAudioProcessor(
                                gain = clip.volume,
                                fadeInMs = clip.fadeInMs,
                                fadeOutMs = clip.fadeOutMs,
                                totalDurationMs = clip.effectiveDurationMs
                            )
                        )
                    }
                }

                val builder = EditedMediaItem.Builder(mediaItem)
                    .setEffects(Effects(audioProcessors, videoEffects))
                if (clip.isImageClip) {
                    builder.setFrameRate(30)
                }
                builder.build()
            }

            val videoSequence = EditedMediaItemSequence(primaryItems)
            val activeAudioTracks = state.audioTracks.filter { !it.isMuted && File(it.filePath).exists() }

            val compositionOverlayEffect = Effects(emptyList(), listOf(OverlayEffect(listOf(overlay))))

            val sequences = buildList {
                add(videoSequence)
                if (activeAudioTracks.isNotEmpty()) {
                    val totalTimelineMs = state.totalDurationMs.coerceAtLeast(1000L)
                    val audioItems = mutableListOf<EditedMediaItem>()
                    var coveredMs = 0L
                    activeAudioTracks.sortedBy { it.startMs }.forEach { track ->
                        if (coveredMs >= totalTimelineMs) return@forEach
                        val gapMs = (track.startMs - coveredMs).coerceAtLeast(0L)
                        if (gapMs > 50L) {
                            // Pad with low-gain trimmed silence if needed, or clamp start
                        }
                        val usableDuration = minOf(track.durationMs, totalTimelineMs - coveredMs).coerceAtLeast(400L)
                        val item = MediaItem.Builder()
                            .setUri(Uri.fromFile(File(track.filePath)))
                            .setClippingConfiguration(
                                MediaItem.ClippingConfiguration.Builder()
                                    .setStartPositionMs(0L)
                                    .setEndPositionMs(usableDuration)
                                    .build()
                            )
                            .build()
                        val proc = VolumeFadeAudioProcessor(
                            gain = track.volume,
                            fadeInMs = track.fadeInMs,
                            fadeOutMs = track.fadeOutMs,
                            totalDurationMs = usableDuration
                        )
                        audioItems += EditedMediaItem.Builder(item)
                            .setRemoveVideo(true)
                            .setEffects(Effects(listOf(proc), emptyList()))
                            .build()
                        coveredMs += usableDuration
                    }
                    if (audioItems.isNotEmpty()) {
                        add(EditedMediaItemSequence(audioItems))
                    }
                }
            }

            val composition = Composition.Builder(sequences)
                .setEffects(compositionOverlayEffect)
                .build()

            try {
                runTransformerExport(
                    context = context,
                    composition = composition,
                    outputFile = outputFile,
                    baseProgress = if (totalReverseClips > 0) 22 else 5,
                    onProgress = { pct -> onStageAndProgress("Exporting ${resolution.shortLabel} MP4...", pct) },
                    onTransformerCreated = onTransformerCreated
                )
            } catch (multiAudioEx: Exception) {
                // Device fallback: if secondary audio track caused muxer/mixer error, retry primary video+overlay track cleanly
                if (sequences.size > 1) {
                    if (outputFile.exists()) outputFile.delete()
                    val fallbackComp = Composition.Builder(listOf(videoSequence))
                        .setEffects(compositionOverlayEffect)
                        .build()
                    runTransformerExport(
                        context = context,
                        composition = fallbackComp,
                        outputFile = outputFile,
                        baseProgress = 25,
                        onProgress = { pct -> onStageAndProgress("Exporting ${resolution.shortLabel} MP4...", pct) },
                        onTransformerCreated = onTransformerCreated
                    )
                } else {
                    throw multiAudioEx
                }
            }

            onStageAndProgress("Finalizing saved video...", 100)
            outputFile
        } finally {
            overlay.release()
            tempReversedFiles.forEach { runCatching { it.delete() } }
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun runTransformerExport(
        context: Context,
        composition: Composition,
        outputFile: File,
        baseProgress: Int,
        onProgress: (Int) -> Unit,
        onTransformerCreated: (Transformer) -> Unit
    ) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine<Unit> { cont ->
            var pollJob: Job? = null
            val transformer = Transformer.Builder(context)
                .setLooper(Looper.getMainLooper())
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(
                    object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                            pollJob?.cancel()
                            if (cont.isActive) cont.resume(Unit)
                        }

                        override fun onError(
                            composition: Composition,
                            exportResult: ExportResult,
                            exportException: ExportException
                        ) {
                            pollJob?.cancel()
                            if (cont.isActive) cont.resumeWithException(exportException)
                        }
                    }
                )
                .build()

            onTransformerCreated(transformer)
            transformer.start(composition, outputFile.absolutePath)

            val progressHolder = ProgressHolder()
            pollJob = kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                while (isActive && cont.isActive) {
                    val state = transformer.getProgress(progressHolder)
                    if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                        val mapped = baseProgress + ((progressHolder.progress / 100f) * (98 - baseProgress)).roundToInt()
                        onProgress(mapped.coerceIn(baseProgress, 99))
                    }
                    delay(180L)
                }
            }

            cont.invokeOnCancellation {
                pollJob.cancel()
                Handler(Looper.getMainLooper()).post {
                    runCatching { transformer.cancel() }
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
class VolumeFadeAudioProcessor(
    private val gain: Float,
    private val fadeInMs: Long,
    private val fadeOutMs: Long,
    private val totalDurationMs: Long
) : BaseAudioProcessor() {

    private var processedFrames: Long = 0L

    override fun onConfigure(inputAudioFormat: AudioFormat): AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
        }
        return inputAudioFormat
    }

    override fun onFlush() {
        processedFrames = 0L
    }

    override fun onReset() {
        processedFrames = 0L
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val out = replaceOutputBuffer(remaining)
        val channels = inputAudioFormat.channelCount.coerceAtLeast(1)
        val sr = inputAudioFormat.sampleRate.coerceAtLeast(1)
        val bytesPerFrame = channels * 2
        val frameCount = remaining / bytesPerFrame

        for (f in 0 until frameCount) {
            val currentMs = (processedFrames * 1000L) / sr
            var env = gain.coerceIn(0f, 2f)
            if (fadeInMs > 0L && currentMs < fadeInMs) {
                env *= (currentMs.toFloat() / fadeInMs.toFloat()).coerceIn(0f, 1f)
            }
            if (fadeOutMs > 0L && totalDurationMs > fadeOutMs) {
                val fadeStart = totalDurationMs - fadeOutMs
                if (currentMs > fadeStart) {
                    env *= (1f - ((currentMs - fadeStart).toFloat() / fadeOutMs.toFloat())).coerceIn(0f, 1f)
                }
            }
            for (ch in 0 until channels) {
                val sample = inputBuffer.short
                val scaled = (sample * env).roundToInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                out.putShort(scaled.toShort())
            }
            processedFrames++
        }
        out.flip()
    }
}

@OptIn(UnstableApi::class)
private class DynamicTimelineBitmapOverlay(
    private val context: Context,
    private val state: VideoEditState,
    private val canvasWidth: Int,
    private val canvasHeight: Int
) : BitmapOverlay() {

    private val bitmap: Bitmap = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
    private val canvas = Canvas(bitmap)
    private val pipImageCache = mutableMapOf<String, Bitmap>()
    private val pipVideoRetrievers = mutableMapOf<String, MediaMetadataRetriever>()
    private val pipFrameBucketCache = mutableMapOf<String, Pair<Long, Bitmap>>()

    private val clipBoundaries: List<Pair<Long, VideoTransitionType>> = buildList {
        var cursor = 0L
        state.clips.forEachIndexed { idx, c ->
            cursor += c.effectiveDurationMs
            if (idx < state.clips.lastIndex && c.transitionAfter != VideoTransitionType.NONE) {
                add(cursor to c.transitionAfter)
            }
        }
    }

    init {
        state.pipOverlays.forEach { pip ->
            val file = File(pip.sourcePath)
            if (file.exists()) {
                if (pip.isVideo) {
                    runCatching {
                        val r = MediaMetadataRetriever()
                        r.setDataSource(file.absolutePath)
                        pipVideoRetrievers[pip.id] = r
                    }
                } else {
                    PhotoEditEngine.decodeOrientedSampledBitmap(file.absolutePath, 512)?.let { bmp ->
                        pipImageCache[pip.id] = if (pip.chromaKeyEnabled) {
                            applyChromaKeyMatte(bmp, pip.chromaKeyColorArgb.toInt(), pip.chromaTolerance).also {
                                if (it !== bmp) bmp.recycle()
                            }
                        } else {
                            bmp
                        }
                    }
                }
            }
        }
    }

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val timeMs = presentationTimeUs / 1000L
        bitmap.eraseColor(Color.TRANSPARENT)

        val w = canvasWidth.toFloat()
        val h = canvasHeight.toFloat()

        // 1. Transitions around clip cut boundaries
        clipBoundaries.forEach { (boundaryMs, type) ->
            val halfWindow = 280L
            val delta = timeMs - boundaryMs
            if (abs(delta) <= halfWindow) {
                val t = 1f - (abs(delta).toFloat() / halfWindow.toFloat())
                when (type) {
                    VideoTransitionType.CROSSFADE -> {
                        canvas.drawColor(Color.argb((t * 170).roundToInt(), 255, 244, 248))
                    }
                    VideoTransitionType.PASTEL_FLASH -> {
                        canvas.drawColor(Color.argb((t * 225).roundToInt(), 255, 224, 236))
                    }
                    VideoTransitionType.SLIDE_LEFT -> {
                        val p = Paint().apply { color = Color.argb(230, 255, 241, 246) }
                        val barW = w * 0.25f * t
                        val cx = ((delta + halfWindow).toFloat() / (2f * halfWindow)) * w
                        canvas.drawRect(cx - barW, 0f, cx + barW, h, p)
                    }
                    VideoTransitionType.ZOOM_DISSOLVE -> {
                        val p = Paint().apply {
                            color = Color.argb((t * 185).roundToInt(), 237, 231, 246)
                        }
                        canvas.drawCircle(w / 2f, h / 2f, maxOf(w, h) * 0.65f * t, p)
                    }
                    VideoTransitionType.HEART_WIPE -> {
                        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.argb((t * 210).roundToInt(), 248, 187, 208)
                        }
                        canvas.drawCircle(w * 0.42f, h * 0.45f, w * 0.35f * t, p)
                        canvas.drawCircle(w * 0.58f, h * 0.45f, w * 0.35f * t, p)
                    }
                    VideoTransitionType.NONE -> Unit
                }
            }
        }

        // 2. Picture-in-Picture overlays with Keyframe Interpolation & Chroma Key
        state.pipOverlays.forEach { pip ->
            if (timeMs in pip.startMs..pip.endMs) {
                val span = (pip.endMs - pip.startMs).coerceAtLeast(1L).toFloat()
                val progress = ((timeMs - pip.startMs) / span).coerceIn(0f, 1f)
                val cx = (pip.startX + (pip.endX - pip.startX) * progress) * w
                val cy = (pip.startY + (pip.endY - pip.startY) * progress) * h
                val scale = pip.startScale + (pip.endScale - pip.startScale) * progress
                val rot = pip.startRotationDeg + (pip.endRotationDeg - pip.startRotationDeg) * progress
                val alpha = ((pip.startOpacity + (pip.endOpacity - pip.startOpacity) * progress).coerceIn(0f, 1f) * 255).roundToInt()

                val bmp = resolvePipBitmap(pip, timeMs - pip.startMs) ?: return@forEach
                val targetW = w * scale.coerceIn(0.12f, 0.9f)
                val targetH = targetW * (bmp.height.toFloat() / bmp.width.toFloat().coerceAtLeast(1f))

                canvas.save()
                canvas.translate(cx, cy)
                canvas.rotate(rot)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                    this.alpha = alpha
                }
                val dst = RectF(-targetW / 2f, -targetH / 2f, targetW / 2f, targetH / 2f)
                if (!pip.chromaKeyEnabled) {
                    val clipPath = Path().apply {
                        addRoundRect(dst, targetW * 0.08f, targetW * 0.08f, Path.Direction.CW)
                    }
                    canvas.clipPath(clipPath)
                }
                canvas.drawBitmap(bmp, null, dst, paint)
                if (!pip.chromaKeyEnabled) {
                    val borderP = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        style = Paint.Style.STROKE
                        strokeWidth = 4f
                        color = Color.argb(alpha, 255, 255, 255)
                    }
                    canvas.drawRoundRect(dst, targetW * 0.08f, targetW * 0.08f, borderP)
                }
                canvas.restore()
            }
        }

        // 3. Stickers
        state.stickerOverlays.forEach { stk ->
            if (timeMs in stk.startMs..stk.endMs) {
                val model = CreativeLibraryCatalog.stickerById(stk.stickerId) ?: return@forEach
                drawStickerOnCanvas(
                    canvas = canvas,
                    sticker = model,
                    centerX = stk.xFraction * w,
                    centerY = stk.yFraction * h,
                    baseSizePx = minOf(w, h) * 0.14f,
                    scale = stk.scale,
                    rotationDeg = stk.rotationDeg,
                    opacity = stk.opacity
                )
            }
        }

        // 4. Text, Animated Captions & Subtitles
        state.textOverlays.forEach { txt ->
            if (timeMs in txt.startMs..txt.endMs && txt.text.isNotBlank()) {
                val localMs = (timeMs - txt.startMs).coerceAtLeast(0L)
                val animProgress = (localMs / 420f).coerceIn(0f, 1f)
                var textAlpha = 255
                var scaleFactor = 1f
                var yOffsetPx = 0f
                var displayText = txt.text

                when (txt.animationStyle) {
                    CaptionAnimationStyle.STATIC -> Unit
                    CaptionAnimationStyle.FADE_IN -> {
                        textAlpha = (animProgress * 255).roundToInt()
                    }
                    CaptionAnimationStyle.POP_SCALE -> {
                        scaleFactor = 0.65f + 0.35f * animProgress
                        textAlpha = (animProgress * 255).roundToInt()
                    }
                    CaptionAnimationStyle.SLIDE_UP -> {
                        yOffsetPx = (1f - animProgress) * minOf(w, h) * 0.06f
                        textAlpha = (animProgress * 255).roundToInt()
                    }
                    CaptionAnimationStyle.TYPEWRITER -> {
                        val totalSpan = (txt.endMs - txt.startMs).coerceAtLeast(600L)
                        val typeFrac = (localMs.toFloat() / (totalSpan * 0.55f)).coerceIn(0.15f, 1f)
                        val chars = (txt.text.length * typeFrac).roundToInt().coerceIn(1, txt.text.length)
                        displayText = txt.text.take(chars)
                    }
                    CaptionAnimationStyle.PULSE -> {
                        scaleFactor = 1f + 0.06f * sin((localMs / 180.0)).toFloat()
                    }
                }

                canvas.save()
                canvas.translate(txt.xFraction * w, txt.yFraction * h + yOffsetPx)
                canvas.scale(scaleFactor, scaleFactor)

                val font = EditorFontOption.fromId(txt.fontId)
                val pxSize = (minOf(w, h) * (txt.fontSizeSp / 380f)).coerceAtLeast(18f)
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = txt.colorArgb.toInt()
                    alpha = textAlpha
                    textSize = pxSize
                    typeface = font.toAndroidTypeface(context)
                    textAlign = Paint.Align.CENTER
                }

                val lines = displayText.split("\n")
                val lineH = pxSize * 1.25f
                val maxW = lines.maxOfOrNull { textPaint.measureText(it) } ?: 60f
                val totalH = lines.size * lineH

                val bgAlpha = Color.alpha(txt.bgColorArgb.toInt())
                if (bgAlpha > 0) {
                    val padX = pxSize * 0.7f
                    val padY = pxSize * 0.36f
                    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = txt.bgColorArgb.toInt()
                        alpha = ((bgAlpha / 255f) * textAlpha).roundToInt()
                    }
                    canvas.drawRoundRect(
                        RectF(-maxW / 2f - padX, -totalH / 2f - padY, maxW / 2f + padX, totalH / 2f + padY),
                        pxSize * 0.48f,
                        pxSize * 0.48f,
                        bgPaint
                    )
                }

                var yc = -totalH / 2f + pxSize * 0.9f
                lines.forEach { line ->
                    canvas.drawText(line, 0f, yc, textPaint)
                    yc += lineH
                }
                canvas.restore()
            }
        }

        return bitmap
    }

    private fun resolvePipBitmap(pip: PipOverlayItem, localTimeMs: Long): Bitmap? {
        if (!pip.isVideo) return pipImageCache[pip.id]
        val retriever = pipVideoRetrievers[pip.id] ?: return null
        val bucket = (localTimeMs / 66L)
        val cached = pipFrameBucketCache[pip.id]
        if (cached != null && cached.first == bucket) return cached.second

        val frame = retriever.getFrameAtTime(localTimeMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            ?: return cached?.second
        val scaled = Bitmap.createScaledBitmap(frame, 360, ((360f * frame.height) / frame.width.coerceAtLeast(1)).roundToInt().coerceAtLeast(1), true)
        if (scaled !== frame) frame.recycle()
        val finalPip = if (pip.chromaKeyEnabled) {
            applyChromaKeyMatte(scaled, pip.chromaKeyColorArgb.toInt(), pip.chromaTolerance).also {
                if (it !== scaled) scaled.recycle()
            }
        } else {
            scaled
        }
        cached?.second?.recycle()
        pipFrameBucketCache[pip.id] = bucket to finalPip
        return finalPip
    }

    private fun applyChromaKeyMatte(src: Bitmap, keyColor: Int, tolerance: Float): Bitmap {
        val w = src.width
        val h = src.height
        val pix = IntArray(w * h)
        src.getPixels(pix, 0, w, 0, 0, w, h)
        val kr = Color.red(keyColor)
        val kg = Color.green(keyColor)
        val kb = Color.blue(keyColor)
        val threshold = (tolerance.coerceIn(0.1f, 0.7f) * 442f)

        for (i in pix.indices) {
            val c = pix[i]
            val dr = Color.red(c) - kr
            val dg = Color.green(c) - kg
            val db = Color.blue(c) - kb
            val dist = kotlin.math.sqrt((dr * dr + dg * dg + db * db).toDouble()).toFloat()
            if (dist < threshold * 0.75f) {
                pix[i] = Color.TRANSPARENT
            } else if (dist < threshold) {
                val a = (((dist - threshold * 0.75f) / (threshold * 0.25f)).coerceIn(0f, 1f) * 255f).roundToInt()
                pix[i] = Color.argb(a, Color.red(c), Color.green(c), Color.blue(c))
            }
        }
        return Bitmap.createBitmap(pix, w, h, Bitmap.Config.ARGB_8888)
    }

    override fun release() {
        super.release()
        runCatching { bitmap.recycle() }
        pipImageCache.values.forEach { runCatching { it.recycle() } }
        pipImageCache.clear()
        pipFrameBucketCache.values.forEach { runCatching { it.second.recycle() } }
        pipFrameBucketCache.clear()
        pipVideoRetrievers.values.forEach { runCatching { it.release() } }
        pipVideoRetrievers.clear()
    }
}
