package io.github.appspiriment.kolt.utils.time

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoUnit
import java.time.temporal.IsoFields
import java.time.temporal.TemporalAdjusters
import java.util.Locale


// ─────────────────────────────────────────────────────────────────────────────
// Factory Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current local date-time in the system default zone. */
fun nowLocal(): LocalDateTime = LocalDateTime.now(ZoneId.systemDefault())

/** Current local date-time in a specific zone. */
fun nowLocal(zoneId: ZoneId): LocalDateTime = LocalDateTime.now(zoneId)

/**
 * Converts absolute UTC millis to LocalDateTime in the given zone.
 * Best for timestamps from backend or System.currentTimeMillis().
 */
fun fromUtcMillisToLocalDateTime(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId)

/** Legacy name from old implementation */
fun millisToLocalDateTime(millis: Long): LocalDateTime =
    fromUtcMillisToLocalDateTime(millis)

/**
 * Treats millis as wall-clock time (ignores timezone) and converts to LocalDateTime.
 * Useful for time pickers.
 */
fun fromWallClockMillisToLocalDateTime(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)

// ─────────────────────────────────────────────────────────────────────────────
// Formatting (Old popular formats + new style)
// ─────────────────────────────────────────────────────────────────────────────

/** Formats with pattern using English locale. */
fun LocalDateTime.format(pattern: String): String =
    DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(Locale.ENGLISH).format(this)

/** Formats using a pre-built formatter. */
fun LocalDateTime.format(formatter: DateTimeFormatter): String = formatter.format(this)

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
    runCatching {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH))
    }.getOrNull()

/** Parses ISO local date-time string (yyyy-MM-ddTHH:mm:ss). */
fun String.toLocalDateTimeIsoOrNull(): LocalDateTime? =
    runCatching { LocalDateTime.parse(this, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }.getOrNull()

/** Legacy convenience from old code */
fun String.toLocalDateTimeOrNow(pattern: String): LocalDateTime =
    toLocalDateTimeOrNull(pattern) ?: nowLocal()

// ─────────────────────────────────────────────────────────────────────────────
// Epoch / Millis Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** Converts this LocalDateTime to UTC millis using the given zone. */
fun LocalDateTime.toUtcMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    this.atZone(zoneId).toInstant().toEpochMilli()

/** Wall-clock millis (treats this local time as if it were in UTC). */
val LocalDateTime.wallClockMillis: Long
    get() = this.toInstant(ZoneOffset.UTC).toEpochMilli()

/** Midnight (00:00:00) of this date in the given zone, as UTC millis. */
fun LocalDateTime.midnightMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    this.toLocalDate().atStartOfDay(zoneId).toInstant().toEpochMilli()

// Legacy properties from old code
val LocalDateTime.midnightMillis: Long get() = midnightMillis()
val LocalDateTime.noonMillis: Long get() = noonInstance.toUtcMillis()

// ─────────────────────────────────────────────────────────────────────────────
// Component Accessors
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.date: LocalDate get() = toLocalDate()
val LocalDateTime.time: LocalTime get() = toLocalTime()

/** Month as enum */
val LocalDateTime.monthEnum: Month get() = month

/** Quarter of the year (1-4) */
val LocalDateTime.quarter: Int get() = (monthValue - 1) / 3 + 1

/** ISO Week of year (1-53) */
val LocalDateTime.weekOfYear: Int get() = get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)

// ─────────────────────────────────────────────────────────────────────────────
// Navigation
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.nextDay: LocalDateTime get() = plusDays(1)
val LocalDateTime.previousDay: LocalDateTime get() = minusDays(1)
val LocalDateTime.nextWeek: LocalDateTime get() = plusWeeks(1)
val LocalDateTime.previousWeek: LocalDateTime get() = minusWeeks(1)
val LocalDateTime.nextMonth: LocalDateTime get() = plusMonths(1)
val LocalDateTime.previousMonth: LocalDateTime get() = minusMonths(1)
val LocalDateTime.nextYear: LocalDateTime get() = plusYears(1)
val LocalDateTime.previousYear: LocalDateTime get() = minusYears(1)

fun LocalDateTime.next(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.next(dayOfWeek))
fun LocalDateTime.nextOrSame(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.nextOrSame(dayOfWeek))
fun LocalDateTime.previous(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.previous(dayOfWeek))
fun LocalDateTime.previousOrSame(dayOfWeek: DayOfWeek): LocalDateTime = with(TemporalAdjusters.previousOrSame(dayOfWeek))

