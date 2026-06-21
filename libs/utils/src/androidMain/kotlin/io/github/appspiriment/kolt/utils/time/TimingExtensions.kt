package io.github.appspiriment.kolt.utils.time

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToLong

/**
 * Enterprise-grade Timing, Epoch, Vedic (Nazhika) & Coordinate Utilities.
 * Optimized for performance with formatter caching.
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

private val formatterCache = ConcurrentHashMap<String, DateTimeFormatter>()

// ─────────────────────────────────────────────────────────────────────────────
// Internal constants (shared style with other time files)
// ─────────────────────────────────────────────────────────────────────────────

internal val LOCALE_EN: Locale = Locale.ENGLISH
internal val UTC: ZoneOffset = ZoneOffset.UTC

// ─────────────────────────────────────────────────────────────────────────────
// Formatter factory (consistent with LocalDate / LocalDateTime)
// ─────────────────────────────────────────────────────────────────────────────

/** Returns a [DateTimeFormatter] for the pattern using English locale. */
fun formatterFor(pattern: String): DateTimeFormatter =
    formatterCache.getOrPut(pattern) {
        DateTimeFormatter.ofPattern(pattern, LOCALE_EN)
    }

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
fun Long?.millisToDateTime(format: String, timeZone: ZoneId = ZoneId.systemDefault()): String? {
    return this?.let {
        try {
            formatterFor(format)
                .withZone(timeZone)
                .format(Instant.ofEpochMilli(it))
        } catch (e: Exception) {
            null
        }
    }
}

/** Formats to "hh:mm a" (e.g. 02:30 PM) */
fun Long?.millisToHmaTime(timeZone: ZoneId = ZoneId.systemDefault()): String? =
    this.millisToDateTime("hh:mm a", timeZone)

/** Formats to "MMM dd hh:mm a" (e.g. Jan 15 02:30 PM) */
fun Long?.millisToMMddHmaTime(timeZone: ZoneId = ZoneId.systemDefault()): String? =
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

fun Long.isToday(zoneId: ZoneId = ZoneId.systemDefault()): Boolean =
    Instant.ofEpochMilli(this).atZone(zoneId).toLocalDate() == LocalDate.now(zoneId)

fun Long.isExpired(): Boolean = this < System.currentTimeMillis()

fun getDaysBetween(startMillis: Long, endMillis: Long): Long =
    ChronoUnit.DAYS.between(
        Instant.ofEpochMilli(startMillis),
        Instant.ofEpochMilli(endMillis)
    )

/** Relative time string (e.g. "3m ago", "Yesterday", "15 Jan 2025") */
fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
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
    return "%02d:%02d:%02d".format(h, m, s)
}

fun Long.minusDays(days: Long): Long = this - (days * MILLIS_IN_DAY)
fun Long.plusDays(days: Long): Long = this + (days * MILLIS_IN_DAY)

fun isSameDay(millis1: Long, millis2: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val d1 = Instant.ofEpochMilli(millis1).atZone(zoneId).toLocalDate()
    val d2 = Instant.ofEpochMilli(millis2).atZone(zoneId).toLocalDate()
    return d1 == d2
}