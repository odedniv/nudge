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
import kotlin.math.min
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.Settings
import me.odedniv.nudge.logic.Vibration
import me.odedniv.nudge.logic.Vibration.Companion.MAX_AMPLITUDE
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
      // amplitude
      item {
        Text(
          text = stringResource(R.string.vibration_amplitude),
          modifier = Modifier.fillMaxWidth().padding(4.dp),
          textAlign = TextAlign.Center,
        )
      }
      item {
        AmplitudeSlider(
          value = value.amplitude,
          onUpdate = { onUpdate(value.copy(amplitude = it)) },
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
private fun AmplitudeSlider(value: Int, onUpdate: (Int) -> Unit) {
  InlineSlider(
    value = value,
    onValueChange = { onUpdate(min(it, MAX_AMPLITUDE)) },
    valueProgression = 0..(MAX_AMPLITUDE + 1) step (MAX_AMPLITUDE + 1) / 8,
    decreaseIcon = {
      Icon(InlineSliderDefaults.Decrease, stringResource(R.string.vibration_amplitude_decrease))
    },
    increaseIcon = {
      Icon(InlineSliderDefaults.Increase, stringResource(R.string.vibration_amplitude_increase))
    },
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).padding(bottom = 12.dp),
  )
}

@Preview(widthDp = 227, heightDp = 227)
@Composable
fun VibrationAlertPreview() {
  NudgeTheme { VibrationView(value = Settings.DEFAULT.vibration, onUpdate = {}) }
}
