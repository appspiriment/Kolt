@file:OptIn(ExperimentalTime::class)

package io.github.appspiriment.kolt.utils.time

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.offsetAt
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
import kotlin.time.ExperimentalTime

// ─────────────────────────────────────────────────────────────────────────────
// Type
// ─────────────────────────────────────────────────────────────────────────────

/**
 * kotlinx-datetime deliberately has no `ZonedDateTime` type (it favors composing
 * [Instant] + [TimeZone] explicitly). This is a minimal stand-in preserving the
 * java.time.ZonedDateTime-shaped API this file used to expose.
 */
data class ZonedDateTime(val instant: Instant, val zone: TimeZone) : Comparable<ZonedDateTime> {
    val localDateTime: LocalDateTime get() = instant.toLocalDateTime(zone)
    override fun compareTo(other: ZonedDateTime): Int = instant.compareTo(other.instant)
}

private fun ZonedDateTime.withLocal(newLocal: LocalDateTime): ZonedDateTime =
    ZonedDateTime(newLocal.toInstant(zone), zone)

// ─────────────────────────────────────────────────────────────────────────────
// Factory Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current ZonedDateTime in UTC. */
fun nowUtc(): ZonedDateTime = ZonedDateTime(Clock.System.now(), TimeZone.UTC)

/** Current ZonedDateTime in the system default zone. */
fun nowSystem(): ZonedDateTime = ZonedDateTime(Clock.System.now(), TimeZone.currentSystemDefault())

/** Current ZonedDateTime in a specific zone. */
fun nowIn(zone: TimeZone): ZonedDateTime = ZonedDateTime(Clock.System.now(), zone)

/** Legacy name from old implementation */
fun millisToZonedDateTime(millis: Long): ZonedDateTime =
    ZonedDateTime(Instant.fromEpochMilliseconds(millis), TimeZone.currentSystemDefault())

/** Converts absolute UTC millis to ZonedDateTime in the given zone. */
fun Long.toZonedDateTime(zone: TimeZone = TimeZone.currentSystemDefault()): ZonedDateTime =
    ZonedDateTime(Instant.fromEpochMilliseconds(this), zone)

/** Converts wall-clock millis to ZonedDateTime (same local time in target zone). */
fun Long.toZonedDateTimeFromWallClock(zone: TimeZone = TimeZone.currentSystemDefault()): ZonedDateTime {
    val local = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    return ZonedDateTime(local.toInstant(zone), zone)
}

/** Converts epoch seconds to ZonedDateTime in UTC. */
fun Long.toZonedDateTimeFromSeconds(): ZonedDateTime =
    ZonedDateTime(Instant.fromEpochSeconds(this), TimeZone.UTC)


// ─────────────────────────────────────────────────────────────────────────────
// Formatting
// ─────────────────────────────────────────────────────────────────────────────

/** Formats with pattern using English locale. */
fun ZonedDateTime.format(pattern: String): String = localDateTimeFormatterFor(pattern).format(localDateTime)

/** Formats using a pre-built formatter. */
fun ZonedDateTime.format(formatter: DateTimeFormat<LocalDateTime>): String = formatter.format(localDateTime)

// === Popular format extensions from old code (for backward compatibility) ===
val ZonedDateTime.time_hhmm_a: String get() = format("hh:mm a")
val ZonedDateTime.time_hhmmss_a: String get() = format("hh:mm:ss a")
val ZonedDateTime.time_HHmm: String get() = format("HH:mm")

val ZonedDateTime.date_yyyymmdd: String get() = format("yyyyMMdd")
val ZonedDateTime.date_mmm_dd: String get() = format("MMM dd")
val ZonedDateTime.date_dd_MMM_yyyy: String get() = format("dd MMM yyyy")

val ZonedDateTime.dateTime_mmm_dd_hh_mm_a: String get() = format("MMM dd hh:mm a")
val ZonedDateTime.dateTime_mmm_dd_HH_mm: String get() = format("MMM dd HH:mm")
val ZonedDateTime.dateTime_mmm_dd_split_hh_mm_a: String get() = format("MMM dd\nhh:mm a")
val ZonedDateTime.dateTime_mmm_dd_split_HH_mm: String get() = format("MMM dd\nHH:mm")
val ZonedDateTime.dateTime_dd_MMM_yyyy_hh_mm_a: String get() = format("dd MMM yyyy hh:mm a")

val ZonedDateTime.weekName: String get() = format("EEEE")


// ─────────────────────────────────────────────────────────────────────────────
// Parsing
// ─────────────────────────────────────────────────────────────────────────────

/** Parses string with pattern (attached to the system default zone), returns null on failure. */
fun String.toZonedDateTimeOrNull(pattern: String): ZonedDateTime? =
    runCatching {
        val zone = TimeZone.currentSystemDefault()
        val local = localDateTimeFormatterFor(pattern).parse(this)
        ZonedDateTime(local.toInstant(zone), zone)
    }.getOrNull()

