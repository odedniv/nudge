package me.odedniv.nudge.presentation

import android.Manifest.permission
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import me.odedniv.nudge.logic.Notifications
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration

class SettingsActivity : ComponentActivity() {
  private lateinit var requestPostNotificationsLauncher: ActivityResultLauncher<String>
  private var pendingSettings: Settings? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    requestPostNotificationsLauncher =
      registerForActivityResult(RequestPermission()) { granted ->
        if (granted) pendingSettings?.write()
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
        onUpdate = { if (it.checkPermissionsAndWrite()) settings = it },
        onVibrationUpdate = { vibrationExecutor.tryEmit(it) },
      )
    }
  }

  private fun readSettings() =
    Settings.read(this@SettingsActivity).also { it.commit(this@SettingsActivity) }

  private fun Settings.checkPermissionsAndWrite(): Boolean {
    if (
      !runningNotification ||
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        checkSelfPermission(permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
    ) {
      write()
      return true
    }
    pendingSettings = this
    requestPostNotificationsLauncher.launch(permission.POST_NOTIFICATIONS)
    return false
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
