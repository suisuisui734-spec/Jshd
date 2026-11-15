package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.settings.GameSettings
import com.example.settings.GraphicsQuality
import com.example.ui.theme.ArenaMidnight
import com.example.ui.theme.ArenaObsidian
import com.example.ui.theme.ImperialGold
import com.example.ui.theme.LightningCyan
import com.example.ui.theme.TextMuted
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(
  settings: GameSettings,
  onTargetFpsChange: (Int) -> Unit,
  onQualityChange: (GraphicsQuality) -> Unit,
  onRenderScaleChange: (Float) -> Unit,
  onShadowsChange: (Boolean) -> Unit,
  onMasterVolumeChange: (Float) -> Unit,
  onMusicVolumeChange: (Float) -> Unit,
  onSfxVolumeChange: (Float) -> Unit,
  onVoiceVolumeChange: (Float) -> Unit,
  onMuteChange: (Boolean) -> Unit,
  onBack: () -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize().background(ArenaMidnight)) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .windowInsetsPadding(WindowInsets.safeDrawing)
          .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp).testTag("btn_settings_back")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Spacer(Modifier.width(8.dp))
        Text("GRAPHICS & AUDIO SETTINGS", style = MaterialTheme.typography.headlineMedium)
      }

      Spacer(Modifier.height(10.dp))

      Row(
        modifier = Modifier.weight(1f).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        // Graphics Panel
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = ArenaObsidian,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("3D RENDERING & PERFORMANCE", style = MaterialTheme.typography.titleMedium, color = LightningCyan)

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("Target Frame Rate", style = MaterialTheme.typography.bodyLarge)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(30, 60).forEach { fps ->
                  FilterChip(
                    selected = settings.targetFps == fps,
                    onClick = { onTargetFpsChange(fps) },
                    label = { Text("$fps FPS") },
                    modifier = Modifier.testTag("chip_fps_$fps"),
                  )
                }
              }
            }

            Column {
              Text("Graphics Quality Preset", style = MaterialTheme.typography.bodyLarge)
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GraphicsQuality.entries.forEach { q ->
                  FilterChip(
                    selected = settings.graphicsQuality == q,
                    onClick = { onQualityChange(q) },
                    label = { Text(q.name) },
                    modifier = Modifier.testTag("chip_quality_${q.name.lowercase()}"),
                  )
                }
              }
            }

            Column {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Render Resolution Scale", style = MaterialTheme.typography.bodyLarge)
                Text("${(settings.renderScale * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge, color = ImperialGold)
              }
              Slider(
                value = settings.renderScale,
                onValueChange = onRenderScaleChange,
                valueRange = 0.60f..1.00f,
                modifier = Modifier.testTag("slider_render_scale"),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text("Real-Time Shadow Mapping", style = MaterialTheme.typography.bodyLarge)
                Text("Directional sun shadows across dojo plaza", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              }
              Switch(
                checked = settings.shadowsEnabled,
                onCheckedChange = onShadowsChange,
                modifier = Modifier.testTag("switch_shadows"),
              )
            }
          }
        }

        // Audio Panel
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = ArenaObsidian,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.weight(1f).fillMaxHeight(),
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text("AUDIO MIXER", style = MaterialTheme.typography.titleMedium, color = ImperialGold)
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mute All", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                Switch(
                  checked = settings.isMuted,
                  onCheckedChange = onMuteChange,
                  modifier = Modifier.testTag("switch_mute_all"),
                )
              }
            }

            VolumeSliderRow("Master Volume", settings.masterVolume, onMasterVolumeChange, "slider_master_vol")
            VolumeSliderRow("Music (BGM)", settings.musicVolume, onMusicVolumeChange, "slider_music_vol")
            VolumeSliderRow("Sound Effects (SFX)", settings.sfxVolume, onSfxVolumeChange, "slider_sfx_vol")
            VolumeSliderRow("Character Voice", settings.voiceVolume, onVoiceVolumeChange, "slider_voice_vol")
          }
        }
      }
    }
  }
}

@Composable
private fun VolumeSliderRow(
  label: String,
  value: Float,
  onChange: (Float) -> Unit,
  tag: String,
) {
  Column {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
      Text(label, style = MaterialTheme.typography.bodyMedium)
      Text("${(value * 100).roundToInt()}%", style = MaterialTheme.typography.labelSmall, color = LightningCyan)
    }
    Slider(
      value = value,
      onValueChange = onChange,
      valueRange = 0f..1f,
      modifier = Modifier.height(32.dp).testTag(tag),
    )
  }
}
