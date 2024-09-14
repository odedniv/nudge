package me.odedniv.nudge.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.tooling.preview.devices.WearDevices
import java.time.DayOfWeek
import me.odedniv.nudge.R
import me.odedniv.nudge.logic.allDays
import me.odedniv.nudge.logic.xor
import me.odedniv.nudge.ui.theme.NudgeTheme

@Composable
fun DaysDialog(
  showDialog: Boolean,
  value: Set<DayOfWeek>,
  onUpdate: (Set<DayOfWeek>) -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(showDialog = showDialog, onDismissRequest = onDismiss) {
    DaysView(value = value, onUpdate = onUpdate)
  }
}

@Composable
private fun DaysView(value: Set<DayOfWeek>, onUpdate: (Set<DayOfWeek>) -> Unit) {
  ScalingLazyColumn(anchorType = ScalingLazyListAnchorType.ItemStart) {
    for (day in allDays()) {
      item {
        Chip(
          onClick = { onUpdate(value xor day) },
          label = { Text(stringArrayResource(R.array.days_names)[day.value - 1]) },
          colors =
            if (day in value) ChipDefaults.primaryChipColors()
            else ChipDefaults.secondaryChipColors(),
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Preview(device = WearDevices.LARGE_ROUND)
@Composable
fun DaysPreview() {
  var value by remember {
    mutableStateOf(setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
  }
  NudgeTheme { DaysView(value = value, onUpdate = { value = it }) }
}
