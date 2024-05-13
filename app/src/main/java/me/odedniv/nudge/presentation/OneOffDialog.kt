package me.odedniv.nudge.presentation

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListState
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.tooling.preview.devices.WearDevices
import java.time.Duration
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.format
import me.odedniv.nudge.logic.sum
import me.odedniv.nudge.presentation.theme.NudgeTheme

private val CHIP_MODIFIER = Modifier.fillMaxWidth().padding(4.dp)
private val BUTTON_MODIFIER =
  Modifier.size(ButtonDefaults.DefaultIconSize).wrapContentSize(align = Alignment.Center)

@Composable
fun OneOffDialog(
  showDialog: Boolean,
  value: Settings.OneOff,
  onUpdate: (Settings.OneOff) -> Unit,
  onDismiss: () -> Unit,
  scrollState: ScalingLazyListState,
) {
  Dialog(
    showDialog = showDialog,
    onDismissRequest = onDismiss,
    scrollState = scrollState,
  ) {
    OneOffView(
      value = value,
      onUpdate = onUpdate,
    )
  }
}

@Composable
private fun OneOffView(
  value: Settings.OneOff,
  onUpdate: (Settings.OneOff) -> Unit,
) {
  var showDurationDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScalingLazyListState()
  val context = LocalContext.current

  var now by remember { mutableStateOf(Instant.now()) }
  OneOffTimer(value, now) { now = it }
  val isEnabled = value.isEnabled(now)
  val isRunning = value.isRunning(now)
  val nextElapsedIndex = value.nextElapsedIndex(now)
  val elapsed = value.elapsed(now)

  ScalingLazyColumn {
    if (isEnabled) {
      // pause/resume
      item {
        Row(
          horizontalArrangement = Arrangement.SpaceEvenly,
          modifier = CHIP_MODIFIER,
        ) {
          if (value.pausedAt == null) {
            // pause
            PauseButton(
              onClick = {
                onUpdate(value.copy(pausedAt = Duration.between(value.startedAt, Instant.now())))
              },
            )
          } else {
            // resume
            ResumeButton(
              onClick = {
                onUpdate(value.copy(startedAt = Instant.now() - value.pausedAt, pausedAt = null))
              },
            )
          }
          // stop
          StopButton(
            onClick = { onUpdate(value.copy(startedAt = null, pausedAt = null)) },
          )
        }
      }
    } else if (value.durations.isNotEmpty()) {
      // start
      item { StartButton(onClick = { onUpdate(value.copy(startedAt = Instant.now())) }) }
    }
    // elapsed
    item {
      Text(
        if (isEnabled) {
          stringResource(
            R.string.settings_one_off_running,
            elapsed!!.format(),
            value.durations.sum().format()
          )
        } else {
          stringResource(R.string.settings_one_off_paused, value.total.format())
        },
        textAlign = TextAlign.Center,
        modifier = CHIP_MODIFIER,
      )
    }
    // durations title
    item {
      Text(
        text = stringResource(R.string.one_off_durations_title),
        modifier = CHIP_MODIFIER,
        textAlign = TextAlign.Center,
      )
    }
    // durations
    for ((index, duration) in value.durations.withIndex()) {
      item {
        DurationChip(
          value = duration,
          index = index,
          allowDelete = !isEnabled,
          past = nextElapsedIndex?.let { index < it } ?: false,
          onDelete = {
            onUpdate(value.copy(durations = value.durations.filterIndexed { i, _ -> i != index }))
          },
        )
      }
    }
    // add
    if (!isEnabled) {
      item { AddButton(onClick = { showDurationDialog = true }) }
    }
  }

  // duration dialog
  DurationDialog(
    showDialog = showDurationDialog,
    showSeconds = true,
    value = value.durations.lastOrNull() ?: Settings.DEFAULT.frequency,
    onConfirm = {
      if (it < Vibration.MAXIMUM_DURATION) {
        toastMinimumDuration(context)
        return@DurationDialog
      }
      onUpdate(value.copy(durations = value.durations + it, startedAt = null))
      showDurationDialog = false
    },
    onDismiss = { showDurationDialog = false },
    scrollState = scrollState,
  )
}

@Composable
private fun DurationChip(
  value: Duration,
  index: Int,
  allowDelete: Boolean,
  past: Boolean,
  onDelete: () -> Unit,
) {
  Chip(
    onClick = {},
    colors = if (past) ChipDefaults.primaryChipColors() else ChipDefaults.secondaryChipColors(),
    border = ChipDefaults.chipBorder(),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
      Text(
        text = stringResource(R.string.one_off_duration, index + 1, value.format()),
        modifier = Modifier.weight(1f),
      )
      Button(
        onClick = onDelete,
        // Reserving space.
        enabled = allowDelete,
        modifier = Modifier.alpha(if (allowDelete) 1f else 0f),
      ) {
        Icon(
          imageVector = Icons.Filled.Delete,
          contentDescription = stringResource(R.string.one_off_duration_delete),
          modifier = BUTTON_MODIFIER,
        )
      }
    }
  }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Add,
      contentDescription = stringResource(R.string.one_off_duration_add),
      modifier = BUTTON_MODIFIER,
    )
  }
}

@Composable
private fun PauseButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Pause,
      contentDescription = stringResource(R.string.one_off_pause),
      modifier = BUTTON_MODIFIER,
    )
  }
}

@Composable
private fun ResumeButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.PlayArrow,
      contentDescription = stringResource(R.string.one_off_resume),
      modifier = BUTTON_MODIFIER,
    )
  }
}

@Composable
private fun StopButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Stop,
      contentDescription = stringResource(R.string.one_off_stop),
      modifier = BUTTON_MODIFIER,
    )
  }
}

@Composable
private fun StartButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.PlayArrow,
      contentDescription = stringResource(R.string.one_off_start),
      modifier = BUTTON_MODIFIER,
    )
  }
}

private fun toastMinimumDuration(context: Context) {
  Toast.makeText(
      context,
      context.getString(
        R.string.one_off_minimum_duration_toast,
        Vibration.MAXIMUM_DURATION.seconds
      ),
      Toast.LENGTH_SHORT
    )
    .show()
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun SettingsOneOffViewPreview() {
  NudgeTheme {
    OneOffView(
      value =
        Settings.OneOff(
          startedAt = Instant.EPOCH,
          pausedAt = null,
          durations =
            listOf(5.minutes, 10.minutes + 3.seconds, 10.minutes).map { it.toJavaDuration() },
        ),
      onUpdate = {},
    )
  }
}
