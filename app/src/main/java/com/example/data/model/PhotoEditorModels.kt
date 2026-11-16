package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class PhotoFilterPreset(
    val displayName: String,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val brightness: Float = 0f,
    val tintArgb: Long = 0x00000000L,
    val tintAlpha: Float = 0f
) {
    ORIGINAL("Original"),
    SOFT_ROSE("Soft Rose", contrast = 1.04f, saturation = 1.06f, brightness = 0.04f, tintArgb = 0xFFFFB8D0L, tintAlpha = 0.14f),
    PASTEL_MILK("Pastel Milk", contrast = 0.92f, saturation = 0.88f, brightness = 0.10f, tintArgb = 0xFFFFF2F6L, tintAlpha = 0.16f),
    DREAMY_LAVENDER("Dreamy Lilac", contrast = 0.98f, saturation = 0.95f, brightness = 0.04f, tintArgb = 0xFFD6C2F5L, tintAlpha = 0.16f),
    WARM_VINTAGE("Warm Vintage", contrast = 1.08f, saturation = 0.78f, brightness = 0.02f, tintArgb = 0xFFE6BE8AL, tintAlpha = 0.20f),
    GOLDEN_HOUR("Golden Hour", contrast = 1.10f, saturation = 1.18f, brightness = 0.05f, tintArgb = 0xFFFFCA7AL, tintAlpha = 0.16f),
    PEACH_GLOW("Peach Glow", contrast = 1.02f, saturation = 1.12f, brightness = 0.06f, tintArgb = 0xFFFFC5B4L, tintAlpha = 0.15f),
    FRESH_MINT("Fresh Mint", contrast = 1.04f, saturation = 1.05f, brightness = 0.04f, tintArgb = 0xFFC5ECD8L, tintAlpha = 0.13f),
    SAKURA_DREAM("Sakura Dream", contrast = 0.96f, saturation = 1.14f, brightness = 0.06f, tintArgb = 0xFFFFC8DCL, tintAlpha = 0.18f),
    CRISP_MONO("Crisp B&W", contrast = 1.18f, saturation = 0f, brightness = 0.02f),
    HONEY_FILM("Honey Film", contrast = 1.12f, saturation = 0.92f, brightness = 0.01f, tintArgb = 0xFFEBC986L, tintAlpha = 0.14f),
    MOODY_MOCHA("Moody Mocha", contrast = 1.14f, saturation = 0.82f, brightness = -0.05f, tintArgb = 0xFF9C7254L, tintAlpha = 0.16f),
    POLAROID_98("Polaroid 98", contrast = 1.05f, saturation = 0.90f, brightness = 0.04f, tintArgb = 0xFFDCEBF8L, tintAlpha = 0.12f),
    VELVET_NOIR("Velvet Noir", contrast = 1.28f, saturation = 0.15f, brightness = -0.04f)
}

enum class PhotoCropAspect(val label: String, val ratio: Float?) {
    FREE("Free", null),
    SQUARE_1_1("1:1", 1f),
    PORTRAIT_4_5("4:5", 4f / 5f),
    CARD_3_4("3:4", 3f / 4f),
    LANDSCAPE_4_3("4:3", 4f / 3f),
    STORY_9_16("9:16", 9f / 16f),
    WIDE_16_9("16:9", 16f / 9f)
}

enum class ResizePreset(val label: String, val maxDimensionPx: Int) {
    ORIGINAL("Original", 0),
    LARGE_2048("2048px HD", 2048),
    MEDIUM_1440("1440px", 1440),
    SOCIAL_1080("1080px", 1080)
}

enum class CutoutBackdrop(val label: String, val argb: Long) {
    NONE("Original BG", 0x00000000L),
    TRANSPARENT("Transparent PNG", 0x00000000L),
    PURE_WHITE("Studio White", 0xFFFFFFFFL),
    BLUSH_PINK("Blush Pink", 0xFFFADCE6L),
    SOFT_LAVENDER("Soft Lavender", 0xFFEDE7F6L),
    WARM_CREAM("Warm Cream", 0xFFFFF5EEL),
    SKY_PASTEL("Pastel Sky", 0xFFE4EFFAL)
}

