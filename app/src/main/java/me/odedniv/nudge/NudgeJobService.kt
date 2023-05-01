package me.odedniv.nudge

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import java.time.Instant
import java.time.LocalTime
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NudgeJobService : JobService() {
  override fun onStartJob(params: JobParameters): Boolean {
    val scheduleTime = Instant.ofEpochSecond(params.extras.getLong(EXTRA_SCHEDULE_TIME_SECONDS))
    val settings = Settings.read(this)

    if (!settings.shouldNudge(scheduleTime)) {
      Log.i(TAG, "Skipped nudge for $settings.")
      return false
    }

    Log.i(TAG, "Nudging for $settings.")
    CoroutineScope(Dispatchers.Main).launch {
      settings.vibration.execute(this@NudgeJobService)
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

    private const val TAG = "NudgeJobService"
  }
}