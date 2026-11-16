package com.example.data.model

import com.example.R

enum class CreativeCategory(
    val id: String,
    val label: String,
    val iconGlyph: String,
    val accentHex: Long
) {
    PINK_AESTHETIC("pink_aesthetic", "Pink aesthetic", "🌸", 0xFFE88AA7),
    VINTAGE("vintage", "Vintage", "📜", 0xFFCFA268),
    CUTE_SCRAPBOOK("cute_scrapbook", "Cute scrapbook", "✂️", 0xFFF08CA8),
    MINIMAL("minimal", "Minimal", "🤍", 0xFF7D7482),
    FLORAL("floral", "Floral", "🌷", 0xFF75A488),
    BIRTHDAY("birthday", "Birthday", "🎂", 0xFFE582A5),
    TRAVEL("travel", "Travel", "✈️", 0xFF6B9AC4),
    CAFE("cafe", "Café", "☕", 0xFF9C7254),
    MEMORIES("memories", "Memories", "📸", 0xFFB39DDB),
    ROMANTIC("romantic", "Romantic", "💌", 0xFFD96B8A)
}

enum class EditorFontOption(
    val id: String,
    val displayName: String,
    val fontResId: Int
) {
    PLAYFAIR("playfair", "Playfair", R.font.playfair_display),
    PLUS_JAKARTA("jakarta", "Jakarta", R.font.plus_jakarta_sans),
    PACIFICO("pacifico", "Pacifico", R.font.pacifico),
    DANCING_SCRIPT("dancing", "Dancing", R.font.dancing_script),
    CAVEAT("caveat", "Caveat", R.font.caveat),
    QUICKSAND("quicksand", "Quicksand", R.font.quicksand),
    LORA("lora", "Lora", R.font.lora),
    SPACE_GROTESK("grotesk", "Grotesk", R.font.space_grotesk),
    CORMORANT("cormorant", "Cormorant", R.font.cormorant_garamond),
    SACRAMENTO("sacramento", "Sacramento", R.font.sacramento);

    companion object {
        fun fromId(id: String): EditorFontOption =
            entries.firstOrNull { it.id == id } ?: PLAYFAIR
    }
}

enum class TemplateFrameStyle(val label: String) {
    NONE("No Frame"),
    POLAROID("Polaroid"),
    WASHI_SCRAPBOOK("Washi Tape"),
    FILM_STRIP("Film Strip"),
    LACE_BORDER("Scallop Lace"),
    GOLD_FOIL("Gold Foil"),
    ARCH_FRAME("Soft Arch"),
    POSTCARD("Postcard"),
    DOUBLE_MINIMAL("Double Line"),
    COLLAGE_SPLIT("Collage Edge"),
    HEART_LOCKET("Heart Charm")
}

enum class TemplatePatternType {
    NONE, DOTS, HEARTS, PETALS, STARS, CHECKS
}

data class AestheticTemplate(
    val id: String,
    val name: String,
    val category: CreativeCategory,
    val tagline: String,
    val bgStartArgb: Long,
    val bgEndArgb: Long,
    val borderArgb: Long,
    val accentArgb: Long,
    val inkArgb: Long,
    val washiArgb: Long,
    val frameStyle: TemplateFrameStyle,
    val patternType: TemplatePatternType,
    val cornerGlyph: String,
    val badgeText: String,
    val fontId: String
)

enum class StickerVectorKind {
    FLOWER, HEART, STAR, BOW, BUTTERFLY, SPARKLE, DOODLE, STAMP, RIBBON, CLOUD, CUP, POLAROID
}

enum class StickerBadgeStyle {
    CLEAN_VECTOR, PASTEL_PILL, STAMP_SEAL, WASHI_TAG, SPARKLE_BADGE
}

data class CreativeSticker(
    val id: String,
    val name: String,
    val category: CreativeCategory,
    val glyph: String,
    val subLabel: String,
    val vectorKind: StickerVectorKind,
    val badgeStyle: StickerBadgeStyle,
    val primaryArgb: Long,
    val secondaryArgb: Long,
    val tags: List<String>
)

object CreativeLibraryCatalog {

    private data class CategoryPaletteSeed(
        val bgPairs: List<Pair<Long, Long>>,
        val borderArgbs: List<Long>,
        val accentArgbs: List<Long>,
        val inkArgb: Long,
        val washiArgbs: List<Long>,
        val adjectives: List<String>,
        val motifs: List<String>,
        val glyphs: List<String>,
        val preferredFrames: List<TemplateFrameStyle>,
        val preferredPatterns: List<TemplatePatternType>,
        val fontIds: List<String>
    )

