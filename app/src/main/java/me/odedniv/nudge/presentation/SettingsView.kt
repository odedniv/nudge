package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import java.time.Duration
import me.odedniv.nudge.R
import me.odedniv.nudge.Settings
import me.odedniv.nudge.presentation.theme.NudgeTheme

private val CHIP_MODIFIER = Modifier
  .fillMaxWidth()
  .padding(4.dp)

@Composable
fun SettingsView(value: Settings, onUpdate: (Settings) -> Unit) {
  NudgeTheme {
    var settings by remember { mutableStateOf(value) }
    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showVibrationDialog by remember { mutableStateOf(false) }

    if (showFrequencyDialog) {
      FrequencyDialog(
        value = settings.frequency,
        onDismiss = {
          showFrequencyDialog = false
          if (it == null) return@FrequencyDialog
          settings = settings.copy(frequency = it).also(onUpdate)
        }
      )
    }

    if (showVibrationDialog) {
      VibrationDialog(
        value = settings.vibration,
        onDismiss = {
          showVibrationDialog = false
          settings = settings.copy(vibration = it).also(onUpdate)
        }
      )
    }

    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
    ) {
      item {
        Text(
          text = stringResource(R.string.settings_subtitle),
          modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
          textAlign = TextAlign.Center,
        )
      }
      item {
        StartedChip(
          value = settings.started,
          onUpdate = { settings = settings.copy(started = it).also(onUpdate) },
        )
      }
      item {
        RunningNotificationChip(
          value = settings.runningNotification,
          onUpdate = {
            settings = settings.copy(runningNotification = it).also(onUpdate)
          },
        )
      }
      item {
        FrequencyChip(
          value = settings.frequency,
          onShowFrequencyDialog = { showFrequencyDialog = true },
        )
      }
      item {
        VibrationChip(
          onShowVibrationDialog = { showVibrationDialog = true },
        )
      }
    }
  }
}

@Composable
private fun StartedChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  ToggleChip(
    checked = value,
    onCheckedChange = onUpdate,
    label = {
      ChipLabel(stringResource(if (value) R.string.settings_stop else R.string.settings_start))
    },
    toggleControl = {
      Switch(checked = value)
    },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun RunningNotificationChip(value: Boolean, onUpdate: (Boolean) -> Unit) {
  ToggleChip(
    checked = value,
    onCheckedChange = { onUpdate(it) },
    label = {
      ChipLabel(stringResource(R.string.settings_running_notification))
    },
    toggleControl = {
      Switch(checked = value)
    },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun FrequencyChip(value: Duration, onShowFrequencyDialog: () -> Unit) {
  Chip(
    onClick = onShowFrequencyDialog,
    label = {
      ChipLabel(stringResource(R.string.settings_frequency))
    },
    secondaryLabel = {
      ChipLabel(stringResource(R.string.settings_frequency_minutes, value.toMinutes()))
    },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun VibrationChip(onShowVibrationDialog: () -> Unit) {
  Chip(
    onClick = onShowVibrationDialog,
    label = {
      ChipLabel(stringResource(R.string.settings_vibration))
    },
    modifier = CHIP_MODIFIER,
  )
}

@Composable
private fun ChipLabel(text: String) {
  Text(
    text = text,
    modifier = Modifier.fillMaxWidth(),
  )
}

@Preview(showBackground = true, widthDp = 227, heightDp = 227)
@Composable
fun SettingsViewPreview() {
  NudgeTheme {
    SettingsView(Settings.DEFAULT, onUpdate = {})
  }
}
