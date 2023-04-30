package me.odedniv.nudge

import android.app.job.JobParameters
import android.app.job.JobService
import java.time.Instant
import java.time.LocalTime

class NudgeJobService : JobService() {
  override fun onStartJob(params: JobParameters): Boolean {
    val scheduleTime = Instant.ofEpochSecond(params.extras.getLong(EXTRA_SCHEDULE_TIME_SECONDS))
    maybeNudge(scheduleTime)
    return false
  }

  override fun onStopJob(params: JobParameters) = false

  private fun maybeNudge(scheduleTime: Instant) {
    val settings = Settings.read(this)
    // Don't nudge right after scheduling.
    if (Instant.now() < scheduleTime + settings.frequency) return
    // Don't nudge outside hours.
    if (LocalTime.now() !in settings.hours.start.rangeTo(settings.hours.end)) return

    settings.vibration.execute(this)
  }

  companion object {
    const val EXTRA_SCHEDULE_TIME_SECONDS = "schedule_time_seconds"
  }
}