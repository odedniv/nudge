package me.odedniv.nudge.logic

import android.app.AlarmManager
import android.app.AlarmManager.AlarmClockInfo
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.getSystemService
import java.time.Duration
import java.time.Instant
import me.odedniv.nudge.presentation.SettingsActivity
import me.odedniv.nudge.services.NudgeReceiver

class Scheduler(private val context: Context) {
  private val alarmManager: AlarmManager by lazy { requireNotNull(context.getSystemService()) }
  private val notifications: Notifications by lazy { Notifications(context) }

  fun commit(settings: Settings) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
      Log.e(TAG, "No permission to schedule exact alarm.")
      return
    }
    if (settings.started) {
      val nextTrigger = settings.nextTrigger()
      Log.i(TAG, "Scheduling next nudge for: $nextTrigger")
      alarmManager.setAlarmClock(
        AlarmClockInfo(nextTrigger.toEpochMilli(), settingsPendingIntent),
        nudgePendingIntent
      )
    } else {
      alarmManager.cancel(nudgePendingIntent)
    }
    if (settings.runningNotification) {
      notifications.running(settings.started)
    } else {
      notifications.cancelRunning()
    }
  }

  private val nudgePendingIntent
    get() =
      PendingIntent.getBroadcast(
        context,
        /* requestCode = */ 0,
        Intent(context, NudgeReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

  private val settingsPendingIntent
    get() =
      PendingIntent.getActivity(
        context,
        /* requestCode = */ 0,
        Intent(context, SettingsActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

  private fun Settings.nextTrigger(): Instant {
    val now = Instant.now()
    return (now + frequency) - Duration.ofMillis(now.toEpochMilli() % frequency.toMillis())
  }

  companion object {
    private const val TAG = "Scheduler"
  }
}
