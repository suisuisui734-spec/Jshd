package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.app.AnimeArenaApp
import com.example.assets.AssetPaths
import com.example.audio.BgmTrack
import com.example.settings.GameSettings
import com.example.settings.GraphicsQuality
import com.example.storage.MatchRecordEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ArenaViewModel(application: Application) : AndroidViewModel(application) {
  private val app = application as AnimeArenaApp

  val settings: StateFlow<GameSettings> =
    app.settingsStore.settingsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.Eagerly,
      initialValue = GameSettings(),
    )

  val recentMatches: StateFlow<List<MatchRecordEntity>> =
    app.matchRepository.recentMatches.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val totalWins: StateFlow<Int> =
    app.matchRepository.totalWins.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0,
    )

  val bestCombo: StateFlow<Int> =
    app.matchRepository.bestCombo.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0,
    )

  val audioEngine get() = app.audioEngine
  val matchRepository get() = app.matchRepository

  fun playMenuMusic() {
    app.audioEngine.playBgm(BgmTrack.MENU, loop = true)
  }

  fun playUiClick() {
    app.audioEngine.playSfx(AssetPaths.SFX_UI_CLICK)
  }

  fun setTargetFps(fps: Int) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(targetFps = fps) } }
  }

  fun setGraphicsQuality(quality: GraphicsQuality) {
    viewModelScope.launch {
      app.settingsStore.updateSettings {
        it.copy(
          graphicsQuality = quality,
          renderScale = quality.defaultRenderScale,
          shadowsEnabled = quality != GraphicsQuality.LOW,
        )
      }
    }
  }

  fun setRenderScale(scale: Float) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(renderScale = scale) } }
  }

  fun setShadowsEnabled(enabled: Boolean) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(shadowsEnabled = enabled) } }
  }

  fun setMasterVolume(v: Float) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(masterVolume = v) } }
  }

  fun setMusicVolume(v: Float) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(musicVolume = v) } }
  }

  fun setSfxVolume(v: Float) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(sfxVolume = v) } }
  }

  fun setVoiceVolume(v: Float) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(voiceVolume = v) } }
  }

  fun setMuted(muted: Boolean) {
    viewModelScope.launch { app.settingsStore.updateSettings { it.copy(isMuted = muted) } }
  }

  fun clearMatchHistory() {
    viewModelScope.launch { app.matchRepository.clearHistory() }
  }
}
