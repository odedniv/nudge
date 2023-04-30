package me.odedniv.nudge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.odedniv.nudge.Settings

class SettingsActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      SettingsView(
        initialSettings = Settings.read(this),
        onUpdate = { onUpdateSettings(it) },
      )
    }
  }

  private fun onUpdateSettings(settings: Settings) {
    settings.write(this)
  }
}
