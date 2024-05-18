package me.odedniv.nudge.logic

import android.icu.util.Calendar
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant

fun Iterable<Duration>.sum(): Duration = fold(Duration.ZERO, Duration::plus)

fun Duration.format(): String =
  toHours().toString() +
    ":" +
    minutesPart.toString().padStart(2, '0') +
    ":" +
    secondsPart.toString().padStart(2, '0')

fun Instant.orNull(): Instant? = if (epochSecond == 0L) null else this

fun Duration.orNull(): Duration? = if (seconds == 0L) null else this

val Duration.minutesPart: Int
  get() = (toMinutes() % 60).toInt()

val Duration.secondsPart: Int
  get() = (seconds % 60).toInt()

/**
 * Converts [Calendar.SUNDAY] (1) .. [Calendar.SATURDAY] (7) to [DayOfWeek.MONDAY] (1) to
 * [DayOfWeek.SUNDAY] (7).
 */
fun Int.calendarToJavaDayOfWeek(): DayOfWeek =
  if (this == Calendar.SUNDAY) {
    DayOfWeek.SUNDAY
  } else {
    DayOfWeek.of(this - 1)
  }

infix fun <T> Set<T>.xor(element: T): Set<T> =
  if (element in this) this - element else this + element

fun allDays(): List<DayOfWeek> {
  val firstDayOfWeek: DayOfWeek = Calendar.getInstance().firstDayOfWeek.calendarToJavaDayOfWeek()
  return (0L..6L).map { firstDayOfWeek + it }
}
