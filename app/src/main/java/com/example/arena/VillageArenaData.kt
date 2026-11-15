package com.example.arena

import com.example.core.Vec3

data class CylinderCollider(
  val x: Float,
  val z: Float,
  val radius: Float,
  val label: String,
)

data class BoxCollider(
  val minX: Float,
  val maxX: Float,
  val minZ: Float,
  val maxZ: Float,
  val label: String,
)

object VillageArenaData {
  const val ARENA_NAME = "Kagerou Twilight Village"
  const val ARENA_RADIUS = 21.2f
  const val FLOOR_Y = 0f

  val PLAYER_SPAWN = Vec3(0f, 0f, 6.5f)
  val OPPONENT_SPAWN = Vec3(0f, 0f, -6.5f)

  val LANTERN_POSITIONS =
    listOf(
      Vec3(-13.5f, 2.3f, -13.5f),
      Vec3(13.5f, 2.3f, -13.5f),
      Vec3(-13.5f, 2.3f, 13.5f),
      Vec3(13.5f, 2.3f, 13.5f),
    )

  // Solid cylinder obstacles matching the 3D GLB positions of lanterns, trees, and torii posts
  val CYLINDER_OBSTACLES =
    listOf(
      CylinderCollider(-13.5f, -13.5f, 0.85f, "Stone Lantern NW"),
      CylinderCollider(13.5f, -13.5f, 0.85f, "Stone Lantern NE"),
      CylinderCollider(-13.5f, 13.5f, 0.85f, "Stone Lantern SW"),
      CylinderCollider(13.5f, 13.5f, 0.85f, "Stone Lantern SE"),
      CylinderCollider(-18.5f, 11.0f, 0.90f, "Sakura Tree SW"),
      CylinderCollider(18.5f, 11.0f, 0.90f, "Sakura Tree SE"),
      CylinderCollider(-19.0f, -10.0f, 0.90f, "Sakura Tree NW"),
      CylinderCollider(19.0f, -10.0f, 0.90f, "Sakura Tree NE"),
      CylinderCollider(-2.4f, 25.2f, 0.55f, "South Torii Pillar L"),
      CylinderCollider(2.4f, 25.2f, 0.55f, "South Torii Pillar R"),
    )

  // Solid podium/building colliders around the perimeter
  val BOX_OBSTACLES =
    listOf(
      BoxCollider(-8.0f, 8.0f, -31.0f, -21.8f, "Dojo Pagoda"),
      BoxCollider(23.2f, 31.8f, -10.0f, 2.0f, "East Machiya"),
      BoxCollider(-31.8f, -23.2f, -10.0f, 2.0f, "West Machiya"),
    )
}