data class NormalizedPoint(
    val x: Float,
    val y: Float
)

sealed interface PhotoEditLayer {
    val id: String
    val name: String
    val isVisible: Boolean
    val isLocked: Boolean
    val xFraction: Float
    val yFraction: Float
    val scale: Float
    val rotationDeg: Float
    val opacity: Float

    data class StickerLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        val stickerId: String,
        override val isVisible: Boolean = true,
        override val isLocked: Boolean = false,
        override val xFraction: Float = 0.5f,
        override val yFraction: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotationDeg: Float = 0f,
        override val opacity: Float = 1f
    ) : PhotoEditLayer

    data class TextLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        val text: String,
        val fontId: String = EditorFontOption.PLAYFAIR.id,
        val textColorArgb: Long = 0xFF422330L,
        val pillColorArgb: Long = 0xFFFFF4F8L,
        val hasPillBackground: Boolean = true,
        val fontSizeSp: Float = 24f,
        override val isVisible: Boolean = true,
        override val isLocked: Boolean = false,
        override val xFraction: Float = 0.5f,
        override val yFraction: Float = 0.78f,
        override val scale: Float = 1f,
        override val rotationDeg: Float = 0f,
        override val opacity: Float = 1f
    ) : PhotoEditLayer

    data class DoodleStrokeLayer(
        override val id: String = UUID.randomUUID().toString(),
        override val name: String,
        val points: List<NormalizedPoint>,
        val colorArgb: Long = 0xFFE88AA7L,
        val strokeWidthFraction: Float = 0.015f,
        val isEraser: Boolean = false,
        override val isVisible: Boolean = true,
        override val isLocked: Boolean = false,
        override val xFraction: Float = 0.5f,
        override val yFraction: Float = 0.5f,
        override val scale: Float = 1f,
        override val rotationDeg: Float = 0f,
        override val opacity: Float = 1f
    ) : PhotoEditLayer
}

data class PhotoAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val exposure: Float = 0f,
    val sharpness: Float = 0f,
    val blurRadius: Float = 0f,
    val filterPreset: PhotoFilterPreset = PhotoFilterPreset.ORIGINAL,
    val rotationQuarterTurns: Int = 0,
    val straightenDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val cropAspect: PhotoCropAspect = PhotoCropAspect.FREE,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f,
    val resizePreset: ResizePreset = ResizePreset.ORIGINAL,
    val frameStyle: TemplateFrameStyle = TemplateFrameStyle.NONE,
    val frameThickness: Float = 0.045f,
    val appliedTemplateId: String? = null,
    val backgroundRemoved: Boolean = false,
    val cutoutBackdrop: CutoutBackdrop = CutoutBackdrop.NONE
)

