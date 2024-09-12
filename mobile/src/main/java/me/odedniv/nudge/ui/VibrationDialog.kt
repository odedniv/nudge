package me.odedniv.nudge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.VibrationPatternValue
import me.odedniv.nudge.ui.theme.NudgeTheme

@Composable
fun VibrationDialog(
  showDialog: Boolean,
  value: Vibration,
  onUpdate: (Vibration) -> Unit,
  onDismiss: () -> Unit,
) {
  if (!showDialog) return
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_done)) }
    },
    title = { Text(stringResource(R.string.vibration_title)) },
    text = { VibrationView(value = value, onUpdate = onUpdate) },
  )
}

@Composable
private fun VibrationView(value: Vibration, onUpdate: (Vibration) -> Unit) {
  LazyColumn(horizontalAlignment = Alignment.CenterHorizontally) {
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
  Row(horizontalArrangement = Arrangement.Center) {
    for ((optionValue, optionText) in options) {
      Button(
        onClick = { onUpdate(optionValue) },
        colors =
          if (value == optionValue) ButtonDefaults.buttonColors()
          else ButtonDefaults.filledTonalButtonColors(),
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

@Preview
@Composable
fun VibrationPreview() {
  var value by remember { mutableStateOf(Settings.DEFAULT.vibration) }
  NudgeTheme {
    VibrationDialog(showDialog = true, value = value, onUpdate = { value = it }, onDismiss = {})
  }
}
