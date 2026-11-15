package com.example.settings

enum class GraphicsQuality(
  val label: String,
  val shadowMapSize: Int,
  val defaultRenderScale: Float,
  val msaaSamples: Int,
  val maxVfxParticles: Int,
) {
  LOW("Low (Battery)", 512, 0.70f, 1, 28),
  MEDIUM("Medium (Balanced)", 1024, 0.85f, 2, 48),
  HIGH("High (HD Anime)", 2048, 1.00f, 4, 72),
}

data class GameSettings(
  val targetFps: Int = 30,
  val graphicsQuality: GraphicsQuality = GraphicsQuality.MEDIUM,
  val renderScale: Float = 0.85f,
  val shadowsEnabled: Boolean = true,
  val masterVolume: Float = 0.85f,
  val musicVolume: Float = 0.70f,
  val sfxVolume: Float = 0.90f,
  val voiceVolume: Float = 0.95f,
  val isMuted: Boolean = false,
)
