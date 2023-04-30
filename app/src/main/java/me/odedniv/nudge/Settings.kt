package me.odedniv.nudge

import android.content.Context
import android.content.SharedPreferences
import java.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration

data class Settings(
  val started: Boolean,
  val runningNotification: Boolean,
  val frequency: Duration,
) {

  fun write(context: Context) {
    Scheduler(context).commit(this)
    with(context.preferences.edit()) {
      putBoolean(KEY_STARTED, started)
      putBoolean(KEY_RUNNING_NOTIFICATION, runningNotification)
      putLong(KEY_FREQUENCY_SECONDS, frequency.seconds)
      apply()
    }
  }

  companion object {
    private const val SHARED_PREFERENCES_NAME = "main"
    private const val KEY_STARTED = "started"
    private const val KEY_RUNNING_NOTIFICATION = "running_notification"
    private const val KEY_FREQUENCY_SECONDS = "frequency"

    private const val DEFAULT_STARTED = false
    private const val DEFAULT_RUNNING_NOTIFICATION = false
    private val DEFAULT_FREQUENCY: Duration = 1.hours.toJavaDuration()

    val DEFAULT = Settings(
      started = DEFAULT_STARTED,
      runningNotification = DEFAULT_RUNNING_NOTIFICATION,
      frequency = DEFAULT_FREQUENCY,
    )

    val MINIMUM_FREQUENCY: Duration = 15.minutes.toJavaDuration()

    fun read(context: Context): Settings =
      with(context.preferences) {
        Settings(
          started = getBoolean(KEY_STARTED, DEFAULT_STARTED),
          runningNotification = getBoolean(KEY_RUNNING_NOTIFICATION, DEFAULT_RUNNING_NOTIFICATION),
          frequency = Duration.ofSeconds(getLong(KEY_FREQUENCY_SECONDS, DEFAULT_FREQUENCY.seconds)),
        ).also { Scheduler(context).commit(it) }
      }

    fun commit(context: Context) {
      read(context) // Triggers commit.
    }

    private val Context.preferences: SharedPreferences
      get() = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }
}

