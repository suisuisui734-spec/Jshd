package com.example.combat

import com.example.animation.AnimationState
import com.example.characters.AttackMoveType
import com.example.characters.FighterActionPhase
import com.example.characters.FighterState
import com.example.characters.MoveSpec
import com.example.core.Math3D
import com.example.core.Vec3
import kotlin.math.max
import kotlin.math.roundToInt

class CombatSystem {
  var hitstopTimerSec: Float = 0f
    private set

  fun reset() {
    hitstopTimerSec = 0f
  }

  fun tickHitstop(dt: Float): Boolean {
    if (hitstopTimerSec > 0f) {
      hitstopTimerSec = (hitstopTimerSec - dt).coerceAtLeast(0f)
      return true
    }
    return false
  }

  fun requestMove(
    attacker: FighterState,
    defender: FighterState,
    requested: AttackMoveType,
    events: MutableList<CombatFeedbackEvent>,
  ): Boolean {
    val move = attacker.spec.moves[requested] ?: return false

    // Smart combo routing: pressing LIGHT while in Light1/Light2 chains to Light2/Light3,
    // or in air chains to AIR_COMBO!
    val resolvedType = resolveComboChain(attacker, requested)
    val resolvedMove = attacker.spec.moves[resolvedType] ?: move

    if (attacker.canStartMove(resolvedMove, allowCancel = true)) {
      startMove(attacker, defender, resolvedMove, events)
      return true
    }

    // Buffer input if currently inside an active/early recovery attack
    if (
      attacker.actionPhase == FighterActionPhase.ATTACK_STARTUP ||
        attacker.actionPhase == FighterActionPhase.ATTACK_ACTIVE ||
        attacker.actionPhase == FighterActionPhase.ATTACK_RECOVERY
    ) {
      val current = attacker.activeMove
      if (current != null && current.canCancelInto.contains(resolvedType)) {
        attacker.queuedComboMove = resolvedType
      }
    }
    return false
  }

  private fun resolveComboChain(attacker: FighterState, requested: AttackMoveType): AttackMoveType {
    if (!attacker.isGrounded && (requested == AttackMoveType.LIGHT_1 || requested == AttackMoveType.HEAVY_1)) {
      return AttackMoveType.AIR_COMBO
    }
    val active = attacker.activeMove?.type
    if (
      attacker.actionPhase == FighterActionPhase.ATTACK_ACTIVE ||
        attacker.actionPhase == FighterActionPhase.ATTACK_RECOVERY
    ) {
      return when (requested) {
        AttackMoveType.LIGHT_1 ->
          when (active) {
            AttackMoveType.LIGHT_1 -> AttackMoveType.LIGHT_2
            AttackMoveType.LIGHT_2 -> AttackMoveType.LIGHT_3
            else -> AttackMoveType.LIGHT_1
          }
        AttackMoveType.HEAVY_1 ->
          when (active) {
            AttackMoveType.HEAVY_1 -> AttackMoveType.HEAVY_2
            else -> AttackMoveType.HEAVY_1
          }
        else -> requested
      }
    }
    return requested
  }

  private fun startMove(
    attacker: FighterState,
    defender: FighterState,
    move: MoveSpec,
    events: MutableList<CombatFeedbackEvent>,
  ) {
    // Face opponent immediately on attack startup (anime arena snap-lock feel)
    val toOpp = defender.position - attacker.position
    if (toOpp.lengthXZ() > 0.05f) {
      attacker.yawRad = Math3D.yawFromDir(toOpp.x, toOpp.z)
    }

    attacker.energy = (attacker.energy - move.energyCost).coerceAtLeast(0f)
    if (move.cooldownSec > 0f) {
      attacker.cooldowns[move.type] = move.cooldownSec
    }
    attacker.actionPhase = FighterActionPhase.ATTACK_STARTUP
    attacker.activeMove = move
    attacker.moveElapsedSec = 0f
    attacker.moveHasConnected = false
    attacker.queuedComboMove = null

    val forward = Math3D.forwardFromYaw(attacker.yawRad)
    attacker.velocity =
      Vec3(
        forward.x * move.forwardLungeSpeed,
        if (move.type == AttackMoveType.AIR_COMBO && !attacker.isGrounded) 2.5f else attacker.velocity.y,
        forward.z * move.forwardLungeSpeed,
      )

    attacker.animMachine.transitionTo(move.animState, forceRestart = true)
    events.add(
      CombatFeedbackEvent.MoveStarted(
        attackerIsPlayer = attacker.isPlayer,
        moveType = move.type,
        element = attacker.spec.element,
        origin = attacker.position,
        yawRad = attacker.yawRad,
      )
    )
  }

