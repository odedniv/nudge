package me.odedniv.nudge.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.R
import me.odedniv.nudge.ui.theme.NudgeTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalTimeDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  title: Int,
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
  onDismiss: () -> Unit,
) {
  if (!showDialog) return
  val state =
    rememberTimePickerState(
      initialHour = value.hour,
      initialMinute = value.minute,
      initialSecond = value.second,
      showSeconds = showSeconds,
      is24Hour = true,
    )
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute, state.second)) }) {
        Text(stringResource(R.string.dialog_confirm))
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_dismiss)) }
    },
    title = { Text(stringResource(title)) },
    text = { TimeInput(state) },
  )
}

@Composable
fun DurationDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  title: Int,
  value: Duration,
  onConfirm: (Duration) -> Unit,
  onDismiss: () -> Unit,
) {
  LocalTimeDialog(
    showDialog = showDialog,
    showSeconds = showSeconds,
    title = title,
    value = LocalTime.MIN + value,
    onConfirm = { onConfirm(Duration.between(LocalTime.MIN, it)) },
    onDismiss = onDismiss,
  )
}

@Preview
@Composable
fun LocalTimePreview() {
  NudgeTheme {
    LocalTimeDialog(
      showDialog = true,
      showSeconds = false,
      title = R.string.app_name,
      value = LocalTime.of(12, 34),
      onConfirm = {},
      onDismiss = {},
    )
  }
}