    private val templateSeeds: Map<CreativeCategory, CategoryPaletteSeed> = mapOf(
        CreativeCategory.PINK_AESTHETIC to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFFF1F6L to 0xFFFADCE6L, 0xFFFFE5EFL to 0xFFFFD4E5L, 0xFFFFF8FAL to 0xFFF8CBE0L, 0xFFFFDCE8L to 0xFFF3BDD0L),
            borderArgbs = listOf(0xFFF3BDD0L, 0xFFE88AA7L, 0xFFF7A8C4L),
            accentArgbs = listOf(0xFF9E3B5DL, 0xFFB84C72L, 0xFFC75B80L),
            inkArgb = 0xFF422330L,
            washiArgbs = listOf(0xFFF7B8CDL, 0xFFFFD6E6L, 0xFFEFA5C2L),
            adjectives = listOf("Blush", "Peony", "Cotton", "Velvet", "Rosy", "Sugar", "Petal", "Ballet"),
            motifs = listOf("Ribbon", "Dream", "Glow", "Note", "Polaroid", "Diary"),
            glyphs = listOf("🌸", "🎀", "💖", "✨", "🩰", "🌷"),
            preferredFrames = listOf(TemplateFrameStyle.POLAROID, TemplateFrameStyle.LACE_BORDER, TemplateFrameStyle.WASHI_SCRAPBOOK, TemplateFrameStyle.ARCH_FRAME),
            preferredPatterns = listOf(TemplatePatternType.HEARTS, TemplatePatternType.DOTS, TemplatePatternType.PETALS),
            fontIds = listOf("playfair", "sacramento", "pacifico", "dancing")
        ),
        CreativeCategory.VINTAGE to CategoryPaletteSeed(
            bgPairs = listOf(0xFFF5EBDDL to 0xFFEBD7BFL, 0xFFFBF3E4L to 0xFFEADBC6L, 0xFFF2E6D5L to 0xFFE3C8A8L, 0xFFF8F0E3L to 0xFFDCC4A8L),
            borderArgbs = listOf(0xFFCFA268L, 0xFFB58958L, 0xFF9C7248L),
            accentArgbs = listOf(0xFF5C4636L, 0xFF7B563BL, 0xFF8C6239L),
            inkArgb = 0xFF3E2C20L,
            washiArgbs = listOf(0xFFE4C59BL, 0xFFD9B482L, 0xFFCBB18DL),
            adjectives = listOf("Sepia", "Antique", "Heirloom", "Faded", "Parchment", "Golden", "Retro", "Film"),
            motifs = listOf("Postcard", "Chronicle", "Atelier", "Reel", "Journal", "Press"),
            glyphs = listOf("📜", "🎞️", "🕰️", "🗝️", "✉️", "🍂"),
            preferredFrames = listOf(TemplateFrameStyle.FILM_STRIP, TemplateFrameStyle.POSTCARD, TemplateFrameStyle.GOLD_FOIL, TemplateFrameStyle.POLAROID),
            preferredPatterns = listOf(TemplatePatternType.CHECKS, TemplatePatternType.DOTS, TemplatePatternType.NONE),
            fontIds = listOf("cormorant", "lora", "playfair", "caveat")
        ),
        CreativeCategory.CUTE_SCRAPBOOK to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFFF6F9L to 0xFFFCE8D5L, 0xFFFFF0F5L to 0xFFEDE7F6L, 0xFFFFF9EEL to 0xFFFFDFECL, 0xFFF6EEFFL to 0xFFFFE7F0L),
            borderArgbs = listOf(0xFFF4B6CEL, 0xFFCDB9EBL, 0xFFF5C99BL),
            accentArgbs = listOf(0xFFB44472L, 0xFF6B4C9AL, 0xFFC9688CL),
            inkArgb = 0xFF3F2636L,
            washiArgbs = listOf(0xFFFFC9DDL, 0xFFD5C4F3L, 0xFFFFE3B8L),
            adjectives = listOf("Washi", "Doodle", "Sticker", "Kawaii", "Patchwork", "Pastel", "Candy", "Ribbon"),
            motifs = listOf("Collage", "Page", "Album", "Snippet", "Cutout", "Card"),
            glyphs = listOf("✂️", "🎀", "⭐", "🦋", "🍓", "🖍️"),
            preferredFrames = listOf(TemplateFrameStyle.WASHI_SCRAPBOOK, TemplateFrameStyle.COLLAGE_SPLIT, TemplateFrameStyle.LACE_BORDER, TemplateFrameStyle.POLAROID),
            preferredPatterns = listOf(TemplatePatternType.CHECKS, TemplatePatternType.STARS, TemplatePatternType.HEARTS),
            fontIds = listOf("caveat", "pacifico", "quicksand", "dancing")
        ),
        CreativeCategory.MINIMAL to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFFFFFFL to 0xFFFAF9FBL, 0xFFF9F7F8L to 0xFFF1EEF3L, 0xFFFFFEFDL to 0xFFF4F1ECL, 0xFFFAFAFAL to 0xFFEFECEAL),
            borderArgbs = listOf(0xFFDAD4DDL, 0xFFC9C1CDL, 0xFFE2DDD7L),
            accentArgbs = listOf(0xFF4A424EL, 0xFF635A68L, 0xFF5C5552L),
            inkArgb = 0xFF242026L,
            washiArgbs = listOf(0xFFE7E2EAL, 0xFFDDD6E0L, 0xFFE8E2D9L),
            adjectives = listOf("Pure", "Gallery", "Studio", "Silk", "Mono", "Airy", "Soft", "Clean"),
            motifs = listOf("Matte", "Frame", "Edition", "Canvas", "Margin", "Sheet"),
            glyphs = listOf("🤍", "◻️", "✦", "〰️", "🕊️", "🫧"),
            preferredFrames = listOf(TemplateFrameStyle.DOUBLE_MINIMAL, TemplateFrameStyle.ARCH_FRAME, TemplateFrameStyle.POLAROID, TemplateFrameStyle.NONE),
            preferredPatterns = listOf(TemplatePatternType.NONE, TemplatePatternType.DOTS),
            fontIds = listOf("grotesk", "jakarta", "lora", "cormorant")
        ),
        CreativeCategory.FLORAL to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFDF2F6L to 0xFFDCEEE2L, 0xFFF6FBF8L to 0xFFE5F3E9L, 0xFFFFF5F7L to 0xFFE8F4EBL, 0xFFF4FAF4L to 0xFFFBE6EFL),
            borderArgbs = listOf(0xFF75A488L, 0xFF9AC0A8L, 0xFFE8A4BDL),
            accentArgbs = listOf(0xFF3E6E52L, 0xFF4F8264L, 0xFF9E4B6DL),
            inkArgb = 0xFF283C31L,
            washiArgbs = listOf(0xFFC5E3CFL, 0xFFF5CADBL, 0xFFD4EADBL),
            adjectives = listOf("Botanical", "Petal", "Meadow", "Sakura", "Wildflower", "Clover", "Camellia", "Garden"),
            motifs = listOf("Herbarium", "Bouquet", "Bloom", "Press", "Wreath", "Arbor"),
            glyphs = listOf("🌷", "🌸", "🌿", "🌼", "🌺", "🦋"),
            preferredFrames = listOf(TemplateFrameStyle.ARCH_FRAME, TemplateFrameStyle.LACE_BORDER, TemplateFrameStyle.WASHI_SCRAPBOOK, TemplateFrameStyle.POLAROID),
            preferredPatterns = listOf(TemplatePatternType.PETALS, TemplatePatternType.DOTS, TemplatePatternType.HEARTS),
            fontIds = listOf("playfair", "cormorant", "sacramento", "lora")
        ),
        CreativeCategory.BIRTHDAY to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFFF2F8L to 0xFFFFF5DCL, 0xFFF9EEFFL to 0xFFFFE5F0L, 0xFFFFF8E8L to 0xFFFADCE6L, 0xFFFFECF4L to 0xFFEADFFF),
            borderArgbs = listOf(0xFFF09DBDL, 0xFFE9BA6BL, 0xFFB89CE6L),
            accentArgbs = listOf(0xFFB54173L, 0xFF7D4EA3L, 0xFFC57B2EL),
            inkArgb = 0xFF402235L,
            washiArgbs = listOf(0xFFFFD1E3L, 0xFFFFE6A8L, 0xFFD8C7F6L),
            adjectives = listOf("Confetti", "Sparkle", "Wish", "Celebration", "Cupcake", "Candlelight", "Ribbon", "Party"),
            motifs = listOf("Invite", "Toast", "Milestone", "Greeting", "Party", "Moment"),
            glyphs = listOf("🎂", "🎈", "🎉", "✨", "🧁", "🕯️"),
            preferredFrames = listOf(TemplateFrameStyle.LACE_BORDER, TemplateFrameStyle.WASHI_SCRAPBOOK, TemplateFrameStyle.HEART_LOCKET, TemplateFrameStyle.POLAROID),
            preferredPatterns = listOf(TemplatePatternType.STARS, TemplatePatternType.DOTS, TemplatePatternType.HEARTS),
            fontIds = listOf("pacifico", "dancing", "quicksand", "caveat")
        ),
        CreativeCategory.TRAVEL to CategoryPaletteSeed(
            bgPairs = listOf(0xFFE4EFFAL to 0xFFFFFBF7L, 0xFFEBF4FCL to 0xFFFCE8D5L, 0xFFE7F2F8L to 0xFFEDE7F6L, 0xFFF3F8FDL to 0xFFE3EFE8L),
            borderArgbs = listOf(0xFF8DB4D8L, 0xFF7FA3C7L, 0xFFC8A575L),
            accentArgbs = listOf(0xFF396188L, 0xFF4B759EL, 0xFF8B633CL),
            inkArgb = 0xFF23384DL,
            washiArgbs = listOf(0xFFBED8F0L, 0xFFE9CFA7L, 0xFFCFE4D8L),
            adjectives = listOf("Passport", "Postcard", "Horizon", "Wanderlust", "Coastal", "Skyline", "Boarding", "Atlas"),
            motifs = listOf("Dispatch", "Stamp", "Route", "Logbook", "Snapshot", "Souvenir"),
            glyphs = listOf("✈️", "🗺️", "🧭", "☁️", "🎫", "📷"),
            preferredFrames = listOf(TemplateFrameStyle.POSTCARD, TemplateFrameStyle.FILM_STRIP, TemplateFrameStyle.COLLAGE_SPLIT, TemplateFrameStyle.DOUBLE_MINIMAL),
            preferredPatterns = listOf(TemplatePatternType.CHECKS, TemplatePatternType.DOTS, TemplatePatternType.NONE),
            fontIds = listOf("grotesk", "caveat", "jakarta", "lora")
        ),
        CreativeCategory.CAFE to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFDF5ECL to 0xFFF3E2D0L, 0xFFFAF0E6L to 0xFFEAD4BFL, 0xFFFFF8F2L to 0xFFF6DFD8L, 0xFFF7ECE1L to 0xFFECD8C7L),
            borderArgbs = listOf(0xFFC59B78L, 0xFFB08463L, 0xFFDAA59CL),
            accentArgbs = listOf(0xFF6B4730L, 0xFF885B3EL, 0xFF9C545BL),
            inkArgb = 0xFF3D281CL,
            washiArgbs = listOf(0xFFE5C7ACL, 0xFFF2D4CEL, 0xFFD9B796L),
            adjectives = listOf("Latte", "Espresso", "Croissant", "Matcha", "Bistro", "Caramel", "Mocha", "Patisserie"),
            motifs = listOf("Menu", "Table", "Morning", "Pause", "Coaster", "Receipt"),
            glyphs = listOf("☕", "🥐", "🍰", "🫖", "🍨", "🌿"),
            preferredFrames = listOf(TemplateFrameStyle.ARCH_FRAME, TemplateFrameStyle.POLAROID, TemplateFrameStyle.POSTCARD, TemplateFrameStyle.DOUBLE_MINIMAL),
            preferredPatterns = listOf(TemplatePatternType.CHECKS, TemplatePatternType.DOTS, TemplatePatternType.NONE),
            fontIds = listOf("cormorant", "caveat", "playfair", "quicksand")
        ),
        CreativeCategory.MEMORIES to CategoryPaletteSeed(
            bgPairs = listOf(0xFFF4EFFF to 0xFFFFF1F6L, 0xFFEDE7F6L to 0xFFFCE8D5L, 0xFFF8F4FFL to 0xFFE6EEFAL, 0xFFF9F2FFL to 0xFFFADCE6L),
            borderArgbs = listOf(0xFFB39DDBL, 0xFFC5B1E8L, 0xFFE5A4C0L),
            accentArgbs = listOf(0xFF563D7CL, 0xFF6E5099L, 0xFF9E3B5DL),
            inkArgb = 0xFF2E2246L,
            washiArgbs = listOf(0xFFD0BFEFL, 0xFFF5C1D6L, 0xFFE4D8FAL),
            adjectives = listOf("Keepsake", "Twilight", "Starlight", "Lavender", "Cherished", "Timeless", "Lullaby", "Silver"),
            motifs = listOf("Archive", "Capsule", "Chapter", "Entry", "Echo", "Frame"),
            glyphs = listOf("📸", "✨", "🌙", "💜", "🦋", "🕊️"),
            preferredFrames = listOf(TemplateFrameStyle.POLAROID, TemplateFrameStyle.FILM_STRIP, TemplateFrameStyle.WASHI_SCRAPBOOK, TemplateFrameStyle.DOUBLE_MINIMAL),
            preferredPatterns = listOf(TemplatePatternType.STARS, TemplatePatternType.DOTS, TemplatePatternType.PETALS),
            fontIds = listOf("playfair", "lora", "quicksand", "dancing")
        ),
        CreativeCategory.ROMANTIC to CategoryPaletteSeed(
            bgPairs = listOf(0xFFFFEDF2L to 0xFFF9D5E2L, 0xFFFFF3F5L to 0xFFF6D2DCL, 0xFFFFE8EEL to 0xFFF2DCF8L, 0xFFFFF6F4L to 0xFFF8D0DCL),
            borderArgbs = listOf(0xFFD96B8AL, 0xFFCFA268L, 0xFFE99CB3L),
            accentArgbs = listOf(0xFF9A2E4FL, 0xFFB04365L, 0xFF8C5438L),
            inkArgb = 0xFF431E2CL,
            washiArgbs = listOf(0xFFF6B7C9L, 0xFFECC69BL, 0xFFF9D7E3L),
            adjectives = listOf("Loveletter", "Velvet", "Sweetheart", "Rosewater", "Poetic", "Whisper", "Sonnet", "Locket"),
            motifs = listOf("Seal", "Letter", "Promise", "Serenade", "Vow", "Bouquet"),
            glyphs = listOf("💌", "💖", "🌹", "🕊️", "💍", "✨"),
            preferredFrames = listOf(TemplateFrameStyle.HEART_LOCKET, TemplateFrameStyle.GOLD_FOIL, TemplateFrameStyle.LACE_BORDER, TemplateFrameStyle.ARCH_FRAME),
            preferredPatterns = listOf(TemplatePatternType.HEARTS, TemplatePatternType.PETALS, TemplatePatternType.DOTS),
            fontIds = listOf("sacramento", "dancing", "cormorant", "playfair")
        )
    )

    // 240 unique curated templates (24 per category x 10 categories)
    val templates: List<AestheticTemplate> by lazy {
        buildList {
            for (category in CreativeCategory.entries) {
                val seed = templateSeeds.getValue(category)
                var idx = 0
                for (adj in seed.adjectives) {
                    for (mIdx in 0 until 3) {
                        val motif = seed.motifs[(idx + mIdx) % seed.motifs.size]
                        val bgPair = seed.bgPairs[idx % seed.bgPairs.size]
                        val border = seed.borderArgbs[idx % seed.borderArgbs.size]
                        val accent = seed.accentArgbs[idx % seed.accentArgbs.size]
                        val washi = seed.washiArgbs[idx % seed.washiArgbs.size]
                        val frame = seed.preferredFrames[idx % seed.preferredFrames.size]
                        val pattern = seed.preferredPatterns[(idx + 1) % seed.preferredPatterns.size]
                        val glyph = seed.glyphs[idx % seed.glyphs.size]
                        val font = seed.fontIds[idx % seed.fontIds.size]
                        val variantNum = (idx % 3) + 1
                        add(
                            AestheticTemplate(
                                id = "${category.id}_${idx + 1}",
                                name = "$adj $motif" + if (variantNum > 1) " No.$variantNum" else "",
                                category = category,
                                tagline = "${frame.label} • ${category.label}",
                                bgStartArgb = bgPair.first,
                                bgEndArgb = bgPair.second,
                                borderArgb = border,
                                accentArgb = accent,
                                inkArgb = seed.inkArgb,
                                washiArgb = washi,
                                frameStyle = frame,
                                patternType = pattern,
                                cornerGlyph = glyph,
                                badgeText = "${category.label.uppercase()} • ${frame.label.uppercase()}",
                                fontId = font
                            )
                        )
                        idx++
                    }
                }
            }
        }
    }

    // 120 unique curated stickers (12 per category x 10 categories)
    val stickers: List<CreativeSticker> by lazy {
        listOf(
            // PINK AESTHETIC (12)
            CreativeSticker("stk_pink_1", "Blush Blossom", CreativeCategory.PINK_AESTHETIC, "🌸", "Blush", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFF49AB8, 0xFFFFE5EE, listOf("flower", "pink", "sakura")),
            CreativeSticker("stk_pink_2", "Satin Bow", CreativeCategory.PINK_AESTHETIC, "🎀", "Bow", StickerVectorKind.BOW, StickerBadgeStyle.PASTEL_PILL, 0xFFE8789E, 0xFFFFDCE8, listOf("bow", "ribbon", "cute")),
            CreativeSticker("stk_pink_3", "Sugar Heart", CreativeCategory.PINK_AESTHETIC, "💖", "Sweet", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFFEC5E90, 0xFFFFD8E6, listOf("heart", "pink", "love")),
            CreativeSticker("stk_pink_4", "Peony Sparkle", CreativeCategory.PINK_AESTHETIC, "✨", "Glow", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFFF4A9C4, 0xFFFFF0F6, listOf("sparkle", "glow", "aesthetic")),
            CreativeSticker("stk_pink_5", "Ballet Butterfly", CreativeCategory.PINK_AESTHETIC, "🦋", "Dreamy", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.PASTEL_PILL, 0xFFE88AA7, 0xFFF9D9E5, listOf("butterfly", "pink")),
            CreativeSticker("stk_pink_6", "Pink Starlight", CreativeCategory.PINK_AESTHETIC, "🌟", "Shine", StickerVectorKind.STAR, StickerBadgeStyle.CLEAN_VECTOR, 0xFFF79AC0, 0xFFFFEAF2, listOf("star", "shine")),
            CreativeSticker("stk_pink_7", "Strawberry Milk", CreativeCategory.PINK_AESTHETIC, "🍓", "Berry", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFE96E88, 0xFFFFE0E8, listOf("doodle", "cute", "berry")),
            CreativeSticker("stk_pink_8", "Rose Stamp", CreativeCategory.PINK_AESTHETIC, "🌺", "Rose", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFFD96A8E, 0xFFFFE3EC, listOf("flower", "stamp")),
            CreativeSticker("stk_pink_9", "Candy Cloud", CreativeCategory.PINK_AESTHETIC, "☁️", "Fluffy", StickerVectorKind.CLOUD, StickerBadgeStyle.WASHI_TAG, 0xFFF3A7C2, 0xFFFFEFF5, listOf("cloud", "doodle")),
            CreativeSticker("stk_pink_10", "Ribbon Tag", CreativeCategory.PINK_AESTHETIC, "🩰", "Soft", StickerVectorKind.RIBBON, StickerBadgeStyle.WASHI_TAG, 0xFFE88AA7, 0xFFFDE6EF, listOf("bow", "ribbon")),
            CreativeSticker("stk_pink_11", "Twin Hearts", CreativeCategory.PINK_AESTHETIC, "💕", "Lovely", StickerVectorKind.HEART, StickerBadgeStyle.SPARKLE_BADGE, 0xFFEA6E98, 0xFFFFDCE8, listOf("hearts", "pink")),
            CreativeSticker("stk_pink_12", "Cherry Doodle", CreativeCategory.PINK_AESTHETIC, "🍒", "Pop", StickerVectorKind.DOODLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE05777, 0xFFFFDFE8, listOf("doodle", "cute")),

            // VINTAGE (12)
            CreativeSticker("stk_vint_1", "Wax Seal", CreativeCategory.VINTAGE, "🔴", "1998", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF9C4B42, 0xFFF3DEC7, listOf("seal", "vintage", "stamp")),
            CreativeSticker("stk_vint_2", "Pressed Rose", CreativeCategory.VINTAGE, "🥀", "Pressed", StickerVectorKind.FLOWER, StickerBadgeStyle.PASTEL_PILL, 0xFF8B584B, 0xFFF4E8D8, listOf("flower", "vintage")),
            CreativeSticker("stk_vint_3", "Postmark Stamp", CreativeCategory.VINTAGE, "✉️", "AIR MAIL", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF7B563B, 0xFFF5EBDC, listOf("stamp", "vintage", "letter")),
            CreativeSticker("stk_vint_4", "Film Frame", CreativeCategory.VINTAGE, "🎞️", "35MM", StickerVectorKind.POLAROID, StickerBadgeStyle.WASHI_TAG, 0xFF5C4636, 0xFFEBD9C3, listOf("film", "vintage", "doodle")),
            CreativeSticker("stk_vint_5", "Antique Key", CreativeCategory.VINTAGE, "🗝️", "Secret", StickerVectorKind.DOODLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFA67B4E, 0xFFF6EAD7, listOf("doodle", "vintage")),
            CreativeSticker("stk_vint_6", "Sepia Star", CreativeCategory.VINTAGE, "✦", "Classic", StickerVectorKind.STAR, StickerBadgeStyle.SPARKLE_BADGE, 0xFFCFA268, 0xFFF8EEDB, listOf("star", "vintage")),
            CreativeSticker("stk_vint_7", "Autumn Leaf", CreativeCategory.VINTAGE, "🍂", "Warm", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFB06D44, 0xFFF5E5D5, listOf("doodle", "leaf")),
            CreativeSticker("stk_vint_8", "Gold Ribbon", CreativeCategory.VINTAGE, "🎗️", "Archive", StickerVectorKind.RIBBON, StickerBadgeStyle.WASHI_TAG, 0xFFCFA268, 0xFFF7ECD8, listOf("ribbon", "bow")),
            CreativeSticker("stk_vint_9", "Parchment Heart", CreativeCategory.VINTAGE, "🤎", "Sepia", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFF8D5E46, 0xFFF2E2D0, listOf("heart", "vintage")),
            CreativeSticker("stk_vint_10", "Monarch Wing", CreativeCategory.VINTAGE, "🦋", "Specimen", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.STAMP_SEAL, 0xFF9C6942, 0xFFF4E6D3, listOf("butterfly", "vintage")),
            CreativeSticker("stk_vint_11", "Pocket Watch", CreativeCategory.VINTAGE, "🕰️", "Hour", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF7D5A3D, 0xFFF4E9D9, listOf("doodle", "vintage")),
            CreativeSticker("stk_vint_12", "Candle Glow", CreativeCategory.VINTAGE, "🕯️", "Warmth", StickerVectorKind.SPARKLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFC88F52, 0xFFF8EDE0, listOf("sparkle", "vintage")),

            // CUTE SCRAPBOOK (12)
            CreativeSticker("stk_scrap_1", "Washi Tape Pink", CreativeCategory.CUTE_SCRAPBOOK, "🩹", "Note", StickerVectorKind.RIBBON, StickerBadgeStyle.WASHI_TAG, 0xFFF49AB8, 0xFFFFE5EE, listOf("washi", "scrapbook")),
            CreativeSticker("stk_scrap_2", "Smiley Star", CreativeCategory.CUTE_SCRAPBOOK, "⭐", "Happy!", StickerVectorKind.STAR, StickerBadgeStyle.CLEAN_VECTOR, 0xFFF3B74B, 0xFFFFF5D4, listOf("star", "doodle")),
            CreativeSticker("stk_scrap_3", "Rainbow Doodle", CreativeCategory.CUTE_SCRAPBOOK, "🌈", "Joy", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFE88AA7, 0xFFFCEEF5, listOf("doodle", "rainbow")),
            CreativeSticker("stk_scrap_4", "Cute Bow Clip", CreativeCategory.CUTE_SCRAPBOOK, "🎀", "Sweet", StickerVectorKind.BOW, StickerBadgeStyle.CLEAN_VECTOR, 0xFFEA769C, 0xFFFFDCE9, listOf("bow", "scrapbook")),
            CreativeSticker("stk_scrap_5", "Sparkle Cluster", CreativeCategory.CUTE_SCRAPBOOK, "✨", "Magic", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFFB39DDB, 0xFFF2ECFC, listOf("sparkles", "scrapbook")),
            CreativeSticker("stk_scrap_6", "Heart Memo", CreativeCategory.CUTE_SCRAPBOOK, "💖", "Note", StickerVectorKind.HEART, StickerBadgeStyle.WASHI_TAG, 0xFFE96894, 0xFFFFDDE9, listOf("heart", "scrapbook")),
            CreativeSticker("stk_scrap_7", "Daisy Sticker", CreativeCategory.CUTE_SCRAPBOOK, "🌼", "Sunny", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFF2BC4F, 0xFFFFF6DA, listOf("flower", "doodle")),
            CreativeSticker("stk_scrap_8", "Flutter Buddy", CreativeCategory.CUTE_SCRAPBOOK, "🦋", "Flutter", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.PASTEL_PILL, 0xFF9C7FD4, 0xFFEDE7F6, listOf("butterfly", "scrapbook")),
            CreativeSticker("stk_scrap_9", "Polaroid Mini", CreativeCategory.CUTE_SCRAPBOOK, "🖼️", "Snap", StickerVectorKind.POLAROID, StickerBadgeStyle.STAMP_SEAL, 0xFFE88AA7, 0xFFFFF4F8, listOf("polaroid", "doodle")),
            CreativeSticker("stk_scrap_10", "Paperclip Charm", CreativeCategory.CUTE_SCRAPBOOK, "📎", "Clipped", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF8C74A8, 0xFFF2ECFB, listOf("doodle", "scrapbook")),
            CreativeSticker("stk_scrap_11", "Sticker Cloud", CreativeCategory.CUTE_SCRAPBOOK, "☁️", "Daydream", StickerVectorKind.CLOUD, StickerBadgeStyle.CLEAN_VECTOR, 0xFF9EC3E6, 0xFFEEF6FD, listOf("cloud", "doodle")),
            CreativeSticker("stk_scrap_12", "Lucky Clover", CreativeCategory.CUTE_SCRAPBOOK, "🍀", "Lucky", StickerVectorKind.FLOWER, StickerBadgeStyle.PASTEL_PILL, 0xFF75A488, 0xFFE3F3E8, listOf("flower", "clover")),

            // MINIMAL (12)
            CreativeSticker("stk_min_1", "Fine Star", CreativeCategory.MINIMAL, "✦", "Pure", StickerVectorKind.STAR, StickerBadgeStyle.CLEAN_VECTOR, 0xFF4A424E, 0xFFF2F0F4, listOf("star", "minimal")),
            CreativeSticker("stk_min_2", "Line Heart", CreativeCategory.MINIMAL, "🤍", "Minimal", StickerVectorKind.HEART, StickerBadgeStyle.PASTEL_PILL, 0xFF6B6270, 0xFFF5F3F7, listOf("heart", "minimal")),
            CreativeSticker("stk_min_3", "Gallery Stamp", CreativeCategory.MINIMAL, "◻️", "FRAME", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF4A424E, 0xFFF8F7FA, listOf("stamp", "minimal")),
            CreativeSticker("stk_min_4", "Botanical Line", CreativeCategory.MINIMAL, "🌾", "Pure", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFF756E66, 0xFFF6F4F0, listOf("flower", "minimal")),
            CreativeSticker("stk_min_5", "Sparkle Dot", CreativeCategory.MINIMAL, "✧", "Clean", StickerVectorKind.SPARKLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFF564F5B, 0xFFF4F2F6, listOf("sparkle", "minimal")),
            CreativeSticker("stk_min_6", "Silk Bow", CreativeCategory.MINIMAL, "🎀", "Quiet", StickerVectorKind.BOW, StickerBadgeStyle.WASHI_TAG, 0xFF7E7584, 0xFFF3F0F5, listOf("bow", "minimal")),
            CreativeSticker("stk_min_7", "Dove Silhouette", CreativeCategory.MINIMAL, "🕊️", "Calm", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF6C6A72, 0xFFF4F3F6, listOf("doodle", "minimal")),
            CreativeSticker("stk_min_8", "Airy Cloud", CreativeCategory.MINIMAL, "☁️", "Airy", StickerVectorKind.CLOUD, StickerBadgeStyle.CLEAN_VECTOR, 0xFF8B8894, 0xFFF6F5F8, listOf("cloud", "minimal")),
            CreativeSticker("stk_min_9", "Washi Cream", CreativeCategory.MINIMAL, "▫️", "Studio", StickerVectorKind.RIBBON, StickerBadgeStyle.WASHI_TAG, 0xFF6E6760, 0xFFF4F0EA, listOf("washi", "minimal")),
            CreativeSticker("stk_min_10", "Mono Butterfly", CreativeCategory.MINIMAL, "🦋", "Light", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.CLEAN_VECTOR, 0xFF5D5663, 0xFFF2F0F4, listOf("butterfly", "minimal")),
            CreativeSticker("stk_min_11", "Pearl Bubble", CreativeCategory.MINIMAL, "🫧", "Clear", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF787D8A, 0xFFF3F5F8, listOf("doodle", "minimal")),
            CreativeSticker("stk_min_12", "Edition Tag", CreativeCategory.MINIMAL, "〰️", "01", StickerVectorKind.POLAROID, StickerBadgeStyle.STAMP_SEAL, 0xFF4A424E, 0xFFF5F4F6, listOf("stamp", "minimal")),

            // FLORAL (12)
            CreativeSticker("stk_flor_1", "Pink Tulip", CreativeCategory.FLORAL, "🌷", "Spring", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE4799C, 0xFFFFE6EF, listOf("flower", "tulip")),
            CreativeSticker("stk_flor_2", "Cherry Petal", CreativeCategory.FLORAL, "🌸", "Sakura", StickerVectorKind.FLOWER, StickerBadgeStyle.SPARKLE_BADGE, 0xFFF094B5, 0xFFFFECF3, listOf("flower", "sakura")),
            CreativeSticker("stk_flor_3", "Wild Sunflower", CreativeCategory.FLORAL, "🌻", "Bloom", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE6AC3A, 0xFFFFF4D6, listOf("flower", "sunflower")),
            CreativeSticker("stk_flor_4", "Hibiscus Glow", CreativeCategory.FLORAL, "🌺", "Garden", StickerVectorKind.FLOWER, StickerBadgeStyle.PASTEL_PILL, 0xFFE36486, 0xFFFFE2EB, listOf("flower", "floral")),
            CreativeSticker("stk_flor_5", "Sage Leaf", CreativeCategory.FLORAL, "🌿", "Botanical", StickerVectorKind.DOODLE, StickerBadgeStyle.WASHI_TAG, 0xFF5C8D6F, 0xFFE1F2E7, listOf("leaf", "floral")),
            CreativeSticker("stk_flor_6", "Garden Butterfly", CreativeCategory.FLORAL, "🦋", "Flutter", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.CLEAN_VECTOR, 0xFF6FA38A, 0xFFE4F4EB, listOf("butterfly", "garden")),
            CreativeSticker("stk_flor_7", "Bouquet Bow", CreativeCategory.FLORAL, "💐", "Bouquet", StickerVectorKind.BOW, StickerBadgeStyle.PASTEL_PILL, 0xFFD97296, 0xFFFDE8F0, listOf("flower", "bow")),
            CreativeSticker("stk_flor_8", "Petal Heart", CreativeCategory.FLORAL, "💖", "Flora", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFF75A488, 0xFFE3F3E8, listOf("heart", "floral")),
            CreativeSticker("stk_flor_9", "Pollen Sparkle", CreativeCategory.FLORAL, "✨", "Dew", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFF7CA88A, 0xFFE8F6ED, listOf("sparkle", "floral")),
            CreativeSticker("stk_flor_10", "Daisy Chain", CreativeCategory.FLORAL, "🌼", "Meadow", StickerVectorKind.FLOWER, StickerBadgeStyle.WASHI_TAG, 0xFFEAB94E, 0xFFFFF5DB, listOf("flower", "daisy")),
            CreativeSticker("stk_flor_11", "Herbarium Seal", CreativeCategory.FLORAL, "🌱", "Sprout", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF4F8264, 0xFFE0F1E6, listOf("stamp", "floral")),
            CreativeSticker("stk_flor_12", "Lotus Calm", CreativeCategory.FLORAL, "🪷", "Lotus", StickerVectorKind.FLOWER, StickerBadgeStyle.PASTEL_PILL, 0xFFE78DAA, 0xFFFFE9F1, listOf("flower", "lotus")),

            // BIRTHDAY (12)
            CreativeSticker("stk_bday_1", "Birthday Cake", CreativeCategory.BIRTHDAY, "🎂", "Make a Wish", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFE8739B, 0xFFFFE5EF, listOf("cake", "birthday")),
            CreativeSticker("stk_bday_2", "Party Balloon", CreativeCategory.BIRTHDAY, "🎈", "Celebrate", StickerVectorKind.DOODLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE86787, 0xFFFFE0E8, listOf("balloon", "birthday")),
            CreativeSticker("stk_bday_3", "Sparkle Confetti", CreativeCategory.BIRTHDAY, "🎉", "Hooray!", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFFE8A44B, 0xFFFFF3D8, listOf("confetti", "sparkle")),
            CreativeSticker("stk_bday_4", "Cupcake Treat", CreativeCategory.BIRTHDAY, "🧁", "Sweet Day", StickerVectorKind.DOODLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE97EA2, 0xFFFFE8F1, listOf("cupcake", "doodle")),
            CreativeSticker("stk_bday_5", "Gift Ribbon", CreativeCategory.BIRTHDAY, "🎁", "Present", StickerVectorKind.BOW, StickerBadgeStyle.WASHI_TAG, 0xFFD96494, 0xFFFFE3EF, listOf("bow", "gift")),
            CreativeSticker("stk_bday_6", "Golden Star", CreativeCategory.BIRTHDAY, "🌟", "Birthday Star", StickerVectorKind.STAR, StickerBadgeStyle.SPARKLE_BADGE, 0xFFE8AF46, 0xFFFFF6DC, listOf("star", "birthday")),
            CreativeSticker("stk_bday_7", "Candle Wish", CreativeCategory.BIRTHDAY, "🕯️", "Wish", StickerVectorKind.SPARKLE, StickerBadgeStyle.PASTEL_PILL, 0xFFE89A4B, 0xFFFFF1DF, listOf("candle", "birthday")),
            CreativeSticker("stk_bday_8", "Crown Tiara", CreativeCategory.BIRTHDAY, "👑", "Queen", StickerVectorKind.STAR, StickerBadgeStyle.STAMP_SEAL, 0xFFDFA542, 0xFFFFF4D8, listOf("crown", "star")),
            CreativeSticker("stk_bday_9", "Party Heart", CreativeCategory.BIRTHDAY, "💖", "Cheers", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFFEA5F8D, 0xFFFFDFEB, listOf("heart", "birthday")),
            CreativeSticker("stk_bday_10", "Festive Bow", CreativeCategory.BIRTHDAY, "🎀", "Party", StickerVectorKind.BOW, StickerBadgeStyle.PASTEL_PILL, 0xFFAA7ED6, 0xFFF2EAFC, listOf("bow", "birthday")),
            CreativeSticker("stk_bday_11", "Sparkle Magic", CreativeCategory.BIRTHDAY, "🪄", "Magic", StickerVectorKind.SPARKLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFB388E0, 0xFFF3ECFC, listOf("sparkle", "magic")),
            CreativeSticker("stk_bday_12", "Flower Bouquet", CreativeCategory.BIRTHDAY, "💐", "For You", StickerVectorKind.FLOWER, StickerBadgeStyle.WASHI_TAG, 0xFFE6779E, 0xFFFFE7F0, listOf("flower", "birthday")),

            // TRAVEL (12)
            CreativeSticker("stk_trv_1", "Boarding Pass", CreativeCategory.TRAVEL, "✈️", "FLIGHT", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF4B7BA8, 0xFFE3EFFA, listOf("flight", "travel", "stamp")),
            CreativeSticker("stk_trv_2", "Fluffy Cloud", CreativeCategory.TRAVEL, "☁️", "Sky High", StickerVectorKind.CLOUD, StickerBadgeStyle.CLEAN_VECTOR, 0xFF7CAAD4, 0xFFEAF4FD, listOf("cloud", "sky")),
            CreativeSticker("stk_trv_3", "Compass Rose", CreativeCategory.TRAVEL, "🧭", "Explore", StickerVectorKind.STAR, StickerBadgeStyle.STAMP_SEAL, 0xFF567D9E, 0xFFE4EFFA, listOf("compass", "travel")),
            CreativeSticker("stk_trv_4", "Travel Polaroid", CreativeCategory.TRAVEL, "📸", "POSTCARD", StickerVectorKind.POLAROID, StickerBadgeStyle.WASHI_TAG, 0xFF4B759E, 0xFFE5F0FA, listOf("polaroid", "travel")),
            CreativeSticker("stk_trv_5", "World Map", CreativeCategory.TRAVEL, "🗺️", "Adventure", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF6B9AC4, 0xFFE6F1FB, listOf("map", "travel")),
            CreativeSticker("stk_trv_6", "Suitcase Tag", CreativeCategory.TRAVEL, "🧳", "Voyage", StickerVectorKind.DOODLE, StickerBadgeStyle.WASHI_TAG, 0xFFA67B56, 0xFFF7EBDD, listOf("luggage", "travel")),
            CreativeSticker("stk_trv_7", "Golden Sun", CreativeCategory.TRAVEL, "☀️", "Sunny Trip", StickerVectorKind.STAR, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE6A73E, 0xFFFFF3D6, listOf("sun", "travel")),
            CreativeSticker("stk_trv_8", "Ocean Wave", CreativeCategory.TRAVEL, "🌊", "Coast", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFF5993BE, 0xFFE2F1FA, listOf("wave", "coast")),
            CreativeSticker("stk_trv_9", "Ticket Stub", CreativeCategory.TRAVEL, "🎫", "ADMIT ONE", StickerVectorKind.RIBBON, StickerBadgeStyle.STAMP_SEAL, 0xFFC58D56, 0xFFF8EBD9, listOf("ticket", "travel")),
            CreativeSticker("stk_trv_10", "Palm Breeze", CreativeCategory.TRAVEL, "🌴", "Escape", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFF5D9473, 0xFFE2F2E7, listOf("palm", "travel")),
            CreativeSticker("stk_trv_11", "Starry Compass", CreativeCategory.TRAVEL, "✨", "Wander", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFF608DB8, 0xFFE6F1FB, listOf("sparkle", "travel")),
            CreativeSticker("stk_trv_12", "Passport Heart", CreativeCategory.TRAVEL, "🩵", "Journey", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFF6CA4CE, 0xFFE6F3FC, listOf("heart", "travel")),

            // CAFE (12)
            CreativeSticker("stk_cafe_1", "Latte Art", CreativeCategory.CAFE, "☕", "Coffee Time", StickerVectorKind.CUP, StickerBadgeStyle.PASTEL_PILL, 0xFF8C6245, 0xFFF7E9DC, listOf("coffee", "cafe")),
            CreativeSticker("stk_cafe_2", "Butter Croissant", CreativeCategory.CAFE, "🥐", "Bakery", StickerVectorKind.DOODLE, StickerBadgeStyle.CLEAN_VECTOR, 0xFFC88B4E, 0xFFF9EBD9, listOf("croissant", "cafe")),
            CreativeSticker("stk_cafe_3", "Strawberry Cake", CreativeCategory.CAFE, "🍰", "Dessert", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFDA6D8D, 0xFFFFE5EE, listOf("cake", "cafe")),
            CreativeSticker("stk_cafe_4", "Teapot Warmth", CreativeCategory.CAFE, "🫖", "Tea Hour", StickerVectorKind.CUP, StickerBadgeStyle.WASHI_TAG, 0xFF7D9E85, 0xFFE6F2E9, listOf("tea", "cafe")),
            CreativeSticker("stk_cafe_5", "Iced Matcha", CreativeCategory.CAFE, "🧋", "Iced", StickerVectorKind.CUP, StickerBadgeStyle.CLEAN_VECTOR, 0xFF6A9471, 0xFFE4F2E6, listOf("matcha", "drink")),
            CreativeSticker("stk_cafe_6", "Bistro Stamp", CreativeCategory.CAFE, "🧁", "PATISSERIE", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFF8C5B42, 0xFFF7EAE0, listOf("stamp", "cafe")),
            CreativeSticker("stk_cafe_7", "Cafe Heart", CreativeCategory.CAFE, "🤎", "Cozy", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFF8D6045, 0xFFF5E6D9, listOf("heart", "cozy")),
            CreativeSticker("stk_cafe_8", "Sugar Sparkle", CreativeCategory.CAFE, "✨", "Morning", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFFC9925B, 0xFFFBF0E3, listOf("sparkle", "cafe")),
            CreativeSticker("stk_cafe_9", "Cafe Tulip", CreativeCategory.CAFE, "🌷", "Table", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFE07D9A, 0xFFFFE8F0, listOf("flower", "cafe")),
            CreativeSticker("stk_cafe_10", "Menu Ribbon", CreativeCategory.CAFE, "🎀", "Special", StickerVectorKind.BOW, StickerBadgeStyle.WASHI_TAG, 0xFFAC7656, 0xFFF6E8DC, listOf("bow", "cafe")),
            CreativeSticker("stk_cafe_11", "Cookie Bite", CreativeCategory.CAFE, "🍪", "Snack", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFA87148, 0xFFF6E7D8, listOf("doodle", "cookie")),
            CreativeSticker("stk_cafe_12", "Warm Cloud", CreativeCategory.CAFE, "☁️", "Steam", StickerVectorKind.CLOUD, StickerBadgeStyle.CLEAN_VECTOR, 0xFFB08C72, 0xFFF7ECE3, listOf("cloud", "steam")),

            // MEMORIES (12)
            CreativeSticker("stk_mem_1", "Lavender Sparkle", CreativeCategory.MEMORIES, "✨", "Keep Forever", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFF8E6AC8, 0xFFF0E8FC, listOf("sparkle", "memories")),
            CreativeSticker("stk_mem_2", "Instant Snapshot", CreativeCategory.MEMORIES, "📸", "Snapshot", StickerVectorKind.POLAROID, StickerBadgeStyle.STAMP_SEAL, 0xFF7D5EB5, 0xFFEDE6FA, listOf("polaroid", "camera")),
            CreativeSticker("stk_mem_3", "Purple Butterfly", CreativeCategory.MEMORIES, "🦋", "Free", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.CLEAN_VECTOR, 0xFF9C7FD4, 0xFFEFE8FC, listOf("butterfly", "lavender")),
            CreativeSticker("stk_mem_4", "Crescent Moon", CreativeCategory.MEMORIES, "🌙", "Twilight", StickerVectorKind.STAR, StickerBadgeStyle.PASTEL_PILL, 0xFF7B5FB0, 0xFFEDE6FA, listOf("moon", "star")),
            CreativeSticker("stk_mem_5", "Lilac Heart", CreativeCategory.MEMORIES, "💜", "Cherish", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFF956FD1, 0xFFF1EBFC, listOf("heart", "purple")),
            CreativeSticker("stk_mem_6", "Memory Star", CreativeCategory.MEMORIES, "🌟", "Golden", StickerVectorKind.STAR, StickerBadgeStyle.SPARKLE_BADGE, 0xFFD9A64C, 0xFFFFF4DC, listOf("star", "memories")),
            CreativeSticker("stk_mem_7", "Washi Note", CreativeCategory.MEMORIES, "📝", "Diary", StickerVectorKind.RIBBON, StickerBadgeStyle.WASHI_TAG, 0xFF8768BF, 0xFFEEE8FA, listOf("washi", "diary")),
            CreativeSticker("stk_mem_8", "Lilac Blossom", CreativeCategory.MEMORIES, "🪻", "Hyacinth", StickerVectorKind.FLOWER, StickerBadgeStyle.PASTEL_PILL, 0xFF8B6EC9, 0xFFEEE8FB, listOf("flower", "lilac")),
            CreativeSticker("stk_mem_9", "Dreamy Cloud", CreativeCategory.MEMORIES, "☁️", "Dream", StickerVectorKind.CLOUD, StickerBadgeStyle.CLEAN_VECTOR, 0xFF9F8AD4, 0xFFF1ECFC, listOf("cloud", "dream")),
            CreativeSticker("stk_mem_10", "Keepsake Bow", CreativeCategory.MEMORIES, "🎀", "Tied", StickerVectorKind.BOW, StickerBadgeStyle.PASTEL_PILL, 0xFFA27BD6, 0xFFF2ECFC, listOf("bow", "keepsake")),
            CreativeSticker("stk_mem_11", "Time Capsule", CreativeCategory.MEMORIES, "⏳", "Timeless", StickerVectorKind.DOODLE, StickerBadgeStyle.STAMP_SEAL, 0xFF6F539E, 0xFFEDE6FA, listOf("doodle", "timeless")),
            CreativeSticker("stk_mem_12", "Shooting Star", CreativeCategory.MEMORIES, "🌠", "Wish", StickerVectorKind.STAR, StickerBadgeStyle.CLEAN_VECTOR, 0xFF8663C2, 0xFFEEE8FA, listOf("star", "wish")),

            // ROMANTIC (12)
            CreativeSticker("stk_rom_1", "Love Letter", CreativeCategory.ROMANTIC, "💌", "XOXO", StickerVectorKind.STAMP, StickerBadgeStyle.PASTEL_PILL, 0xFFD9587E, 0xFFFFE2EC, listOf("letter", "romantic", "love")),
            CreativeSticker("stk_rom_2", "Crimson Rose", CreativeCategory.ROMANTIC, "🌹", "Always", StickerVectorKind.FLOWER, StickerBadgeStyle.CLEAN_VECTOR, 0xFFC73E63, 0xFFFFDFE8, listOf("rose", "flower", "romantic")),
            CreativeSticker("stk_rom_3", "Locket Heart", CreativeCategory.ROMANTIC, "💝", "Forever", StickerVectorKind.HEART, StickerBadgeStyle.SPARKLE_BADGE, 0xFFD95D84, 0xFFFFE3ED, listOf("heart", "locket")),
            CreativeSticker("stk_rom_4", "Cupid Sparkle", CreativeCategory.ROMANTIC, "💘", "Sweetheart", StickerVectorKind.HEART, StickerBadgeStyle.CLEAN_VECTOR, 0xFFDF527D, 0xFFFFDFEA, listOf("heart", "cupid")),
            CreativeSticker("stk_rom_5", "Velvet Bow", CreativeCategory.ROMANTIC, "🎀", "Yours", StickerVectorKind.BOW, StickerBadgeStyle.WASHI_TAG, 0xFFC74A72, 0xFFFFE0EA, listOf("bow", "romantic")),
            CreativeSticker("stk_rom_6", "Peace Dove", CreativeCategory.ROMANTIC, "🕊️", "Tender", StickerVectorKind.DOODLE, StickerBadgeStyle.PASTEL_PILL, 0xFFB86B84, 0xFFFBE9EF, listOf("dove", "romantic")),
            CreativeSticker("stk_rom_7", "Promise Ring", CreativeCategory.ROMANTIC, "💍", "Promise", StickerVectorKind.STAR, StickerBadgeStyle.STAMP_SEAL, 0xFFCFA268, 0xFFFDF0DE, listOf("ring", "promise")),
            CreativeSticker("stk_rom_8", "Soul Butterfly", CreativeCategory.ROMANTIC, "🦋", "Flutter", StickerVectorKind.BUTTERFLY, StickerBadgeStyle.CLEAN_VECTOR, 0xFFD96B8A, 0xFFFFE5EE, listOf("butterfly", "romantic")),
            CreativeSticker("stk_rom_9", "Sparkling Kiss", CreativeCategory.ROMANTIC, "✨", "Adore", StickerVectorKind.SPARKLE, StickerBadgeStyle.SPARKLE_BADGE, 0xFFE07596, 0xFFFFE8F0, listOf("sparkle", "romantic")),
            CreativeSticker("stk_rom_10", "Heart Beat", CreativeCategory.ROMANTIC, "💓", "Warmth", StickerVectorKind.HEART, StickerBadgeStyle.PASTEL_PILL, 0xFFDA527C, 0xFFFFDFEA, listOf("heart", "love")),
            CreativeSticker("stk_rom_11", "Peony Kiss", CreativeCategory.ROMANTIC, "🌺", "Darling", StickerVectorKind.FLOWER, StickerBadgeStyle.WASHI_TAG, 0xFFD45B82, 0xFFFFE2EC, listOf("flower", "romantic")),
            CreativeSticker("stk_rom_12", "Sonnet Stamp", CreativeCategory.ROMANTIC, "🦢", "Grace", StickerVectorKind.STAMP, StickerBadgeStyle.STAMP_SEAL, 0xFFB04365, 0xFFFFE6EE, listOf("swan", "stamp"))
        )
    }

    fun templateById(id: String?): AestheticTemplate? =
        id?.let { key -> templates.firstOrNull { it.id == key } }

    fun stickerById(id: String?): CreativeSticker? =
        id?.let { key -> stickers.firstOrNull { it.id == key } }
}
