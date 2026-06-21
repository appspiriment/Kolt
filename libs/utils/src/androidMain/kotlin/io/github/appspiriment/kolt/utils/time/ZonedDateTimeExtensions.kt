package io.github.appspiriment.kolt.utils.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// Factory Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current ZonedDateTime in UTC. */
fun nowUtc(): ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

/** Current ZonedDateTime in the system default zone. */
fun nowSystem(): ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault())

/** Current ZonedDateTime in a specific zone. */
fun nowIn(zoneId: ZoneId): ZonedDateTime = ZonedDateTime.now(zoneId)

/** Legacy name from old implementation */
fun millisToZonedDateTime(millis: Long): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())

/** Converts absolute UTC millis to ZonedDateTime in the given zone. */
fun Long.toZonedDateTime(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)

/** Converts wall-clock millis to ZonedDateTime (same local time in target zone). */
fun Long.toZonedDateTimeFromWallClock(zoneId: ZoneId = ZoneId.systemDefault()): ZonedDateTime {
    val local = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneOffset.UTC)
    return local.atZone(zoneId)
}

/** Converts epoch seconds to ZonedDateTime in UTC. */
fun Long.toZonedDateTimeFromSeconds(): ZonedDateTime =
    ZonedDateTime.ofInstant(Instant.ofEpochSecond(this), ZoneOffset.UTC)


// ─────────────────────────────────────────────────────────────────────────────
// Formatting
// ─────────────────────────────────────────────────────────────────────────────

/** Formats with pattern using English locale. */
fun ZonedDateTime.format(pattern: String): String =
    DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(LOCALE_EN).format(this)

/** Formats using a pre-built formatter. */
fun ZonedDateTime.format(formatter: DateTimeFormatter): String = formatter.format(this)

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

/** Parses string with pattern, returns null on failure. */
fun String.toZonedDateTimeOrNull(pattern: String): ZonedDateTime? =
    runCatching {
        ZonedDateTime.parse(this, DateTimeFormatter.ofPattern(pattern, LOCALE_EN))
    }.getOrNull()

/** Parses ISO zoned date-time string. */
fun String.toZonedDateTimeIsoOrNull(): ZonedDateTime? =
    runCatching { ZonedDateTime.parse(this, DateTimeFormatter.ISO_ZONED_DATE_TIME) }.getOrNull()

/** Parses ISO offset date-time string. */
fun String.toZonedDateTimeOffsetOrNull(): ZonedDateTime? =
    runCatching { ZonedDateTime.parse(this, DateTimeFormatter.ISO_OFFSET_DATE_TIME) }.getOrNull()

/** Legacy convenience from old code */
fun String.toZonedDateTimeOrNow(pattern: String): ZonedDateTime =
    toZonedDateTimeOrNull(pattern) ?: nowSystem()


// ─────────────────────────────────────────────────────────────────────────────
// Epoch / Millis Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** UTC millis (absolute timestamp). Best for storage and comparisons. */
val ZonedDateTime.utcMillis: Long get() = toInstant().toEpochMilli()

/** Wall-clock millis (treats local time as if it were UTC). */
val ZonedDateTime.wallClockMillis: Long
    get() = toLocalDateTime().toInstant(ZoneOffset.UTC).toEpochMilli()

/** Epoch seconds in UTC. */
val ZonedDateTime.epochSeconds: Long get() = toEpochSecond()

/** Midnight of this date in the current zone, as UTC millis. */
val ZonedDateTime.midnightMillis: Long
    get() = toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()

// Legacy properties from old code
val ZonedDateTime.millis: Long get() = utcMillis
val ZonedDateTime.noonMillis: Long get() = noonInstance.utcMillis


// ─────────────────────────────────────────────────────────────────────────────
// Zone Handling
// ─────────────────────────────────────────────────────────────────────────────

/** Returns this instant in UTC. */
val ZonedDateTime.asUtc: ZonedDateTime get() = withZoneSameInstant(ZoneOffset.UTC)

/** Returns this instant in system default zone. */
val ZonedDateTime.asSystemZone: ZonedDateTime get() = withZoneSameInstant(ZoneId.systemDefault())

/** Converts to target zone keeping the same instant. */
fun ZonedDateTime.toZone(targetZone: ZoneId): ZonedDateTime = withZoneSameInstant(targetZone)

/** Reinterprets the local time in a new zone (same wall time, different zone). */
fun ZonedDateTime.reinterpretInZone(newZone: ZoneId): ZonedDateTime = withZoneSameLocal(newZone)

val ZonedDateTime.utcOffsetString: String get() = offset.toString()
val ZonedDateTime.zoneIdString: String get() = zone.id


// ─────────────────────────────────────────────────────────────────────────────
// Component Accessors
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.date: LocalDate get() = toLocalDate()
val ZonedDateTime.time: LocalTime get() = toLocalTime()
val ZonedDateTime.local: LocalDateTime get() = toLocalDateTime()

val ZonedDateTime.monthEnum: Month get() = month
val ZonedDateTime.quarter: Int get() = (monthValue - 1) / 3 + 1
val ZonedDateTime.weekOfYear: Int get() = get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
val ZonedDateTime.isLeapYear: Boolean get() = Year.of(year).isLeap


