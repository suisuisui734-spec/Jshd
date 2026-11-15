package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.assets.AssetPaths
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches Anime Arena Legends`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Anime Arena Legends", appName)
  }

  @Test
  fun `all 3d glb models and audio assets exist in apk assets`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val assetManager = context.assets

    val requiredModels =
      listOf(
        AssetPaths.ARENA_VILLAGE_GLB,
        AssetPaths.FIGHTER_RAIDEN_GLB,
        AssetPaths.FIGHTER_HOMURA_GLB,
        AssetPaths.VFX_SLASH_CYAN_GLB,
        AssetPaths.VFX_SLASH_FIRE_GLB,
        AssetPaths.VFX_RING_CYAN_GLB,
        AssetPaths.VFX_RING_FIRE_GLB,
        AssetPaths.VFX_SHARD_CYAN_GLB,
        AssetPaths.VFX_SHARD_FIRE_GLB,
      )

    for (modelPath in requiredModels) {
      assetManager.open(modelPath).use { stream ->
        assertTrue("Expected non-empty GLB asset: $modelPath", stream.available() > 100)
      }
    }

    val requiredAudio =
      listOf(
        AssetPaths.BGM_MENU,
        AssetPaths.BGM_BATTLE,
        AssetPaths.BGM_BATTLE_INTENSE,
        AssetPaths.BGM_ULTIMATE,
        AssetPaths.BGM_VICTORY,
        AssetPaths.BGM_DEFEAT,
        AssetPaths.SFX_LIGHT_HIT,
        AssetPaths.SFX_HEAVY_IMPACT,
        AssetPaths.SFX_BLOCK,
        AssetPaths.SFX_DODGE,
        AssetPaths.SFX_JUMP,
        AssetPaths.SFX_LIGHTNING,
        AssetPaths.SFX_FIRE,
        AssetPaths.SFX_ULTIMATE_IMPACT,
      )

    for (audioPath in requiredAudio) {
      assetManager.open(audioPath).use { stream ->
        assertTrue("Expected non-empty WAV asset: $audioPath", stream.available() > 100)
      }
    }
  }
}
