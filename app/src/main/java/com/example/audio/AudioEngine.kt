package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.assets.AssetPaths
import com.example.core.FighterId
import com.example.settings.GameSettings

enum class BgmTrack(val assetPath: String) {
  MENU(AssetPaths.BGM_MENU),
  BATTLE_NORMAL(AssetPaths.BGM_BATTLE),
  BATTLE_INTENSE(AssetPaths.BGM_BATTLE_INTENSE),
  ULTIMATE(AssetPaths.BGM_ULTIMATE),
  VICTORY(AssetPaths.BGM_VICTORY),
  DEFEAT(AssetPaths.BGM_DEFEAT),
}

class AudioEngine(private val context: Context) {

  private var soundPool: SoundPool? = null
  private val loadedSounds = mutableMapOf<String, Int>()
  private var activeVoiceStreamId: Int = 0
  private var bgmPlayer: MediaPlayer? = null
  private var currentBgm: BgmTrack? = null
  private var settings: GameSettings = GameSettings()
  private var isPaused: Boolean = false

  fun initialize() {
    if (soundPool != null) return
    val attrs =
      AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_GAME)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    soundPool = SoundPool.Builder().setMaxStreams(10).setAudioAttributes(attrs).build()
    preloadAllAssets()
  }

  private fun preloadAllAssets() {
    val paths =
      listOf(
        AssetPaths.SFX_LIGHT_HIT,
        AssetPaths.SFX_HEAVY_IMPACT,
        AssetPaths.SFX_BLOCK,
        AssetPaths.SFX_DODGE,
        AssetPaths.SFX_FOOTSTEP,
        AssetPaths.SFX_JUMP,
        AssetPaths.SFX_ENERGY_CHARGE,
        AssetPaths.SFX_ENERGY_RELEASE,
        AssetPaths.SFX_LIGHTNING,
        AssetPaths.SFX_FIRE,
        AssetPaths.SFX_ULTIMATE_IMPACT,
        AssetPaths.SFX_CINEMATIC_WHOOSH,
        AssetPaths.SFX_UI_CLICK,
        AssetPaths.SFX_VICTORY,
        AssetPaths.SFX_DEFEAT,
      ) +
        listOf(FighterId.RAIDEN, FighterId.HOMURA).flatMap { fid ->
          listOf("intro", "idle", "light", "heavy", "special", "ult_prep", "ult_shout", "hit", "victory", "defeat")
            .map { cue -> AssetPaths.voiceClip(fid, cue) }
        }

    val sp = soundPool ?: return
    for (p in paths) {
      runCatching {
        context.assets.openFd(p).use { afd ->
          loadedSounds[p] = sp.load(afd, 1)
        }
      }
    }
  }

  fun updateSettings(newSettings: GameSettings) {
    settings = newSettings
    applyBgmVolume()
  }

  fun playSfx(assetPath: String, rate: Float = 1f, volumeScale: Float = 1f) {
    val sp = soundPool ?: return
    if (settings.isMuted) return
    val soundId = loadedSounds[assetPath] ?: return
    val vol = (settings.masterVolume * settings.sfxVolume * volumeScale).coerceIn(0f, 1f)
    if (vol <= 0.005f) return
    sp.play(soundId, vol, vol, 1, 0, rate.coerceIn(0.8f, 1.3f))
  }

  fun playVoice(assetPath: String, interruptPrevious: Boolean = true) {
    val sp = soundPool ?: return
    if (settings.isMuted) return
    val soundId = loadedSounds[assetPath] ?: return
    val vol = (settings.masterVolume * settings.voiceVolume).coerceIn(0f, 1f)
    if (vol <= 0.005f) return
    if (interruptPrevious && activeVoiceStreamId != 0) {
      sp.stop(activeVoiceStreamId)
    }
    activeVoiceStreamId = sp.play(soundId, vol, vol, 2, 0, 1f)
  }

  fun playBgm(track: BgmTrack, loop: Boolean = true) {
    if (currentBgm == track && bgmPlayer != null) {
      if (!isPaused && bgmPlayer?.isPlaying == false) {
        runCatching { bgmPlayer?.start() }
      }
      return
    }
    stopBgm()
    currentBgm = track
    runCatching {
      context.assets.openFd(track.assetPath).use { afd ->
        val mp = MediaPlayer()
        mp.setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        mp.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        mp.isLooping = loop
        mp.prepare()
        bgmPlayer = mp
        applyBgmVolume()
        if (!isPaused) {
          mp.start()
        }
      }
    }
  }

  fun stopBgm() {
    runCatching {
      bgmPlayer?.stop()
      bgmPlayer?.release()
    }
    bgmPlayer = null
    currentBgm = null
  }

  fun onPause() {
    isPaused = true
    runCatching {
      if (bgmPlayer?.isPlaying == true) bgmPlayer?.pause()
      soundPool?.autoPause()
    }
  }

  fun onResume() {
    isPaused = false
    runCatching {
      soundPool?.autoResume()
      applyBgmVolume()
      if (bgmPlayer != null && bgmPlayer?.isPlaying == false) {
        bgmPlayer?.start()
      }
    }
  }

  private fun applyBgmVolume() {
    val vol =
      if (settings.isMuted) 0f
      else (settings.masterVolume * settings.musicVolume * 0.78f).coerceIn(0f, 1f)
    runCatching { bgmPlayer?.setVolume(vol, vol) }
  }

  fun release() {
    stopBgm()
    soundPool?.release()
    soundPool = null
    loadedSounds.clear()
  }
}