  fun performJump(fighter: FighterState, events: MutableList<CombatFeedbackEvent>): Boolean {
    if (!fighter.isAlive || !fighter.isGrounded) return false
    if (fighter.actionPhase != FighterActionPhase.NEUTRAL && fighter.actionPhase != FighterActionPhase.GUARDING) {
      return false
    }
    fighter.actionPhase = FighterActionPhase.NEUTRAL
    fighter.isGrounded = false
    fighter.velocity = Vec3(fighter.velocity.x, fighter.spec.jumpImpulse, fighter.velocity.z)
    fighter.animMachine.transitionTo(AnimationState.JUMP, forceRestart = true)
    events.add(
      CombatFeedbackEvent.ActionTriggered(
        fighter.isPlayer,
        ActionKind.JUMP,
        fighter.position,
        fighter.spec.element,
      )
    )
    return true
  }

  fun performDodge(
    fighter: FighterState,
    moveInputX: Float,
    moveInputZ: Float,
    events: MutableList<CombatFeedbackEvent>,
  ): Boolean {
    if (!fighter.isAlive) return false
    if (
      fighter.actionPhase != FighterActionPhase.NEUTRAL &&
        fighter.actionPhase != FighterActionPhase.GUARDING &&
        fighter.actionPhase != FighterActionPhase.ATTACK_RECOVERY
    ) {
      return false
    }
    fighter.activeMove = null
    fighter.queuedComboMove = null
    fighter.actionPhase = FighterActionPhase.DODGING
    fighter.dodgeTimerSec = 0.34f
    fighter.invulnerableTimerSec = 0.26f

    val dir =
      if (moveInputX * moveInputX + moveInputZ * moveInputZ > 0.04f) {
        Vec3(moveInputX, 0f, moveInputZ).normalizedXZ()
      } else {
        Math3D.rightFromYaw(fighter.yawRad)
      }
    fighter.velocity = Vec3(dir.x * fighter.spec.dodgeSpeed, fighter.velocity.y, dir.z * fighter.spec.dodgeSpeed)
    fighter.animMachine.transitionTo(AnimationState.DODGE, forceRestart = true)
    events.add(
      CombatFeedbackEvent.ActionTriggered(
        fighter.isPlayer,
        ActionKind.DODGE,
        fighter.position,
        fighter.spec.element,
      )
    )
    return true
  }

  fun setGuarding(fighter: FighterState, active: Boolean, events: MutableList<CombatFeedbackEvent>) {
    if (!fighter.isAlive) return
    if (active) {
      if (fighter.actionPhase == FighterActionPhase.NEUTRAL && fighter.guardGauge > 10f) {
        fighter.actionPhase = FighterActionPhase.GUARDING
        fighter.animMachine.transitionTo(AnimationState.GUARD)
        events.add(
          CombatFeedbackEvent.ActionTriggered(
            fighter.isPlayer,
            ActionKind.GUARD_START,
            fighter.position,
            fighter.spec.element,
          )
        )
      }
    } else if (fighter.actionPhase == FighterActionPhase.GUARDING) {
      fighter.actionPhase = FighterActionPhase.NEUTRAL
    }
  }

