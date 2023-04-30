package me.odedniv.nudge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.odedniv.nudge.Settings

class VibrationActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      VibrationView(
        value = Settings.read(this).vibration,
        onUpdate = {
          it.execute(this)
          Settings
            .read(this)
            .copy(vibration = it)
            .write(this)
        },
      )
    }
  }
}