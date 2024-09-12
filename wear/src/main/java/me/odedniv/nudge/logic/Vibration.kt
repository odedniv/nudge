package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import me.odedniv.nudge.R

typealias VibrationPattern = List<VibrationPatternValue>

typealias VibrationPatternValue = Int // Numbers from 1-3 representing relative vibration length.

fun VibrationPattern.asString(): String = joinToString("-")

fun String.asVibrationPattern(): VibrationPattern = split("-").map { it.toInt() }

data class Vibration(
  private val context: Context?,
  val pattern: List<VibrationPatternValue>,
  val multiplier: Float,
) {
  val id: String
    get() =
      context!!.getString(
        R.string.settings_vibration_description,
        pattern.asString(),
        (multiplier * 100).roundToInt(),
      )

  val totalDuration: Duration by lazy { timings.sum() }

  suspend fun execute() {
    val vibrator: Vibrator = requireNotNull(context!!.getSystemService())
    vibrator.cancel()
    try {
      vibrator.vibrate(vibrationEffect)
      delay(totalDuration.toMillis())
    } catch (e: CancellationException) {
      vibrator.cancel()
      throw e
    }
  }

  val timings: List<Duration> by lazy {
    listOf(Duration.ZERO) +
      pattern.flatMap { listOf(BASELINE + (PATTERN_VALUE * it * multiplier), SPACING) }.dropLast(1)
  }

  private val vibrationEffect by lazy {
    VibrationEffect.createWaveform(
      /* timings = */ timings.map { it.toMillis() }.toLongArray(),
      /* repeat = */ -1,
    )
  }

  companion object {
    private val SPACING = 200.milliseconds.toJavaDuration()
    private val BASELINE = 100.milliseconds.toJavaDuration()
    private val PATTERN_VALUE = 200.milliseconds.toJavaDuration()

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT = Vibration(context = null, pattern = listOf(1, 2, 3), multiplier = 0.5f)
  }
}
