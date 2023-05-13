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
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration

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

    Notifications(this).createChannels()
    val initialSettings = readSettings()

    val vibrationExecutor =
      MutableSharedFlow<Vibration>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    lifecycleScope.launch { vibrationExecutor.collect { it.execute(this@SettingsActivity) } }

    setContent {
      var settings by remember { mutableStateOf(initialSettings) }
      ObserveEventChange { event ->
        if (event == Lifecycle.Event.ON_RESUME) settings = readSettings()
      }

      SettingsView(
        value = settings,
        onUpdate = {
          if (it.requestPermissions()) return@SettingsView
          settings = it.apply { write() }
        },
        onVibrationUpdate = { vibrationExecutor.tryEmit(it) },
      )
    }
  }

  private fun readSettings() =
    Settings.read(this@SettingsActivity).also { it.commit(this@SettingsActivity) }

  private fun Settings.requestPermissions(): Boolean {
    if (started) {
      if (!canScheduleExactAlarm) {
        pendingSettings.set(this)
        requestPermissionIntentLauncher.launch(
          Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).setData(Uri.parse("package:$packageName"))
        )
        return true
      }
    }
    if (runningNotification && !canPostNotifications) {
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

  private fun Settings.write() {
    write(this@SettingsActivity)
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
