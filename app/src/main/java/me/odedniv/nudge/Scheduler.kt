package me.odedniv.nudge

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.PersistableBundle
import androidx.core.content.getSystemService
import java.time.Instant

class Scheduler(private val context: Context) {
  private val jobScheduler: JobScheduler by lazy { requireNotNull(context.getSystemService()) }
  private val notifications: Notifications by lazy { Notifications(context) }

  fun commit(settings: Settings) {
    if (settings.started) {
      jobScheduler.schedule(
        JobInfo.Builder(JOB_ID, ComponentName(context, NudgeJobService::class.java))
          .setPeriodic(settings.frequency.toMillis())
          .setPersisted(true)
          .setExtras(
            PersistableBundle().apply {
              putLong(NudgeJobService.EXTRA_SCHEDULE_TIME_SECONDS, Instant.now().epochSecond)
            }
          )
          .build()
      )
    } else {
      jobScheduler.cancel(JOB_ID)
    }
    if (settings.runningNotification) {
      notifications.running(settings.started)
    } else {
      notifications.cancelRunning()
    }
  }

  companion object {
    private const val JOB_ID = 1
  }
}