data class PhotoEditState(
    val projectId: Long = 0L,
    val sourcePhotoId: Long? = null,
    val sourcePath: String,
    val cutoutMaskPath: String? = null,
    val adjustments: PhotoAdjustments = PhotoAdjustments(),
    val layers: List<PhotoEditLayer> = emptyList()
) {
    fun toJsonString(): String {
        val adj = adjustments
        val adjObj = JSONObject().apply {
            put("brightness", adj.brightness)
            put("contrast", adj.contrast)
            put("saturation", adj.saturation)
            put("exposure", adj.exposure)
            put("sharpness", adj.sharpness)
            put("blurRadius", adj.blurRadius)
            put("filterPreset", adj.filterPreset.name)
            put("rotationQuarterTurns", adj.rotationQuarterTurns)
            put("straightenDegrees", adj.straightenDegrees)
            put("flipHorizontal", adj.flipHorizontal)
            put("flipVertical", adj.flipVertical)
            put("cropAspect", adj.cropAspect.name)
            put("cropLeft", adj.cropLeft)
            put("cropTop", adj.cropTop)
            put("cropRight", adj.cropRight)
            put("cropBottom", adj.cropBottom)
            put("resizePreset", adj.resizePreset.name)
            put("frameStyle", adj.frameStyle.name)
            put("frameThickness", adj.frameThickness)
            put("appliedTemplateId", adj.appliedTemplateId ?: "")
            put("backgroundRemoved", adj.backgroundRemoved)
            put("cutoutBackdrop", adj.cutoutBackdrop.name)
        }
        val layerArray = JSONArray()
        layers.forEach { layer ->
            val obj = JSONObject().apply {
                put("id", layer.id)
                put("name", layer.name)
                put("isVisible", layer.isVisible)
                put("isLocked", layer.isLocked)
                put("xFraction", layer.xFraction)
                put("yFraction", layer.yFraction)
                put("scale", layer.scale)
                put("rotationDeg", layer.rotationDeg)
                put("opacity", layer.opacity)
                when (layer) {
                    is PhotoEditLayer.StickerLayer -> {
                        put("kind", "STICKER")
                        put("stickerId", layer.stickerId)
                    }
                    is PhotoEditLayer.TextLayer -> {
                        put("kind", "TEXT")
                        put("text", layer.text)
                        put("fontId", layer.fontId)
                        put("textColorArgb", layer.textColorArgb)
                        put("pillColorArgb", layer.pillColorArgb)
                        put("hasPillBackground", layer.hasPillBackground)
                        put("fontSizeSp", layer.fontSizeSp)
                    }
                    is PhotoEditLayer.DoodleStrokeLayer -> {
                        put("kind", "DOODLE")
                        put("colorArgb", layer.colorArgb)
                        put("strokeWidthFraction", layer.strokeWidthFraction)
                        put("isEraser", layer.isEraser)
                        val pts = JSONArray()
                        layer.points.forEach { p ->
                            pts.put(JSONObject().put("x", p.x).put("y", p.y))
                        }
                        put("points", pts)
                    }
                }
            }
            layerArray.put(obj)
        }
        return JSONObject().apply {
            put("sourcePath", sourcePath)
            put("cutoutMaskPath", cutoutMaskPath ?: "")
            put("adjustments", adjObj)
            put("layers", layerArray)
        }.toString()
    }

    companion object {
        fun fromJsonString(json: String, projectId: Long = 0L): PhotoEditState? = runCatching {
            val root = JSONObject(json)
            val adjObj = root.getJSONObject("adjustments")
            val adj = PhotoAdjustments(
                brightness = adjObj.optDouble("brightness", 0.0).toFloat(),
                contrast = adjObj.optDouble("contrast", 1.0).toFloat(),
                saturation = adjObj.optDouble("saturation", 1.0).toFloat(),
                exposure = adjObj.optDouble("exposure", 0.0).toFloat(),
                sharpness = adjObj.optDouble("sharpness", 0.0).toFloat(),
                blurRadius = adjObj.optDouble("blurRadius", 0.0).toFloat(),
                filterPreset = PhotoFilterPreset.entries.firstOrNull { it.name == adjObj.optString("filterPreset") }
                    ?: PhotoFilterPreset.ORIGINAL,
                rotationQuarterTurns = adjObj.optInt("rotationQuarterTurns", 0),
                straightenDegrees = adjObj.optDouble("straightenDegrees", 0.0).toFloat(),
                flipHorizontal = adjObj.optBoolean("flipHorizontal", false),
                flipVertical = adjObj.optBoolean("flipVertical", false),
                cropAspect = PhotoCropAspect.entries.firstOrNull { it.name == adjObj.optString("cropAspect") }
                    ?: PhotoCropAspect.FREE,
                cropLeft = adjObj.optDouble("cropLeft", 0.0).toFloat(),
                cropTop = adjObj.optDouble("cropTop", 0.0).toFloat(),
                cropRight = adjObj.optDouble("cropRight", 1.0).toFloat(),
                cropBottom = adjObj.optDouble("cropBottom", 1.0).toFloat(),
                resizePreset = ResizePreset.entries.firstOrNull { it.name == adjObj.optString("resizePreset") }
                    ?: ResizePreset.ORIGINAL,
                frameStyle = TemplateFrameStyle.entries.firstOrNull { it.name == adjObj.optString("frameStyle") }
                    ?: TemplateFrameStyle.NONE,
                frameThickness = adjObj.optDouble("frameThickness", 0.045).toFloat(),
                appliedTemplateId = adjObj.optString("appliedTemplateId").takeIf { it.isNotBlank() },
                backgroundRemoved = adjObj.optBoolean("backgroundRemoved", false),
                cutoutBackdrop = CutoutBackdrop.entries.firstOrNull { it.name == adjObj.optString("cutoutBackdrop") }
                    ?: CutoutBackdrop.NONE
            )
            val layerArr = root.optJSONArray("layers") ?: JSONArray()
            val layers = buildList {
                for (i in 0 until layerArr.length()) {
                    val o = layerArr.getJSONObject(i)
                    val id = o.optString("id")
                    val name = o.optString("name")
                    val visible = o.optBoolean("isVisible", true)
                    val locked = o.optBoolean("isLocked", false)
                    val x = o.optDouble("xFraction", 0.5).toFloat()
                    val y = o.optDouble("yFraction", 0.5).toFloat()
                    val scale = o.optDouble("scale", 1.0).toFloat()
                    val rot = o.optDouble("rotationDeg", 0.0).toFloat()
                    val op = o.optDouble("opacity", 1.0).toFloat()
                    when (o.optString("kind")) {
                        "STICKER" -> add(
                            PhotoEditLayer.StickerLayer(
                                id = id,
                                name = name,
                                stickerId = o.optString("stickerId"),
                                isVisible = visible,
                                isLocked = locked,
                                xFraction = x,
                                yFraction = y,
                                scale = scale,
                                rotationDeg = rot,
                                opacity = op
                            )
                        )
                        "TEXT" -> add(
                            PhotoEditLayer.TextLayer(
                                id = id,
                                name = name,
                                text = o.optString("text"),
                                fontId = o.optString("fontId", EditorFontOption.PLAYFAIR.id),
                                textColorArgb = o.optLong("textColorArgb", 0xFF422330L),
                                pillColorArgb = o.optLong("pillColorArgb", 0xFFFFF4F8L),
                                hasPillBackground = o.optBoolean("hasPillBackground", true),
                                fontSizeSp = o.optDouble("fontSizeSp", 24.0).toFloat(),
                                isVisible = visible,
                                isLocked = locked,
                                xFraction = x,
                                yFraction = y,
                                scale = scale,
                                rotationDeg = rot,
                                opacity = op
                            )
                        )
                        "DOODLE" -> {
                            val ptsArr = o.optJSONArray("points") ?: JSONArray()
                            val pts = buildList {
                                for (j in 0 until ptsArr.length()) {
                                    val p = ptsArr.getJSONObject(j)
                                    add(NormalizedPoint(p.getDouble("x").toFloat(), p.getDouble("y").toFloat()))
                                }
                            }
                            add(
                                PhotoEditLayer.DoodleStrokeLayer(
                                    id = id,
                                    name = name,
                                    points = pts,
                                    colorArgb = o.optLong("colorArgb", 0xFFE88AA7L),
                                    strokeWidthFraction = o.optDouble("strokeWidthFraction", 0.015).toFloat(),
                                    isEraser = o.optBoolean("isEraser", false),
                                    isVisible = visible,
                                    isLocked = locked,
                                    xFraction = x,
                                    yFraction = y,
                                    scale = scale,
                                    rotationDeg = rot,
                                    opacity = op
                                )
                            )
                        }
                    }
                }
            }
            PhotoEditState(
                projectId = projectId,
                sourcePath = root.getString("sourcePath"),
                cutoutMaskPath = root.optString("cutoutMaskPath").takeIf { it.isNotBlank() },
                adjustments = adj,
                layers = layers
            )
        }.getOrNull()
    }
}
