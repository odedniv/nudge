package me.odedniv.nudge.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import me.odedniv.nudge.logic.Settings

class BootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    Settings.commit(context)
  }
}