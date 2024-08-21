package me.odedniv.nudge.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text

@Composable
fun HeaderText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.ExtraBold,
  )
}

@Composable
fun TitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    textAlign = TextAlign.Center,
    fontWeight = FontWeight.Bold,
  )
}

@Composable
fun SubtitleText(resourceId: Int) {
  Text(
    text = stringResource(resourceId),
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
    textAlign = TextAlign.Center,
  )
}
