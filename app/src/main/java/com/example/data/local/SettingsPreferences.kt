package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.ThemeMode
import com.example.data.model.ThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pose_memories_prefs")

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themePalette: ThemePalette = ThemePalette.BLUSH_PINK,
    val defaultTimerSeconds: Int = 0,
    val photoPoseOutlineEnabled: Boolean = true,
    val videoPoseGuideEnabled: Boolean = true,
    val outlineScale: Float = 1.0f,
    val outlineVerticalOffset: Float = 0f,
    val outlineAlpha: Float = 0.88f,
    val savePhotoWithGuide: Boolean = false,
    val saveVideoWithGuide: Boolean = false,
    val favoriteTemplateIds: Set<String> = emptySet(),
    val favoriteStickerIds: Set<String> = emptySet()
)

class SettingsPreferences(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PALETTE = stringPreferencesKey("theme_palette")
        val DEFAULT_TIMER = intPreferencesKey("default_timer")
        val PHOTO_OUTLINE_ENABLED = booleanPreferencesKey("photo_outline_enabled")
        val VIDEO_POSE_GUIDE_ENABLED = booleanPreferencesKey("video_pose_guide_enabled")
        val OUTLINE_SCALE = floatPreferencesKey("outline_scale")
        val OUTLINE_OFFSET = floatPreferencesKey("outline_offset")
        val OUTLINE_ALPHA = floatPreferencesKey("outline_alpha")
        val SAVE_PHOTO_WITH_GUIDE = booleanPreferencesKey("save_photo_with_guide")
        val SAVE_VIDEO_WITH_GUIDE = booleanPreferencesKey("save_video_with_guide")
        val FAVORITE_TEMPLATE_IDS = stringSetPreferencesKey("favorite_template_ids")
        val FAVORITE_STICKER_IDS = stringSetPreferencesKey("favorite_sticker_ids")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val mode = prefs[Keys.THEME_MODE]?.let {
            runCatching { ThemeMode.valueOf(it) }.getOrNull()
        } ?: ThemeMode.SYSTEM
        val palette = prefs[Keys.THEME_PALETTE]?.let {
            runCatching { ThemePalette.valueOf(it) }.getOrNull()
        } ?: ThemePalette.BLUSH_PINK
        UserSettings(
            themeMode = mode,
            themePalette = palette,
            defaultTimerSeconds = prefs[Keys.DEFAULT_TIMER] ?: 0,
            photoPoseOutlineEnabled = prefs[Keys.PHOTO_OUTLINE_ENABLED] ?: true,
            videoPoseGuideEnabled = prefs[Keys.VIDEO_POSE_GUIDE_ENABLED] ?: true,
            outlineScale = prefs[Keys.OUTLINE_SCALE] ?: 1.0f,
            outlineVerticalOffset = prefs[Keys.OUTLINE_OFFSET] ?: 0f,
            outlineAlpha = prefs[Keys.OUTLINE_ALPHA] ?: 0.88f,
            savePhotoWithGuide = prefs[Keys.SAVE_PHOTO_WITH_GUIDE] ?: false,
            saveVideoWithGuide = prefs[Keys.SAVE_VIDEO_WITH_GUIDE] ?: false,
            favoriteTemplateIds = prefs[Keys.FAVORITE_TEMPLATE_IDS] ?: emptySet(),
            favoriteStickerIds = prefs[Keys.FAVORITE_STICKER_IDS] ?: emptySet()
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setThemePalette(palette: ThemePalette) {
        context.dataStore.edit { it[Keys.THEME_PALETTE] = palette.name }
    }

    suspend fun setDefaultTimerSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_TIMER] = seconds }
    }

    suspend fun setPhotoPoseOutlineEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PHOTO_OUTLINE_ENABLED] = enabled }
    }

    suspend fun setVideoPoseGuideEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.VIDEO_POSE_GUIDE_ENABLED] = enabled }
    }

    suspend fun setOutlineScale(scale: Float) {
        context.dataStore.edit { it[Keys.OUTLINE_SCALE] = scale }
    }

    suspend fun setOutlineVerticalOffset(offset: Float) {
        context.dataStore.edit { it[Keys.OUTLINE_OFFSET] = offset }
    }

    suspend fun setOutlineAlpha(alpha: Float) {
        context.dataStore.edit { it[Keys.OUTLINE_ALPHA] = alpha }
    }

    suspend fun resetOutlineStyle() {
        context.dataStore.edit {
            it[Keys.OUTLINE_SCALE] = 1.0f
            it[Keys.OUTLINE_OFFSET] = 0f
            it[Keys.OUTLINE_ALPHA] = 0.88f
        }
    }

    suspend fun setSavePhotoWithGuide(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAVE_PHOTO_WITH_GUIDE] = enabled }
    }

    suspend fun setSaveVideoWithGuide(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SAVE_VIDEO_WITH_GUIDE] = enabled }
    }

    suspend fun toggleFavoriteTemplate(templateId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_TEMPLATE_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(templateId)) current.remove(templateId)
            prefs[Keys.FAVORITE_TEMPLATE_IDS] = current
        }
    }

    suspend fun toggleFavoriteSticker(stickerId: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_STICKER_IDS]?.toMutableSet() ?: mutableSetOf()
            if (!current.add(stickerId)) current.remove(stickerId)
            prefs[Keys.FAVORITE_STICKER_IDS] = current
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
