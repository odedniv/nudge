package me.odedniv.nudge.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.tooling.preview.devices.WearDevices
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Hours
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.allDays
import me.odedniv.nudge.logic.format
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun SettingsView(
  value: Settings,
  onUpdate: (Settings) -> Unit,
  onVibrationUpdate: (Vibration) -> Unit,
) {
  NudgeTheme {
    var showOneOffDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showHoursStartDialog by remember { mutableStateOf(false) }
    var showHoursEndDialog by remember { mutableStateOf(false) }
    var showDaysDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScalingLazyListState()
    val context = LocalContext.current

    ScalingLazyColumn(anchorType = ScalingLazyListAnchorType.ItemStart) {
      // one-off
      item {
        OneOffChip(
          value = value.oneOff,
          onClick = { showOneOffDialog = true },
        )
      }
      // periodic title
      item { TitleText(R.string.settings_periodic_title) }
      // periodic toggle
      item {
        PeriodicChip(
          value = value.periodic,
          oneOff = value.oneOff,
          onUpdate = { onUpdate(value.copy(periodic = it)) },
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
      item { SubtitleText(R.string.settings_hours) }
      item {
        HoursChip(
          value = value.hours,
          onClickStart = { showHoursStartDialog = true },
          onClickEnd = { showHoursEndDialog = true },
        )
      }
      // days
      item { SubtitleText(R.string.settings_days) }
      item {
        DaysChip(
          value = value.days,
          onClick = { showDaysDialog = true },
        )
      }
      // common title
      item { TitleText(R.string.settings_common_title) }
      // vibration
      item {
        VibrationChip(
          value = value.vibration,
          onClick = { showVibrationDialog = true },
        )
      }
    }
    // one-off dialog
    OneOffDialog(
      showDialog = showOneOffDialog,
      value = value.oneOff,
      onUpdate = { onUpdate(value.copy(oneOff = it)) },
      onDismiss = { showOneOffDialog = false },
      scrollState = scrollState,
    )
    // frequency dialog
    DurationDialog(
      showDialog = showFrequencyDialog,
      showSeconds = Settings.DEBUG,
      value = value.frequency,
      onConfirm = {
        if (it < Settings.MINIMUM_FREQUENCY) {
          toastMinimumFrequency(context)
          return@DurationDialog
        }
        onUpdate(value.copy(frequency = it))
        showFrequencyDialog = false
      },
      onDismiss = { showFrequencyDialog = false },
      scrollState = scrollState,
    )
    // hours.start dialog
    LocalTimeDialog(
      showDialog = showHoursStartDialog,
      showSeconds = false,
      value = value.hours.start,
      onConfirm = {
        if (it >= value.hours.end) {
          toastHoursMustBeBefore(context, value.hours.end)
          return@LocalTimeDialog
        }
        onUpdate(value.copy(hours = value.hours.copy(start = it)))
        showHoursStartDialog = false
      },
      onDismiss = { showHoursStartDialog = false },
      scrollState = scrollState,
    )
    // hours.end dialog
    LocalTimeDialog(
      showDialog = showHoursEndDialog,
      showSeconds = false,
      value = value.hours.end,
      onConfirm = {
        if (it <= value.hours.start) {
          toastHoursMustBeAfter(context, value.hours.start)
          return@LocalTimeDialog
        }
        onUpdate(value.copy(hours = value.hours.copy(end = it)))
        showHoursEndDialog = false
      },
      onDismiss = { showHoursEndDialog = false },
      scrollState = scrollState,
    )
    // days dialog
    DaysDialog(
      showDialog = showDaysDialog,
      value = value.days,
      onUpdate = { onUpdate(value.copy(days = it)) },
      onDismiss = { showDaysDialog = false },
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
private fun OneOffChip(value: Settings.OneOff, onClick: () -> Unit) {
  var now by remember { mutableStateOf(Instant.now()) }
  OneOffTimer(value, now) { now = it }
  val isRunning = value.isRunning(now)
  val elapsed = value.elapsed(now)

  Chip(
    onClick = onClick,
    label = { Text(stringResource(R.string.settings_one_off)) },
    colors =
      if (isRunning) ChipDefaults.primaryChipColors() else ChipDefaults.secondaryChipColors(),
    secondaryLabel = {
      if (elapsed != null) {
        Text(
          stringResource(R.string.settings_one_off_running, elapsed.format(), value.total.format())
        )
      }
    },
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
private fun PeriodicChip(value: Boolean, oneOff: Settings.OneOff, onUpdate: (Boolean) -> Unit) {
  var now by remember { mutableStateOf(Instant.now()) }
  OneOffTimer(oneOff, now) { now = it }
  val oneOffIsEnabled = oneOff.isEnabled(now)

  ToggleChip(
    checked = value,
    onCheckedChange = onUpdate,
    enabled = !oneOffIsEnabled,
    label = {
      Text(
        if (!value) stringResource(R.string.settings_periodic_start)
        else stringResource(R.string.settings_periodic_stop)
      )
    },
    toggleControl = { Switch(checked = value) },
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun RunningNotificationChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  ToggleChip(
    checked = value,
    onCheckedChange = onUpdate,
    label = { Text(stringResource(R.string.settings_running_notification)) },
    toggleControl = { Switch(checked = value) },
    modifier = Modifier.fillMaxWidth(),
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
    modifier = Modifier.fillMaxWidth(),
  )
}

@Composable
private fun HoursChip(value: Hours, onClickStart: () -> Unit, onClickEnd: () -> Unit) {
  Row(
    horizontalArrangement = Arrangement.SpaceEvenly,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth(),
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
private fun DaysChip(value: Set<DayOfWeek>, onClick: () -> Unit) {
  Chip(
    onClick = onClick,
    label = { Text(stringResource(R.string.settings_days)) },
    secondaryLabel = {
      Text(
        allDays()
          .filter { it in value }
          .map { stringArrayResource(R.array.settings_days_names)[it.value - 1] }
          .joinToString(", ")
      )
    },
    modifier = Modifier.fillMaxWidth(),
  )
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
          value.activeDuration.toMillis() / 1000.0,
        )
      )
    },
    modifier = Modifier.fillMaxWidth(),
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

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SettingsViewPreview() {
  NudgeTheme {
    SettingsView(
      value = Settings.DEFAULT,
      onUpdate = {},
      onVibrationUpdate = {},
    )
  }
}
