package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.characters.ElementType
import com.example.characters.FighterCatalog
import com.example.characters.FighterSpec
import com.example.core.AiDifficulty
import com.example.core.FighterId
import com.example.core.GameMode
import com.example.ui.theme.ArenaMidnight
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.ArenaSlate
import com.example.ui.theme.FlameScarlet
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.TextMuted

@Composable
fun CharacterSelectScreen(
  mode: GameMode,
  onBack: () -> Unit,
  onStartBattle: (player: FighterId, opponent: FighterId, difficulty: AiDifficulty) -> Unit,
) {
  var selectedPlayer by remember { mutableStateOf(FighterId.RAIDEN) }
  var selectedOpponent by remember { mutableStateOf(FighterId.HOMURA) }
  var difficulty by remember { mutableStateOf(AiDifficulty.WARRIOR) }

  val playerSpec = FighterCatalog.get(selectedPlayer)
  val opponentSpec = FighterCatalog.get(selectedOpponent)

  Box(modifier = Modifier.fillMaxSize().background(ArenaMidnight)) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 20.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      // Top Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("btn_char_select_back"),
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Spacer(Modifier.width(8.dp))
          Column {
            Text(
              text = "${mode.name} — FIGHTER SELECT",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "Choose your warrior, rival, and combat intensity",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
            )
          }
        }

        if (mode != GameMode.TRAINING) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AiDifficulty.entries.forEach { diff ->
              FilterChip(
                selected = difficulty == diff,
                onClick = { difficulty = diff },
                label = { Text(diff.label, style = MaterialTheme.typography.labelLarge) },
                modifier = Modifier.testTag("chip_diff_${diff.name.lowercase()}"),
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ImperialGold.copy(alpha = 0.22f),
                    selectedLabelColor = ImperialGold,
                  ),
              )
            }
          }
        }
      }

      // Main Selection Row
      Row(
        modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Player Selection Panel
        FighterPickerCard(
          header = "PLAYER 1",
          selected = selectedPlayer,
          spec = playerSpec,
          onSelect = { selectedPlayer = it },
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )

        // Center Stage Card + Launch
        Column(
          modifier = Modifier.weight(0.75f).fillMaxHeight(),
          verticalArrangement = Arrangement.SpaceBetween,
          horizontalAlignment = Alignment.CenterHorizontally,
        ) {
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = ArenaObsidian,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                painter = painterResource(id = R.drawable.img_arena_village),
                contentDescription = "Kagerou Twilight Village Stage",
                modifier = Modifier.fillMaxWidth().height(92.dp).clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop,
              )
              Spacer(Modifier.height(6.dp))
              Text("STAGE: KAGEROU VILLAGE", style = MaterialTheme.typography.labelLarge, color = ImperialGold)
              Text(
                text = "3D Dojo Plaza • Stone Lanterns • Sakura Courtyard",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
              )
            }
          }

          Text("VS", style = MaterialTheme.typography.displayLarge, color = ImperialGold)

          Button(
            onClick = { onStartBattle(selectedPlayer, selectedOpponent, difficulty) },
            modifier = Modifier.fillMaxWidth().height(54.dp).testTag("btn_enter_arena"),
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = LightningCyan,
                contentColor = ArenaMidnight,
              ),
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("ENTER ARENA", style = MaterialTheme.typography.titleMedium)
          }
        }

        // Opponent Selection Panel
        FighterPickerCard(
          header = if (mode == GameMode.TRAINING) "TRAINING DUMMY" else "OPPONENT (AI)",
          selected = selectedOpponent,
          spec = opponentSpec,
          onSelect = { selectedOpponent = it },
          modifier = Modifier.weight(1f).fillMaxHeight(),
        )
      }
    }
  }
}

@Composable
private fun FighterPickerCard(
  header: String,
  selected: FighterId,
  spec: FighterSpec,
  onSelect: (FighterId) -> Unit,
  modifier: Modifier = Modifier,
) {
  val accent = if (spec.element == ElementType.LIGHTNING) LightningCyan else FlameScarlet

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = ArenaObsidian,
    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.65f)),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(12.dp),
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(header, style = MaterialTheme.typography.labelLarge, color = accent)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FighterId.entries.forEach { fid ->
            val f = FighterCatalog.get(fid)
            val isSelected = fid == selected
            val c = if (f.element == ElementType.LIGHTNING) LightningCyan else FlameScarlet
            Box(
              modifier =
                Modifier.height(34.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) c.copy(alpha = 0.25f) else ArenaSlate)
                  .border(1.dp, if (isSelected) c else Color.Transparent, RoundedCornerShape(8.dp))
                  .clickable { onSelect(fid) }
                  .padding(horizontal = 10.dp)
                  .testTag("pick_${header.lowercase().replace(" ", "_")}_${fid.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = fid.name,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) c else TextMuted,
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Image(
          painter = painterResource(id = spec.portraitRes),
          contentDescription = spec.displayName,
          modifier =
            Modifier.size(92.dp)
              .clip(RoundedCornerShape(12.dp))
              .border(1.5.dp, accent, RoundedCornerShape(12.dp)),
          contentScale = ContentScale.Crop,
        )
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
          Text(spec.displayName, style = MaterialTheme.typography.titleMedium)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (spec.element == ElementType.LIGHTNING) Icons.Default.Bolt else Icons.Default.LocalFireDepartment,
              contentDescription = null,
              tint = accent,
              modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(spec.title, style = MaterialTheme.typography.labelSmall, color = accent)
          }
          Text(
            text = "HP ${spec.maxHp} • SPD ${spec.runSpeed}",
            style = MaterialTheme.typography.labelSmall,
            color = ImperialGold,
          )
        }
      }

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text("Special: ${spec.specialName}", style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text("Ultimate: ${spec.ultimateName}", style = MaterialTheme.typography.labelSmall, color = ImperialGold)
      }
    }
  }
}