// ─────────────────────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.nextDay: ZonedDateTime get() = plusDays(1)
val ZonedDateTime.previousDay: ZonedDateTime get() = minusDays(1)
val ZonedDateTime.nextWeek: ZonedDateTime get() = plusWeeks(1)
val ZonedDateTime.previousWeek: ZonedDateTime get() = minusWeeks(1)
val ZonedDateTime.nextMonth: ZonedDateTime get() = plusMonths(1)
val ZonedDateTime.previousMonth: ZonedDateTime get() = minusMonths(1)
val ZonedDateTime.nextYear: ZonedDateTime get() = plusYears(1)
val ZonedDateTime.previousYear: ZonedDateTime get() = minusYears(1)

fun ZonedDateTime.next(dayOfWeek: DayOfWeek): ZonedDateTime = with(TemporalAdjusters.next(dayOfWeek))
fun ZonedDateTime.nextOrSame(dayOfWeek: DayOfWeek): ZonedDateTime = with(TemporalAdjusters.nextOrSame(dayOfWeek))
fun ZonedDateTime.previous(dayOfWeek: DayOfWeek): ZonedDateTime = with(TemporalAdjusters.previous(dayOfWeek))
fun ZonedDateTime.previousOrSame(dayOfWeek: DayOfWeek): ZonedDateTime = with(TemporalAdjusters.previousOrSame(dayOfWeek))


// ─────────────────────────────────────────────────────────────────────────────
// Boundaries
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.startOfDay: ZonedDateTime get() = toLocalDate().atStartOfDay(zone)
val ZonedDateTime.endOfDay: ZonedDateTime get() = toLocalDate().atTime(LocalTime.MAX).atZone(zone)

// Legacy name from old code
val ZonedDateTime.end_of_day: ZonedDateTime get() = endOfDay

val ZonedDateTime.midnightInstance: ZonedDateTime
    get() = withHour(0).withMinute(0).withSecond(0).withNano(0)

val ZonedDateTime.noonInstance: ZonedDateTime
    get() = withHour(12).withMinute(0).withSecond(0).withNano(0)

val ZonedDateTime.nextWholeHour: ZonedDateTime
    get() = plusHours(1).withMinute(0).withSecond(0).withNano(0)

val ZonedDateTime.currentWholeHour: ZonedDateTime
    get() = withMinute(0).withSecond(0).withNano(0)

val ZonedDateTime.truncatedToMinute: ZonedDateTime get() = truncatedTo(ChronoUnit.MINUTES)
val ZonedDateTime.truncatedToSecond: ZonedDateTime get() = truncatedTo(ChronoUnit.SECONDS)

val ZonedDateTime.startOfMonth: ZonedDateTime get() = with(TemporalAdjusters.firstDayOfMonth())
val ZonedDateTime.endOfMonth: ZonedDateTime get() = with(TemporalAdjusters.lastDayOfMonth())
val ZonedDateTime.startOfYear: ZonedDateTime get() = with(TemporalAdjusters.firstDayOfYear())
val ZonedDateTime.endOfYear: ZonedDateTime get() = with(TemporalAdjusters.lastDayOfYear())


// ─────────────────────────────────────────────────────────────────────────────
// Comparisons & Helpers
// ─────────────────────────────────────────────────────────────────────────────

val ZonedDateTime.isToday: Boolean get() = toLocalDate() == LocalDate.now(zone)
val ZonedDateTime.isYesterday: Boolean get() = toLocalDate() == LocalDate.now(zone).minusDays(1)
val ZonedDateTime.isTomorrow: Boolean get() = toLocalDate() == LocalDate.now(zone).plusDays(1)

val ZonedDateTime.isWeekend: Boolean get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
val ZonedDateTime.isWeekday: Boolean get() = !isWeekend

val ZonedDateTime.isPast: Boolean get() = toInstant() < Instant.now()
val ZonedDateTime.isFuture: Boolean get() = toInstant() > Instant.now()

fun ZonedDateTime.isBetween(start: ZonedDateTime, end: ZonedDateTime): Boolean =
    !isBefore(start) && !isAfter(end)

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
        val seconds = ChronoUnit.SECONDS.between(this, nowSystem())
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

val ZonedDateTime.decimalHours: Double
    get() = hour.toDouble() + minute.toDouble() / 60 + second.toDouble() / 3600

val ZonedDateTime.decimalTime: Double get() = decimalHours

val ZonedDateTime.decimalYears: Double
    get() = year.toDouble() + (month.value.toDouble() / 12) + (dayOfMonth.toDouble() / 365.25) - 1.086


// ─────────────────────────────────────────────────────────────────────────────
// Serialization Support
// ─────────────────────────────────────────────────────────────────────────────

object ZonedDateTimeSerializer : KSerializer<ZonedDateTimeJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ZonedDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ZonedDateTimeJson) {
        encoder.encodeString(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(value.value))
    }

    override fun deserialize(decoder: Decoder): ZonedDateTimeJson =
        ZonedDateTimeJson(ZonedDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_ZONED_DATE_TIME))
}

@JvmInline
@Serializable(with = ZonedDateTimeSerializer::class)
value class ZonedDateTimeJson(val value: ZonedDateTime)

/** Convenient way to serialize ZonedDateTime */
val ZonedDateTime.serialized: ZonedDateTimeJson get() = ZonedDateTimeJson(this)