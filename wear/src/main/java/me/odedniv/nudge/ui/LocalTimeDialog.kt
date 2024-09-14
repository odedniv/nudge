package me.odedniv.nudge.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.tooling.preview.devices.WearDevices
import com.google.android.horologist.composables.TimePicker
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.ui.theme.NudgeTheme

@Composable
fun LocalTimeDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(showDialog = showDialog, onDismissRequest = onDismiss) {
    LocalTimeView(showSeconds = showSeconds, value = value, onConfirm = onConfirm)
  }
}

@Composable
fun DurationDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  value: Duration,
  onConfirm: (Duration) -> Unit,
  onDismiss: () -> Unit,
) {
  LocalTimeDialog(
    showDialog = showDialog,
    showSeconds = showSeconds,
    value = LocalTime.MIN + value,
    onConfirm = { onConfirm(Duration.between(LocalTime.MIN, it)) },
    onDismiss = onDismiss,
  )
}

@Composable
private fun LocalTimeView(showSeconds: Boolean, value: LocalTime, onConfirm: (LocalTime) -> Unit) {
  TimePicker(time = value, onTimeConfirm = onConfirm, showSeconds = showSeconds)
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun LocalTimePreview() {
  NudgeTheme { LocalTimeView(showSeconds = true, value = LocalTime.of(12, 34), onConfirm = {}) }
}
