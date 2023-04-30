package me.odedniv.nudge

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import kotlin.time.Duration.Companion.milliseconds

/** (amplitude) -> VibrationEffect */
private val STYLES = mapOf<String, (Int) -> VibrationEffect>(
  "short" to { VibrationEffect.createOneShot(125, it) },
  "medium" to { VibrationEffect.createOneShot(250, it) },
  "long" to { VibrationEffect.createOneShot(500, it) },
  "212" to { waveForm(200 to it, 200 to 0, 200 to it / 2, 200 to 0, 200 to it) },
  "123" to { waveForm(200 to it / 2, 200 to 0, 200 to (it div 1.5), 200 to 0, 200 to it) },
)

data class Vibration(val styleName: String = STYLES.keys.first(), val amplitude: Int = 127) {
  fun execute(context: Context) {
    val vibrator: Vibrator = requireNotNull(context.getSystemService())
    vibrator.vibrate(STYLES[styleName]!!(amplitude))
  }

  companion object {
    val STYLE_NAMES_TO_RESOURCES: Map<String, Int> = mapOf(
      "short" to R.string.vibration_style_short,
      "medium" to R.string.vibration_style_medium,
      "long" to R.string.vibration_style_long,
      "212" to R.string.vibration_style_212,
      "123" to R.string.vibration_style_123,
    )

    const val MAX_AMPLITUDE = 255
  }
}

private fun waveForm(vararg pairs: Pair<Int, Int>) =
  VibrationEffect.createWaveform(
    /* timings = */ pairs.map { it.first.toLong() }.toLongArray(),
    /* amplitudes = */ pairs.map { it.second }.toIntArray(),
    /* repeat = */ -1
  )

private infix fun Int.div(double: Double): Int = (this / double).toInt()