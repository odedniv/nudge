package me.odedniv.nudge.presentation

import android.content.Context
import android.content.Intent
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
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Hours
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.presentation.theme.NudgeTheme

private val CHIP_MODIFIER = Modifier.fillMaxWidth().padding(4.dp)

@Composable
fun SettingsView(value: Settings, onUpdate: (Settings) -> Unit) {
  NudgeTheme {
    var settings by remember { mutableStateOf(value) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showHoursStartDialog by remember { mutableStateOf(false) }
    var showHoursEndDialog by remember { mutableStateOf(false) }

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
      // settings.started
      item {
        StartedChip(
          value = settings.started,
          onUpdate = { settings = settings.copy(started = it).also(onUpdate) },
        )
      }
      // settings.runningNotification
      item {
        RunningNotificationChip(
          value = settings.runningNotification,
          onUpdate = { settings = settings.copy(runningNotification = it).also(onUpdate) },
        )
      }
      // settings.frequency
      item {
        FrequencyChip(
          value = settings.frequency,
          onClick = { showFrequencyDialog = true },
        )
      }
      // settings.hours
      item {
        HoursChip(
          value = settings.hours,
          onClickStart = { showHoursStartDialog = true },
          onClickEnd = { showHoursEndDialog = true },
        )
      }
      // settings.vibration
      item {
        VibrationChip(
          onClick = {
            context.startActivity(Intent(context, VibrationActivity::class.java))
          },
        )
      }
    }
    // settings.frequency dialog
    DurationDialog(
      showDialog = showFrequencyDialog,
      value = settings.frequency,
      onConfirm = {
        if (it == null) {
          showFrequencyDialog = false
          return@DurationDialog
        }
        if (it < Settings.MINIMUM_FREQUENCY) {
          toastMinimumFrequency(context)
          return@DurationDialog
        }
        settings = settings.copy(frequency = it).also(onUpdate)
        showFrequencyDialog = false
      },
      scrollState = scrollState,
    )
    // settings.hours.start dialog
    LocalTimeDialog(
      showDialog = showHoursStartDialog,
      value = settings.hours.start,
      onConfirm = {
        if (it == null) {
          showHoursStartDialog = false
          return@LocalTimeDialog
        }
        if (it >= settings.hours.end) {
          toastHoursMustBeBefore(context, settings.hours.end)
          return@LocalTimeDialog
        }
        settings = settings.copy(hours = settings.hours.copy(start = it)).also(onUpdate)
        showHoursStartDialog = false
      },
      scrollState = scrollState,
    )
    // settings.hours.end dialog
    LocalTimeDialog(
      showDialog = showHoursEndDialog,
      value = settings.hours.end,
      onConfirm = {
        if (it == null) {
          showHoursEndDialog = false
          return@LocalTimeDialog
        }
        if (it <= settings.hours.start) {
          toastHoursMustBeAfter(context, settings.hours.start)
          return@LocalTimeDialog
        }
        settings = settings.copy(hours = settings.hours.copy(end = it)).also(onUpdate)
        showHoursEndDialog = false
      },
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
    onCheckedChange = { onUpdate(it) },
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
private fun VibrationChip(onClick: () -> Unit) {
  Chip(
    onClick = onClick,
    label = { Text(stringResource(R.string.settings_vibration)) },
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
  NudgeTheme { SettingsView(Settings.DEFAULT, onUpdate = {}) }
}
