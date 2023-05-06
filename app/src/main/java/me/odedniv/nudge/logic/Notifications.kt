package me.odedniv.nudge.logic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import me.odedniv.nudge.R
import me.odedniv.nudge.presentation.SettingsActivity
import me.odedniv.nudge.services.ToggleReceiver

class Notifications(private val context: Context) {
  private val notificationManager: NotificationManager by lazy {
    requireNotNull(context.getSystemService())
  }

  fun createChannels(settings: Settings) {
    notificationManager.createNotificationChannel(
      NotificationChannel(
        RUNNING_CHANNEL_ID,
        context.getString(R.string.notifications_running_started),
        IMPORTANCE_LOW
      )
    )
    // Re-create channel group to remove all previous channels.
    notificationManager.deleteNotificationChannelGroup(NUDGE_CHANNEL_GROUP_ID)
    NotificationChannelGroup(
        NUDGE_CHANNEL_GROUP_ID,
        context.getString(R.string.notifications_nudge),
      )
      .also { notificationManager.createNotificationChannelGroup(it) }
    // Create the channel.
    NotificationChannel(
        /* id = */ settings.vibration.id,
        /* name = */ settings.vibration.id,
        IMPORTANCE_HIGH,
      )
      .apply {
        group = NUDGE_CHANNEL_GROUP_ID
        vibrationPattern = settings.vibration.pattern
      }
      .also { notificationManager.createNotificationChannel(it) }
  }

  fun running(started: Boolean) {
    notificationManager.notify(
      RUNNING_ID,
      Notification.Builder(context, RUNNING_CHANNEL_ID)
        .setContentTitle(
          context.getString(
            if (started) R.string.notifications_running_started
            else R.string.notifications_running_stopped
          )
        )
        .setContentText(context.getString(R.string.notifications_running_text))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setAutoCancel(false)
        .setOngoing(true)
        .setActions(
          if (started) stopAction else startAction,
          settingsAction,
        )
        .build()
    )
  }

  fun cancelRunning() {
    notificationManager.cancel(RUNNING_ID)
  }

  fun nudge(vibration: Vibration) {
    notificationManager.notify(
      NUDGE_ID,
      Notification.Builder(context, /* channelId = */ vibration.id)
        .setContentTitle(context.getString(R.string.notifications_nudge))
        .setSmallIcon(R.mipmap.ic_launcher)
        .setCategory(Notification.CATEGORY_ALARM)
        .setTimeoutAfter((vibration.duration + NUDGE_TIMEOUT_BUFFER).toMillis())
        .setActions(stopAction, settingsAction)
        .setAutoCancel(true)
        // Unused except to show heads-up display.
        .setFullScreenIntent(headsUpPendingIntent, /* highPriority = */ true)
        .build()
    )
  }

  private val startAction
    get() =
      Notification.Action.Builder(
          Icon.createWithResource(context, android.R.drawable.ic_media_play),
          context.getString(R.string.notifications_running_start),
          togglePendingIntent(to = true),
        )
        .build()

  private val stopAction
    get() =
      Notification.Action.Builder(
          Icon.createWithResource(context, android.R.drawable.ic_media_pause),
          context.getString(R.string.notifications_running_stop),
          togglePendingIntent(to = false),
        )
        .build()

  private fun togglePendingIntent(to: Boolean) =
    PendingIntent.getBroadcast(
      context,
      /* requestCode = */ 0,
      Intent(context, ToggleReceiver::class.java).putExtra(ToggleReceiver.EXTRA_TO, to),
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

  private val headsUpPendingIntent
    get() =
      PendingIntent.getActivity(
        context,
        /* requestCode = */ 0,
        Intent(context, SettingsActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

  private val settingsAction
    get() =
      Notification.Action.Builder(
          Icon.createWithResource(context, android.R.drawable.ic_menu_manage),
          context.getString(R.string.notifications_running_settings),
          PendingIntent.getActivity(
            context,
            /* requestCode = */ 0,
            Intent(context, SettingsActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
          )
        )
        .build()

  companion object {
    private const val RUNNING_CHANNEL_ID = "running"
    private const val RUNNING_ID = 1

    private const val NUDGE_CHANNEL_GROUP_ID = "nudge"
    const val NUDGE_ID = 2

    private val NUDGE_TIMEOUT_BUFFER: Duration = 5.seconds.toJavaDuration()
  }
}