// ─────────────────────────────────────────────────────────────────────────────
// Boundaries
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.startOfDay: LocalDateTime get() = toLocalDate().atStartOfDay()
val LocalDateTime.endOfDay: LocalDateTime get() = toLocalDate().atTime(LocalTime.MAX)

// Legacy name
val LocalDateTime.end_of_day: LocalDateTime get() = endOfDay

val LocalDateTime.midnightInstance: LocalDateTime
    get() = withHour(0).withMinute(0).withSecond(0).withNano(0)

val LocalDateTime.noonInstance: LocalDateTime
    get() = withHour(12).withMinute(0).withSecond(0).withNano(0)

val LocalDateTime.nextWholeHour: LocalDateTime
    get() = plusHours(1).withMinute(0).withSecond(0).withNano(0)

val LocalDateTime.currentWholeHour: LocalDateTime
    get() = withMinute(0).withSecond(0).withNano(0)

val LocalDateTime.truncatedToMinute: LocalDateTime get() = truncatedTo(ChronoUnit.MINUTES)
val LocalDateTime.truncatedToSecond: LocalDateTime get() = truncatedTo(ChronoUnit.SECONDS)

val LocalDateTime.startOfMonth: LocalDateTime get() = with(TemporalAdjusters.firstDayOfMonth())
val LocalDateTime.endOfMonth: LocalDateTime get() = with(TemporalAdjusters.lastDayOfMonth())
val LocalDateTime.startOfYear: LocalDateTime get() = with(TemporalAdjusters.firstDayOfYear())
val LocalDateTime.endOfYear: LocalDateTime get() = with(TemporalAdjusters.lastDayOfYear())

// ─────────────────────────────────────────────────────────────────────────────
// Comparisons & Helpers
// ─────────────────────────────────────────────────────────────────────────────

val LocalDateTime.isToday: Boolean get() = toLocalDate() == LocalDate.now(ZoneId.systemDefault())
val LocalDateTime.isYesterday: Boolean get() = toLocalDate() == LocalDate.now(ZoneId.systemDefault()).minusDays(1)
val LocalDateTime.isTomorrow: Boolean get() = toLocalDate() == LocalDate.now(ZoneId.systemDefault()).plusDays(1)

val LocalDateTime.isWeekend: Boolean get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY
val LocalDateTime.isWeekday: Boolean get() = !isWeekend

fun LocalDateTime.isBetween(start: LocalDateTime, end: LocalDateTime): Boolean =
    !isBefore(start) && !isAfter(end)

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
    get() = year.toDouble() + (monthValue.toDouble() / 12) + (dayOfMonth.toDouble() / 365.25) - 1.086

/**
 * Replaces the time component with decimal hours, preserving date.
 * @throws IllegalArgumentException if decimalHours is outside [0, 24]
 */
fun LocalDateTime.withDecimalHours(decimalHours: Double): LocalDateTime {
    require(decimalHours in 0.0..24.0) { "decimalHours must be between 0.0 and 24.0" }
    val totalSeconds = (decimalHours * 3600).toLong()
    return withHour((totalSeconds / 3600).toInt())
        .withMinute(((totalSeconds % 3600) / 60).toInt())
        .withSecond((totalSeconds % 60).toInt())
        .withNano(0)
}

// ─────────────────────────────────────────────────────────────────────────────
// Duration Helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Duration between this and another LocalDateTime */
fun LocalDateTime.durationUntil(other: LocalDateTime): Duration = Duration.between(this, other)

fun LocalDateTime.secondsUntil(other: LocalDateTime): Long = ChronoUnit.SECONDS.between(this, other)
fun LocalDateTime.minutesUntil(other: LocalDateTime): Long = ChronoUnit.MINUTES.between(this, other)
fun LocalDateTime.hoursUntil(other: LocalDateTime): Long = ChronoUnit.HOURS.between(this, other)
fun LocalDateTime.daysUntil(other: LocalDateTime): Long = ChronoUnit.DAYS.between(this, other)

// ─────────────────────────────────────────────────────────────────────────────
// Serialization Support (unchanged — good!)
// ─────────────────────────────────────────────────────────────────────────────

object LocalDateTimeSerializer : KSerializer<LocalDateTimeJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTimeJson) {
        encoder.encodeString(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(value.value))
    }

    override fun deserialize(decoder: Decoder): LocalDateTimeJson =
        LocalDateTimeJson(LocalDateTime.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
}

@JvmInline
@Serializable(with = LocalDateTimeSerializer::class)
value class LocalDateTimeJson(val value: LocalDateTime)

/** Convenient way to serialize LocalDateTime */
val LocalDateTime.serialized: LocalDateTimeJson get() = LocalDateTimeJson(this)