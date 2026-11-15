package com.example.combat

import com.example.animation.AnimationState
import com.example.characters.AttackMoveType
import com.example.characters.FighterActionPhase
import com.example.characters.FighterState
import com.example.core.Math3D
import com.example.core.Vec3

enum class UltimateStage {
  INACTIVE,
  PREP_CLOSEUP,
  ENERGY_GATHER,
  STRIKE_DASH,
  IMPACT_SLOWMO,
  RECOVERY_BLEND,
}

data class UltimateSnapshot(
  val isActive: Boolean = false,
  val stage: UltimateStage = UltimateStage.INACTIVE,
  val attackerIsPlayer: Boolean = true,
  val title: String = "",
  val canSkip: Boolean = false,
  val cinematicProgress: Float = 0f,
  val timeScale: Float = 1f,
  val flashAlpha: Float = 0f,
)

/**
 * Orchestrates the 12-step Cinematic Ultimate Attack sequence with synchronized
 * animations, close-up & tracking camera phases, slow-motion impact, dynamic lighting,
 * particle surges, voice cues, and optional Skip after activation.
 */
class UltimateCinematicController {
  private var stage: UltimateStage = UltimateStage.INACTIVE
  private var elapsedSec: Float = 0f
  private var attacker: FighterState? = null
  private var defender: FighterState? = null
  private var impactApplied: Boolean = false
  private var shoutTriggered: Boolean = false

  var onStageAudioCue: ((FighterState, UltimateStage) -> Unit)? = null
  var onSpawnUltimateVfx: ((FighterState, FighterState, UltimateStage) -> Unit)? = null

  val isActive: Boolean get() = stage != UltimateStage.INACTIVE

  fun tryActivate(
    user: FighterState,
    target: FighterState,
    combatSystem: CombatSystem,
    events: MutableList<CombatFeedbackEvent>,
  ): Boolean {
    if (isActive) return false
    val ultMove = user.spec.moves[AttackMoveType.ULTIMATE] ?: return false
    if (!user.canStartMove(ultMove, allowCancel = true)) return false

    // Step 1-2: Consume energy, lock actors, begin UltimatePrep animation
    user.energy = (user.energy - ultMove.energyCost).coerceAtLeast(0f)
    user.cooldowns[AttackMoveType.ULTIMATE] = ultMove.cooldownSec
    user.actionPhase = FighterActionPhase.ULTIMATE_CINEMATIC
    user.activeMove = ultMove
    user.velocity = Vec3.ZERO

    target.actionPhase = FighterActionPhase.ULTIMATE_CINEMATIC
    target.activeMove = null
    target.velocity = Vec3.ZERO

    val toTarget = target.position - user.position
    if (toTarget.lengthXZ() > 0.05f) {
      user.yawRad = Math3D.yawFromDir(toTarget.x, toTarget.z)
      target.yawRad = Math3D.yawFromDir(-toTarget.x, -toTarget.z)
    }

    user.animMachine.transitionTo(AnimationState.ULTIMATE_PREP, forceRestart = true)
    attacker = user
    defender = target
    stage = UltimateStage.PREP_CLOSEUP
    elapsedSec = 0f
    impactApplied = false
    shoutTriggered = false

    onStageAudioCue?.invoke(user, UltimateStage.PREP_CLOSEUP)
    onSpawnUltimateVfx?.invoke(user, target, UltimateStage.PREP_CLOSEUP)
    events.add(
      CombatFeedbackEvent.MoveStarted(
        attackerIsPlayer = user.isPlayer,
        moveType = AttackMoveType.ULTIMATE,
        element = user.spec.element,
        origin = user.position,
        yawRad = user.yawRad,
      )
    )
    return true
  }

  fun skipCinematic(combatSystem: CombatSystem, events: MutableList<CombatFeedbackEvent>) {
    if (!isActive || elapsedSec < 0.35f) return
    val atk = attacker ?: return
    val def = defender ?: return
    if (!impactApplied) {
      applyUltimateImpact(atk, def, combatSystem, events)
    }
    finishCinematic()
  }

