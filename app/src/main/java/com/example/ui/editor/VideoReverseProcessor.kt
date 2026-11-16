package com.example.ui.editor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import kotlin.math.roundToInt

/**
 * Creates a reversed MP4 clip cleanly using MediaMetadataRetriever + hardware/software H.264 encoder
 * so reverse playback works reliably across Android devices.
 */
object VideoReverseProcessor {

    suspend fun createReversedClip(
        sourcePath: String,
        trimStartMs: Long,
        trimEndMs: Long,
        outputFile: File,
        onProgress: (Float) -> Unit = {}
    ): File = withContext(Dispatchers.Default) {
        val retriever = MediaMetadataRetriever()
        var encoder: MediaCodec? = null
        var muxer: MediaMuxer? = null

        try {
            retriever.setDataSource(sourcePath)
            val srcW = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 720
            val srcH = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 1280
            val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)?.toIntOrNull() ?: 0

            val orientedW = if (rotation == 90 || rotation == 270) srcH else srcW
            val orientedH = if (rotation == 90 || rotation == 270) srcW else srcH

            val scale = if (maxOf(orientedW, orientedH) > 720) 720f / maxOf(orientedW, orientedH).toFloat() else 1f
            val encW = (((orientedW * scale).roundToInt() / 16).coerceAtLeast(2)) * 16
            val encH = (((orientedH * scale).roundToInt() / 16).coerceAtLeast(2)) * 16

            val fps = 24
            val durationMs = (trimEndMs - trimStartMs).coerceIn(400L, 20_000L)
            val totalFrames = ((durationMs / 1000.0) * fps).roundToInt().coerceIn(8, 480)
            val frameStepUs = (durationMs * 1000L) / totalFrames

            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, encW, encH).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, 3_000_000)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            val inputSurface = encoder.createInputSurface()
            encoder.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var trackIndex = -1
            var muxerStarted = false
            val bufferInfo = MediaCodec.BufferInfo()

            fun drainEncoder(endOfStream: Boolean) {
                if (endOfStream) {
                    encoder.signalEndOfInputStream()
                }
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000L)
                    if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        if (!endOfStream) break
                    } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        if (!muxerStarted) {
                            trackIndex = muxer.addTrack(encoder.outputFormat)
                            muxer.start()
                            muxerStarted = true
                        }
                    } else if (status >= 0) {
                        val encodedData: ByteBuffer = encoder.getOutputBuffer(status) ?: continue
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                            bufferInfo.size = 0
                        }
                        if (bufferInfo.size != 0 && muxerStarted) {
                            encodedData.position(bufferInfo.offset)
                            encodedData.limit(bufferInfo.offset + bufferInfo.size)
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(status, false)
                        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            break
                        }
                    }
                }
            }

            for (i in 0 until totalFrames) {
                val srcTimeUs = (trimEndMs * 1000L - i * frameStepUs).coerceAtLeast(trimStartMs * 1000L)
                val frame = retriever.getFrameAtTime(srcTimeUs, MediaMetadataRetriever.OPTION_CLOSEST)
                    ?: retriever.getFrameAtTime(srcTimeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    val canvas: Canvas = inputSurface.lockCanvas(null)
                    canvas.drawColor(Color.BLACK)
                    canvas.drawBitmap(frame, null, Rect(0, 0, encW, encH), null)
                    inputSurface.unlockCanvasAndPost(canvas)
                    frame.recycle()
                }
                drainEncoder(endOfStream = false)
                onProgress((i + 1).toFloat() / totalFrames.toFloat())
            }
            drainEncoder(endOfStream = true)
            outputFile
        } finally {
            runCatching { encoder?.stop() }
            runCatching { encoder?.release() }
            runCatching { muxer?.stop() }
            runCatching { muxer?.release() }
            runCatching { retriever.release() }
        }
    }
}
