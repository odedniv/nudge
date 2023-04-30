package me.odedniv.nudge

import android.content.Context

class Scheduler(private val context: Context) {
  private val notifications: Notifications by lazy { Notifications(context) }

  fun commit(settings: Settings) {
    if (settings.runningNotification) {
      notifications.running(settings.started)
    } else {
      notifications.cancelRunning()
    }
  }
}