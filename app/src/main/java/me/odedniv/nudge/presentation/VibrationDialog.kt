package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.tooling.preview.devices.WearDevices
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.VibrationPatternValue
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun VibrationDialog(
  showDialog: Boolean,
  value: Vibration,
  onUpdate: (Vibration) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(showDialog = showDialog, onDismissRequest = { onDismiss() }) {
    VibrationView(value = value, onUpdate = onUpdate)
  }
}

@Composable
private fun VibrationView(value: Vibration, onUpdate: (Vibration) -> Unit) {
  ScalingLazyColumn {
    item { HeaderText(R.string.vibration_title) }
    // multiplier
    item { SubtitleText(R.string.vibration_multiplier) }
    item {
      MultiplierSlider(
        value = value.multiplier,
        onUpdate = { onUpdate(value.copy(multiplier = it)) },
      )
    }
    // pattern
    item { SubtitleText(R.string.vibration_pattern) }
    for ((index, patternValue) in value.pattern.withIndex()) {
      item {
        PatternSlider(
          value = patternValue,
          onUpdate = { newPatternValue ->
            onUpdate(
              value.copy(
                pattern =
                  value.pattern.mapIndexed { i, v -> if (i == index) newPatternValue else v }
              )
            )
          },
        )
      }
    }
    // add/delete pattern
    item {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        if (value.pattern.size > 1) {
          RemoveButton(onClick = { onUpdate(value.copy(pattern = value.pattern.dropLast(1))) })
        }
        AddButton(
          onClick = { onUpdate(value.copy(pattern = value.pattern + value.pattern.last())) }
        )
      }
    }
  }
}

@Composable
private fun MultiplierSlider(value: Float, onUpdate: (Float) -> Unit) {
  ButtonSlider(
    value = value,
    options = listOf(0.5f to "50%", 0.75f to "75%", 1f to "100%"),
    onUpdate = onUpdate,
  )
}

@Composable
private fun PatternSlider(value: VibrationPatternValue, onUpdate: (VibrationPatternValue) -> Unit) {
  ButtonSlider(
    value = value,
    options = listOf(1 to "1", 2 to "2", 3 to "3"),
    onUpdate = { onUpdate(it) },
  )
}

@Composable
private fun <T> ButtonSlider(value: T, options: List<Pair<T, String>>, onUpdate: (T) -> Unit) {
  Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
    for ((optionValue, optionText) in options) {
      Button(
        onClick = { onUpdate(optionValue) },
        colors =
          if (value == optionValue) ButtonDefaults.primaryButtonColors()
          else ButtonDefaults.secondaryButtonColors(),
        modifier = Modifier.padding(horizontal = 2.dp),
      ) {
        Text(optionText)
      }
    }
  }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Add,
      contentDescription = stringResource(R.string.vibration_pattern_add),
    )
  }
}

@Composable
private fun RemoveButton(onClick: () -> Unit) {
  Button(onClick = onClick) {
    Icon(
      imageVector = Icons.Filled.Delete,
      contentDescription = stringResource(R.string.vibration_pattern_remove),
    )
  }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun VibrationAlertPreview() {
  NudgeTheme { VibrationView(value = Settings.DEFAULT.vibration, onUpdate = {}) }
}
