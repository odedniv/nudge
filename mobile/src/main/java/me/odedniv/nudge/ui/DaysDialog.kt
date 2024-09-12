package me.odedniv.nudge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
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
  if (!showDialog) return
  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_confirm)) }
    },
    title = { Text(stringResource(R.string.settings_days)) },
    text = { DaysView(value = value, onUpdate = onUpdate) },
  )
}

@Composable
private fun DaysView(value: Set<DayOfWeek>, onUpdate: (Set<DayOfWeek>) -> Unit) {
  LazyColumn(verticalArrangement = Arrangement.Center) {
    for (day in allDays()) {
      item {
        Button(
          onClick = { onUpdate(value xor day) },
          colors =
            if (day in value) ButtonDefaults.buttonColors()
            else ButtonDefaults.filledTonalButtonColors(),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(stringArrayResource(R.array.days_names)[day.value - 1])
        }
      }
    }
  }
}

@Preview
@Composable
fun DaysPreview() {
  var value by remember {
    mutableStateOf(setOf(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY))
  }
  NudgeTheme {
    DaysDialog(showDialog = true, onDismiss = {}, value = value, onUpdate = { value = it })
  }
}
