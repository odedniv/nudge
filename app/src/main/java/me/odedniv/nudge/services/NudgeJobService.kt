package me.odedniv.nudge.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
import android.util.Log
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings

class NudgeJobService : JobService() {
  override fun onStartJob(params: JobParameters): Boolean {
    val scheduleTime = Instant.ofEpochSecond(params.extras.getLong(EXTRA_SCHEDULE_TIME_SECONDS))
    val settings = Settings.read(this)

    if (!settings.shouldNudge(scheduleTime)) {
      Log.i(TAG, "Skipped nudge for $settings.")
      return false
    }

    val notifications = Notifications(this)
    startForeground(
      Notifications.NUDGE_ID,
      notifications.nudgeNotification(),
      FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
    )

    Log.i(TAG, "Nudging for $settings.")
    CoroutineScope(Dispatchers.Main).launch {
      settings.vibration.execute(this@NudgeJobService)
      delay(DELAY.toMillis())
      jobFinished(params, /* wantsReschedule = */ false)
    }
    return true
  }

  override fun onStopJob(params: JobParameters) = false

  private fun Settings.shouldNudge(scheduleTime: Instant): Boolean =
    (Instant.now() > scheduleTime + frequency) &&
      (LocalTime.now() in hours.start.rangeTo(hours.end))

  companion object {
    const val EXTRA_SCHEDULE_TIME_SECONDS = "schedule_time_seconds"

    private val DELAY = Duration.ofSeconds(5)

    private const val TAG = "NudgeJobService"
  }
}
