package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class VideoAspectRatio(val label: String, val ratio: Float?) {
    ORIGINAL("Original", null),
    PORTRAIT_9_16("9:16", 9f / 16f),
    LANDSCAPE_16_9("16:9", 16f / 9f),
    SQUARE_1_1("1:1", 1f),
    PORTRAIT_4_5("4:5", 4f / 5f),
    CLASSIC_4_3("4:3", 4f / 3f)
}

enum class VideoExportResolution(
    val label: String,
    val shortLabel: String,
    val width: Int,
    val height: Int
) {
    HD_720P("720p HD", "720p", 1280, 720),
    FHD_1080P("1080p Full HD", "1080p", 1920, 1080),
    UHD_4K("4K Ultra HD", "4K", 3840, 2160)
}

enum class VideoTransitionType(val label: String) {
    NONE("None"),
    CROSSFADE("Crossfade"),
    SLIDE_LEFT("Slide"),
    ZOOM_DISSOLVE("Zoom"),
    PASTEL_FLASH("Pastel Flash"),
    HEART_WIPE("Heart Wipe")
}

enum class VideoColorFilter(
    val label: String,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val brightness: Float = 0f,
    val tintArgb: Long = 0x00000000L,
    val tintAlpha: Float = 0f
) {
    ORIGINAL("Original"),
    PASTEL_FILM("Pastel Film", contrast = 0.96f, saturation = 0.95f, brightness = 0.05f, tintArgb = 0xFFFFD6E6L, tintAlpha = 0.14f),
    WARM_SUNSET("Warm Sunset", contrast = 1.08f, saturation = 1.15f, brightness = 0.04f, tintArgb = 0xFFFFC178L, tintAlpha = 0.16f),
    ROSE_TINT("Rose Tint", contrast = 1.04f, saturation = 1.08f, brightness = 0.03f, tintArgb = 0xFFF49AB8L, tintAlpha = 0.14f),
    LAVENDER_MIST("Lavender Mist", contrast = 0.98f, saturation = 0.94f, brightness = 0.04f, tintArgb = 0xFFCDB9EBL, tintAlpha = 0.16f),
    VINTAGE_SEPIA("Vintage 98", contrast = 1.08f, saturation = 0.72f, brightness = 0.02f, tintArgb = 0xFFD9AE74L, tintAlpha = 0.22f),
    CRISP_MONO("Crisp B&W", contrast = 1.22f, saturation = 0f, brightness = 0.02f),
    TEAL_PEACH("Teal & Peach", contrast = 1.12f, saturation = 1.12f, brightness = 0.01f, tintArgb = 0xFFB8DFD8L, tintAlpha = 0.12f)
}

enum class CaptionAnimationStyle(val label: String) {
    STATIC("Static"),
    FADE_IN("Fade In"),
    POP_SCALE("Pop Scale"),
    SLIDE_UP("Slide Up"),
    TYPEWRITER("Typewriter"),
    PULSE("Soft Pulse")
}

enum class AudioTrackKind(val label: String) {
    MUSIC("Music"),
    SFX("Sound FX"),
    VOICEOVER("Voiceover")
}

data class VideoClipItem(
    val id: String = UUID.randomUUID().toString(),
    val sourcePath: String,
    val thumbnailPath: String = "",
    val isImageClip: Boolean = false,
    val sourceDurationMs: Long = 5000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = sourceDurationMs,
    val speed: Float = 1f,
    val isReversed: Boolean = false,
    val rotationDeg: Int = 0,
    val flipHorizontal: Boolean = false,
    val cropZoom: Float = 1f,
    val cropPanX: Float = 0f,
    val cropPanY: Float = 0f,
    val stabilize: Boolean = false,
    val volume: Float = 1f,
    val fadeInMs: Long = 0L,
    val fadeOutMs: Long = 0L,
    val transitionAfter: VideoTransitionType = VideoTransitionType.NONE,
    val filter: VideoColorFilter = VideoColorFilter.ORIGINAL,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f
) {
    val effectiveDurationMs: Long
        get() {
            val span = (trimEndMs - trimStartMs).coerceAtLeast(300L)
            return (span / speed.coerceIn(0.25f, 4f)).toLong().coerceAtLeast(300L)
        }
}

data class PipOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "PiP Layer",
    val sourcePath: String,
    val isVideo: Boolean = false,
    val startMs: Long = 0L,
    val endMs: Long = 4000L,
    val startX: Float = 0.72f,
    val startY: Float = 0.28f,
    val endX: Float = 0.72f,
    val endY: Float = 0.28f,
    val startScale: Float = 0.34f,
    val endScale: Float = 0.34f,
    val startRotationDeg: Float = 0f,
    val endRotationDeg: Float = 0f,
    val startOpacity: Float = 1f,
    val endOpacity: Float = 1f,
    val chromaKeyEnabled: Boolean = false,
    val chromaKeyColorArgb: Long = 0xFF28C840L,
    val chromaTolerance: Float = 0.32f
)

