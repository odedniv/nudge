package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.dialog.Dialog
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun VibrationDialog(
  showDialog: Boolean,
  value: Vibration,
  onUpdate: (Vibration) -> Unit,
  onDismiss: () -> Unit,
  scrollState: ScalingLazyListState,
) {
  Dialog(
    showDialog = showDialog,
    onDismissRequest = { onDismiss() },
    scrollState = scrollState,
  ) {
    VibrationView(
      value = value,
      onUpdate = onUpdate,
    )
  }
}

@Composable
fun VibrationView(value: Vibration, onUpdate: (Vibration) -> Unit) {
  NudgeTheme {
    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
    ) {
      // Title
      item {
        Text(
          text = stringResource(R.string.vibration_title),
          modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
          textAlign = TextAlign.Center,
        )
      }
      // duration
      item {
        Text(
          text = stringResource(R.string.vibration_duration),
          modifier = Modifier.fillMaxWidth().padding(4.dp),
          textAlign = TextAlign.Center,
        )
      }
      item {
        PercentSlider(
          value = value.durationMultiplier,
          valueProgression = 25..100 step 25,
          decreaseContentDescription = stringResource(R.string.vibration_duration_decrease),
          increaseContentDescription = stringResource(R.string.vibration_duration_increase),
          onUpdate = { onUpdate(value.copy(durationMultiplier = it)) },
        )
      }
      // styleName
      for ((styleName, styleResource) in Vibration.STYLE_NAMES_TO_RESOURCES) {
        item {
          ToggleChip(
            checked = styleName == value.styleName,
            onCheckedChange = {
              if (!it) return@ToggleChip
              onUpdate(value.copy(styleName = styleName))
            },
            label = { Text(stringResource(styleResource)) },
            toggleControl = { RadioButton(selected = styleName == value.styleName) },
            modifier = Modifier.fillMaxWidth().padding(4.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun PercentSlider(
  value: Float,
  valueProgression: IntProgression,
  decreaseContentDescription: String,
  increaseContentDescription: String,
  onUpdate: (Float) -> Unit
) {
  InlineSlider(
    value = (value * 100).toInt(),
    onValueChange = { onUpdate(it / 100.0f) },
    valueProgression = valueProgression,
    decreaseIcon = { Icon(InlineSliderDefaults.Decrease, decreaseContentDescription) },
    increaseIcon = { Icon(InlineSliderDefaults.Increase, increaseContentDescription) },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 12.dp),
  )
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun VibrationAlertPreview() {
  NudgeTheme { VibrationView(value = Settings.DEFAULT.vibration, onUpdate = {}) }
}
