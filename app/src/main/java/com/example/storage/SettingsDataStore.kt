package com.example.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settings.GameSettings
import com.example.settings.GraphicsQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "anime_arena_settings")

class SettingsDataStore(private val context: Context) {

  private object Keys {
    val TARGET_FPS = intPreferencesKey("target_fps")
    val QUALITY = stringPreferencesKey("graphics_quality")
    val RENDER_SCALE = floatPreferencesKey("render_scale")
    val SHADOWS = booleanPreferencesKey("shadows_enabled")
    val MASTER_VOL = floatPreferencesKey("master_volume")
    val MUSIC_VOL = floatPreferencesKey("music_volume")
    val SFX_VOL = floatPreferencesKey("sfx_volume")
    val VOICE_VOL = floatPreferencesKey("voice_volume")
    val MUTED = booleanPreferencesKey("is_muted")
  }

  val settingsFlow: Flow<GameSettings> =
    context.settingsDataStore.data.map { prefs ->
      val qualityName = prefs[Keys.QUALITY] ?: GraphicsQuality.MEDIUM.name
      val quality = runCatching { GraphicsQuality.valueOf(qualityName) }.getOrDefault(GraphicsQuality.MEDIUM)
      GameSettings(
        targetFps = prefs[Keys.TARGET_FPS] ?: 30,
        graphicsQuality = quality,
        renderScale = (prefs[Keys.RENDER_SCALE] ?: quality.defaultRenderScale).coerceIn(0.6f, 1.0f),
        shadowsEnabled = prefs[Keys.SHADOWS] ?: true,
        masterVolume = (prefs[Keys.MASTER_VOL] ?: 0.85f).coerceIn(0f, 1f),
        musicVolume = (prefs[Keys.MUSIC_VOL] ?: 0.70f).coerceIn(0f, 1f),
        sfxVolume = (prefs[Keys.SFX_VOL] ?: 0.90f).coerceIn(0f, 1f),
        voiceVolume = (prefs[Keys.VOICE_VOL] ?: 0.95f).coerceIn(0f, 1f),
        isMuted = prefs[Keys.MUTED] ?: false,
      )
    }

  suspend fun updateSettings(transform: (GameSettings) -> GameSettings) {
    context.settingsDataStore.edit { prefs ->
      val currentQuality =
        runCatching { GraphicsQuality.valueOf(prefs[Keys.QUALITY] ?: GraphicsQuality.MEDIUM.name) }
          .getOrDefault(GraphicsQuality.MEDIUM)
      val current =
        GameSettings(
          targetFps = prefs[Keys.TARGET_FPS] ?: 30,
          graphicsQuality = currentQuality,
          renderScale = prefs[Keys.RENDER_SCALE] ?: currentQuality.defaultRenderScale,
          shadowsEnabled = prefs[Keys.SHADOWS] ?: true,
          masterVolume = prefs[Keys.MASTER_VOL] ?: 0.85f,
          musicVolume = prefs[Keys.MUSIC_VOL] ?: 0.70f,
          sfxVolume = prefs[Keys.SFX_VOL] ?: 0.90f,
          voiceVolume = prefs[Keys.VOICE_VOL] ?: 0.95f,
          isMuted = prefs[Keys.MUTED] ?: false,
        )
      val updated = transform(current)
      prefs[Keys.TARGET_FPS] = if (updated.targetFps >= 60) 60 else 30
      prefs[Keys.QUALITY] = updated.graphicsQuality.name
      prefs[Keys.RENDER_SCALE] = updated.renderScale.coerceIn(0.6f, 1.0f)
      prefs[Keys.SHADOWS] = updated.shadowsEnabled
      prefs[Keys.MASTER_VOL] = updated.masterVolume.coerceIn(0f, 1f)
      prefs[Keys.MUSIC_VOL] = updated.musicVolume.coerceIn(0f, 1f)
      prefs[Keys.SFX_VOL] = updated.sfxVolume.coerceIn(0f, 1f)
      prefs[Keys.VOICE_VOL] = updated.voiceVolume.coerceIn(0f, 1f)
      prefs[Keys.MUTED] = updated.isMuted
    }
  }
}
