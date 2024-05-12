package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.getSystemService
import java.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
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
        duration.toMillis() / 1000.0,
      )

  suspend fun execute() {
    val vibrator: Vibrator = requireNotNull(context!!.getSystemService())
    vibrator.cancel()
    vibrator.vibrate(vibrationEffect)
    delay(timings.sum())
  }

  val pattern: LongArray by lazy { timings }

  val styleResourceId: Int
    get() = STYLE_NAMES_TO_RESOURCES[styleName] ?: DEFAULT.styleResourceId

  val duration: Duration by lazy { Duration.ofMillis(timings.sum()) }

  private val style: VibrationEffectDescription by lazy { STYLES[styleName] ?: DEFAULT.style }

  private val vibrationEffect by lazy {
    VibrationEffect.createWaveform(/* timings = */ timings, /* repeat = */ -1)
  }

  private val timings: LongArray by lazy {
    style.map { (it * durationMultiplier * 1000).toLong() }.toLongArray()
  }

  companion object {
    /** name -> List(Pair<duration multiplier>) */
    private val STYLES =
      mapOf<String, VibrationEffectDescription>(
        "solid" to listOf(0.0, 1.0), // 1s: ----
        "212" to listOf(0.0, 0.45, 0.45, 0.2, 0.45, 0.45), // 2s: --  -  --
        "123" to listOf(0.0, 0.2, 0.35, 0.35, 0.35, 0.75), // 2s: -  --  ----
      )

    val STYLE_NAMES_TO_RESOURCES: Map<String, Int> =
      mapOf(
        "solid" to R.string.vibration_style_solid,
        "212" to R.string.vibration_style_212,
        "123" to R.string.vibration_style_123,
      )

    val MAXIMUM_DURATION: Duration = STYLES.values.maxOf { it.sum() }.seconds.toJavaDuration()

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT =
      Vibration(
        context = null,
        styleName = STYLES.keys.first(),
        durationMultiplier = 0.5f,
      )
  }
}

private typealias VibrationEffectDescription = List<Double>
