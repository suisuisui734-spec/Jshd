package com.example.camera

import com.example.characters.FighterState
import com.example.combat.UltimateCinematicController
import com.example.combat.UltimateStage
import com.example.core.Math3D
import com.example.core.Vec3
import kotlin.math.cos
import kotlin.math.sin

class ArenaCameraController {
  var eye: Vec3 = Vec3(0f, 3.2f, 12.0f)
    private set

  var target: Vec3 = Vec3(0f, 1.2f, 0f)
    private set

  var fovDegrees: Float = 50f
    private set

  private var shakeAmplitude: Float = 0f
  private var shakeTime: Float = 0f
  private var orbitYaw: Float = 0f

  fun reset(playerPos: Vec3, oppPos: Vec3) {
    val dir = (oppPos - playerPos).normalizedXZ()
    val right = Vec3(dir.z, 0f, -dir.x)
    eye = playerPos - dir * 5.2f + right * 1.4f + Vec3(0f, 2.5f, 0f)
    target = Math3D.lerpVec3(playerPos, oppPos, 0.55f) + Vec3(0f, 1.25f, 0f)
    fovDegrees = 50f
    shakeAmplitude = 0f
  }

  fun triggerShake(amplitude: Float) {
    shakeAmplitude = maxOf(shakeAmplitude, amplitude.coerceIn(0f, 0.55f))
  }

  fun update(
    dt: Float,
    player: FighterState,
    opponent: FighterState,
    lockOnEnabled: Boolean,
    ultimateCtrl: UltimateCinematicController,
    matchEnded: Boolean,
    winner: FighterState?,
  ) {
    shakeTime += dt * 42f
    shakeAmplitude = (shakeAmplitude - dt * 1.8f).coerceAtLeast(0f)

    var desiredEye = eye
    var desiredTarget = target
    var desiredFov = 50f
    var damp = (1f - kotlin.math.exp(-8.5f * dt))

    if (ultimateCtrl.isActive) {
      val atk = ultimateCtrl.currentAttacker() ?: player
      val def = ultimateCtrl.currentDefender() ?: opponent
      val forward = Math3D.forwardFromYaw(atk.yawRad)
      val right = Math3D.rightFromYaw(atk.yawRad)
      val t = ultimateCtrl.currentElapsed()

      when (ultimateCtrl.currentStage()) {
        UltimateStage.PREP_CLOSEUP -> {
          // Step 3-4: Dramatic close-up on attacker's anime face & katana pose
          desiredEye = atk.position + forward * 1.95f + right * 0.55f + Vec3(0f, 1.58f, 0f)
          desiredTarget = atk.position + Vec3(0f, 1.48f, 0f)
          desiredFov = 36f
          damp = (1f - kotlin.math.exp(-12f * dt))
        }
        UltimateStage.ENERGY_GATHER -> {
          // Step 5-7: Orbiting low-angle energy gathering shot
          val angle = (t - 0.55f) * 2.2f
          desiredEye =
            atk.position +
              forward * (2.6f * cos(angle)) +
              right * (2.6f * sin(angle)) +
              Vec3(0f, 1.35f, 0f)
          desiredTarget = atk.position + Vec3(0f, 1.25f, 0f)
          desiredFov = 46f
          damp = (1f - kotlin.math.exp(-10f * dt))
        }
        UltimateStage.STRIKE_DASH -> {
          // Step 9: Dynamic tracking camera following the dash strike
          desiredEye = atk.position - forward * 2.4f + right * 2.6f + Vec3(0f, 1.9f, 0f)
          desiredTarget = Math3D.lerpVec3(atk.position, def.position, 0.6f) + Vec3(0f, 1.2f, 0f)
          desiredFov = 58f
          damp = (1f - kotlin.math.exp(-14f * dt))
        }
        UltimateStage.IMPACT_SLOWMO -> {
          // Step 10: Wide dramatic impact angle
          desiredEye = def.position + right * 4.2f - forward * 1.4f + Vec3(0f, 1.7f, 0f)
          desiredTarget = def.position + Vec3(0f, 1.1f, 0f)
          desiredFov = 54f
          damp = (1f - kotlin.math.exp(-12f * dt))
        }
        UltimateStage.RECOVERY_BLEND,
        UltimateStage.INACTIVE -> {
          desiredFov = 50f
        }
      }
    } else if (matchEnded && winner != null) {
      orbitYaw += dt * 0.55f
      val fwd = Math3D.forwardFromYaw(winner.yawRad + orbitYaw * 0.5f)
      desiredEye = winner.position + fwd * 3.1f + Vec3(0f, 1.45f, 0f)
      desiredTarget = winner.position + Vec3(0f, 1.25f, 0f)
      desiredFov = 42f
    } else {
      // Standard third-person arena battle camera with optional lock-on framing
      val dirToOpp =
        if (lockOnEnabled) (opponent.position - player.position).normalizedXZ()
        else Math3D.forwardFromYaw(player.yawRad)
      val right = Vec3(dirToOpp.z, 0f, -dirToOpp.x)
      val distBetween = Math3D.distanceXZ(player.position, opponent.position).coerceIn(2.0f, 18.0f)
      val backDist = 4.6f + (distBetween * 0.14f)
      desiredEye =
        player.position - dirToOpp * backDist + right * 1.35f + Vec3(0f, 2.35f + player.position.y * 0.45f, 0f)
      desiredTarget =
        if (lockOnEnabled) {
          Math3D.lerpVec3(player.position, opponent.position, 0.58f) + Vec3(0f, 1.2f, 0f)
        } else {
          player.position + dirToOpp * 3.2f + Vec3(0f, 1.25f, 0f)
        }
      desiredFov = 50f
    }

    eye = Math3D.lerpVec3(eye, desiredEye, damp)
    target = Math3D.lerpVec3(target, desiredTarget, damp)
    fovDegrees = Math3D.lerp(fovDegrees, desiredFov, damp)

    if (shakeAmplitude > 0.002f) {
      val sx = sin(shakeTime * 1.7f) * shakeAmplitude
      val sy = cos(shakeTime * 2.3f) * shakeAmplitude * 0.7f
      eye = eye + Vec3(sx, sy, 0f)
      target = target + Vec3(sx * 0.4f, sy * 0.4f, 0f)
    }
  }
}
