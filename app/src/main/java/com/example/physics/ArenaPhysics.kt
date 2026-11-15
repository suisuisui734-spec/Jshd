package com.example.physics

import com.example.arena.VillageArenaData
import com.example.characters.FighterState
import com.example.core.Vec3
import kotlin.math.sqrt

object ArenaPhysics {
  const val GRAVITY = -26.0f
  const val FIGHTER_RADIUS = 0.58f
  private const val GROUND_FRICTION = 14.0f
  private const val AIR_DRAG = 2.4f

  fun stepFighterPhysics(fighter: FighterState, dt: Float) {
    val drag = if (fighter.isGrounded) GROUND_FRICTION else AIR_DRAG
    val vx = fighter.velocity.x
    val vz = fighter.velocity.z
    val damping = (1f - drag * dt).coerceAtLeast(0f)

    var newVx = vx * damping
    var newVz = vz * damping
    var newVy = fighter.velocity.y

    if (!fighter.isGrounded) {
      newVy += GRAVITY * dt
    }

    var nx = fighter.position.x + newVx * dt
    var ny = fighter.position.y + newVy * dt
    var nz = fighter.position.z + newVz * dt

    if (ny <= VillageArenaData.FLOOR_Y) {
      ny = VillageArenaData.FLOOR_Y
      newVy = 0f
      fighter.isGrounded = true
    } else {
      fighter.isGrounded = false
    }

    // Resolve cylinder colliders (lanterns, sakura trees, torii pillars)
    for (obs in VillageArenaData.CYLINDER_OBSTACLES) {
      val dx = nx - obs.x
      val dz = nz - obs.z
      val minDist = obs.radius + FIGHTER_RADIUS
      val distSq = dx * dx + dz * dz
      if (distSq < minDist * minDist) {
        val dist = sqrt(distSq.coerceAtLeast(1e-6f))
        val push = minDist - dist
        nx += (dx / dist) * push
        nz += (dz / dist) * push
      }
    }

    // Resolve box colliders (buildings / dojo steps)
    for (box in VillageArenaData.BOX_OBSTACLES) {
      val closestX = nx.coerceIn(box.minX, box.maxX)
      val closestZ = nz.coerceIn(box.minZ, box.maxZ)
      val dx = nx - closestX
      val dz = nz - closestZ
      val distSq = dx * dx + dz * dz
      if (distSq < FIGHTER_RADIUS * FIGHTER_RADIUS) {
        val dist = sqrt(distSq.coerceAtLeast(1e-6f))
        val push = FIGHTER_RADIUS - dist
        nx += (dx / dist) * push
        nz += (dz / dist) * push
      }
    }

    // Enforce circular arena boundary
    val rSq = nx * nx + nz * nz
    val maxR = VillageArenaData.ARENA_RADIUS - FIGHTER_RADIUS
    if (rSq > maxR * maxR) {
      val r = sqrt(rSq)
      nx = (nx / r) * maxR
      nz = (nz / r) * maxR
      newVx *= 0.2f
      newVz *= 0.2f
    }

    fighter.position = Vec3(nx, ny, nz)
    fighter.velocity = Vec3(newVx, newVy, newVz)
  }

  fun resolveFighterSeparation(a: FighterState, b: FighterState) {
    val dy = kotlin.math.abs(a.position.y - b.position.y)
    if (dy > 1.4f) return
    val dx = b.position.x - a.position.x
    val dz = b.position.z - a.position.z
    val minDist = FIGHTER_RADIUS * 1.9f
    val distSq = dx * dx + dz * dz
    if (distSq < minDist * minDist) {
      val dist = sqrt(distSq.coerceAtLeast(1e-5f))
      val overlap = (minDist - dist) * 0.5f
      val nx = if (dist > 1e-4f) dx / dist else 1f
      val nz = if (dist > 1e-4f) dz / dist else 0f
      a.position = Vec3(a.position.x - nx * overlap, a.position.y, a.position.z - nz * overlap)
      b.position = Vec3(b.position.x + nx * overlap, b.position.y, b.position.z + nz * overlap)
    }
  }
}
