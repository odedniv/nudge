package me.odedniv.nudge

import android.app.job.JobParameters
import android.app.job.JobService
import java.time.Instant

class NudgeJobService : JobService() {
  override fun onStartJob(params: JobParameters): Boolean {
    val settings = Settings.read(this)
    val scheduleTime = Instant.ofEpochSecond(params.extras.getLong(EXTRA_SCHEDULE_TIME_SECONDS))
    if (Instant.now() < scheduleTime + settings.frequency) return false

    settings.vibration.execute(this)
    return false
  }

  override fun onStopJob(params: JobParameters) = false

  companion object {
    const val EXTRA_SCHEDULE_TIME_SECONDS = "schedule_time_seconds"
  }
}