package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.InlineSlider
import androidx.wear.compose.material.InlineSliderDefaults
import androidx.wear.compose.material.Picker
import androidx.wear.compose.material.ScalingLazyListState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.rememberPickerState
import androidx.wear.compose.material.rememberScalingLazyListState
import me.odedniv.nudge.R
import me.odedniv.nudge.Settings
import me.odedniv.nudge.Vibration
import me.odedniv.nudge.presentation.theme.NudgeTheme

@Composable
fun VibrationDialog(value: Vibration, onDismiss: (Vibration) -> Unit) {
  var vibration by remember { mutableStateOf(value) }
  val scrollState = rememberScalingLazyListState()
  val context = LocalContext.current

  Dialog(
    showDialog = true,
    onDismissRequest = { onDismiss(vibration) },
    scrollState = scrollState,
  ) {
    VibrationAlert(
      value = vibration,
      onUpdate = {
        vibration = it
        vibration.execute(context)
      },
      scrollState = scrollState,
    )
  }
}

@Composable
private fun VibrationAlert(
  value: Vibration,
  onUpdate: (Vibration) -> Unit,
  scrollState: ScalingLazyListState,
) {
  Alert(
    title = {
      Text(
        text = stringResource(R.string.settings_vibration),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    message = {
      Text(
        text = stringResource(R.string.settings_vibration_message),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    },
    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
    scrollState = scrollState,
  ) {
    item {
      SubtitleText(stringResource(R.string.settings_vibration_amplitude))
    }
    item {
      AmplitudeSlider(
        value = value.amplitude,
        onUpdate = { onUpdate(value.copy(amplitude = it)) },
      )
    }
    item {
      SubtitleText(stringResource(R.string.settings_vibration_style))
    }
    item {
      StylePicker(
        value = value.styleIndex,
        onUpdate = { onUpdate(value.copy(styleIndex = it)) },
      )
    }
  }
}

@Composable
private fun SubtitleText(text: String) {
  Text(
    text = text,
    modifier = Modifier.fillMaxWidth(),
    textAlign = TextAlign.Center,
  )
}

@Composable
private fun AmplitudeSlider(value: Int, onUpdate: (Int) -> Unit) {
  InlineSlider(
    value = value,
    onValueChange = { onUpdate(it) },
    valueProgression = (0..255),
    decreaseIcon = {
      Icon(
        InlineSliderDefaults.Decrease,
        stringResource(R.string.settings_vibration_amplitude_decrease)
      )
    },
    increaseIcon = {
      Icon(
        InlineSliderDefaults.Increase,
        stringResource(R.string.settings_vibration_amplitude_increase)
      )
    },
  )
}

@Composable
private fun StylePicker(value: Int, onUpdate: (Int) -> Unit) {
  val context = LocalContext.current
  val state = rememberPickerState(
    initialNumberOfOptions = Vibration.STYLES_COUNT,
    initiallySelectedOption = value,
    repeatItems = false,
  )
  val contentDescription by remember {
    derivedStateOf {
      context.getString(R.string.settings_vibration_style_index, state.selectedOption)
    }
  }
  Picker(
    state = state,
    contentDescription = contentDescription,
    onSelected = { onUpdate(state.selectedOption) },
  ) {
    Text(
      text = stringResource(R.string.settings_vibration_style_index, it),
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center,
    )
  }
}

@Preview(showBackground = true, widthDp = 227, heightDp = 227)
@Composable
fun VibrationDialogPreview() {
  NudgeTheme {
    VibrationAlert(
      value = Settings.DEFAULT.vibration,
      onUpdate = {},
      scrollState = rememberScalingLazyListState(),
    )
  }
}
