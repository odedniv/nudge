package me.odedniv.nudge.logic

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

data class Settings(
  private val context: Context?,
  val oneOff: OneOff,
  val periodic: Boolean,
  val runningNotification: Boolean,
  val frequency: Duration,
  val hours: Hours,
  val days: Set<DayOfWeek>,
  val vibration: Vibration,
) {
  data class OneOff(
    val startedAt: Instant?,
    val pausedAt: Duration?,
    val durations: List<Duration>,
  ) {
    fun isEnabled(now: Instant): Boolean = elapsed(now) != null

    fun isRunning(now: Instant): Boolean = pausedAt == null && isEnabled(now)

    /** Returns the running time, or null if not running. */
    fun elapsed(now: Instant): Duration? {
      if (startedAt == null) return null
      if (pausedAt != null) return pausedAt
      val result = Duration.between(startedAt, now)
      return if (result < total) result else null
    }

    val total: Duration by lazy { durations.sum() }

    /** Returns the index of the next duration, or null if all durations elapsed. */
    fun nextElapsedIndex(now: Instant): Int? {
      startedAt ?: return null
      val elapsed = elapsed(now) ?: return null
      var total = Duration.ZERO
      for ((index, duration) in durations.withIndex()) {
        total += duration
        if (total > elapsed) return index
      }
      return null
    }

    companion object {
      val MINIMUM_DURATION: Duration = 5.seconds.toJavaDuration()
    }
  }

  fun write() {
    commit()
    with(context!!.preferences.edit()) {
      putLong(KEY_ONE_OFF_STARTED_AT, oneOff.startedAt?.epochSecond ?: 0)
      putLong(KEY_ONE_OFF_PAUSED_AT, oneOff.pausedAt?.seconds ?: 0)
      putString(KEY_ONE_OFF_DURATIONS, oneOff.durations.map { it.seconds }.joinToString(","))
      putBoolean(KEY_PERIODIC, periodic)
      putBoolean(KEY_RUNNING_NOTIFICATION, runningNotification)
      putLong(KEY_FREQUENCY_SECONDS, frequency.seconds)
      putLong(KEY_HOURS_START_SECONDS, hours.start.toSecondOfDay().toLong())
      putLong(KEY_HOURS_END_SECONDS, hours.end.toSecondOfDay().toLong())
      putStringSet(KEY_DAYS, days.map { it.name }.toSet())
      putString(KEY_VIBRATION_PATTERN, vibration.pattern.joinToString("-"))
      putFloat(KEY_VIBRATION_MULTIPLIER, vibration.multiplier)
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
    private const val KEY_ONE_OFF_STARTED_AT = "one_off.started_at"
    private const val KEY_ONE_OFF_PAUSED_AT = "one_off.paused_at"
    private const val KEY_ONE_OFF_DURATIONS = "one_off.durations"
    private const val KEY_PERIODIC = "periodic"
    private const val KEY_RUNNING_NOTIFICATION = "running_notification"
    private const val KEY_FREQUENCY_SECONDS = "frequency_seconds"
    private const val KEY_HOURS_START_SECONDS = "hours_start_seconds"
    private const val KEY_HOURS_END_SECONDS = "hours_end_seconds"
    private const val KEY_DAYS = "days"
    private const val KEY_VIBRATION_PATTERN = "vibration_pattern"
    private const val KEY_VIBRATION_MULTIPLIER = "vibration_multiplier"

    private val DEFAULT_ONE_OFF = OneOff(startedAt = null, pausedAt = null, durations = listOf())
    private const val DEFAULT_PERIODIC = false
    private const val DEFAULT_RUNNING_NOTIFICATION = false
    private val DEFAULT_FREQUENCY: Duration =
      if (DEBUG) 10.seconds.toJavaDuration() else 1.hours.toJavaDuration()
    private val DEFAULT_HOURS =
      Hours(LocalTime.of(if (DEBUG) 0 else 10, 0), LocalTime.of(if (DEBUG) 23 else 20, 0))
    private val DEFAULT_FIRST_DAY_OF_WEEK: DayOfWeek =
      Calendar.getInstance().firstDayOfWeek.calendarToJavaDayOfWeek()
    private val DEFAULT_DAYS: Set<DayOfWeek> =
      (0L..4L).map { DEFAULT_FIRST_DAY_OF_WEEK + it }.toSet()
    private val DEFAULT_DAYS_NAMES: Set<String> = DEFAULT_DAYS.map { it.name }.toSet()
    @SuppressLint("StaticFieldLeak") // context is null
    private val DEFAULT_VIBRATION = Vibration.DEFAULT

    @SuppressLint("StaticFieldLeak") // context is null
    val DEFAULT =
      Settings(
        context = null,
        oneOff = DEFAULT_ONE_OFF,
        periodic = DEFAULT_PERIODIC,
        runningNotification = DEFAULT_RUNNING_NOTIFICATION,
        frequency = DEFAULT_FREQUENCY,
        vibration = DEFAULT_VIBRATION,
        hours = DEFAULT_HOURS,
        days = DEFAULT_DAYS,
      )

    val MINIMUM_FREQUENCY: Duration = if (!DEBUG) 10.minutes.toJavaDuration() else Duration.ZERO

    fun read(context: Context): Settings =
      with(context.preferences) {
        Settings(
          context = context,
          oneOff =
            OneOff(
              startedAt = Instant.ofEpochSecond(getLong(KEY_ONE_OFF_STARTED_AT, 0L)).orNull(),
              pausedAt = Duration.ofSeconds(getLong(KEY_ONE_OFF_PAUSED_AT, 0L)).orNull(),
              durations =
                getString(KEY_ONE_OFF_DURATIONS, null)
                  ?.takeIf { it.isNotEmpty() }
                  ?.run { split(",").mapNotNull { Duration.ofSeconds(it.toLong()) } }
                  ?: DEFAULT_ONE_OFF.durations,
            ),
          periodic = getBoolean(KEY_PERIODIC, DEFAULT_PERIODIC),
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
          days =
            getStringSet(KEY_DAYS, DEFAULT_DAYS_NAMES)?.map { DayOfWeek.valueOf(it) }?.toSet()
              ?: DEFAULT_DAYS,
          vibration =
            Vibration(
              context = context,
              pattern =
                getString(KEY_VIBRATION_PATTERN, null)
                  ?.takeIf { it.isNotEmpty() }
                  ?.asVibrationPattern() ?: Vibration.DEFAULT.pattern,
              multiplier = getFloat(KEY_VIBRATION_MULTIPLIER, DEFAULT_VIBRATION.multiplier),
            ),
        )
      }

    fun commit(context: Context): Settings = read(context).apply { commit() }

    private val Context.preferences: SharedPreferences
      get() = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
  }
}

data class Hours(val start: LocalTime, val end: LocalTime) {
  operator fun contains(value: LocalTime) = start <= value && value < end
}
