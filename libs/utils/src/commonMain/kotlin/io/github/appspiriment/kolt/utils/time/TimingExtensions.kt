@file:OptIn(ExperimentalTime::class)

package io.github.appspiriment.kolt.utils.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToLong
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Enterprise-grade Timing, Epoch, Vedic (Nazhika) & Coordinate Utilities.
 */

/* ******************************************************
 * Constants
 * ******************************************************/

const val MILLIS_IN_SECOND = 1_000L
const val MILLIS_IN_MINUTE = 60_000L
const val MILLIS_IN_HOUR = 3_600_000L
const val MILLIS_IN_DAY = 86_400_000L

/** Conversion factor: 1 Hour = 2.5 Nazhika (traditional Vedic time unit) */
private const val HOURS_TO_NAZHIKA = 2.5

/* ******************************************************
 * Decimal & Millis Conversions
 * ******************************************************/

fun Long.millisToDecimalHour(): Double = this.toDouble() / MILLIS_IN_HOUR
fun Long.millisToDays(): Double = this.toDouble() / MILLIS_IN_DAY

fun Double.fromHoursToMillis(): Long = (this * MILLIS_IN_HOUR).toLong()
fun Double.fromHoursToSeconds(): Long = (this * 3600).roundToLong()

/** Converts decimal hours to Triple(Hours, Minutes, Seconds) */
fun Double.fromHoursToHMS(): Triple<Int, Int, Int> {
    val totalSeconds = (this * 3600).toInt()
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return Triple(hours, minutes, seconds)
}

/* ******************************************************
 * Vedic Time: Nazhika & Vinazhika
 * ******************************************************/

fun Double.fromHoursToNazhika(): Pair<Int, Int> {
    val totalNazhika = this * HOURS_TO_NAZHIKA
    val nazhika = totalNazhika.toInt()
    val vinazhika = ((totalNazhika - nazhika) * 60).toInt()
    return Pair(nazhika, vinazhika)
}

fun Double.hourstoNazhikaVinazhikaString(): String =
    fromHoursToNazhika().run { "$first നാ $second വി" }

fun Double.nazhikaToNazhikaVinazhika(): Pair<Int, Int> {
    val nazhika = this.toInt()
    val vinazhika = ((this - nazhika) * 60).toInt()
    return Pair(nazhika, vinazhika)
}

fun Double.nazhikatoNazhikaVinazhikaString(): String =
    nazhikaToNazhikaVinazhika().let { "${it.first} നാ ${it.second} വി" }

/* ******************************************************
 * Epoch → Formatted String (Core + Legacy Names)
 * ******************************************************/

/** Formats epoch millis with any pattern and timezone. Returns null on error. */
fun Long?.millisToDateTime(format: String, timeZone: TimeZone = TimeZone.currentSystemDefault()): String? {
    return this?.let {
        try {
            localDateTimeFormatterFor(format).format(Instant.fromEpochMilliseconds(it).toLocalDateTime(timeZone))
        } catch (e: Exception) {
            null
        }
    }
}

/** Formats to "hh:mm a" (e.g. 02:30 PM) */
fun Long?.millisToHmaTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String? =
    this.millisToDateTime("hh:mm a", timeZone)

/** Formats to "MMM dd hh:mm a" (e.g. Jan 15 02:30 PM) */
fun Long?.millisToMMddHmaTime(timeZone: TimeZone = TimeZone.currentSystemDefault()): String? =
    this.millisToDateTime("MMM dd hh:mm a", timeZone)

// Short convenient aliases (new)
fun Long?.toHma(): String? = millisToHmaTime()
fun Long?.toMMddHma(): String? = millisToMMddHmaTime()
fun Long?.toDateString(): String? = millisToDateTime("dd MMM yyyy")
fun Long?.toFullDate(): String? = millisToDateTime("dd MMMM yyyy")

/* ******************************************************
 * Angle / Coordinate Utilities (DMS)
 * ******************************************************/

val Double.dms: Triple<Int, Int, Int>
    get() {
        var value = this + (0.5 / 3600.0 / 10000.0) // round to 1/1000 second
        val deg = value.toInt()
        value = (value - deg) * 60
        val min = value.toInt()
        value = (value - min) * 60
        val sec = value.toInt()
        return Triple(deg, min, sec)
    }

fun Double.toDMSString(): String = dms.run { "$first° $second' $third\"" }

/* ******************************************************
 * Additional Common Utilities
 * ******************************************************/

fun Long.isToday(timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date == today(timeZone)

fun Long.isExpired(): Boolean = this < Clock.System.now().toEpochMilliseconds()

fun getDaysBetween(startMillis: Long, endMillis: Long): Long = (endMillis - startMillis) / MILLIS_IN_DAY

/** Relative time string (e.g. "3m ago", "Yesterday", "15 Jan 2025") */
fun Long.toRelativeTime(): String {
    val now = Clock.System.now().toEpochMilliseconds()
    val diff = now - this
    return when {
        diff < MILLIS_IN_MINUTE -> "Just now"
        diff < MILLIS_IN_HOUR -> "${diff / MILLIS_IN_MINUTE}m ago"
        diff < MILLIS_IN_DAY -> "${diff / MILLIS_IN_HOUR}h ago"
        isSameDay(this, now - MILLIS_IN_DAY) -> "Yesterday"
        else -> this.toDateString() ?: ""
    }
}

/** Stopwatch / duration format: 01:30:15 */
fun Long.toDurationString(): String {
    val h = this / MILLIS_IN_HOUR
    val m = (this % MILLIS_IN_HOUR) / MILLIS_IN_MINUTE
    val s = (this % MILLIS_IN_MINUTE) / MILLIS_IN_SECOND
    return "${h.pad2()}:${m.pad2()}:${s.pad2()}"
}

private fun Long.pad2(): String = if (this < 10) "0$this" else "$this"

fun Long.minusDays(days: Long): Long = this - (days * MILLIS_IN_DAY)
fun Long.plusDays(days: Long): Long = this + (days * MILLIS_IN_DAY)

fun isSameDay(millis1: Long, millis2: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val d1 = Instant.fromEpochMilliseconds(millis1).toLocalDateTime(timeZone).date
    val d2 = Instant.fromEpochMilliseconds(millis2).toLocalDateTime(timeZone).date
    return d1 == d2
}
