package me.odedniv.nudge.ui

import androidx.compose.runtime.Composable
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.ui.theme.NudgeTheme

class SettingsActivity : SettingsActivityBase() {
  @Composable
  override fun Content(
    value: Settings,
    onUpdate: (Settings) -> Unit,
    onVibrationUpdate: (Vibration) -> Unit,
  ) {
    NudgeTheme {
      SettingsView(value = value, onUpdate = onUpdate, onVibrationUpdate = onVibrationUpdate)
    }
  }
}
