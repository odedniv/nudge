package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration
import kotlinx.coroutines.delay
import me.odedniv.nudge.R

data class Vibration(
  private val context: Context?,
  val styleName: String,
  val durationMultiplier: Float,
  val amplitudeMultiplier: Float,
) {
  suspend fun execute() {
    val vibrator: Vibrator = requireNotNull(context!!.getSystemService())
    vibrator.cancel()
    vibrator.vibrate(vibrationEffect)
    delay(timings.sum())
  }

  val styleResourceId: Int
    get() = STYLE_NAMES_TO_RESOURCES[styleName] ?: DEFAULT.styleResourceId

  val duration: Duration by lazy { Duration.ofMillis(timings.sum()) }

  private val style: VibrationEffectDescription by lazy { STYLES[styleName] ?: DEFAULT.style }

  private val vibrationEffect by lazy {
    VibrationEffect.createWaveform(
      /* timings = */ timings,
      /* amplitudes = */ amplitudes,
      /* repeat = */ -1,
    )
  }

  private val timings: LongArray by lazy {
    style.map { (it.first * durationMultiplier * 1000).toLong() }.toLongArray()
  }

  private val amplitudes: IntArray by lazy {
    style.map { (it.second * amplitudeMultiplier * 255).toInt() }.toIntArray()
  }

  companion object {
    /** name -> List(Pair<duration multiplier, amplitude multiplier>) */
    private val STYLES =
      mapOf<String, VibrationEffectDescription>(
        "solid" to listOf(1.0 to 1.0),
        "212" to listOf(0.4 to 1.0, 0.4 to 0.0, 0.4 to 0.5, 0.4 to 0.0, 0.4 to 1.0),
        "123" to listOf(0.4 to 0.5, 0.4 to 0.0, 0.4 to 0.75, 0.4 to 0.0, 0.4 to 1.0),
      )

    val STYLE_NAMES_TO_RESOURCES: Map<String, Int> =
      mapOf(
        "solid" to R.string.vibration_style_solid,
        "212" to R.string.vibration_style_212,
        "123" to R.string.vibration_style_123,
      )

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT =
      Vibration(
        context = null,
        styleName = STYLES.keys.first(),
        durationMultiplier = 0.5f,
        amplitudeMultiplier = 0.5f,
      )
  }
}

private typealias VibrationEffectDescription = List<Pair<Double, Double>>
