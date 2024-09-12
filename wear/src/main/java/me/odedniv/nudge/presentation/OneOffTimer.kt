package me.odedniv.nudge.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import java.time.Instant
import kotlinx.coroutines.delay
import me.odedniv.nudge.logic.Settings

@Composable
fun OneOffTimer(value: Settings.OneOff, initial: Instant, onUpdate: (Instant) -> Unit) {
  LaunchedEffect(value) {
      if (!value.isRunning(initial)) return@LaunchedEffect
      while (true) {
          onUpdate(Instant.now())
          delay(100)
      }
  }
}
