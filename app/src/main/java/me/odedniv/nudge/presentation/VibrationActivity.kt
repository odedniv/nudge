package me.odedniv.nudge.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import me.odedniv.nudge.Settings
import me.odedniv.nudge.Vibration

class VibrationActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val executor = MutableSharedFlow<Vibration>(replay = 1, onBufferOverflow = DROP_OLDEST)

    lifecycleScope.launch {
      executor.collect {
        it.execute(this@VibrationActivity)
      }
    }

    setContent {
      VibrationView(
        value = Settings.read(this).vibration,
        onUpdate = {
          executor.tryEmit(it)
          Settings
            .read(this)
            .copy(vibration = it)
            .write(this)
        },
      )
    }
  }
}