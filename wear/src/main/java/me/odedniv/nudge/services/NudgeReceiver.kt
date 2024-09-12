package me.odedniv.nudge.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings

class NudgeReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val settings = Settings.commit(context)
    Log.i(TAG, "Nudging for $settings.")
    Notifications(context).nudge(settings.vibration)
  }

  companion object {
    private const val TAG = "NudgeReceiver"
  }
}