/** Parses an ISO instant string, attached to UTC. */
fun String.toZonedDateTimeIsoOrNull(): ZonedDateTime? =
    runCatching { ZonedDateTime(Instant.parse(this), TimeZone.UTC) }.getOrNull()

/** Parses an ISO offset date-time string, attached to UTC. */
fun String.toZonedDateTimeOffsetOrNull(): ZonedDateTime? = toZonedDateTimeIsoOrNull()

/** Legacy convenience from old code */
fun String.toZonedDateTimeOrNow(pattern: String): ZonedDateTime =
    toZonedDateTimeOrNull(pattern) ?: nowSystem()


// ─────────────────────────────────────────────────────────────────────────────
// Epoch / Millis Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** UTC millis (absolute timestamp). Best for storage and comparisons. */
val ZonedDateTime.utcMillis: Long get() = instant.toEpochMilliseconds()

/** Wall-clock millis (treats local time as if it were UTC). */
val ZonedDateTime.wallClockMillis: Long
    get() = localDateTime.toInstant(TimeZone.UTC).toEpochMilliseconds()

/** Epoch seconds in UTC. */
val ZonedDateTime.epochSeconds: Long get() = instant.epochSeconds

/** Midnight of this date in the current zone, as UTC millis. */
val ZonedDateTime.midnightMillis: Long
    get() = date.toUtcMillis(zone)

// Legacy properties from old code
val ZonedDateTime.millis: Long get() = utcMillis
val ZonedDateTime.noonMillis: Long get() = noonInstance.utcMillis


// ─────────────────────────────────────────────────────────────────────────────
// Zone Handling
// ─────────────────────────────────────────────────────────────────────────────

/** Returns this instant in UTC. */
val ZonedDateTime.asUtc: ZonedDateTime get() = ZonedDateTime(instant, TimeZone.UTC)

/** Returns this instant in system default zone. */
val ZonedDateTime.asSystemZone: ZonedDateTime get() = ZonedDateTime(instant, TimeZone.currentSystemDefault())

/** Converts to target zone keeping the same instant. */
fun ZonedDateTime.toZone(targetZone: TimeZone): ZonedDateTime = ZonedDateTime(instant, targetZone)

/** Reinterprets the local time in a new zone (same wall time, different zone). */
fun ZonedDateTime.reinterpretInZone(newZone: TimeZone): ZonedDateTime =
    ZonedDateTime(localDateTime.toInstant(newZone), newZone)

val ZonedDateTime.utcOffsetString: String get() = zone.offsetAt(instant).toString()
val ZonedDateTime.zoneIdString: String get() = zone.id


// ─────────────────────────────────────────────────────────────────────────────
// Component Accessors
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.date: LocalDate get() = localDateTime.date
val ZonedDateTime.time: LocalTime get() = localDateTime.time
val ZonedDateTime.local: LocalDateTime get() = localDateTime

val ZonedDateTime.monthEnum: Month get() = localDateTime.month
val ZonedDateTime.quarter: Int get() = localDateTime.quarter
val ZonedDateTime.weekOfYear: Int get() = date.weekOfYear
val ZonedDateTime.isLeapYear: Boolean get() = date.isLeapYear


// ─────────────────────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.nextDay: ZonedDateTime get() = withLocal(localDateTime.nextDay)
val ZonedDateTime.previousDay: ZonedDateTime get() = withLocal(localDateTime.previousDay)
val ZonedDateTime.nextWeek: ZonedDateTime get() = withLocal(localDateTime.nextWeek)
val ZonedDateTime.previousWeek: ZonedDateTime get() = withLocal(localDateTime.previousWeek)
val ZonedDateTime.nextMonth: ZonedDateTime get() = withLocal(localDateTime.nextMonth)
val ZonedDateTime.previousMonth: ZonedDateTime get() = withLocal(localDateTime.previousMonth)
val ZonedDateTime.nextYear: ZonedDateTime get() = withLocal(localDateTime.nextYear)
val ZonedDateTime.previousYear: ZonedDateTime get() = withLocal(localDateTime.previousYear)

fun ZonedDateTime.next(dayOfWeek: DayOfWeek): ZonedDateTime = withLocal(localDateTime.next(dayOfWeek))
fun ZonedDateTime.nextOrSame(dayOfWeek: DayOfWeek): ZonedDateTime = withLocal(localDateTime.nextOrSame(dayOfWeek))
fun ZonedDateTime.previous(dayOfWeek: DayOfWeek): ZonedDateTime = withLocal(localDateTime.previous(dayOfWeek))
fun ZonedDateTime.previousOrSame(dayOfWeek: DayOfWeek): ZonedDateTime = withLocal(localDateTime.previousOrSame(dayOfWeek))


