package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ArenaDarkColorScheme =
  darkColorScheme(
    primary = LightningCyan,
    onPrimary = ArenaMidnight,
    secondary = FlameScarlet,
    onSecondary = TextPrimary,
    tertiary = ImperialGold,
    onTertiary = ArenaMidnight,
    background = ArenaMidnight,
    onBackground = TextPrimary,
    surface = ArenaObsidian,
    onSurface = TextPrimary,
    surfaceVariant = ArenaSlate,
    onSurfaceVariant = TextMuted,
    outline = ArenaBorder,
    error = CrimsonDamage,
  )

@Composable
fun AnimeArenaTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = ArenaDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
