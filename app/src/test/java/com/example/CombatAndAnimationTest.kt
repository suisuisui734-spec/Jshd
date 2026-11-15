package com.example

import com.example.ai.AiOpponentController
import com.example.animation.AnimationState
import com.example.animation.AnimationStateMachine
import com.example.arena.VillageArenaData
import com.example.characters.AttackMoveType
import com.example.characters.FighterActionPhase
import com.example.characters.FighterCatalog
import com.example.characters.FighterState
import com.example.combat.CombatFeedbackEvent
import com.example.combat.CombatSystem
import com.example.combat.UltimateCinematicController
import com.example.combat.UltimateStage
import com.example.core.AiDifficulty
import com.example.core.GameMode
import com.example.core.Vec3
import com.example.physics.ArenaPhysics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CombatAndAnimationTest {

  @Test
  fun `combo chain progresses Light1 to Light2 and enforces cooldowns`() {
    val raiden = FighterState(FighterCatalog.RAIDEN, true, Vec3(0f, 0f, 0f), 0f)
    val homura = FighterState(FighterCatalog.HOMURA, false, Vec3(0f, 0f, 1.4f), 3.14f)
    val combat = CombatSystem()
    val events = mutableListOf<CombatFeedbackEvent>()

    assertTrue(combat.requestMove(raiden, homura, AttackMoveType.LIGHT_1, events))
    assertEquals(AnimationState.LIGHT_1, raiden.animMachine.currentState)

    // Step through startup into active hit window
    repeat(10) {
      combat.updateFighterCombatState(raiden, homura, 0.02f, events)
    }
    assertTrue("Expected Light1 to connect at close range", raiden.moveHasConnected)
    assertTrue(homura.hp < homura.spec.maxHp)
    assertEquals(1, raiden.comboCounter)

    // Cancel into Light2 inside cancel window
    assertTrue(combat.requestMove(raiden, homura, AttackMoveType.LIGHT_1, events))
    assertEquals(AnimationState.LIGHT_2, raiden.animMachine.currentState)

    // Special consumes energy and triggers cooldown
    raiden.energy = 35f
    repeat(10) { combat.updateFighterCombatState(raiden, homura, 0.02f, events) }
    assertTrue(combat.requestMove(raiden, homura, AttackMoveType.SPECIAL, events))
    assertTrue((raiden.cooldowns[AttackMoveType.SPECIAL] ?: 0f) > 1.0f)
    // Cannot spam Special while on cooldown or without energy
    assertFalse(combat.requestMove(raiden, homura, AttackMoveType.SPECIAL, events))
  }

  @Test
  fun `ultimate cinematic advances through stages and applies damage`() {
    val raiden = FighterState(FighterCatalog.RAIDEN, true, Vec3(0f, 0f, 0f), 0f)
    val homura = FighterState(FighterCatalog.HOMURA, false, Vec3(0f, 0f, 4.0f), 3.14f)
    val combat = CombatSystem()
    val ult = UltimateCinematicController()
    val events = mutableListOf<CombatFeedbackEvent>()

    raiden.energy = 50f
    assertFalse(ult.tryActivate(raiden, homura, combat, events))

    raiden.energy = 100f
    assertTrue(ult.tryActivate(raiden, homura, combat, events))
    assertEquals(UltimateStage.PREP_CLOSEUP, ult.currentStage())
    assertEquals(0f, raiden.energy, 0.01f)

    // Advance cinematic to impact slowmo (around 1.85s)
    var snapshot = ult.update(0.6f, combat, events)
    assertEquals(UltimateStage.ENERGY_GATHER, snapshot.stage)
    snapshot = ult.update(0.75f, combat, events)
    assertEquals(UltimateStage.STRIKE_DASH, snapshot.stage)
    snapshot = ult.update(0.55f, combat, events)
    assertEquals(UltimateStage.IMPACT_SLOWMO, snapshot.stage)
    assertTrue(snapshot.timeScale < 0.5f)
    assertTrue("Ultimate must deal significant damage", homura.hp <= homura.spec.maxHp - 300)

    // Finish cinematic cleanly
    ult.update(1.1f, combat, events)
    assertFalse(ult.isActive)
    assertEquals(FighterActionPhase.NEUTRAL, raiden.actionPhase)
  }

  @Test
  fun `animation state machine performs smooth crossfade blending`() {
    val sm = AnimationStateMachine(AnimationState.IDLE)
    sm.transitionTo(AnimationState.RUN)
    assertEquals(AnimationState.IDLE, sm.previousState)
    assertEquals(AnimationState.RUN, sm.currentState)
    assertTrue(sm.isBlending())
    assertEquals(0f, sm.blendAlpha(), 0.01f)

    sm.update(0.06f)
    assertTrue(sm.blendAlpha() in 0.4f..0.6f)

    sm.update(0.10f)
    assertFalse(sm.isBlending())
    assertEquals(1f, sm.blendAlpha(), 0.01f)
  }

  @Test
  fun `arena physics enforces cylindrical boundary and ground collision`() {
    val f = FighterState(FighterCatalog.RAIDEN, true, Vec3(40f, 5f, 0f), 0f)
    f.isGrounded = false
    repeat(60) {
      ArenaPhysics.stepFighterPhysics(f, 0.033f)
    }
    assertTrue(f.position.x <= VillageArenaData.ARENA_RADIUS)
    assertEquals(VillageArenaData.FLOOR_Y, f.position.y, 0.01f)
    assertTrue(f.isGrounded)
  }

  @Test
  fun `ai opponent engages player in versus mode`() {
    val player = FighterState(FighterCatalog.RAIDEN, true, Vec3(0f, 0f, 5f), 3.14f)
    val ai = FighterState(FighterCatalog.HOMURA, false, Vec3(0f, 0f, -5f), 0f)
    val combat = CombatSystem()
    val ult = UltimateCinematicController()
    val aiCtrl = AiOpponentController(AiDifficulty.MASTER, GameMode.VERSUS)
    val events = mutableListOf<CombatFeedbackEvent>()

    repeat(20) {
      aiCtrl.update(0.05f, ai, player, combat, ult, events)
      ArenaPhysics.stepFighterPhysics(ai, 0.05f)
    }
    assertTrue("AI should move closer to engage player", ai.position.z > -4.5f)
  }
}