  fun update(
    rawDt: Float,
    combatSystem: CombatSystem,
    events: MutableList<CombatFeedbackEvent>,
  ): UltimateSnapshot {
    val atk = attacker
    val def = defender
    if (stage == UltimateStage.INACTIVE || atk == null || def == null) {
      return UltimateSnapshot()
    }

    elapsedSec += rawDt
    var timeScale = 1f
    var flashAlpha = 0f

    when {
      elapsedSec < 0.55f -> {
        stage = UltimateStage.PREP_CLOSEUP
        timeScale = 0.65f
      }
      elapsedSec < 1.25f -> {
        if (stage != UltimateStage.ENERGY_GATHER) {
          stage = UltimateStage.ENERGY_GATHER
          onStageAudioCue?.invoke(atk, UltimateStage.ENERGY_GATHER)
          onSpawnUltimateVfx?.invoke(atk, def, UltimateStage.ENERGY_GATHER)
        }
        timeScale = 0.75f
      }
      elapsedSec < 1.75f -> {
        if (stage != UltimateStage.STRIKE_DASH) {
          stage = UltimateStage.STRIKE_DASH
          atk.animMachine.transitionTo(AnimationState.ULTIMATE_STRIKE, forceRestart = true)
          if (!shoutTriggered) {
            shoutTriggered = true
            onStageAudioCue?.invoke(atk, UltimateStage.STRIKE_DASH)
          }
          onSpawnUltimateVfx?.invoke(atk, def, UltimateStage.STRIKE_DASH)
        }
        // Rush toward opponent smoothly
        val toDef = def.position - atk.position
        val dist = toDef.lengthXZ()
        if (dist > 1.75f) {
          val dir = toDef.normalizedXZ()
          atk.position = atk.position + dir * (18f * rawDt)
        }
        timeScale = 1.0f
      }
      elapsedSec < 2.35f -> {
        if (stage != UltimateStage.IMPACT_SLOWMO) {
          stage = UltimateStage.IMPACT_SLOWMO
          if (!impactApplied) {
            applyUltimateImpact(atk, def, combatSystem, events)
          }
          onStageAudioCue?.invoke(atk, UltimateStage.IMPACT_SLOWMO)
          onSpawnUltimateVfx?.invoke(atk, def, UltimateStage.IMPACT_SLOWMO)
        }
        // Step 10: Slow motion impact moment
        timeScale = 0.25f
        flashAlpha = (1f - (elapsedSec - 1.75f) / 0.35f).coerceIn(0f, 0.75f)
      }
      elapsedSec < 2.85f -> {
        stage = UltimateStage.RECOVERY_BLEND
        timeScale = 0.85f
      }
      else -> {
        finishCinematic()
        return UltimateSnapshot()
      }
    }

    return UltimateSnapshot(
      isActive = true,
      stage = stage,
      attackerIsPlayer = atk.isPlayer,
      title = atk.spec.ultimateName,
      canSkip = elapsedSec >= 0.40f && stage != UltimateStage.RECOVERY_BLEND,
      cinematicProgress = (elapsedSec / 2.85f).coerceIn(0f, 1f),
      timeScale = timeScale,
      flashAlpha = flashAlpha,
    )
  }

  private fun applyUltimateImpact(
    atk: FighterState,
    def: FighterState,
    combatSystem: CombatSystem,
    events: MutableList<CombatFeedbackEvent>,
  ) {
    impactApplied = true
    val move = atk.spec.moves[AttackMoveType.ULTIMATE] ?: return
    val dist = Math3D.distanceXZ(atk.position, def.position)
    // Teleport close if within ultimate engagement radius (14m)
    if (dist <= 14.5f) {
      val dir = (def.position - atk.position).normalizedXZ()
      atk.position = Vec3(def.position.x - dir.x * 1.7f, atk.position.y, def.position.z - dir.z * 1.7f)
      combatSystem.applyHitToDefender(atk, def, move, isUltimate = true, events = events)
    }
  }

  private fun finishCinematic() {
    attacker?.let { atk ->
      atk.activeMove = null
      atk.actionPhase = if (atk.isAlive) FighterActionPhase.NEUTRAL else FighterActionPhase.DEFEATED
      if (atk.isAlive) atk.animMachine.transitionTo(AnimationState.IDLE)
    }
    defender?.let { def ->
      if (def.actionPhase == FighterActionPhase.ULTIMATE_CINEMATIC) {
        def.actionPhase = if (def.isAlive) FighterActionPhase.NEUTRAL else FighterActionPhase.DEFEATED
        if (def.isAlive) def.animMachine.transitionTo(AnimationState.IDLE)
      }
    }
    attacker = null
    defender = null
    stage = UltimateStage.INACTIVE
    elapsedSec = 0f
  }

  fun currentAttacker(): FighterState? = attacker

  fun currentDefender(): FighterState? = defender

  fun currentStage(): UltimateStage = stage

  fun currentElapsed(): Float = elapsedSec
}
