@file:OptIn(ExperimentalTime::class)

package io.github.appspiriment.kolt.utils.time

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmInline
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime


// ─────────────────────────────────────────────────────────────────────────────
// Factory Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current local date-time in [timeZone] (defaults to the system default zone). */
fun nowLocal(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Clock.System.now().toLocalDateTime(timeZone)

/**
 * Converts absolute UTC millis to LocalDateTime in the given zone.
 * Best for timestamps from backend or Clock.System.now().
 */
fun fromUtcMillisToLocalDateTime(millis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone)

/** Legacy name from old implementation */
fun millisToLocalDateTime(millis: Long): LocalDateTime =
    fromUtcMillisToLocalDateTime(millis)

/**
 * Treats millis as wall-clock time (ignores timezone) and converts to LocalDateTime.
 * Useful for time pickers.
 */
fun fromWallClockMillisToLocalDateTime(millis: Long): LocalDateTime =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC)

// ─────────────────────────────────────────────────────────────────────────────
// Formatting (Old popular formats + new style)
// ─────────────────────────────────────────────────────────────────────────────

// ponytail: plain (non-thread-safe) cache — see localDateFormatterCache for rationale.
private val localDateTimeFormatterCache = mutableMapOf<String, DateTimeFormat<LocalDateTime>>()

/** Returns a [DateTimeFormat] for [pattern], built with English month/weekday names. */
fun localDateTimeFormatterFor(pattern: String): DateTimeFormat<LocalDateTime> =
    localDateTimeFormatterCache.getOrPut(pattern) {
        LocalDateTime.Format { byUnicodePattern(pattern) }
    }

/** Formats with pattern using English locale. */
fun LocalDateTime.format(pattern: String): String = localDateTimeFormatterFor(pattern).format(this)

/** Formats using a pre-built formatter. */
fun LocalDateTime.format(formatter: DateTimeFormat<LocalDateTime>): String = formatter.format(this)

// === Old popular format extensions (kept for backward compatibility) ===
val LocalDateTime.time_hhmm_a: String get() = format("hh:mm a")
val LocalDateTime.time_hhmmss_a: String get() = format("hh:mm:ss a")
val LocalDateTime.time_HHmm: String get() = format("HH:mm")

val LocalDateTime.date_yyyymmdd: String get() = format("yyyyMMdd")
val LocalDateTime.date_mmm_dd: String get() = format("MMM dd")
val LocalDateTime.date_dd_MMM_yyyy: String get() = format("dd MMM yyyy")

val LocalDateTime.dateTime_mmm_dd_hh_mm_a: String get() = format("MMM dd hh:mm a")
val LocalDateTime.dateTime_mmm_dd_HH_mm: String get() = format("MMM dd HH:mm")
val LocalDateTime.dateTime_mmm_dd_split_hh_mm_a: String get() = format("MMM dd\nhh:mm a")
val LocalDateTime.dateTime_mmm_dd_split_HH_mm: String get() = format("MMM dd\nHH:mm")
val LocalDateTime.dateTime_dd_MMM_yyyy_hh_mm_a: String get() = format("dd MMM yyyy hh:mm a")

val LocalDateTime.weekName: String get() = format("EEEE")

// ─────────────────────────────────────────────────────────────────────────────
// Parsing
// ─────────────────────────────────────────────────────────────────────────────

/** Parses string with pattern, returns null on failure. */
fun String.toLocalDateTimeOrNull(pattern: String): LocalDateTime? =
    runCatching { localDateTimeFormatterFor(pattern).parse(this) }.getOrNull()

/** Parses ISO local date-time string (yyyy-MM-ddTHH:mm:ss). */
fun String.toLocalDateTimeIsoOrNull(): LocalDateTime? =
    runCatching { LocalDateTime.parse(this) }.getOrNull()

/** Legacy convenience from old code */
fun String.toLocalDateTimeOrNow(pattern: String): LocalDateTime =
    toLocalDateTimeOrNull(pattern) ?: nowLocal()

