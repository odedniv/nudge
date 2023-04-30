package me.odedniv.nudge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ToggleReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    require(intent.hasExtra(EXTRA_TO))
    Settings
      .read(context)
      .copy(started = intent.getBooleanExtra(EXTRA_TO, false))
      .write(context)
  }

  companion object {
    const val EXTRA_TO = "to"
  }
}