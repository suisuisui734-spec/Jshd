package com.example.characters

import com.example.animation.AnimationState
import com.example.animation.AnimationStateMachine
import com.example.core.Vec3

enum class FighterActionPhase {
  NEUTRAL,
  ATTACK_STARTUP,
  ATTACK_ACTIVE,
  ATTACK_RECOVERY,
  HITSTUN,
  BLOCKSTUN,
  GUARDING,
  DODGING,
  ULTIMATE_CINEMATIC,
  DEFEATED,
  VICTORIOUS,
}

class FighterState(
  val spec: FighterSpec,
  val isPlayer: Boolean,
  initialPos: Vec3,
  initialYawRad: Float,
) {
  var position: Vec3 = initialPos
  var velocity: Vec3 = Vec3.ZERO
  var yawRad: Float = initialYawRad
  var isGrounded: Boolean = true

  var hp: Int = spec.maxHp
  var energy: Float = 25f // Start with partial energy for snappy opening combat
  var guardGauge: Float = 100f

  var actionPhase: FighterActionPhase = FighterActionPhase.NEUTRAL
  var activeMove: MoveSpec? = null
  var moveElapsedSec: Float = 0f
  var moveHasConnected: Boolean = false
  var queuedComboMove: AttackMoveType? = null

  var stunTimerSec: Float = 0f
  var dodgeTimerSec: Float = 0f
  var invulnerableTimerSec: Float = 0f
  var footstepTimerSec: Float = 0f

  var comboCounter: Int = 0
  var comboResetTimerSec: Float = 0f
  var maxComboAchieved: Int = 0
  var totalDamageDealt: Int = 0
  var ultimatesLanded: Int = 0

  val cooldowns: MutableMap<AttackMoveType, Float> = mutableMapOf()
  val animMachine: AnimationStateMachine = AnimationStateMachine(AnimationState.IDLE)

  val isAlive: Boolean get() = hp > 0

  fun canStartMove(move: MoveSpec, allowCancel: Boolean): Boolean {
    if (!isAlive) return false
    if ((cooldowns[move.type] ?: 0f) > 0f) return false
    if (energy < move.energyCost) return false
    return when (actionPhase) {
      FighterActionPhase.NEUTRAL -> true
      FighterActionPhase.GUARDING -> move.type == AttackMoveType.ULTIMATE
      FighterActionPhase.ATTACK_ACTIVE,
      FighterActionPhase.ATTACK_RECOVERY -> {
        if (!allowCancel) return false
        val current = activeMove ?: return false
        moveHasConnected &&
          moveElapsedSec >= current.cancelWindowStartSec &&
          current.canCancelInto.contains(move.type)
      }
      else -> false
    }
  }

  fun resetForRound(pos: Vec3, yaw: Float, startEnergy: Float = 25f) {
    position = pos
    velocity = Vec3.ZERO
    yawRad = yaw
    isGrounded = true
    hp = spec.maxHp
    energy = startEnergy
    guardGauge = 100f
    actionPhase = FighterActionPhase.NEUTRAL
    activeMove = null
    moveElapsedSec = 0f
    moveHasConnected = false
    queuedComboMove = null
    stunTimerSec = 0f
    dodgeTimerSec = 0f
    invulnerableTimerSec = 0f
    comboCounter = 0
    comboResetTimerSec = 0f
    cooldowns.clear()
    animMachine.transitionTo(AnimationState.IDLE, forceRestart = true)
  }
}
