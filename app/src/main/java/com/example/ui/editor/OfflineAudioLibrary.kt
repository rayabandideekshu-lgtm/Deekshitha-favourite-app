package com.example.ui.editor

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.data.model.AudioTrackKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

data class BuiltInAudioAsset(
    val id: String,
    val title: String,
    val kind: AudioTrackKind,
    val durationMs: Long,
    val description: String
)

object OfflineAudioLibrary {

    val catalog: List<BuiltInAudioAsset> = listOf(
        BuiltInAudioAsset("bgm_lofi", "Pastel Morning Lofi", AudioTrackKind.MUSIC, 12_000L, "Warm Rhodes chords"),
        BuiltInAudioAsset("bgm_musicbox", "Dreamy Music Box", AudioTrackKind.MUSIC, 12_000L, "Delicate bell melody"),
        BuiltInAudioAsset("bgm_piano", "Café Waltz Pad", AudioTrackKind.MUSIC, 12_000L, "Soft acoustic harmony"),
        BuiltInAudioAsset("bgm_garden", "Sunlit Meadow", AudioTrackKind.MUSIC, 12_000L, "Airy botanical shimmer"),
        BuiltInAudioAsset("sfx_sparkle", "Sparkle Chime", AudioTrackKind.SFX, 1_400L, "Magical transition twinkle"),
        BuiltInAudioAsset("sfx_shutter", "Camera Shutter", AudioTrackKind.SFX, 600L, "Crisp retro click"),
        BuiltInAudioAsset("sfx_whoosh", "Soft Whoosh", AudioTrackKind.SFX, 900L, "Gentle air sweep"),
        BuiltInAudioAsset("sfx_pop", "Heart Bubble Pop", AudioTrackKind.SFX, 550L, "Cute sticker pop")
    )

    suspend fun ensureAudioAssetFile(audioDir: File, assetId: String): Pair<File, Long> =
        withContext(Dispatchers.IO) {
            if (!audioDir.exists()) audioDir.mkdirs()
            val asset = catalog.firstOrNull { it.id == assetId } ?: catalog.first()
            val outFile = File(audioDir, "${asset.id}.wav")
            if (outFile.exists() && outFile.length() > 100L) {
                return@withContext outFile to asset.durationMs
            }

            val sampleRate = 22050
            val numSamples = ((asset.durationMs / 1000.0) * sampleRate).toInt().coerceAtLeast(1000)
            val pcm = ShortArray(numSamples)

            when (asset.id) {
                "bgm_lofi" -> synthesizeChordProgression(pcm, sampleRate, listOf(261.63, 329.63, 392.00, 493.88), listOf(220.0, 261.63, 329.63, 392.0), 0.24)
                "bgm_musicbox" -> synthesizeArpeggio(pcm, sampleRate, doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 783.99, 659.25, 587.33, 698.46), 0.26)
                "bgm_piano" -> synthesizeChordProgression(pcm, sampleRate, listOf(293.66, 349.23, 440.0, 523.25), listOf(246.94, 293.66, 392.0, 440.0), 0.25)
                "bgm_garden" -> synthesizeArpeggio(pcm, sampleRate, doubleArrayOf(440.0, 554.37, 659.25, 880.0, 659.25, 587.33, 493.88, 659.25), 0.22)
                "sfx_sparkle" -> synthesizeSparkle(pcm, sampleRate)
                "sfx_shutter" -> synthesizeShutter(pcm, sampleRate)
                "sfx_whoosh" -> synthesizeWhoosh(pcm, sampleRate)
                "sfx_pop" -> synthesizePop(pcm, sampleRate)
            }

