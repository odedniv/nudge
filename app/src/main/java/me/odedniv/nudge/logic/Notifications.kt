package me.odedniv.nudge.logic

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.content.getSystemService
import me.odedniv.nudge.R
import me.odedniv.nudge.presentation.SettingsActivity
import me.odedniv.nudge.services.ToggleReceiver

class Notifications(private val context: Context) {
  private val notificationManager: NotificationManager by lazy { requireNotNull(context.getSystemService()) }

  fun createChannels() {
    notificationManager.createNotificationChannel(
      NotificationChannel(
        RUNNING_CHANNEL_ID,
        context.getString(R.string.notification_started),
        NotificationManager.IMPORTANCE_LOW
      )
    )
  }

  fun running(started: Boolean) {
    notificationManager.notify(
      RUNNING_ID,
      Notification.Builder(context, RUNNING_CHANNEL_ID)
        .setContentTitle(
          context.getString(
            if (started) R.string.notification_started else R.string.notification_stopped
          )
        )
        .setContentText(context.getString(R.string.notification_text))
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

  private val startAction
    get() = Notification.Action.Builder(
      Icon.createWithResource(context, android.R.drawable.ic_media_play),
      context.getString(R.string.notification_start),
      togglePendingIntent(to = true),
    ).build()

  private val stopAction
    get() = Notification.Action.Builder(
      Icon.createWithResource(context, android.R.drawable.ic_media_pause),
      context.getString(R.string.notification_stop),
      togglePendingIntent(to = false),
    ).build()

  private fun togglePendingIntent(to: Boolean) = PendingIntent.getBroadcast(
    context,
    /* requestCode = */ 0,
    Intent(context, ToggleReceiver::class.java)
      .putExtra(ToggleReceiver.EXTRA_TO, to),
    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
  )

  private val settingsAction
    get() = Notification.Action.Builder(
      Icon.createWithResource(context, android.R.drawable.ic_menu_manage),
      context.getString(R.string.notification_settings),
      PendingIntent.getActivity(
        context, /* requestCode = */
        0,
        Intent(context, SettingsActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )
    ).build()

  companion object {
    private const val RUNNING_CHANNEL_ID = "running"
    private const val RUNNING_ID = 1
  }
}