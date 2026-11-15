package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.storage.MatchRecordEntity
import com.example.ui.theme.ArenaMidnight
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.CrimsonDamage
import com.example.ui.theme.EmeraldHp
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.TextMuted

@Composable
fun StatsScreen(
  totalWins: Int,
  bestCombo: Int,
  matches: List<MatchRecordEntity>,
  onClearHistory: () -> Unit,
  onBack: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize().background(ArenaMidnight)) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("btn_stats_back")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
          Spacer(Modifier.width(8.dp))
          Text("WARRIOR RECORDS & MATCH LOG", style = MaterialTheme.typography.headlineMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Text("WINS: $totalWins", style = MaterialTheme.typography.titleMedium, color = EmeraldHp)
          Text("BEST COMBO: $bestCombo", style = MaterialTheme.typography.titleMedium, color = ImperialGold)
          OutlinedButton(
            onClick = onClearHistory,
            enabled = matches.isNotEmpty(),
            modifier = Modifier.height(44.dp).testTag("btn_clear_stats"),
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("CLEAR")
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      if (matches.isEmpty()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ImperialGold, modifier = Modifier.size(48.dp))
            Text("No matches recorded yet.", style = MaterialTheme.typography.titleMedium)
            Text("Enter Versus or Arcade Mode to record your combat legacy.", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(matches, key = { it.id }) { m ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = ArenaObsidian,
              border = BorderStroke(1.dp, if (m.won) EmeraldHp.copy(alpha = 0.5f) else CrimsonDamage.copy(alpha = 0.4f)),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column {
                  Text(
                    text = "${if (m.won) "WIN" else "LOSS"} • ${m.playerFighter} vs ${m.opponentFighter}",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (m.won) EmeraldHp else CrimsonDamage,
                  )
                  Text("Mode: ${m.gameMode} • Time Left: ${m.remainingSeconds}s", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                  Text("${m.maxCombo} MAX COMBO", style = MaterialTheme.typography.labelLarge, color = ImperialGold)
                  Text("${m.damageDealt} DMG", style = MaterialTheme.typography.labelLarge, color = LightningCyan)
                  Text("${m.ultimatesLanded} ULT", style = MaterialTheme.typography.labelLarge, color = TextMuted)
                }
              }
            }
          }
        }
      }
    }
  }
}
