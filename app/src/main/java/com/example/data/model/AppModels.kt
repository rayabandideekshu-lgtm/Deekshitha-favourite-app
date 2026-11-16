package com.example.data.model

enum class PoseCategory(val label: String, val subtitle: String) {
    WOMEN("Women", "Graceful & expressive"),
    MEN("Men", "Confident & relaxed"),
    CHILDREN("Children", "Playful & age-appropriate"),
    COUPLES("Couples", "Warm & connected"),
    FRIENDS("Friends", "Candid & joyful"),
    FAMILY("Family", "Together & heartfelt"),
    GROUPS("Groups", "Balanced & lively"),
    SOLO_PORTRAITS("Solo portraits", "Editorial & natural")
}

enum class PoseOutlineType(val displayName: String) {
    RELAXED_CAFE_SITTING("Relaxed Café Sitting"),
    STANDING_PORTRAIT("Standing Portrait"),
    HANDS_ON_WAIST("Hands on Waist"),
    WALKING_POSE("Walking Pose"),
    LOOKING_OVER_SHOULDER("Looking Over Shoulder"),
    CUTE_SITTING_POSE("Cute Sitting Pose"),
    MIRROR_SELFIE("Mirror Selfie"),
    SIDE_PROFILE("Side Profile"),
    CASUAL_LEAN("Casual Lean"),
    GROUP_POSE("Group Pose")
}

enum class PoseDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    CREATIVE("Creative")
}

data class PoseSuggestion(
    val id: String,
    val title: String,
    val instruction: String,
    val framingTip: String,
    val category: PoseCategory,
    val difficulty: PoseDifficulty,
    val outlineType: PoseOutlineType
)

data class PoseOutlineConfig(
    val enabled: Boolean = true,
    val scale: Float = 1.0f,
    val verticalOffsetFraction: Float = 0f,
    val alpha: Float = 0.88f,
    val saveWithGuide: Boolean = false
)

data class AlignmentGuidance(
    val message: String = "Position yourself inside the white pose outline.",
    val isAligned: Boolean = false,
    val hasDetectedPerson: Boolean = false
)

data class PoseInspirationBatch(
    val suggestions: List<PoseSuggestion>,
    val isSampleMode: Boolean,
    val notice: String?
)

data class CaptionResult(
    val threeLineCaption: String,
    val isSampleMode: Boolean,
    val notice: String?
)

enum class JournalTemplate(
    val id: String,
    val displayName: String,
    val tagline: String
) {
    PINK_AESTHETIC("pink_aesthetic", "Pink Aesthetic", "Blush & rose petals"),
    LAVENDER_DREAMS("lavender_dreams", "Lavender Dreams", "Dreamy lilac mist"),
    MINIMAL_WHITE("minimal_white", "Minimal White", "Clean gallery frame"),
    VINTAGE_SCRAPBOOK("vintage_scrapbook", "Vintage Scrapbook", "Warm paper & washi"),
    FLOWER_GARDEN("flower_garden", "Flower Garden", "Botanical spring"),
    CUTE_MEMORIES("cute_memories", "Cute Memories", "Sparkles & hearts")
}

enum class JournalSticker(
    val id: String,
    val label: String,
    val glyph: String
) {
    FLOWER("flower", "Flower", "🌸"),
    STAR("star", "Star", "✨"),
    HEART("heart", "Heart", "💖"),
    CAMERA("camera", "Camera", "📸"),
    RIBBON("ribbon", "Ribbon", "🎀"),
    BUTTERFLY("butterfly", "Butterfly", "🦋"),
    TULIP("tulip", "Tulip", "🌷"),
    CLOUD("cloud", "Cloud", "☁️")
}

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class ThemePalette(val label: String) {
    BLUSH_PINK("Blush Pink"),
    SOFT_LAVENDER("Soft Lavender"),
    WARM_CREAM("Warm Cream")
}

enum class GallerySortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First")
}

enum class ArchiveItemType(val label: String) {
    PHOTO("Photo"),
    VIDEO("Video"),
    PHOTO_JOURNAL("Photo Journal"),
    VIDEO_JOURNAL("Video Journal")
}

enum class ArchiveFilterType(val label: String) {
    ALL("All"),
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    PHOTO_JOURNALS("Photo Journals"),
    VIDEO_JOURNALS("Video Journals")
}

data class ArchivedMemoryItem(
    val uniqueKey: String,
    val entityId: Long,
    val type: ArchiveItemType,
    val thumbnailPath: String,
    val mediaPath: String,
    val caption: String,
    val place: String,
    val originalTimestampMillis: Long,
    val archivedAtMillis: Long
)
