package me.odedniv.nudge.presentation

import android.Manifest.permission
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.launch
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings

class SettingsActivity : ComponentActivity() {
  private val alarmManager: AlarmManager by lazy { requireNotNull(getSystemService()) }
  private lateinit var requestPermissionIntentLauncher: ActivityResultLauncher<Intent>
  private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
  private var pendingSettings: AtomicReference<Settings?> = AtomicReference(null)

  private val canScheduleExactAlarm: Boolean
    get() = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

  private val canPostNotifications: Boolean
    get() =
      Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    requestPermissionIntentLauncher =
      registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == RESULT_OK && pendingSettings.get()?.requestPermissions() == false) {
          tryWritePendingSettings()
        }
      }
    requestPermissionLauncher =
      registerForActivityResult(RequestPermission()) {
        if (it && pendingSettings.get()?.requestPermissions() == false) {
          tryWritePendingSettings()
        }
      }

    val initialSettings = Settings.commit(this)

    setContent {
      var settings by remember { mutableStateOf(initialSettings) }
      ObserveEventChange { event ->
        if (event == Lifecycle.Event.ON_RESUME) settings = Settings.commit(this)
      }

      SettingsView(
        value = settings,
        onUpdate = {
          if (it.requestPermissions()) return@SettingsView
          settings = it.apply { write() }
        },
        onVibrationUpdate = { lifecycleScope.launch { it.execute() } },
      )
    }
  }

  private fun Settings.requestPermissions(): Boolean {
    if (started && !canScheduleExactAlarm) {
      pendingSettings.set(this)
      requestPermissionIntentLauncher.launch(
        Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(Uri.parse("package:$packageName"))
      )
      return true
    }
    if ((started || runningNotification) && !canPostNotifications) {
      pendingSettings.set(this)
      requestPermissionLauncher.launch(permission.POST_NOTIFICATIONS)
      return true
    }
    return false
  }

  private fun tryWritePendingSettings() {
    val pendingSettings = this.pendingSettings.getAndSet(null) ?: return
    if (pendingSettings.started && !canScheduleExactAlarm) return
    if (pendingSettings.runningNotification && !canPostNotifications) return
    pendingSettings.write()
  }
}

@Composable
fun ObserveEventChange(onEvent: (Lifecycle.Event) -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event -> onEvent(event) }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }
}
