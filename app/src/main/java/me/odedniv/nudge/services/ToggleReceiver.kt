package me.odedniv.nudge.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.Instant
import me.odedniv.nudge.logic.Settings

class ToggleReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    require(intent.hasExtra(EXTRA_TO))
    val to = intent.getBooleanExtra(EXTRA_TO, false)
    val settings = Settings.read(context)
    if (settings.oneOff.isRunning(Instant.now()) && !to) {
      settings.copy(oneOff = settings.oneOff.copy(startedAt = null)).write()
    } else {
      settings.copy(periodic = to).write()
    }
  }

  companion object {
    const val EXTRA_TO = "to"
  }
}
