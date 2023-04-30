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
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.rememberScalingLazyListState
import com.google.android.horologist.composables.TimePicker
import java.time.Duration
import java.time.LocalTime
import me.odedniv.nudge.R
import me.odedniv.nudge.Settings
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun FrequencyAlert(
  value: Duration,
  onConfirm: (Duration) -> Unit,
  scrollState: ScalingLazyListState,
) {
  val frequencyMinimumToastString = stringResource(
    R.string.settings_frequency_minimum_toast,
    Settings.MINIMUM_FREQUENCY.toMinutes()
  )
  val context = LocalContext.current

  Alert(
    title = {
      Text(
        text = stringResource(R.string.settings_frequency),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    message = {
      Text(
        text = stringResource(R.string.settings_frequency_message),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    scrollState = scrollState,
  ) {
    item {
      TimePicker(
        time = LocalTime.MIN + value,
        onTimeConfirm = {
          val newFrequency = Duration.between(LocalTime.MIN, it)
          if (newFrequency < Settings.MINIMUM_FREQUENCY) {
            Toast.makeText(context, frequencyMinimumToastString, Toast.LENGTH_SHORT).show()
            return@TimePicker
          }
          onConfirm(newFrequency)
        },
        modifier = Modifier.fillMaxWidth(),
        showSeconds = false,
      )
    }
  }
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun FrequencyAlertPreview() {
  NudgeTheme {
    FrequencyAlert(
      value = Settings.DEFAULT.frequency,
      onConfirm = {},
      scrollState = rememberScalingLazyListState(),
    )
  }
}
