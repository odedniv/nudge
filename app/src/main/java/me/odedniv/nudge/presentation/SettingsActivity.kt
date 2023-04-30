package me.odedniv.nudge.presentation

import android.Manifest.permission
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import me.odedniv.nudge.Notifications
import me.odedniv.nudge.Settings

class SettingsActivity : ComponentActivity() {
  private lateinit var requestPostNotificationsLauncher: ActivityResultLauncher<String>
  private var pendingSettings: Settings? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    requestPostNotificationsLauncher = registerForActivityResult(RequestPermission()) { granted ->
      if (granted) {
        pendingSettings?.write()
      } else {
        setContent() // Rollback
      }
    }
    Notifications(this).createChannels()
    setContent()
  }

  private fun setContent() {
    setContent {
      SettingsView(
        initialSettings = Settings.read(this),
        onUpdate = { it.checkPermissionsAndWrite() },
      )
    }
  }

  private fun Settings.checkPermissionsAndWrite() {
    if (
      !runningNotification ||
      Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
      checkSelfPermission(permission.POST_NOTIFICATIONS) == PERMISSION_GRANTED
    ) {
      write()
      return
    }
    pendingSettings = this
    requestPostNotificationsLauncher.launch(permission.POST_NOTIFICATIONS)
  }

  private fun Settings.write() {
    write(this@SettingsActivity)
  }
}