// ─────────────────────────────────────────────────────────────────────────────
// Epoch / Millis Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** Converts this LocalDateTime to UTC millis using the given zone. */
fun LocalDateTime.toUtcMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    this.toInstant(timeZone).toEpochMilliseconds()

/** Wall-clock millis (treats this local time as if it were in UTC). */
val LocalDateTime.wallClockMillis: Long
    get() = this.toInstant(TimeZone.UTC).toEpochMilliseconds()

/** Midnight (00:00:00) of this date in the given zone, as UTC millis. */
fun LocalDateTime.midnightMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    this.date.toUtcMillis(timeZone)

// Legacy properties from old code
val LocalDateTime.midnightMillis: Long get() = midnightMillis()
val LocalDateTime.noonMillis: Long get() = noonInstance.toUtcMillis()

// ─────────────────────────────────────────────────────────────────────────────
// Component Accessors
// ─────────────────────────────────────────────────────────────────────────────

// Note: LocalDateTime already exposes native `date`/`time` properties in kotlinx-datetime.

/** Month as enum */
val LocalDateTime.monthEnum: Month get() = month

/** Quarter of the year (1-4) */
val LocalDateTime.quarter: Int get() = (monthNumber - 1) / 3 + 1

/** ISO Week of year (1-53) */
val LocalDateTime.weekOfYear: Int get() = date.weekOfYear

// ─────────────────────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.nextDay: LocalDateTime get() = LocalDateTime(date.nextDay, time)
val LocalDateTime.previousDay: LocalDateTime get() = LocalDateTime(date.previousDay, time)
val LocalDateTime.nextWeek: LocalDateTime get() = LocalDateTime(date.nextWeek, time)
val LocalDateTime.previousWeek: LocalDateTime get() = LocalDateTime(date.previousWeek, time)
val LocalDateTime.nextMonth: LocalDateTime get() = LocalDateTime(date.nextMonth, time)
val LocalDateTime.previousMonth: LocalDateTime get() = LocalDateTime(date.previousMonth, time)
val LocalDateTime.nextYear: LocalDateTime get() = LocalDateTime(date.nextYear, time)
val LocalDateTime.previousYear: LocalDateTime get() = LocalDateTime(date.previousYear, time)

fun LocalDateTime.next(dayOfWeek: DayOfWeek): LocalDateTime = LocalDateTime(date.next(dayOfWeek), time)
fun LocalDateTime.nextOrSame(dayOfWeek: DayOfWeek): LocalDateTime = LocalDateTime(date.nextOrSame(dayOfWeek), time)
fun LocalDateTime.previous(dayOfWeek: DayOfWeek): LocalDateTime = LocalDateTime(date.previous(dayOfWeek), time)
fun LocalDateTime.previousOrSame(dayOfWeek: DayOfWeek): LocalDateTime = LocalDateTime(date.previousOrSame(dayOfWeek), time)

// ─────────────────────────────────────────────────────────────────────────────
// Boundaries
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.startOfDay: LocalDateTime get() = date.atMidnight
val LocalDateTime.endOfDay: LocalDateTime get() = date.atEndOfDay

// Legacy name
val LocalDateTime.end_of_day: LocalDateTime get() = endOfDay

val LocalDateTime.midnightInstance: LocalDateTime
    get() = LocalDateTime(date, LocalTime(0, 0))

val LocalDateTime.noonInstance: LocalDateTime
    get() = LocalDateTime(date, LocalTime(12, 0))

val LocalDateTime.nextWholeHour: LocalDateTime
    get() = if (hour == 23) LocalDateTime(date.nextDay, LocalTime(0, 0)) else LocalDateTime(date, LocalTime(hour + 1, 0))

val LocalDateTime.currentWholeHour: LocalDateTime
    get() = LocalDateTime(date, LocalTime(hour, 0))

val LocalDateTime.truncatedToMinute: LocalDateTime get() = LocalDateTime(date, LocalTime(hour, minute))
val LocalDateTime.truncatedToSecond: LocalDateTime get() = LocalDateTime(date, LocalTime(hour, minute, second))

