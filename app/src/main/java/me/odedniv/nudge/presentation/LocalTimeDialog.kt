package me.odedniv.nudge.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.dialog.Dialog
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
    LocalTimeView(
      value = value,
      onConfirm = onConfirm,
    )
  }
}

@Composable
private fun LocalTimeView(
  value: LocalTime,
  onConfirm: (LocalTime) -> Unit,
) {
  ScalingLazyColumn {
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
  LocalTimeDialog(
    showDialog = showDialog,
    value = LocalTime.MIN + value,
    onConfirm = { onConfirm(it?.let { Duration.between(LocalTime.MIN, it) }) },
    scrollState = scrollState,
  )
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun LocalTimeViewPreview() {
  NudgeTheme {
    LocalTimeView(
      value = LocalTime.of(12, 34),
      onConfirm = {},
    )
  }
}