data class VideoTextOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isSubtitle: Boolean = false,
    val startMs: Long = 0L,
    val endMs: Long = 3500L,
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.80f,
    val fontId: String = EditorFontOption.PLAYFAIR.id,
    val colorArgb: Long = 0xFFFFFFFFL,
    val bgColorArgb: Long = 0xAA3A2232L,
    val fontSizeSp: Float = 22f,
    val animationStyle: CaptionAnimationStyle = CaptionAnimationStyle.POP_SCALE
)

data class VideoStickerOverlayItem(
    val id: String = UUID.randomUUID().toString(),
    val stickerId: String,
    val startMs: Long = 0L,
    val endMs: Long = 4000L,
    val xFraction: Float = 0.5f,
    val yFraction: Float = 0.25f,
    val scale: Float = 1f,
    val rotationDeg: Float = 0f,
    val opacity: Float = 1f
)

data class VideoAudioLayerItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val kind: AudioTrackKind,
    val filePath: String,
    val startMs: Long = 0L,
    val durationMs: Long = 5000L,
    val volume: Float = 0.85f,
    val fadeInMs: Long = 300L,
    val fadeOutMs: Long = 400L,
    val isMuted: Boolean = false
)

data class VideoEditState(
    val projectId: Long = 0L,
    val title: String = "My Pastel Film",
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.PORTRAIT_9_16,
    val clips: List<VideoClipItem> = emptyList(),
    val pipOverlays: List<PipOverlayItem> = emptyList(),
    val textOverlays: List<VideoTextOverlayItem> = emptyList(),
    val stickerOverlays: List<VideoStickerOverlayItem> = emptyList(),
    val audioTracks: List<VideoAudioLayerItem> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(0L)

    fun clipTimeRange(index: Int): LongRange {
        var cursor = 0L
        clips.forEachIndexed { idx, clip ->
            val end = cursor + clip.effectiveDurationMs
            if (idx == index) return cursor until end
            cursor = end
        }
        return 0L until 0L
    }

    fun toJsonString(): String {
        val root = JSONObject()
        root.put("title", title)
        root.put("aspectRatio", aspectRatio.name)

        val clipArr = JSONArray()
        clips.forEach { c ->
            clipArr.put(
                JSONObject().apply {
                    put("id", c.id)
                    put("sourcePath", c.sourcePath)
                    put("thumbnailPath", c.thumbnailPath)
                    put("isImageClip", c.isImageClip)
                    put("sourceDurationMs", c.sourceDurationMs)
                    put("trimStartMs", c.trimStartMs)
                    put("trimEndMs", c.trimEndMs)
                    put("speed", c.speed)
                    put("isReversed", c.isReversed)
                    put("rotationDeg", c.rotationDeg)
                    put("flipHorizontal", c.flipHorizontal)
                    put("cropZoom", c.cropZoom)
                    put("cropPanX", c.cropPanX)
                    put("cropPanY", c.cropPanY)
                    put("stabilize", c.stabilize)
                    put("volume", c.volume)
                    put("fadeInMs", c.fadeInMs)
                    put("fadeOutMs", c.fadeOutMs)
                    put("transitionAfter", c.transitionAfter.name)
                    put("filter", c.filter.name)
                    put("brightness", c.brightness)
                    put("contrast", c.contrast)
                    put("saturation", c.saturation)
                }
            )
        }
        root.put("clips", clipArr)

        val pipArr = JSONArray()
        pipOverlays.forEach { p ->
            pipArr.put(
                JSONObject().apply {
                    put("id", p.id)
                    put("title", p.title)
                    put("sourcePath", p.sourcePath)
                    put("isVideo", p.isVideo)
                    put("startMs", p.startMs)
                    put("endMs", p.endMs)
                    put("startX", p.startX)
                    put("startY", p.startY)
                    put("endX", p.endX)
                    put("endY", p.endY)
                    put("startScale", p.startScale)
                    put("endScale", p.endScale)
                    put("startRotationDeg", p.startRotationDeg)
                    put("endRotationDeg", p.endRotationDeg)
                    put("startOpacity", p.startOpacity)
                    put("endOpacity", p.endOpacity)
                    put("chromaKeyEnabled", p.chromaKeyEnabled)
                    put("chromaKeyColorArgb", p.chromaKeyColorArgb)
                    put("chromaTolerance", p.chromaTolerance)
                }
            )
        }
        root.put("pipOverlays", pipArr)

        val textArr = JSONArray()
        textOverlays.forEach { t ->
            textArr.put(
                JSONObject().apply {
                    put("id", t.id)
                    put("text", t.text)
                    put("isSubtitle", t.isSubtitle)
                    put("startMs", t.startMs)
                    put("endMs", t.endMs)
                    put("xFraction", t.xFraction)
                    put("yFraction", t.yFraction)
                    put("fontId", t.fontId)
                    put("colorArgb", t.colorArgb)
                    put("bgColorArgb", t.bgColorArgb)
                    put("fontSizeSp", t.fontSizeSp)
                    put("animationStyle", t.animationStyle.name)
                }
            )
        }
        root.put("textOverlays", textArr)

        val stkArr = JSONArray()
        stickerOverlays.forEach { s ->
            stkArr.put(
                JSONObject().apply {
                    put("id", s.id)
                    put("stickerId", s.stickerId)
                    put("startMs", s.startMs)
                    put("endMs", s.endMs)
                    put("xFraction", s.xFraction)
                    put("yFraction", s.yFraction)
                    put("scale", s.scale)
                    put("rotationDeg", s.rotationDeg)
                    put("opacity", s.opacity)
                }
            )
        }
        root.put("stickerOverlays", stkArr)

        val audArr = JSONArray()
        audioTracks.forEach { a ->
            audArr.put(
                JSONObject().apply {
                    put("id", a.id)
                    put("title", a.title)
                    put("kind", a.kind.name)
                    put("filePath", a.filePath)
                    put("startMs", a.startMs)
                    put("durationMs", a.durationMs)
                    put("volume", a.volume)
                    put("fadeInMs", a.fadeInMs)
                    put("fadeOutMs", a.fadeOutMs)
                    put("isMuted", a.isMuted)
                }
            )
        }
        root.put("audioTracks", audArr)

        return root.toString()
    }

    companion object {
        fun fromJsonString(json: String, projectId: Long = 0L): VideoEditState? = runCatching {
            val root = JSONObject(json)
            val clipArr = root.optJSONArray("clips") ?: JSONArray()
            val clips = buildList {
                for (i in 0 until clipArr.length()) {
                    val o = clipArr.getJSONObject(i)
                    val srcDur = o.optLong("sourceDurationMs", 5000L)
                    add(
                        VideoClipItem(
                            id = o.optString("id"),
                            sourcePath = o.optString("sourcePath"),
                            thumbnailPath = o.optString("thumbnailPath"),
                            isImageClip = o.optBoolean("isImageClip", false),
                            sourceDurationMs = srcDur,
                            trimStartMs = o.optLong("trimStartMs", 0L),
                            trimEndMs = o.optLong("trimEndMs", srcDur),
                            speed = o.optDouble("speed", 1.0).toFloat(),
                            isReversed = o.optBoolean("isReversed", false),
                            rotationDeg = o.optInt("rotationDeg", 0),
                            flipHorizontal = o.optBoolean("flipHorizontal", false),
                            cropZoom = o.optDouble("cropZoom", 1.0).toFloat(),
                            cropPanX = o.optDouble("cropPanX", 0.0).toFloat(),
                            cropPanY = o.optDouble("cropPanY", 0.0).toFloat(),
                            stabilize = o.optBoolean("stabilize", false),
                            volume = o.optDouble("volume", 1.0).toFloat(),
                            fadeInMs = o.optLong("fadeInMs", 0L),
                            fadeOutMs = o.optLong("fadeOutMs", 0L),
                            transitionAfter = VideoTransitionType.entries.firstOrNull { it.name == o.optString("transitionAfter") }
                                ?: VideoTransitionType.NONE,
                            filter = VideoColorFilter.entries.firstOrNull { it.name == o.optString("filter") }
                                ?: VideoColorFilter.ORIGINAL,
                            brightness = o.optDouble("brightness", 0.0).toFloat(),
                            contrast = o.optDouble("contrast", 1.0).toFloat(),
                            saturation = o.optDouble("saturation", 1.0).toFloat()
                        )
                    )
                }
            }
            val pipArr = root.optJSONArray("pipOverlays") ?: JSONArray()
            val pips = buildList {
                for (i in 0 until pipArr.length()) {
                    val o = pipArr.getJSONObject(i)
                    add(
                        PipOverlayItem(
                            id = o.optString("id"),
                            title = o.optString("title", "PiP"),
                            sourcePath = o.optString("sourcePath"),
                            isVideo = o.optBoolean("isVideo", false),
                            startMs = o.optLong("startMs", 0L),
                            endMs = o.optLong("endMs", 4000L),
                            startX = o.optDouble("startX", 0.72).toFloat(),
                            startY = o.optDouble("startY", 0.28).toFloat(),
                            endX = o.optDouble("endX", 0.72).toFloat(),
                            endY = o.optDouble("endY", 0.28).toFloat(),
                            startScale = o.optDouble("startScale", 0.34).toFloat(),
                            endScale = o.optDouble("endScale", 0.34).toFloat(),
                            startRotationDeg = o.optDouble("startRotationDeg", 0.0).toFloat(),
                            endRotationDeg = o.optDouble("endRotationDeg", 0.0).toFloat(),
                            startOpacity = o.optDouble("startOpacity", 1.0).toFloat(),
                            endOpacity = o.optDouble("endOpacity", 1.0).toFloat(),
                            chromaKeyEnabled = o.optBoolean("chromaKeyEnabled", false),
                            chromaKeyColorArgb = o.optLong("chromaKeyColorArgb", 0xFF28C840L),
                            chromaTolerance = o.optDouble("chromaTolerance", 0.32).toFloat()
                        )
                    )
                }
            }
            val textArr = root.optJSONArray("textOverlays") ?: JSONArray()
            val texts = buildList {
                for (i in 0 until textArr.length()) {
                    val o = textArr.getJSONObject(i)
                    add(
                        VideoTextOverlayItem(
                            id = o.optString("id"),
                            text = o.optString("text"),
                            isSubtitle = o.optBoolean("isSubtitle", false),
                            startMs = o.optLong("startMs", 0L),
                            endMs = o.optLong("endMs", 3500L),
                            xFraction = o.optDouble("xFraction", 0.5).toFloat(),
                            yFraction = o.optDouble("yFraction", 0.8).toFloat(),
                            fontId = o.optString("fontId", EditorFontOption.PLAYFAIR.id),
                            colorArgb = o.optLong("colorArgb", 0xFFFFFFFFL),
                            bgColorArgb = o.optLong("bgColorArgb", 0xAA3A2232L),
                            fontSizeSp = o.optDouble("fontSizeSp", 22.0).toFloat(),
                            animationStyle = CaptionAnimationStyle.entries.firstOrNull { it.name == o.optString("animationStyle") }
                                ?: CaptionAnimationStyle.POP_SCALE
                        )
                    )
                }
            }
            val stkArr = root.optJSONArray("stickerOverlays") ?: JSONArray()
            val stks = buildList {
                for (i in 0 until stkArr.length()) {
                    val o = stkArr.getJSONObject(i)
                    add(
                        VideoStickerOverlayItem(
                            id = o.optString("id"),
                            stickerId = o.optString("stickerId"),
                            startMs = o.optLong("startMs", 0L),
                            endMs = o.optLong("endMs", 4000L),
                            xFraction = o.optDouble("xFraction", 0.5).toFloat(),
                            yFraction = o.optDouble("yFraction", 0.25).toFloat(),
                            scale = o.optDouble("scale", 1.0).toFloat(),
                            rotationDeg = o.optDouble("rotationDeg", 0.0).toFloat(),
                            opacity = o.optDouble("opacity", 1.0).toFloat()
                        )
                    )
                }
            }
            val audArr = root.optJSONArray("audioTracks") ?: JSONArray()
            val auds = buildList {
                for (i in 0 until audArr.length()) {
                    val o = audArr.getJSONObject(i)
                    add(
                        VideoAudioLayerItem(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            kind = AudioTrackKind.entries.firstOrNull { it.name == o.optString("kind") }
                                ?: AudioTrackKind.MUSIC,
                            filePath = o.optString("filePath"),
                            startMs = o.optLong("startMs", 0L),
                            durationMs = o.optLong("durationMs", 5000L),
                            volume = o.optDouble("volume", 0.85).toFloat(),
                            fadeInMs = o.optLong("fadeInMs", 300L),
                            fadeOutMs = o.optLong("fadeOutMs", 400L),
                            isMuted = o.optBoolean("isMuted", false)
                        )
                    )
                }
            }
            VideoEditState(
                projectId = projectId,
                title = root.optString("title", "My Pastel Film"),
                aspectRatio = VideoAspectRatio.entries.firstOrNull { it.name == root.optString("aspectRatio") }
                    ?: VideoAspectRatio.PORTRAIT_9_16,
                clips = clips,
                pipOverlays = pips,
                textOverlays = texts,
                stickerOverlays = stks,
                audioTracks = auds
            )
        }.getOrNull()
    }
}

sealed interface VideoExportState {
    data object Idle : VideoExportState
    data class Preparing(val stage: String) : VideoExportState
    data class Exporting(val progressPercent: Int, val stage: String) : VideoExportState
    data class Completed(val videoId: Long, val outputPath: String, val savedToGallery: Boolean) : VideoExportState
    data class Error(val message: String) : VideoExportState
}
