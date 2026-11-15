package com.example.assets

import com.example.core.FighterId

object AssetPaths {
  const val ARENA_VILLAGE_GLB = "models/arena_village.glb"
  const val FIGHTER_RAIDEN_GLB = "models/fighter_raiden.glb"
  const val FIGHTER_HOMURA_GLB = "models/fighter_homura.glb"

  const val VFX_SLASH_CYAN_GLB = "models/vfx_slash_cyan.glb"
  const val VFX_SLASH_FIRE_GLB = "models/vfx_slash_fire.glb"
  const val VFX_RING_CYAN_GLB = "models/vfx_ring_cyan.glb"
  const val VFX_RING_FIRE_GLB = "models/vfx_ring_fire.glb"
  const val VFX_SHARD_CYAN_GLB = "models/vfx_shard_cyan.glb"
  const val VFX_SHARD_FIRE_GLB = "models/vfx_shard_fire.glb"

  // BGM
  const val BGM_MENU = "audio/bgm_menu.wav"
  const val BGM_BATTLE = "audio/bgm_battle.wav"
  const val BGM_BATTLE_INTENSE = "audio/bgm_battle_intense.wav"
  const val BGM_ULTIMATE = "audio/bgm_ultimate.wav"
  const val BGM_VICTORY = "audio/bgm_victory.wav"
  const val BGM_DEFEAT = "audio/bgm_defeat.wav"

  // SFX
  const val SFX_LIGHT_HIT = "audio/sfx_light_hit.wav"
  const val SFX_HEAVY_IMPACT = "audio/sfx_heavy_impact.wav"
  const val SFX_BLOCK = "audio/sfx_block.wav"
  const val SFX_DODGE = "audio/sfx_dodge.wav"
  const val SFX_FOOTSTEP = "audio/sfx_footstep.wav"
  const val SFX_JUMP = "audio/sfx_jump.wav"
  const val SFX_ENERGY_CHARGE = "audio/sfx_energy_charge.wav"
  const val SFX_ENERGY_RELEASE = "audio/sfx_energy_release.wav"
  const val SFX_LIGHTNING = "audio/sfx_lightning.wav"
  const val SFX_FIRE = "audio/sfx_fire.wav"
  const val SFX_ULTIMATE_IMPACT = "audio/sfx_ultimate_impact.wav"
  const val SFX_CINEMATIC_WHOOSH = "audio/sfx_cinematic_whoosh.wav"
  const val SFX_UI_CLICK = "audio/sfx_ui_click.wav"
  const val SFX_VICTORY = "audio/sfx_victory.wav"
  const val SFX_DEFEAT = "audio/sfx_defeat.wav"

  fun fighterModel(id: FighterId): String =
    when (id) {
      FighterId.RAIDEN -> FIGHTER_RAIDEN_GLB
      FighterId.HOMURA -> FIGHTER_HOMURA_GLB
    }

  fun voiceClip(id: FighterId, cue: String): String {
    val prefix = if (id == FighterId.RAIDEN) "raiden" else "homura"
    return "audio/vo_${prefix}_$cue.wav"
  }
}
