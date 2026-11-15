package com.example.ai

import com.example.animation.AnimationState
import com.example.characters.AttackMoveType
import com.example.characters.FighterActionPhase
import com.example.characters.FighterState
import com.example.combat.CombatFeedbackEvent
import com.example.combat.CombatSystem
import com.example.combat.UltimateCinematicController
import com.example.core.AiDifficulty
import com.example.core.GameMode
import com.example.core.Math3D
import com.example.core.Vec3
import kotlin.random.Random

class AiOpponentController(
  private val difficulty: AiDifficulty = AiDifficulty.WARRIOR,
  private val gameMode: GameMode = GameMode.VERSUS,
) {
  private var decisionTimer = 0f
  private var strafeSign = 1f
  private var comboStep = 0
  private val rng = Random(2026)

  fun reset() {
    decisionTimer = 0.25f
    strafeSign = 1f
    comboStep = 0
  }

  fun update(
    dt: Float,
    ai: FighterState,
    player: FighterState,
    combatSystem: CombatSystem,
    ultimateCtrl: UltimateCinematicController,
    events: MutableList<CombatFeedbackEvent>,
  ) {
    if (!ai.isAlive || !player.isAlive) return
    if (ultimateCtrl.isActive) return

    // Training Mode: AI stays passive / periodically guards so player can practice combos & specials
    if (gameMode == GameMode.TRAINING) {
      val toPlayer = (player.position - ai.position).normalizedXZ()
      if (ai.actionPhase == FighterActionPhase.NEUTRAL) {
        ai.yawRad = Math3D.yawFromDir(toPlayer.x, toPlayer.z)
        if (ai.isGrounded) ai.animMachine.transitionTo(AnimationState.IDLE)
      }
      return
    }

    decisionTimer -= dt
    val dist = Math3D.distanceXZ(ai.position, player.position)
    val toPlayer = (player.position - ai.position).normalizedXZ()
    val right = Math3D.rightFromYaw(ai.yawRad)

    // Defensive reaction to incoming player attack
    val playerAttacking =
      player.actionPhase == FighterActionPhase.ATTACK_STARTUP ||
        player.actionPhase == FighterActionPhase.ATTACK_ACTIVE

    if (playerAttacking && dist < 3.6f && ai.actionPhase == FighterActionPhase.NEUTRAL) {
      if (decisionTimer <= 0f) {
        decisionTimer = difficulty.reactionDelay
        val roll = rng.nextFloat()
        when {
          roll < difficulty.guardRate * 0.72f -> {
            combatSystem.setGuarding(ai, true, events)
            return
          }
          roll < difficulty.guardRate -> {
            combatSystem.performDodge(ai, right.x * strafeSign, right.z * strafeSign, events)
            strafeSign = -strafeSign
            return
          }
        }
      }
    } else if (ai.actionPhase == FighterActionPhase.GUARDING && !playerAttacking) {
      combatSystem.setGuarding(ai, false, events)
    }

    // Approach & strafe movement when neutral
    if (ai.actionPhase == FighterActionPhase.NEUTRAL) {
      ai.yawRad = Math3D.lerpAngle(ai.yawRad, Math3D.yawFromDir(toPlayer.x, toPlayer.z), 10f * dt)
      if (dist > 3.2f) {
        val sprint = dist > 6.0f
        val speed = if (sprint) ai.spec.runSpeed else ai.spec.moveSpeed
        ai.velocity = Vec3(toPlayer.x * speed, ai.velocity.y, toPlayer.z * speed)
        if (ai.isGrounded) {
          ai.animMachine.transitionTo(if (sprint) AnimationState.RUN else AnimationState.WALK)
        }
      } else if (dist > 1.9f) {
        val moveVec = (toPlayer * 0.55f + right * (0.45f * strafeSign)).normalizedXZ()
        ai.velocity = Vec3(moveVec.x * ai.spec.moveSpeed * 0.85f, ai.velocity.y, moveVec.z * ai.spec.moveSpeed * 0.85f)
        if (ai.isGrounded) ai.animMachine.transitionTo(AnimationState.WALK)
      } else if (ai.isGrounded) {
        ai.animMachine.transitionTo(AnimationState.IDLE)
      }
    }

    if (decisionTimer > 0f) return
    decisionTimer = difficulty.reactionDelay * (0.75f + rng.nextFloat() * 0.55f)

    if (rng.nextFloat() > difficulty.aggression) return

    // Launch Ultimate if energy == 100 and within engagement distance
    if (ai.energy >= 100f && dist < 8.5f && rng.nextFloat() < 0.55f) {
      if (ultimateCtrl.tryActivate(ai, player, combatSystem, events)) {
        return
      }
    }

    // Combo chaining & special attacks
    if (dist <= 6.0f && ai.energy >= 30f && (ai.cooldowns[AttackMoveType.SPECIAL] ?: 0f) <= 0f && rng.nextFloat() < 0.25f) {
      combatSystem.requestMove(ai, player, AttackMoveType.SPECIAL, events)
      comboStep = 0
      return
    }

    if (dist <= 2.75f) {
      val nextMove =
        when (comboStep) {
          0 -> AttackMoveType.LIGHT_1
          1 -> AttackMoveType.LIGHT_1 // chains to LIGHT_2
          2 -> if (rng.nextFloat() < 0.5f) AttackMoveType.HEAVY_1 else AttackMoveType.LAUNCHER
          3 -> if (!player.isGrounded) AttackMoveType.AIR_COMBO else AttackMoveType.HEAVY_1
          else -> AttackMoveType.LIGHT_1
        }
      if (combatSystem.requestMove(ai, player, nextMove, events)) {
        comboStep = (comboStep + 1) % 4
      }
    } else if (dist < 4.5f && rng.nextFloat() < 0.16f) {
      combatSystem.performJump(ai, events)
    }
  }
}
