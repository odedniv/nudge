package me.odedniv.nudge

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService

/** (amplitude) -> VibrationEffect */
private val VIBRATION_EFFECT_STYLES = listOf<(Int) -> VibrationEffect>(
  { VibrationEffect.createOneShot(125, it) },
  { VibrationEffect.createOneShot(250, it) },
  { VibrationEffect.createOneShot(500, it) },
)

data class Vibration(val styleIndex: Int, val amplitude: Int = VibrationEffect.DEFAULT_AMPLITUDE) {
  fun execute(context: Context) {
    val vibrator: Vibrator = requireNotNull(context.getSystemService())
    vibrator.vibrate(VIBRATION_EFFECT_STYLES[styleIndex](amplitude))
  }

  companion object {
    val STYLES_COUNT: Int = VIBRATION_EFFECT_STYLES.size
  }
}