package me.odedniv.nudge.presentation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.android.horologist.composables.TimePicker
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.R
import me.odedniv.nudge.Settings
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun FrequencyDialog(frequency: Duration, onDismiss: (Duration?) -> Unit) {
  val scrollState = rememberScalingLazyListState()
  Dialog(
    showDialog = true,
    onDismissRequest = { onDismiss(null) },
    scrollState = scrollState,
  ) {
    FrequencyAlert(
      frequency = frequency,
      onDismiss = onDismiss,
      scrollState = scrollState,
    )
  }
}

@Composable
private fun FrequencyAlert(
  frequency: Duration,
  onDismiss: (Duration?) -> Unit,
  scrollState: ScalingLazyListState,
) {
  val context = LocalContext.current
  val frequencyMinimumToastString = stringResource(
    R.string.settings_frequency_minimum_toast,
    Settings.MINIMUM_FREQUENCY.toMinutes()
  )
  Alert(
    title = {
      Text(
        text = stringResource(R.string.settings_frequency),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
      )
    },
    message = {
      Text(
        text = stringResource(R.string.settings_frequency_message),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
      )
    },
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    scrollState = scrollState,
  ) {
    item {
      TimePicker(
        time = LocalTime.MIN + frequency,
        onTimeConfirm = {
          val newFrequency = Duration.between(LocalTime.MIN, it)
          if (newFrequency < Settings.MINIMUM_FREQUENCY) {
            Toast.makeText(context, frequencyMinimumToastString, Toast.LENGTH_SHORT).show()
            return@TimePicker
          }
          onDismiss(newFrequency)
        },
        modifier = Modifier.fillMaxWidth(),
        showSeconds = false,
      )
    }
  }
}

@Preview(showBackground = true, widthDp = 227, heightDp = 227)
@Composable
fun FrequencyDialogPreview() {
  NudgeTheme {
    FrequencyAlert(
      frequency = Settings.DEFAULT.frequency,
      onDismiss = {},
      scrollState = rememberScalingLazyListState(),
    )
  }
}
