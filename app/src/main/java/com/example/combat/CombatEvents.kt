package com.example.combat

import com.example.characters.AttackMoveType
import com.example.characters.ElementType
import com.example.core.Vec3

sealed interface CombatFeedbackEvent {
  data class HitLanded(
    val attackerIsPlayer: Boolean,
    val moveType: AttackMoveType,
    val element: ElementType,
    val damage: Int,
    val isBlocked: Boolean,
    val isGuardBreak: Boolean,
    val isCritical: Boolean,
    val isUltimate: Boolean,
    val impactPoint: Vec3,
    val comboCount: Int,
  ) : CombatFeedbackEvent

  data class MoveStarted(
    val attackerIsPlayer: Boolean,
    val moveType: AttackMoveType,
    val element: ElementType,
    val origin: Vec3,
    val yawRad: Float,
  ) : CombatFeedbackEvent

  data class ActionTriggered(
    val isPlayer: Boolean,
    val kind: ActionKind,
    val position: Vec3,
    val element: ElementType,
  ) : CombatFeedbackEvent
}

enum class ActionKind {
  JUMP,
  DODGE,
  GUARD_START,
  FOOTSTEP,
  LAND,
}
