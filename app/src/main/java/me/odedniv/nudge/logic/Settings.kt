package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import java.time.Duration
import java.time.LocalTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

data class Settings(
  private val context: Context?,
  val started: Boolean,
  val runningNotification: Boolean,
  val frequency: Duration,
  val hours: Hours,
  val vibration: Vibration,
) {
  fun write() {
    commit()
    with(context!!.preferences.edit()) {
      putBoolean(KEY_STARTED, started)
      putBoolean(KEY_RUNNING_NOTIFICATION, runningNotification)
      putLong(KEY_FREQUENCY_SECONDS, frequency.seconds)
      putLong(KEY_HOURS_START_SECONDS, hours.start.toSecondOfDay().toLong())
      putLong(KEY_HOURS_END_SECONDS, hours.end.toSecondOfDay().toLong())
      putString(KEY_VIBRATION_STYLE_NAME, vibration.styleName)
      putFloat(KEY_VIBRATION_DURATION_MULTIPLIER, vibration.durationMultiplier)
      apply()
    }
  }

  fun commit() {
    context!!
    Notifications(context).createChannels(this)
    Scheduler(context).commit(this)
  }

  companion object {
    /** Allow shorter minimum frequency. */
    const val DEBUG = false

    private const val SHARED_PREFERENCES_NAME = "main"
    private const val KEY_STARTED = "started"
    private const val KEY_RUNNING_NOTIFICATION = "running_notification"
    private const val KEY_FREQUENCY_SECONDS = "frequency_seconds"
    private const val KEY_HOURS_START_SECONDS = "hours_start_seconds"
    private const val KEY_HOURS_END_SECONDS = "hours_end_seconds"
    private const val KEY_VIBRATION_STYLE_NAME = "vibration_style_name"
    private const val KEY_VIBRATION_DURATION_MULTIPLIER = "vibration_duration_multiplier"

    private const val DEFAULT_STARTED = false
    private const val DEFAULT_RUNNING_NOTIFICATION = false
    private val DEFAULT_FREQUENCY: Duration =
      if (DEBUG) 10.seconds.toJavaDuration() else 1.hours.toJavaDuration()
    private val DEFAULT_HOURS =
      Hours(LocalTime.of(if (DEBUG) 0 else 10, 0), LocalTime.of(if (DEBUG) 23 else 20, 0))
    @SuppressLint("StaticFieldLeak") // context is null
    private val DEFAULT_VIBRATION = Vibration.DEFAULT

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT =
      Settings(
        context = null,
        started = DEFAULT_STARTED,
        runningNotification = DEFAULT_RUNNING_NOTIFICATION,
        frequency = DEFAULT_FREQUENCY,
        vibration = DEFAULT_VIBRATION,
        hours = DEFAULT_HOURS,
      )

    val MINIMUM_FREQUENCY: Duration = if (!DEBUG) 10.minutes.toJavaDuration() else Duration.ZERO

    fun read(context: Context): Settings =
      with(context.preferences) {
        Settings(
          context = context,
          started = getBoolean(KEY_STARTED, DEFAULT_STARTED),
          runningNotification = getBoolean(KEY_RUNNING_NOTIFICATION, DEFAULT_RUNNING_NOTIFICATION),
          frequency = Duration.ofSeconds(getLong(KEY_FREQUENCY_SECONDS, DEFAULT_FREQUENCY.seconds)),
          hours =
            Hours(
              start =
                LocalTime.ofSecondOfDay(
                  getLong(KEY_HOURS_START_SECONDS, DEFAULT_HOURS.start.toSecondOfDay().toLong())
                ),
              end =
                LocalTime.ofSecondOfDay(
                  getLong(KEY_HOURS_END_SECONDS, DEFAULT_HOURS.end.toSecondOfDay().toLong())
                ),
            ),
          vibration =
            Vibration(
              context = context,
              styleName = getString(KEY_VIBRATION_STYLE_NAME, DEFAULT_VIBRATION.styleName)!!,
              durationMultiplier =
                getFloat(KEY_VIBRATION_DURATION_MULTIPLIER, DEFAULT_VIBRATION.durationMultiplier),
            ),
        )
      }

    fun commit(context: Context): Settings = read(context).apply { commit() }

    private val Context.preferences: SharedPreferences
      get() = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }
}

data class Hours(val start: LocalTime, val end: LocalTime)
