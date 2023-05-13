package me.odedniv.nudge.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.PowerManager.PARTIAL_WAKE_LOCK
import android.util.Log
import androidx.core.content.getSystemService
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings

class NudgeReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val powerManager: PowerManager = requireNotNull(context.getSystemService())
    val settings = Settings.read(context)

    Log.i(TAG, "Nudging for $settings.")

    Notifications(context).nudge(settings.delay)

    val wakeLock =
      powerManager.newWakeLock(PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
        setReferenceCounted(false)
        acquire(settings.delay.toMillis())
      }

    CoroutineScope(Dispatchers.Main).launch {
      settings.vibration.execute(context)
      settings.commit(context)
      wakeLock.release()
    }
  }

  private val Settings.delay: Duration
    get() = vibration.duration + DELAY

  companion object {
    private val DELAY = Duration.ofSeconds(5)
    private const val TAG = "NudgeReceiver"
    private const val WAKE_LOCK_TAG = "nudge:$TAG"
  }
}
