package com.example.audio

import com.example.assets.AssetPaths
import com.example.core.FighterId

enum class VoiceCue(val key: String, val priority: Int, val minGapSec: Float) {
  INTRO("intro", 3, 2.0f),
  IDLE("idle", 0, 5.0f),
  LIGHT("light", 1, 0.38f),
  HEAVY("heavy", 2, 0.45f),
  SPECIAL("special", 3, 0.70f),
  ULT_PREP("ult_prep", 4, 0.50f),
  ULT_SHOUT("ult_shout", 5, 0.40f),
  HIT("hit", 2, 0.42f),
  VICTORY("victory", 5, 2.0f),
  DEFEAT("defeat", 5, 2.0f),
}

class VoiceSystem(private val audioEngine: AudioEngine) {
  private val lastPlayedTimeSec = mutableMapOf<FighterId, Float>()
  private val lastPriority = mutableMapOf<FighterId, Int>()
  private var elapsedSec = 0f

  fun update(dt: Float) {
    elapsedSec += dt
  }

  fun trigger(fighterId: FighterId, cue: VoiceCue) {
    val lastTime = lastPlayedTimeSec[fighterId] ?: -10f
    val prevPrio = lastPriority[fighterId] ?: 0
    if (elapsedSec - lastTime < cue.minGapSec && cue.priority <= prevPrio) {
      return
    }
    lastPlayedTimeSec[fighterId] = elapsedSec
    lastPriority[fighterId] = cue.priority
    audioEngine.playVoice(AssetPaths.voiceClip(fighterId, cue.key), interruptPrevious = cue.priority >= 3)
  }

  fun reset() {
    lastPlayedTimeSec.clear()
    lastPriority.clear()
    elapsedSec = 0f
  }
}
