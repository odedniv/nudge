package me.odedniv.nudge.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import kotlin.math.roundToInt
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Hours
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.allDays
import me.odedniv.nudge.logic.asString
import me.odedniv.nudge.logic.format
import me.odedniv.nudge.ui.theme.NudgeTheme

@Composable
fun SettingsView(
  value: Settings,
  onUpdate: (Settings) -> Unit,
  onVibrationUpdate: (Vibration) -> Unit,
) {
  var showOneOffDialog by remember { mutableStateOf(false) }
  var showFrequencyDialog by remember { mutableStateOf(false) }
  var showHoursStartDialog by remember { mutableStateOf(false) }
  var showHoursEndDialog by remember { mutableStateOf(false) }
  var showDaysDialog by remember { mutableStateOf(false) }
  var showVibrationDialog by remember { mutableStateOf(false) }

  val context = LocalContext.current

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    Column(
      modifier = Modifier.padding(innerPadding).fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      // one-off
      OneOffChip(value = value.oneOff, onClick = { showOneOffDialog = true })
      // periodic title
      TitleText(R.string.settings_periodic_title)
      HorizontalDivider()
      // periodic toggle
      PeriodicChip(
        value = value.periodic,
        oneOff = value.oneOff,
        onUpdate = { onUpdate(value.copy(periodic = it)) },
      )
      // runningNotification
      HorizontalDivider()
      RunningNotificationChip(
        value = value.runningNotification,
        onUpdate = { onUpdate(value.copy(runningNotification = it)) },
      )
      HorizontalDivider()
      // frequency
      FrequencyChip(value = value.frequency, onClick = { showFrequencyDialog = true })
      HorizontalDivider()
      // hours
      HoursChip(
        value = value.hours,
        onClickStart = { showHoursStartDialog = true },
        onClickEnd = { showHoursEndDialog = true },
      )
      HorizontalDivider()
      // days
      DaysChip(value = value.days, onClick = { showDaysDialog = true })
      HorizontalDivider()
      // common title
      TitleText(R.string.settings_common_title)
      // vibration
      HorizontalDivider()
      VibrationChip(value = value.vibration, onClick = { showVibrationDialog = true })
      HorizontalDivider()
    }
    // one-off dialog
    OneOffDialog(
      showDialog = showOneOffDialog,
      value = value.oneOff,
      onUpdate = { onUpdate(value.copy(oneOff = it)) },
      onDismiss = { showOneOffDialog = false },
    )
    // frequency dialog
    DurationDialog(
      showDialog = showFrequencyDialog,
      showSeconds = Settings.DEBUG,
      title = R.string.settings_frequency,
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
    )
    // hours.start dialog
    LocalTimeDialog(
      showDialog = showHoursStartDialog,
      showSeconds = false,
      title = R.string.settings_hours,
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
    )
    // hours.end dialog
    LocalTimeDialog(
      showDialog = showHoursEndDialog,
      showSeconds = false,
      title = R.string.settings_hours,
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
    )
    // days dialog
    DaysDialog(
      showDialog = showDaysDialog,
      value = value.days,
      onUpdate = { onUpdate(value.copy(days = it)) },
      onDismiss = { showDaysDialog = false },
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
    )
  }
}

@Composable
private fun OneOffChip(value: Settings.OneOff, onClick: () -> Unit) {
  var now by remember { mutableStateOf(Instant.now()) }
  OneOffTimer(value, now) { now = it }
  val isRunning = value.isRunning(now)
  val elapsed = value.elapsed(now)

  Button(
    onClick = onClick,
    colors =
      if (isRunning) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 12.dp, end = 12.dp),
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(stringResource(R.string.settings_one_off))
      if (elapsed != null) {
        Text(
          stringResource(R.string.settings_one_off_running, elapsed.format(), value.total.format())
        )
      }
    }
  }
}

