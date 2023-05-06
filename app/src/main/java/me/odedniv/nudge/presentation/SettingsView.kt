package me.odedniv.nudge.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.rememberScalingLazyListState
import java.time.Duration
import java.time.LocalTime
import kotlin.math.roundToInt
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Hours
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.presentation.theme.NudgeTheme

private val CHIP_MODIFIER = Modifier.fillMaxWidth().padding(4.dp)

@Composable
fun SettingsView(
  value: Settings,
  onUpdate: (Settings) -> Unit,
  onVibrationUpdate: (Vibration) -> Unit,
) {
  NudgeTheme {
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showHoursStartDialog by remember { mutableStateOf(false) }
    var showHoursEndDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScalingLazyListState()
    val context = LocalContext.current

    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
    ) {
      // Title
      item {
        Text(
          text = stringResource(R.string.settings_subtitle),
          modifier = CHIP_MODIFIER,
          textAlign = TextAlign.Center,
        )
      }
      // started
      item {
        StartedChip(
          value = value.started,
          onUpdate = { onUpdate(value.copy(started = it)) },
        )
      }
      // runningNotification
      item {
        RunningNotificationChip(
          value = value.runningNotification,
          onUpdate = { onUpdate(value.copy(runningNotification = it)) },
        )
      }
      // frequency
      item {
        FrequencyChip(
          value = value.frequency,
          onClick = { showFrequencyDialog = true },
        )
      }
      // hours
      item {
        Text(
          text = stringResource(R.string.settings_hours),
          modifier = CHIP_MODIFIER,
          textAlign = TextAlign.Center,
        )
      }
      item {
        HoursChip(
          value = value.hours,
          onClickStart = { showHoursStartDialog = true },
          onClickEnd = { showHoursEndDialog = true },
        )
      }
      // vibration
      item {
        VibrationChip(
          value = value.vibration,
          onClick = { showVibrationDialog = true },
        )
      }
    }
    // frequency dialog
    DurationDialog(
      showDialog = showFrequencyDialog,
      value = value.frequency,
      onConfirm = {
        if (it == null) {
          showFrequencyDialog = false
          return@DurationDialog
        }
        if (it < Settings.MINIMUM_FREQUENCY) {
          toastMinimumFrequency(context)
          return@DurationDialog
        }
        onUpdate(value.copy(frequency = it))
        showFrequencyDialog = false
      },
      scrollState = scrollState,
    )
    // hours.start dialog
    LocalTimeDialog(
      showDialog = showHoursStartDialog,
      value = value.hours.start,
      onConfirm = {
        if (it == null) {
          showHoursStartDialog = false
          return@LocalTimeDialog
        }
        if (it >= value.hours.end) {
          toastHoursMustBeBefore(context, value.hours.end)
          return@LocalTimeDialog
        }
        onUpdate(value.copy(hours = value.hours.copy(start = it)))
        showHoursStartDialog = false
      },
      scrollState = scrollState,
    )
    // hours.end dialog
    LocalTimeDialog(
      showDialog = showHoursEndDialog,
      value = value.hours.end,
      onConfirm = {
        if (it == null) {
          showHoursEndDialog = false
          return@LocalTimeDialog
        }
        if (it <= value.hours.start) {
          toastHoursMustBeAfter(context, value.hours.start)
          return@LocalTimeDialog
        }
        onUpdate(value.copy(hours = value.hours.copy(end = it)))
        showHoursEndDialog = false
      },
      scrollState = scrollState,
    )
    // vibration dialog
    VibrationDialog(
      showDialog = showVibrationDialog,
      value = value.vibration,
      onUpdate = {
        onUpdate(value.copy(vibration = it))
        onVibrationUpdate(it)
      },
      onDismiss = { showVibrationDialog = false },
      scrollState = scrollState,
    )
  }
}

@Composable
private fun StartedChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  ToggleChip(
    checked = value,
    onCheckedChange = onUpdate,
    label = {
      Text(stringResource(if (value) R.string.settings_stop else R.string.settings_start))
    },
    toggleControl = { Switch(checked = value) },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun RunningNotificationChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  ToggleChip(
    checked = value,
    onCheckedChange = onUpdate,
    label = { Text(stringResource(R.string.settings_running_notification)) },
    toggleControl = { Switch(checked = value) },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun FrequencyChip(value: Duration, onClick: () -> Unit) {
  Chip(
    onClick = onClick,
    label = { Text(stringResource(R.string.settings_frequency)) },
    secondaryLabel = {
      Text(stringResource(R.string.settings_frequency_description, value.toMinutes()))
    },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun HoursChip(value: Hours, onClickStart: () -> Unit, onClickEnd: () -> Unit) {
  Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
    modifier = CHIP_MODIFIER,
  ) {
    Chip(
      onClick = onClickStart,
      label = { Text(value.start.toString()) },
    )
    Text("-")
    Chip(
      onClick = onClickEnd,
      label = { Text(value.end.toString()) },
    )
  }
}

@Composable
private fun VibrationChip(value: Vibration, onClick: () -> Unit) {
  Chip(
    onClick = onClick,
    label = { Text(stringResource(R.string.settings_vibration)) },
    secondaryLabel = {
      Text(
        stringResource(
          R.string.settings_vibration_description,
          stringResource(value.styleResourceId),
          (100.0 * value.amplitudeMultiplier).roundToInt(),
          value.duration.toMillis() / 1000.0,
        )
      )
    },
    modifier = CHIP_MODIFIER,
  )
}

private fun toastMinimumFrequency(context: Context) {
  Toast.makeText(
      context,
      context.getString(
        R.string.settings_frequency_minimum_toast,
        Settings.MINIMUM_FREQUENCY.toMinutes()
      ),
      Toast.LENGTH_SHORT
    )
    .show()
}

private fun toastHoursMustBeBefore(context: Context, value: LocalTime) {
  Toast.makeText(
      context,
      context.getString(R.string.settings_hours_must_be_before, value),
      Toast.LENGTH_SHORT
    )
    .show()
}

private fun toastHoursMustBeAfter(context: Context, value: LocalTime) {
  Toast.makeText(
      context,
      context.getString(R.string.settings_hours_must_be_after, value),
      Toast.LENGTH_SHORT
    )
    .show()
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun SettingsViewPreview() {
  NudgeTheme { SettingsView(Settings.DEFAULT, onUpdate = {}, onVibrationUpdate = {}) }
}
