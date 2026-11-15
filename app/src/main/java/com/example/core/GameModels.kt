package com.example.core

import kotlinx.serialization.Serializable

@Serializable
enum class FighterId {
  RAIDEN,
  HOMURA,
}

@Serializable
enum class GameMode {
  VERSUS,
  TRAINING,
  ARCADE,
}

@Serializable
enum class AiDifficulty(val label: String, val reactionDelay: Float, val aggression: Float, val guardRate: Float) {
  NOVICE("Novice", 0.42f, 0.45f, 0.25f),
  WARRIOR("Warrior", 0.22f, 0.72f, 0.55f),
  MASTER("Master", 0.11f, 0.90f, 0.78f),
}

enum class MatchPhase {
  INTRO,
  FIGHTING,
  ULTIMATE_CINEMATIC,
  PAUSED,
  VICTORY,
  DEFEAT,
  DRAW,
}

data class DamagePopup(
  var id: Long = 0L,
  var amount: Int = 0,
  var isBlocked: Boolean = false,
  var isCritical: Boolean = false,
  var isUltimate: Boolean = false,
  var worldX: Float = 0f,
  var worldY: Float = 0f,
  var worldZ: Float = 0f,
  var ttl: Float = 0f,
)
