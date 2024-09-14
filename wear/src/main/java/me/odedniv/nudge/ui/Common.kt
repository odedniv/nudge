package me.odedniv.nudge.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import me.odedniv.nudge.R
import me.odedniv.nudge.ui.theme.NudgeTheme

@Composable
fun HeaderText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.padding(bottom = 16.dp),
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.ExtraBold,
  )
}

@Composable
fun TitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.padding(vertical = 8.dp),
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.Bold,
  )
}

@Composable
fun SubtitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.padding(top = 4.dp),
    textAlign = TextAlign.Center,
  )
}

@Preview
@Composable
fun CommonPreview() {
  NudgeTheme {
    Column {
      HeaderText(resourceId = R.string.app_name)
      Text("Under Header")
      TitleText(resourceId = R.string.app_name)
      Text("Under Title")
      SubtitleText(resourceId = R.string.app_name)
      Text("Under Subtitle")
    }
  }
}