  fun updateFighterCombatState(
    fighter: FighterState,
    opponent: FighterState,
    dt: Float,
    events: MutableList<CombatFeedbackEvent>,
    infiniteEnergyTraining: Boolean = false,
  ) {
    // Cooldowns & timers
    val keys = fighter.cooldowns.keys.toList()
    for (k in keys) {
      val rem = (fighter.cooldowns[k] ?: 0f) - dt
      if (rem <= 0f) fighter.cooldowns.remove(k) else fighter.cooldowns[k] = rem
    }
    if (fighter.invulnerableTimerSec > 0f) {
      fighter.invulnerableTimerSec = (fighter.invulnerableTimerSec - dt).coerceAtLeast(0f)
    }
    if (fighter.comboResetTimerSec > 0f) {
      fighter.comboResetTimerSec -= dt
      if (fighter.comboResetTimerSec <= 0f) {
        fighter.comboCounter = 0
      }
    }
    if (fighter.actionPhase != FighterActionPhase.GUARDING && fighter.guardGauge < 100f) {
      fighter.guardGauge = (fighter.guardGauge + 14f * dt).coerceAtMost(100f)
    }
    if (infiniteEnergyTraining && fighter.energy < 100f) {
      fighter.energy = (fighter.energy + 35f * dt).coerceAtMost(100f)
    }

    when (fighter.actionPhase) {
      FighterActionPhase.HITSTUN,
      FighterActionPhase.BLOCKSTUN -> {
        fighter.stunTimerSec -= dt
        if (fighter.stunTimerSec <= 0f) {
          fighter.actionPhase = FighterActionPhase.NEUTRAL
          fighter.animMachine.transitionTo(AnimationState.IDLE)
        }
      }
      FighterActionPhase.DODGING -> {
        fighter.dodgeTimerSec -= dt
        if (fighter.dodgeTimerSec <= 0f) {
          fighter.actionPhase = FighterActionPhase.NEUTRAL
        }
      }
      FighterActionPhase.ATTACK_STARTUP,
      FighterActionPhase.ATTACK_ACTIVE,
      FighterActionPhase.ATTACK_RECOVERY -> {
        val move = fighter.activeMove
        if (move == null) {
          fighter.actionPhase = FighterActionPhase.NEUTRAL
        } else {
          fighter.moveElapsedSec += dt
          val t = fighter.moveElapsedSec
          when {
            t < move.startupSec -> fighter.actionPhase = FighterActionPhase.ATTACK_STARTUP
            t < move.startupSec + move.activeSec -> {
              fighter.actionPhase = FighterActionPhase.ATTACK_ACTIVE
              if (!fighter.moveHasConnected) {
                checkHit(fighter, opponent, move, events)
              }
            }
            t < move.totalDurationSec -> {
              fighter.actionPhase = FighterActionPhase.ATTACK_RECOVERY
              val queued = fighter.queuedComboMove
              if (queued != null && fighter.moveHasConnected && t >= move.cancelWindowStartSec) {
                val qMove = fighter.spec.moves[queued]
                if (qMove != null && fighter.canStartMove(qMove, allowCancel = true)) {
                  startMove(fighter, opponent, qMove, events)
                }
              }
            }
            else -> {
              fighter.activeMove = null
              fighter.queuedComboMove = null
              fighter.actionPhase = FighterActionPhase.NEUTRAL
            }
          }
        }
      }
      else -> Unit
    }
  }

  private fun checkHit(
    attacker: FighterState,
    defender: FighterState,
    move: MoveSpec,
    events: MutableList<CombatFeedbackEvent>,
  ) {
    if (!defender.isAlive || defender.invulnerableTimerSec > 0f) return

    val forward = Math3D.forwardFromYaw(attacker.yawRad)
    val hitboxCenter =
      Vec3(
        attacker.position.x + forward.x * (move.rangeMeters * 0.55f),
        attacker.position.y + 1.0f,
        attacker.position.z + forward.z * (move.rangeMeters * 0.55f),
      )
    val defCenter = Vec3(defender.position.x, defender.position.y + 1.0f, defender.position.z)
    val dist = (defCenter - hitboxCenter).length()
    val maxReach = move.hitRadius + (move.rangeMeters * 0.45f)
    if (dist > maxReach) return

    attacker.moveHasConnected = true
    applyHitToDefender(attacker, defender, move, isUltimate = false, events = events)
  }