@Composable
private fun PeriodicChip(value: Boolean, oneOff: Settings.OneOff, onUpdate: (Boolean) -> Unit) {
  var now by remember { mutableStateOf(Instant.now()) }
  OneOffTimer(oneOff, now) { now = it }
  val oneOffIsEnabled = oneOff.isEnabled(now)

  LabeledSwitch(
    checked = value,
    onCheckedChange = onUpdate,
    enabled = !oneOffIsEnabled,
    label = {
      Text(
        if (!value) stringResource(R.string.settings_periodic_start)
        else stringResource(R.string.settings_periodic_stop)
      )
    },
  )
}

@Composable
private fun RunningNotificationChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  LabeledSwitch(
    checked = value,
    onCheckedChange = onUpdate,
    label = { Text(stringResource(R.string.settings_running_notification)) },
  )
}

@Composable
private fun FrequencyChip(value: Duration, onClick: () -> Unit) {
  TextButton(onClick = onClick) {
    Text(stringResource(R.string.settings_frequency), modifier = Modifier.weight(1f))
    Text(stringResource(R.string.settings_frequency_description, value.toMinutes()))
  }
}

@Composable
private fun HoursChip(value: Hours, onClickStart: () -> Unit, onClickEnd: () -> Unit) {
  Row(
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.padding(horizontal = 12.dp),
  ) {
    Text(
      stringResource(R.string.settings_hours),
      modifier = Modifier.weight(1f),
      style = MaterialTheme.typography.labelLarge,
    )
    Button(onClick = onClickStart) { Text(value.start.toString()) }
    Text("-", modifier = Modifier.padding(horizontal = 8.dp))
    Button(onClick = onClickEnd) { Text(value.end.toString()) }
  }
}

@Composable
private fun DaysChip(value: Set<DayOfWeek>, onClick: () -> Unit) {
  TextButton(onClick = onClick) {
    Text(stringResource(R.string.settings_days), modifier = Modifier.weight(1f))
    Text(
      allDays()
        .filter { it in value }
        .map { stringArrayResource(R.array.settings_days_names)[it.value - 1] }
        .joinToString(", ")
    )
  }
}

@Composable
private fun VibrationChip(value: Vibration, onClick: () -> Unit) {
  TextButton(onClick = onClick) {
    Text(stringResource(R.string.settings_vibration), modifier = Modifier.weight(1f))
    Text(
      stringResource(
        R.string.settings_vibration_description,
        value.pattern.asString(),
        (value.multiplier * 100).roundToInt(),
      )
    )
  }
}

@Composable
fun LabeledSwitch(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  enabled: Boolean = true,
  label: @Composable () -> Unit,
) {
  TextButton(onClick = { onCheckedChange(!checked) }, enabled = enabled) {
    Box(modifier = Modifier.weight(1f)) { label() }
    Switch(checked = checked, enabled = enabled, onCheckedChange = onCheckedChange)
  }
}

private fun toastMinimumFrequency(context: Context) {
  Toast.makeText(
      context,
      context.getString(
        R.string.settings_frequency_minimum_toast,
        Settings.MINIMUM_FREQUENCY.toMinutes(),
      ),
      Toast.LENGTH_SHORT,
    )
    .show()
}

private fun toastHoursMustBeBefore(context: Context, value: LocalTime) {
  Toast.makeText(
      context,
      context.getString(R.string.settings_hours_must_be_before, value),
      Toast.LENGTH_SHORT,
    )
    .show()
}

private fun toastHoursMustBeAfter(context: Context, value: LocalTime) {
  Toast.makeText(
      context,
      context.getString(R.string.settings_hours_must_be_after, value),
      Toast.LENGTH_SHORT,
    )
    .show()
}

@Preview
@Composable
fun SettingsPreview() {
  var value by remember { mutableStateOf(Settings.DEFAULT) }
  NudgeTheme { SettingsView(value = value, onUpdate = { value = it }, onVibrationUpdate = {}) }
}
