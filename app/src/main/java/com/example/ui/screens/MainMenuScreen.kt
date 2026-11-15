package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.GameMode
import com.example.ui.theme.ArenaMidnight
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.FlameScarlet
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.TextMuted

@Composable
fun MainMenuScreen(
  totalWins: Int,
  bestCombo: Int,
  isMuted: Boolean,
  onToggleMute: () -> Unit,
  onSelectMode: (GameMode) -> Unit,
  onOpenSettings: () -> Unit,
  onOpenStats: () -> Unit,
  onAppear: () -> Unit,
) {
  LaunchedEffect(Unit) { onAppear() }

  Box(modifier = Modifier.fillMaxSize().background(ArenaMidnight)) {
    Image(
      painter = painterResource(id = R.drawable.img_arena_hero),
      contentDescription = "Kagerou Twilight Village Arena Hero Art",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )

    // Atmospheric twilight vignette
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors =
                listOf(
                  ArenaMidnight.copy(alpha = 0.94f),
                  ArenaMidnight.copy(alpha = 0.72f),
                  ArenaMidnight.copy(alpha = 0.40f),
                )
            )
          )
    )

    Row(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left Column: Branding + Mode Select Buttons
      Column(
        modifier = Modifier.weight(0.55f).fillMaxHeight(),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = null,
              tint = LightningCyan,
              modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
              text = "3D NATIVE ARENA FIGHTER",
              style = MaterialTheme.typography.labelSmall,
              color = LightningCyan,
            )
          }
          Spacer(Modifier.height(4.dp))
          Text(
            text = "ANIME ARENA",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onBackground,
          )
          Text(
            text = "LEGENDS",
            style = MaterialTheme.typography.headlineMedium,
            color = ImperialGold,
          )
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          ModeActionButton(
            title = "VERSUS MODE",
            subtitle = "1v1 Battle vs Tactical AI Opponent",
            icon = Icons.Default.SportsKabaddi,
            accent = LightningCyan,
            testTag = "btn_versus_mode",
            onClick = { onSelectMode(GameMode.VERSUS) },
          )
          ModeActionButton(
            title = "ARCADE MODE",
            subtitle = "3-Stage Gauntlet Ascending Difficulty",
            icon = Icons.Default.EmojiEvents,
            accent = FlameScarlet,
            testTag = "btn_arcade_mode",
            onClick = { onSelectMode(GameMode.ARCADE) },
          )
          ModeActionButton(
            title = "TRAINING DOJO",
            subtitle = "Practice Combos, Launchers & Ultimates",
            icon = Icons.Default.FitnessCenter,
            accent = ImperialGold,
            testTag = "btn_training_mode",
            onClick = { onSelectMode(GameMode.TRAINING) },
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(
            onClick = onOpenStats,
            modifier = Modifier.height(48.dp).testTag("btn_records"),
            border = BorderStroke(1.dp, LightningCyan.copy(alpha = 0.5f)),
          ) {
            Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = ImperialGold)
            Spacer(Modifier.width(8.dp))
            Text("RECORDS ($totalWins W • $bestCombo MAX)", style = MaterialTheme.typography.labelLarge)
          }

          OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.height(48.dp).testTag("btn_settings"),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("SETTINGS", style = MaterialTheme.typography.labelLarge)
          }

          IconButton(
            onClick = onToggleMute,
            modifier =
              Modifier.size(48.dp)
                .clip(CircleShape)
                .background(ArenaObsidian.copy(alpha = 0.85f))
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .testTag("btn_mute_toggle"),
          ) {
            Icon(
              imageVector =
                if (isMuted) Icons.AutoMirrored.Filled.VolumeOff
                else Icons.AutoMirrored.Filled.VolumeUp,
              contentDescription = if (isMuted) "Unmute Audio" else "Mute Audio",
              tint = if (isMuted) FlameScarlet else LightningCyan,
            )
          }
        }
      }

      Spacer(Modifier.width(24.dp))

      // Right Column: Featured Fighters Showcase
      Column(
        modifier = Modifier.weight(0.45f).fillMaxHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End,
      ) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = ArenaObsidian.copy(alpha = 0.82f),
          border = BorderStroke(1.dp, LightningCyan.copy(alpha = 0.35f)),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("FEATURED WARRIORS", style = MaterialTheme.typography.labelLarge, color = ImperialGold)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
              FighterShowcaseMiniCard(
                name = "RAIDEN",
                element = "LIGHTNING",
                imageRes = R.drawable.img_fighter_raiden,
                accent = LightningCyan,
                modifier = Modifier.weight(1f),
              )
              FighterShowcaseMiniCard(
                name = "HOMURA",
                element = "INFERNO",
                imageRes = R.drawable.img_fighter_homura,
                accent = FlameScarlet,
                modifier = Modifier.weight(1f),
              )
            }

            Text(
              text = "Kagerou Village • Skeletal GLB Combat • Cinematic Ultimates",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ModeActionButton(
  title: String,
  subtitle: String,
  icon: ImageVector,
  accent: Color,
  testTag: String,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = Modifier.fillMaxWidth().height(60.dp).testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    colors =
      ButtonDefaults.buttonColors(
        containerColor = ArenaObsidian.copy(alpha = 0.90f),
        contentColor = MaterialTheme.colorScheme.onSurface,
      ),
    border = BorderStroke(1.5.dp, accent.copy(alpha = 0.7f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp)).background(accent.copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = accent)
        }
        Spacer(Modifier.width(12.dp))
        Column {
          Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
          Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
      }
      Text("PLAY ▸", style = MaterialTheme.typography.labelLarge, color = accent)
    }
  }
}

@Composable
private fun FighterShowcaseMiniCard(
  name: String,
  element: String,
  imageRes: Int,
  accent: Color,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(ArenaMidnight.copy(alpha = 0.85f))
        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
        .padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Image(
      painter = painterResource(id = imageRes),
      contentDescription = "$name Portrait",
      modifier = Modifier.fillMaxWidth().height(104.dp).clip(RoundedCornerShape(8.dp)),
      contentScale = ContentScale.Crop,
    )
    Spacer(Modifier.height(6.dp))
    Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    Text(element, style = MaterialTheme.typography.labelSmall, color = accent)
  }
}