            writeWavFile(outFile, pcm, sampleRate)
            outFile to asset.durationMs
        }

    private fun synthesizeChordProgression(
        out: ShortArray,
        sr: Int,
        chordA: List<Double>,
        chordB: List<Double>,
        amp: Double
    ) {
        val half = out.size / 2
        for (i in out.indices) {
            val t = i.toDouble() / sr
            val chord = if (i < half) chordA else chordB
            val localT = (i % half).toDouble() / sr
            val env = (1.0 - exp(-localT * 6.0)) * exp(-localT * 0.25)
            var sum = 0.0
            for (f in chord) {
                sum += sin(2.0 * PI * f * t) * 0.25 + sin(2.0 * PI * f * 0.5 * t) * 0.1
            }
            out[i] = (sum * env * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizeArpeggio(out: ShortArray, sr: Int, notes: DoubleArray, amp: Double) {
        val stepSamples = (sr * 0.45).toInt().coerceAtLeast(1)
        for (i in out.indices) {
            val stepIdx = (i / stepSamples) % notes.size
            val localSamples = i % stepSamples
            val localT = localSamples.toDouble() / sr
            val f = notes[stepIdx]
            val env = (1.0 - exp(-localT * 45.0)) * exp(-localT * 4.5)
            val s = sin(2.0 * PI * f * localT) + 0.3 * sin(2.0 * PI * f * 2.0 * localT)
            out[i] = (s * env * amp * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizeSparkle(out: ShortArray, sr: Int) {
        val freqs = doubleArrayOf(880.0, 1174.66, 1396.91, 1760.0)
        for (i in out.indices) {
            val t = i.toDouble() / sr
            var s = 0.0
            freqs.forEachIndexed { idx, f ->
                val delay = idx * 0.14
                if (t >= delay) {
                    val lt = t - delay
                    s += sin(2.0 * PI * f * lt) * exp(-lt * 6.5) * 0.28
                }
            }
            out[i] = (s * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizeShutter(out: ShortArray, sr: Int) {
        var seed = 1337L
        for (i in out.indices) {
            val t = i.toDouble() / sr
            seed = (seed * 1664525L + 1013904223L) and 0xffffffffL
            val noise = ((seed ushr 16).toDouble() / 32768.0) - 1.0
            val click1 = exp(-t * 95.0)
            val click2 = if (t > 0.09) exp(-(t - 0.09) * 85.0) else 0.0
            val sample = (noise * (click1 + click2) * 0.55)
            out[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizeWhoosh(out: ShortArray, sr: Int) {
        var seed = 9001L
        val dur = out.size.toDouble() / sr
        for (i in out.indices) {
            val t = i.toDouble() / sr
            seed = (seed * 1664525L + 1013904223L) and 0xffffffffL
            val noise = ((seed ushr 16).toDouble() / 32768.0) - 1.0
            val bell = sin(PI * (t / dur))
            val sweep = sin(2.0 * PI * (180.0 + 420.0 * (t / dur)) * t) * 0.2
            val sample = (noise * 0.35 + sweep) * bell
            out[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun synthesizePop(out: ShortArray, sr: Int) {
        for (i in out.indices) {
            val t = i.toDouble() / sr
            val freq = 320.0 + 680.0 * (t * 4.0).coerceAtMost(1.0)
            val env = exp(-t * 14.0)
            val sample = sin(2.0 * PI * freq * t) * env * 0.5
            out[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }

    private fun writeWavFile(file: File, pcm: ShortArray, sampleRate: Int) {
        val dataSize = pcm.size * 2
        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
            put("RIFF".toByteArray())
            putInt(36 + dataSize)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16)
            putShort(1) // PCM
            putShort(1) // Mono
            putInt(sampleRate)
            putInt(sampleRate * 2)
            putShort(2)
            putShort(16)
            put("data".toByteArray())
            putInt(dataSize)
        }.array()

        FileOutputStream(file).use { out ->
            out.write(header)
            val buf = ByteBuffer.allocate(dataSize).order(ByteOrder.LITTLE_ENDIAN)
            pcm.forEach { buf.putShort(it) }
            out.write(buf.array())
        }
    }
}

class VoiceoverRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAtMillis: Long = 0L

    fun startRecording(audioDir: File): File {
        if (!audioDir.exists()) audioDir.mkdirs()
        val file = File(audioDir, "VO_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}.m4a")
        val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        rec.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(128000)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        recorder = rec
        outputFile = file
        startedAtMillis = System.currentTimeMillis()
        return file
    }

    fun stopRecording(): Pair<File, Long>? {
        val file = outputFile
        val elapsed = (System.currentTimeMillis() - startedAtMillis).coerceAtLeast(500L)
        runCatching {
            recorder?.stop()
        }
        runCatching {
            recorder?.release()
        }
        recorder = null
        outputFile = null
        return if (file != null && file.exists() && file.length() > 0L) {
            file to elapsed
        } else {
            null
        }
    }

    fun cancel() {
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}
