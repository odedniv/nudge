package me.odedniv.nudge.logic

import android.content.Context
import android.content.SharedPreferences
import java.time.Duration
import java.time.LocalTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

data class Settings(
  val started: Boolean,
  val runningNotification: Boolean,
  val frequency: Duration,
  val hours: Hours,
  val vibration: Vibration,
) {
  fun write(context: Context) {
    commit(context)
    with(context.preferences.edit()) {
      putBoolean(KEY_STARTED, started)
      putBoolean(KEY_RUNNING_NOTIFICATION, runningNotification)
      putLong(KEY_FREQUENCY_SECONDS, frequency.seconds)
      putLong(KEY_HOURS_START_SECONDS, hours.start.toSecondOfDay().toLong())
      putLong(KEY_HOURS_END_SECONDS, hours.end.toSecondOfDay().toLong())
      putString(KEY_VIBRATION_STYLE_NAME, vibration.styleName)
      putInt(KEY_VIBRATION_AMPLITUDE, vibration.amplitude)
      apply()
    }
  }

  fun commit(context: Context) {
    Scheduler(context).commit(this)
  }

  companion object {
    private const val SHARED_PREFERENCES_NAME = "main"
    private const val KEY_STARTED = "started"
    private const val KEY_RUNNING_NOTIFICATION = "running_notification"
    private const val KEY_FREQUENCY_SECONDS = "frequency_seconds"
    private const val KEY_HOURS_START_SECONDS = "hours_start_seconds"
    private const val KEY_HOURS_END_SECONDS = "hours_end_seconds"
    private const val KEY_VIBRATION_STYLE_NAME = "vibration_style_name"
    private const val KEY_VIBRATION_AMPLITUDE = "vibration_amplitude"

    private const val DEFAULT_STARTED = false
    private const val DEFAULT_RUNNING_NOTIFICATION = false
    private val DEFAULT_FREQUENCY: Duration = 1.hours.toJavaDuration()
    private val DEFAULT_HOURS = Hours(LocalTime.of(10, 0), LocalTime.of(20, 0))
    private val DEFAULT_VIBRATION = Vibration()

    val DEFAULT = Settings(
      started = DEFAULT_STARTED,
      runningNotification = DEFAULT_RUNNING_NOTIFICATION,
      frequency = DEFAULT_FREQUENCY,
      vibration = DEFAULT_VIBRATION,
      hours = DEFAULT_HOURS,
    )

    val MINIMUM_FREQUENCY: Duration = 10.minutes.toJavaDuration()

    fun read(context: Context): Settings =
      with(context.preferences) {
        Settings(
          started = getBoolean(KEY_STARTED, DEFAULT_STARTED),
          runningNotification = getBoolean(KEY_RUNNING_NOTIFICATION, DEFAULT_RUNNING_NOTIFICATION),
          frequency = Duration.ofSeconds(getLong(KEY_FREQUENCY_SECONDS, DEFAULT_FREQUENCY.seconds)),
          hours = Hours(
            start = LocalTime.ofSecondOfDay(
              getLong(KEY_HOURS_START_SECONDS, DEFAULT_HOURS.start.toSecondOfDay().toLong())
            ),
            end = LocalTime.ofSecondOfDay(
              getLong(KEY_HOURS_END_SECONDS, DEFAULT_HOURS.end.toSecondOfDay().toLong())
            ),
          ),
          vibration = Vibration(
            styleName = getString(KEY_VIBRATION_STYLE_NAME, DEFAULT_VIBRATION.styleName)!!,
            amplitude = getInt(KEY_VIBRATION_AMPLITUDE, DEFAULT_VIBRATION.amplitude),
          ),
        )
      }

    fun commit(context: Context) {
      read(context).commit(context)
    }

    private val Context.preferences: SharedPreferences
      get() = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }
}

data class Hours(val start: LocalTime, val end: LocalTime)
