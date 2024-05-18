package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Text

val CHIP_MODIFIER = Modifier.fillMaxWidth().padding(4.dp)
val BUTTON_MODIFIER =
  Modifier.size(ButtonDefaults.DefaultIconSize).wrapContentSize(align = Alignment.Center)

@Composable
fun TitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = CHIP_MODIFIER.padding(top = 8.dp),
    textAlign = TextAlign.Center,
  )
}

@Composable
fun SubtitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = CHIP_MODIFIER,
    textAlign = TextAlign.Center,
  )
}
