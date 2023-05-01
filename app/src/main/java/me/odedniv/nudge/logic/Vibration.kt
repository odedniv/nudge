package me.odedniv.nudge.logic

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration
import kotlinx.coroutines.delay
import me.odedniv.nudge.R

/** (amplitude) -> EffectAndDuration */
private val STYLES =
  mapOf<String, (Int) -> EffectAndDuration>(
    "short" to { createOneShot(125, it) },
    "medium" to { createOneShot(250, it) },
    "long" to { createOneShot(500, it) },
    "212" to { waveForm(200 to it, 200 to 0, 200 to it / 2, 200 to 0, 200 to it) },
    "123" to { waveForm(200 to it / 2, 200 to 0, 200 to (it div 1.5), 200 to 0, 200 to it) },
  )

data class Vibration(val styleName: String = STYLES.keys.first(), val amplitude: Int = 127) {
  suspend fun execute(context: Context) {
    val vibrator: Vibrator = requireNotNull(context.getSystemService())
    val (effect, duration) = STYLES[styleName]!!(amplitude)
    vibrator.vibrate(effect)
    delay(duration.toMillis())
  }

  companion object {
    val STYLE_NAMES_TO_RESOURCES: Map<String, Int> =
      mapOf(
        "short" to R.string.vibration_style_short,
        "medium" to R.string.vibration_style_medium,
        "long" to R.string.vibration_style_long,
        "212" to R.string.vibration_style_212,
        "123" to R.string.vibration_style_123,
      )

    const val MAX_AMPLITUDE = 255
  }
}

private data class EffectAndDuration(val effect: VibrationEffect, val duration: Duration)

private fun createOneShot(milliseconds: Int, amplitude: Int) =
  EffectAndDuration(
    VibrationEffect.createOneShot(milliseconds.toLong(), amplitude),
    Duration.ofMillis(milliseconds.toLong())
  )

private fun waveForm(vararg pairs: Pair<Int, Int>) =
  EffectAndDuration(
    VibrationEffect.createWaveform(
      /* timings = */ pairs.map { it.first.toLong() }.toLongArray(),
      /* amplitudes = */ pairs.map { it.second }.toIntArray(),
      /* repeat = */ -1
    ),
    Duration.ofMillis(pairs.sumOf { it.first }.toLong())
  )

private infix fun Int.div(double: Double): Int = (this / double).toInt()
