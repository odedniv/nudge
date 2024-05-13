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
import java.time.LocalTime
import java.time.ZoneId
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
    val nextTrigger = settings.nextTrigger()
    if (nextTrigger != null) {
      Log.i(TAG, "Scheduling next nudge for: $nextTrigger")
      alarmManager.setAlarmClock(
        AlarmClockInfo(nextTrigger.toEpochMilli(), settingsPendingIntent),
        nudgePendingIntent
      )
    } else {
      alarmManager.cancel(nudgePendingIntent)
    }
    if (settings.runningNotification) {
      notifications.running(settings.periodic)
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

  private fun Settings.nextTrigger(): Instant? {
    val now = Instant.now()
    val zone = ZoneId.systemDefault()
    return (nextOneOffTrigger(now) ?: nextPeriodicTrigger(now, zone))?.let {
      if (it < Instant.MAX) it else null
    }
  }

  private fun Settings.nextOneOffTrigger(now: Instant): Instant? {
    if (oneOff.pausedAt != null) return Instant.MAX
    val oneOffNextElapsedIndex = oneOff.nextElapsedIndex(now) ?: return null
    return oneOff.startedAt!! + oneOff.durations.take(oneOffNextElapsedIndex + 1).sum()
  }

  private fun Settings.nextPeriodicTrigger(now: Instant, zone: ZoneId): Instant? {
    if (!periodic) return null
    val value = (now + frequency) - Duration.ofMillis(now.toEpochMilli() % frequency.toMillis())
    val localTime = value.atZone(zone).toLocalTime()
    if (localTime !in hours) return value.absPlus(hours.start, zone)
    return value
  }

  private fun Instant.absPlus(value: LocalTime, zone: ZoneId): Instant {
    val localTime = atZone(zone).toLocalTime()
    val duration =
      Duration.between(localTime, value).let { if (it.isNegative) it.plusDays(1) else it }
    return this + duration
  }

  companion object {
    private const val TAG = "Scheduler"
  }
}
