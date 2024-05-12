package me.odedniv.nudge.logic

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
