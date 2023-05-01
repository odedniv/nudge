package me.odedniv.nudge.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.android.horologist.composables.TimePicker
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun LocalTimeDialog(
  showDialog: Boolean,
  value: LocalTime,
  onConfirm: (LocalTime?) -> Unit,
  scrollState: ScalingLazyListState,
) {
  Dialog(
    showDialog = showDialog,
    onDismissRequest = { onConfirm(null) },
    scrollState = scrollState,
  ) {
    LocalTimeAlert(
      value = value,
      onConfirm = onConfirm,
      scrollState = scrollState,
    )
  }
}

@Composable
private fun LocalTimeAlert(
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
  scrollState: ScalingLazyListState,
) {
  Alert(title = {}, scrollState = scrollState) {
    item {
      TimePicker(
        time = value,
        onTimeConfirm = onConfirm,
        showSeconds = false,
      )
    }
  }
}

@Composable
fun DurationDialog(
  showDialog: Boolean,
  value: Duration,
  onConfirm: (Duration?) -> Unit,
  scrollState: ScalingLazyListState,
) {
  Dialog(
    showDialog = showDialog,
    onDismissRequest = { onConfirm(null) },
    scrollState = scrollState,
  ) {
    DurationAlert(
      value = value,
      onConfirm = onConfirm,
      scrollState = scrollState,
    )
  }
}

@Composable
private fun DurationAlert(
  value: Duration,
  onConfirm: (Duration) -> Unit,
  scrollState: ScalingLazyListState
) {
  LocalTimeAlert(
    value = LocalTime.MIN + value,
    onConfirm = { onConfirm(Duration.between(LocalTime.MIN, it)) },
    scrollState = scrollState,
  )
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun LocalTimeAlertPreview() {
  NudgeTheme {
    LocalTimeAlert(
      value = LocalTime.of(12, 34),
      onConfirm = {},
      scrollState = rememberScalingLazyListState(),
    )
  }
}
