package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration
import kotlin.math.ceil
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import me.odedniv.nudge.R

data class Vibration(
  private val context: Context?,
  val styleName: String,
  val durationMultiplier: Float,
) {
  val id: String
    get() =
      context!!.getString(
        R.string.settings_vibration_description,
        context.getString(styleResourceId),
        activeDuration.toMillis() / 1000.0,
      )

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

  val pattern: VibrationEffectPattern by lazy {
    style.mapActive { it * durationMultiplier }.toPattern()
  }

  val styleResourceId: Int
    get() = STYLE_NAMES_TO_RESOURCES[styleName] ?: DEFAULT.styleResourceId

  val activeDuration: Duration by lazy { Duration.ofMillis(pattern.active.sum()) }
  val totalDuration: Duration by lazy { Duration.ofMillis(pattern.sum()) }

  private val style: VibrationEffectDescription by lazy { STYLES[styleName] ?: DEFAULT.style }

  private val vibrationEffect by lazy {
    VibrationEffect.createWaveform(/* timings = */ pattern, /* repeat = */ -1)
  }

  companion object {
    /** name -> List(Pair<duration multiplier>) */
    private val STYLES =
      mapOf<String, VibrationEffectDescription>(
        "solid" to vibrationEffectDescription(1.0),
        "111" to vibrationEffectDescription(0.33, 0.33, 0.34),
        "212" to vibrationEffectDescription(0.4, 0.2, 0.4),
        "123" to vibrationEffectDescription(0.2, 0.33, 0.47),
        "321" to vibrationEffectDescription(0.47, 0.33, 0.2),
      )

    val STYLE_NAMES_TO_RESOURCES: Map<String, Int> =
      mapOf(
        "solid" to R.string.vibration_style_solid,
        "111" to R.string.vibration_style_111,
        "212" to R.string.vibration_style_212,
        "123" to R.string.vibration_style_123,
        "321" to R.string.vibration_style_321,
      )

    val MAXIMUM_DURATION: Duration = STYLES.values.maxOf { ceil(it.sum()) }.seconds.toJavaDuration()

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT =
      Vibration(
        context = null,
        styleName = STYLES.keys.first(),
        durationMultiplier = 0.5f,
      )
  }
}

typealias VibrationEffectPattern = LongArray

private val VibrationEffectPattern.active: VibrationEffectPattern
  get() = filterIndexed { i, _ -> i % 2 == 1 }.toLongArray()

private typealias VibrationEffectDescription = List<Double>

private fun VibrationEffectDescription.mapActive(
  block: (Double) -> Double
): VibrationEffectDescription = mapIndexed { i, t -> if (i % 2 == 1) block(t) else t }

private fun VibrationEffectDescription.toPattern(): VibrationEffectPattern =
  map { (it * 1000).toLong() }.toLongArray()

private fun vibrationEffectDescription(vararg active: Double): VibrationEffectDescription =
  listOf(0.0) + active.flatMap { listOf(it, /* spacing = */ 0.2) }.dropLast(1)
