package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePalette

private val BlushPinkLightScheme = lightColorScheme(
    primary = BlushPink,
    onPrimary = Color.White,
    primaryContainer = SoftRose,
    onPrimaryContainer = DeepRose,
    secondary = PastelLavender,
    onSecondary = Color.White,
    secondaryContainer = SoftLavender,
    onSecondaryContainer = DeepPlum,
    tertiary = VintageGold,
    onTertiary = Color.White,
    tertiaryContainer = ButterCream,
    onTertiaryContainer = VintageSepiaInk,
    background = WarmCream,
    onBackground = SoftCharcoal,
    surface = PetalWhite,
    onSurface = SoftCharcoal,
    surfaceVariant = CreamCard,
    onSurfaceVariant = MutedMauve,
    outline = RoseOutline,
    outlineVariant = SoftRose
)

private val LavenderLightScheme = lightColorScheme(
    primary = Color(0xFF9C7FD4),
    onPrimary = Color.White,
    primaryContainer = SoftLavender,
    onPrimaryContainer = DeepPlum,
    secondary = BlushPink,
    onSecondary = Color.White,
    secondaryContainer = SoftRose,
    onSecondaryContainer = DeepRose,
    tertiary = GardenLeaf,
    onTertiary = Color.White,
    tertiaryContainer = MintSage,
    onTertiaryContainer = Color(0xFF284834),
    background = Color(0xFFFCF9FF),
    onBackground = SoftCharcoal,
    surface = PetalWhite,
    onSurface = SoftCharcoal,
    surfaceVariant = Color(0xFFF6F1FC),
    onSurfaceVariant = MutedMauve,
    outline = Color(0xFFE0D5F0),
    outlineVariant = SoftLavender
)

private val WarmCreamLightScheme = lightColorScheme(
    primary = Color(0xFFD48394),
    onPrimary = Color.White,
    primaryContainer = ButterCream,
    onPrimaryContainer = VintageSepiaInk,
    secondary = Color(0xFFA792C9),
    onSecondary = Color.White,
    secondaryContainer = SoftLavender,
    onSecondaryContainer = DeepPlum,
    tertiary = VintageGold,
    onTertiary = Color.White,
    tertiaryContainer = SoftRose,
    onTertiaryContainer = DeepRose,
    background = Color(0xFFFDF7F0),
    onBackground = VintageSepiaInk,
    surface = WarmCream,
    onSurface = VintageSepiaInk,
    surfaceVariant = Color(0xFFF8EDE2),
    onSurfaceVariant = Color(0xFF7D6759),
    outline = Color(0xFFEAD7C7),
    outlineVariant = ButterCream
)

private val DarkPastelScheme = darkColorScheme(
    primary = DarkBlushPink,
    onPrimary = Color(0xFF421827),
    primaryContainer = Color(0xFF5D293B),
    onPrimaryContainer = SoftRose,
    secondary = DarkLavender,
    onSecondary = Color(0xFF2E1F47),
    secondaryContainer = Color(0xFF463566),
    onSecondaryContainer = SoftLavender,
    tertiary = DarkCreamAccent,
    onTertiary = Color(0xFF452C1B),
    tertiaryContainer = Color(0xFF5A3E2D),
    onTertiaryContainer = ButterCream,
    background = DarkPlumBackground,
    onBackground = DarkOnSurface,
    surface = DarkPlumSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkPlumCard,
    onSurfaceVariant = DarkMutedText,
    outline = DarkOutline,
    outlineVariant = Color(0xFF3C2D36)
)

val PoseMemoriesShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(34.dp)
)

@Composable
fun PoseMemoriesTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themePalette: ThemePalette = ThemePalette.BLUSH_PINK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = if (isDark) {
        DarkPastelScheme
    } else {
        when (themePalette) {
            ThemePalette.BLUSH_PINK -> BlushPinkLightScheme
            ThemePalette.SOFT_LAVENDER -> LavenderLightScheme
            ThemePalette.WARM_CREAM -> WarmCreamLightScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PoseMemoriesTypography,
        shapes = PoseMemoriesShapes,
        content = content
    )
}