// ─────────────────────────────────────────────────────────────────────────────
// Boundaries
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.startOfDay: ZonedDateTime get() = withLocal(date.atMidnight)
val ZonedDateTime.endOfDay: ZonedDateTime get() = withLocal(date.atEndOfDay)

// Legacy name from old code
val ZonedDateTime.end_of_day: ZonedDateTime get() = endOfDay

val ZonedDateTime.midnightInstance: ZonedDateTime get() = withLocal(LocalDateTime(date, LocalTime(0, 0)))
val ZonedDateTime.noonInstance: ZonedDateTime get() = withLocal(LocalDateTime(date, LocalTime(12, 0)))
val ZonedDateTime.nextWholeHour: ZonedDateTime get() = withLocal(localDateTime.nextWholeHour)
val ZonedDateTime.currentWholeHour: ZonedDateTime get() = withLocal(localDateTime.currentWholeHour)
val ZonedDateTime.truncatedToMinute: ZonedDateTime get() = withLocal(localDateTime.truncatedToMinute)
val ZonedDateTime.truncatedToSecond: ZonedDateTime get() = withLocal(localDateTime.truncatedToSecond)

val ZonedDateTime.startOfMonth: ZonedDateTime get() = withLocal(localDateTime.startOfMonth)
val ZonedDateTime.endOfMonth: ZonedDateTime get() = withLocal(localDateTime.endOfMonth)
val ZonedDateTime.startOfYear: ZonedDateTime get() = withLocal(localDateTime.startOfYear)
val ZonedDateTime.endOfYear: ZonedDateTime get() = withLocal(localDateTime.endOfYear)


// ─────────────────────────────────────────────────────────────────────────────
// Comparisons & Helpers
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.isToday: Boolean get() = date == today(zone)
val ZonedDateTime.isYesterday: Boolean get() = date == today(zone).previousDay
val ZonedDateTime.isTomorrow: Boolean get() = date == today(zone).nextDay

val ZonedDateTime.isWeekend: Boolean get() = localDateTime.isWeekend
val ZonedDateTime.isWeekday: Boolean get() = !isWeekend

val ZonedDateTime.isPast: Boolean get() = instant < Clock.System.now()
val ZonedDateTime.isFuture: Boolean get() = instant > Clock.System.now()

fun ZonedDateTime.isBetween(start: ZonedDateTime, end: ZonedDateTime): Boolean =
    this >= start && this <= end

val ZonedDateTime.relativeDay: String
    get() = when {
        isToday -> "Today"
        isYesterday -> "Yesterday"
        isTomorrow -> "Tomorrow"
        else -> format("dd MMM yyyy")
    }

/** Human readable "time ago" */
val ZonedDateTime.timeAgo: String
    get() {
        val seconds = (Clock.System.now() - instant).inWholeSeconds
        val abs = kotlin.math.abs(seconds)
        val suffix = if (seconds >= 0) "ago" else "from now"
        return when {
            abs < 60 -> "Just now"
            abs < 3600 -> "${abs / 60} min $suffix"
            abs < 86400 -> "${abs / 3600} hour${if (abs / 3600 > 1) "s" else ""} $suffix"
            abs < 2_592_000 -> "${abs / 86400} day${if (abs / 86400 > 1) "s" else ""} $suffix"
            else -> "${abs / 2_592_000} month${if (abs / 2_592_000 > 1) "s" else ""} $suffix"
        }
    }


// ─────────────────────────────────────────────────────────────────────────────
// Decimal / Fractional Time (from old code)
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.decimalHours: Double get() = localDateTime.decimalHours
val ZonedDateTime.decimalTime: Double get() = decimalHours
val ZonedDateTime.decimalYears: Double get() = localDateTime.decimalYears


// ─────────────────────────────────────────────────────────────────────────────
// Serialization Support
// ─────────────────────────────────────────────────────────────────────────────

object ZonedDateTimeSerializer : KSerializer<ZonedDateTimeJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTimeJson) {
        encoder.encodeString("${value.value.instant}|${value.value.zone.id}")
    }

    override fun deserialize(decoder: Decoder): ZonedDateTimeJson {
        val (instantStr, zoneId) = decoder.decodeString().split("|", limit = 2)
        return ZonedDateTimeJson(ZonedDateTime(Instant.parse(instantStr), TimeZone.of(zoneId)))
    }
}

@JvmInline
@Serializable(with = ZonedDateTimeSerializer::class)
value class ZonedDateTimeJson(val value: ZonedDateTime)

/** Convenient way to serialize ZonedDateTime */
val ZonedDateTime.serialized: ZonedDateTimeJson get() = ZonedDateTimeJson(this)
