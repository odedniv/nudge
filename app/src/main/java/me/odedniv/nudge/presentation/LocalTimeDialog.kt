package me.odedniv.nudge.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.horologist.composables.TimePicker
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun LocalTimeDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
  onDismiss: () -> Unit,
  scrollState: ScalingLazyListState,
) {
  Dialog(
    showDialog = showDialog,
    onDismissRequest = onDismiss,
    scrollState = scrollState,
  ) {
    LocalTimeView(
      showSeconds = showSeconds,
      value = value,
      onConfirm = onConfirm,
    )
  }
}

@Composable
fun DurationDialog(
  showDialog: Boolean,
  showSeconds: Boolean,
  value: Duration,
  onConfirm: (Duration) -> Unit,
  onDismiss: () -> Unit,
  scrollState: ScalingLazyListState,
) {
  LocalTimeDialog(
    showDialog = showDialog,
    showSeconds = showSeconds,
    value = LocalTime.MIN + value,
    onConfirm = { onConfirm(Duration.between(LocalTime.MIN, it)) },
    onDismiss = onDismiss,
    scrollState = scrollState,
  )
}

@Composable
private fun LocalTimeView(
  showSeconds: Boolean,
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
) {
  ScalingLazyColumn {
    item {
      TimePicker(
        time = value,
        onTimeConfirm = onConfirm,
        showSeconds = showSeconds,
      )
    }
  }
}

@Preview(device = Devices.WEAR_OS_LARGE_ROUND)
@Composable
fun LocalTimeViewPreview() {
  NudgeTheme {
    LocalTimeView(
      showSeconds = true,
      value = LocalTime.of(12, 34),
      onConfirm = {},
    )
  }
}