  fun applyHitToDefender(
    attacker: FighterState,
    defender: FighterState,
    move: MoveSpec,
    isUltimate: Boolean,
    events: MutableList<CombatFeedbackEvent>,
  ) {
    val isGuarding =
      !isUltimate &&
        (defender.actionPhase == FighterActionPhase.GUARDING ||
          defender.actionPhase == FighterActionPhase.BLOCKSTUN)

    val dir = (defender.position - attacker.position).normalizedXZ()

    if (isGuarding) {
      defender.guardGauge -= move.guardDamage
      val guardBroken = defender.guardGauge <= 0f
      if (guardBroken) {
        defender.guardGauge = 0f
        val dmg = (move.damage * 0.75f).roundToInt()
        defender.hp = max(0, defender.hp - dmg)
        defender.actionPhase = FighterActionPhase.HITSTUN
        defender.stunTimerSec = move.hitstunSec * 1.4f
        defender.animMachine.transitionTo(AnimationState.HIT_REACTION, forceRestart = true)
        defender.velocity = Vec3(dir.x * move.knockbackHorizontal, 3.0f, dir.z * move.knockbackHorizontal)
        hitstopTimerSec = 0.08f
        events.add(
          CombatFeedbackEvent.HitLanded(
            attackerIsPlayer = attacker.isPlayer,
            moveType = move.type,
            element = attacker.spec.element,
            damage = dmg,
            isBlocked = false,
            isGuardBreak = true,
            isCritical = true,
            isUltimate = false,
            impactPoint = Vec3(defender.position.x, defender.position.y + 1.15f, defender.position.z),
            comboCount = attacker.comboCounter,
          )
        )
      } else {
        defender.hp = max(1, defender.hp - move.chipDamage)
        defender.actionPhase = FighterActionPhase.BLOCKSTUN
        defender.stunTimerSec = move.blockstunSec
        defender.velocity = Vec3(dir.x * (move.knockbackHorizontal * 0.45f), 0f, dir.z * (move.knockbackHorizontal * 0.45f))
        attacker.energy = (attacker.energy + move.energyGainOnHit * 0.4f).coerceAtMost(100f)
        hitstopTimerSec = 0.035f
        events.add(
          CombatFeedbackEvent.HitLanded(
            attackerIsPlayer = attacker.isPlayer,
            moveType = move.type,
            element = attacker.spec.element,
            damage = move.chipDamage,
            isBlocked = true,
            isGuardBreak = false,
            isCritical = false,
            isUltimate = false,
            impactPoint = Vec3(defender.position.x, defender.position.y + 1.15f, defender.position.z),
            comboCount = attacker.comboCounter,
          )
        )
      }
      return
    }

    // Clean Hit!
    attacker.comboCounter += 1
    attacker.comboResetTimerSec = 1.65f
    if (attacker.comboCounter > attacker.maxComboAchieved) {
      attacker.maxComboAchieved = attacker.comboCounter
    }

    // Combo proration prevents unfair infinite 100-0 without scaling
    val comboScale = (1.0f - (attacker.comboCounter - 1) * 0.06f).coerceAtLeast(0.45f)
    val finalDamage =
      if (isUltimate) move.damage
      else (move.damage * comboScale).roundToInt().coerceAtLeast(12)

    defender.hp = max(0, defender.hp - finalDamage)
    attacker.totalDamageDealt += finalDamage
    if (isUltimate) attacker.ultimatesLanded += 1

    attacker.energy = (attacker.energy + move.energyGainOnHit).coerceAtMost(100f)
    defender.energy = (defender.energy + 6.5f).coerceAtMost(100f)

    defender.activeMove = null
    defender.queuedComboMove = null
    defender.actionPhase = if (defender.isAlive) FighterActionPhase.HITSTUN else FighterActionPhase.DEFEATED
    defender.stunTimerSec = move.hitstunSec
    defender.animMachine.transitionTo(
      if (defender.isAlive) AnimationState.HIT_REACTION else AnimationState.DEFEAT,
      forceRestart = true,
    )

    val launchY =
      if (!defender.isGrounded && move.type != AttackMoveType.AIR_COMBO && move.knockbackVertical < 4f) {
        4.6f // Juggle pop in mid-air
      } else {
        move.knockbackVertical
      }
    if (launchY > 1f) defender.isGrounded = false
    defender.velocity = Vec3(dir.x * move.knockbackHorizontal, launchY, dir.z * move.knockbackHorizontal)

    hitstopTimerSec =
      when {
        isUltimate -> 0.14f
        move.type == AttackMoveType.HEAVY_1 || move.type == AttackMoveType.HEAVY_2 || move.type == AttackMoveType.SPECIAL -> 0.065f
        else -> 0.032f
      }

    events.add(
      CombatFeedbackEvent.HitLanded(
        attackerIsPlayer = attacker.isPlayer,
        moveType = move.type,
        element = attacker.spec.element,
        damage = finalDamage,
        isBlocked = false,
        isGuardBreak = false,
        isCritical = move.type == AttackMoveType.HEAVY_2 || move.type == AttackMoveType.SPECIAL || isUltimate,
        isUltimate = isUltimate,
        impactPoint = Vec3(defender.position.x, defender.position.y + 1.15f, defender.position.z),
        comboCount = attacker.comboCounter,
      )
    )
  }
}