val LocalDateTime.startOfMonth: LocalDateTime get() = LocalDateTime(date.startOfMonth, time)
val LocalDateTime.endOfMonth: LocalDateTime get() = LocalDateTime(date.endOfMonth, time)
val LocalDateTime.startOfYear: LocalDateTime get() = LocalDateTime(date.startOfYear, time)
val LocalDateTime.endOfYear: LocalDateTime get() = LocalDateTime(date.endOfYear, time)

// ─────────────────────────────────────────────────────────────────────────────
// Comparisons & Helpers
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.isToday: Boolean get() = date == today()
val LocalDateTime.isYesterday: Boolean get() = date == today().previousDay
val LocalDateTime.isTomorrow: Boolean get() = date == today().nextDay

val LocalDateTime.isWeekend: Boolean get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
val LocalDateTime.isWeekday: Boolean get() = !isWeekend

fun LocalDateTime.isBetween(start: LocalDateTime, end: LocalDateTime): Boolean =
    this >= start && this <= end

val LocalDateTime.relativeDay: String
    get() = when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        isTomorrow -> "Tomorrow"
        else -> format("dd MMM yyyy")
    }

// ─────────────────────────────────────────────────────────────────────────────
// Decimal / Fractional Time
// ─────────────────────────────────────────────────────────────────────────────

/** Time as decimal hours since midnight (e.g. 09:30 → 9.5) */
val LocalDateTime.decimalHours: Double
    get() = hour + minute / 60.0 + second / 3600.0

val LocalDateTime.decimalTime: Double get() = decimalHours  // alias

/** Old decimalYears (approximate) */
val LocalDateTime.decimalYears: Double
    get() = year.toDouble() + (monthNumber.toDouble() / 12) + (dayOfMonth.toDouble() / 365.25) - 1.086

/**
 * Replaces the time component with decimal hours, preserving date.
 * @throws IllegalArgumentException if decimalHours is outside [0, 24]
 */
fun LocalDateTime.withDecimalHours(decimalHours: Double): LocalDateTime {
    require(decimalHours in 0.0..24.0) { "decimalHours must be between 0.0 and 24.0" }
    val totalSeconds = (decimalHours * 3600).toLong()
    return LocalDateTime(
        date,
        LocalTime(
            (totalSeconds / 3600).toInt(),
            ((totalSeconds % 3600) / 60).toInt(),
            (totalSeconds % 60).toInt(),
        )
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Duration Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Duration between this and another LocalDateTime (naive, zone-agnostic — same as java.time). */
fun LocalDateTime.durationUntil(other: LocalDateTime): Duration =
    other.toInstant(TimeZone.UTC) - this.toInstant(TimeZone.UTC)

fun LocalDateTime.secondsUntil(other: LocalDateTime): Long = durationUntil(other).inWholeSeconds
fun LocalDateTime.minutesUntil(other: LocalDateTime): Long = durationUntil(other).inWholeMinutes
fun LocalDateTime.hoursUntil(other: LocalDateTime): Long = durationUntil(other).inWholeHours
fun LocalDateTime.daysUntil(other: LocalDateTime): Long = durationUntil(other).inWholeDays

// ─────────────────────────────────────────────────────────────────────────────
// Serialization Support (unchanged — good!)
// ─────────────────────────────────────────────────────────────────────────────

object LocalDateTimeSerializer : KSerializer<LocalDateTimeJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTimeJson) {
        encoder.encodeString(value.value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTimeJson =
        LocalDateTimeJson(LocalDateTime.parse(decoder.decodeString()))
}

@JvmInline
@Serializable(with = LocalDateTimeSerializer::class)
value class LocalDateTimeJson(val value: LocalDateTime)

/** Convenient way to serialize LocalDateTime */
val LocalDateTime.serialized: LocalDateTimeJson get() = LocalDateTimeJson(this)
