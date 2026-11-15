package com.example.animation

import com.example.core.Math3D

/**
 * Tracks active and previous skeletal animation states and computes crossfade
 * blend weights so Filament's Animator can smoothly interpolate bone poses.
 */
class AnimationStateMachine(initial: AnimationState = AnimationState.IDLE) {
  var currentState: AnimationState = initial
    private set

  var previousState: AnimationState = initial
    private set

  var stateTime: Float = 0f
    private set

  var previousTime: Float = 0f
    private set

  var blendElapsed: Float = 1f
    private set

  var blendDuration: Float = 0.12f
    private set

  var playbackRate: Float = 1f

  fun transitionTo(
    next: AnimationState,
    forceRestart: Boolean = false,
    customBlendSec: Float? = null,
    rate: Float = 1f,
  ) {
    if (next == currentState && !forceRestart) return
    previousState = currentState
    previousTime = stateTime
    currentState = next
    stateTime = 0f
    blendDuration = (customBlendSec ?: next.defaultBlendSec).coerceAtLeast(0.01f)
    blendElapsed = 0f
    playbackRate = rate
  }

  fun update(dt: Float) {
    val scaledDt = dt * playbackRate
    stateTime += scaledDt
    previousTime += scaledDt
    if (blendElapsed < blendDuration) {
      blendElapsed = (blendElapsed + dt).coerceAtMost(blendDuration)
    }
    if (currentState.looping && currentState.durationSec > 0f) {
      stateTime %= currentState.durationSec
    } else if (stateTime > currentState.durationSec) {
      stateTime = currentState.durationSec
    }
    if (previousState.looping && previousState.durationSec > 0f) {
      previousTime %= previousState.durationSec
    } else if (previousTime > previousState.durationSec) {
      previousTime = previousState.durationSec
    }
  }

  /** Normalized [0..1] weight of currentState over previousState during crossfade. */
  fun blendAlpha(): Float = Math3D.clamp(blendElapsed / blendDuration, 0f, 1f)

  fun isBlending(): Boolean = blendElapsed < blendDuration

  fun isFinished(): Boolean = !currentState.looping && stateTime >= currentState.durationSec
}
