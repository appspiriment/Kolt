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
import java.time.Period
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
// Factory helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current date in the system default zone. */
fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

/** Current date in a specific zone. */
fun today(zoneId: ZoneId): LocalDate = LocalDate.now(zoneId)

/**
 * Converts ABSOLUTE epoch-millis (UTC) to [LocalDate] based on [zoneId].
 */
fun fromUtcMillisToLocalDate(millis: Long, zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()

/** Legacy name from old implementation */
fun millisToLocalDate(millis: Long): LocalDate = fromUtcMillisToLocalDate(millis)

/**
 * Converts WALL-CLOCK millis (local time reading) directly to [LocalDate].
 * Interprets the millis as if they were UTC/Zone-blind.
 */
fun fromWallClockMillisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()


// ─────────────────────────────────────────────────────────────────────────────
// Formatting
// ─────────────────────────────────────────────────────────────────────────────

/** Formats this [LocalDate] with [pattern] in English locale. */
fun LocalDate.format(pattern: String): String = formatterFor(pattern).format(this)

/** Formats this [LocalDate] with an explicit [DateTimeFormatter]. */
fun LocalDate.format(formatter: DateTimeFormatter): String = formatter.format(this)

// === Popular format extensions kept from old code for backward compatibility ===
val LocalDate.date_yyyymmdd: String get() = format("yyyyMMdd")
val LocalDate.date_mmm_dd: String get() = format("MMM dd")
val LocalDate.date_dd_MMM_yyyy: String get() = format("dd MMM yyyy")

val LocalDate.weekName: String get() = format("EEEE")


// ─────────────────────────────────────────────────────────────────────────────
// Parsing
// ─────────────────────────────────────────────────────────────────────────────

/** Parses to [LocalDate] with [pattern], or `null` on failure. */
fun String.toLocalDateOrNull(pattern: String): LocalDate? =
    runCatching { LocalDate.parse(this, formatterFor(pattern)) }.getOrNull()

/** Parses an ISO-8601 string (`yyyy-MM-dd`) to [LocalDate], or `null` on failure. */
fun String.toLocalDateIsoOrNull(): LocalDate? =
    runCatching { LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()

/** Legacy convenience from old code */
fun String.toLocalDateOrNow(pattern: String): LocalDate =
    toLocalDateOrNull(pattern) ?: today()


// ─────────────────────────────────────────────────────────────────────────────
// Epoch / millis
// ─────────────────────────────────────────────────────────────────────────────

/** Absolute UTC millis for the start of this day (00:00) in [zoneId]. */
fun LocalDate.toUtcMillis(zoneId: ZoneId = ZoneId.systemDefault()): Long =
    this.atStartOfDay(zoneId).toInstant().toEpochMilli()

/** Wall-clock millis for the start of this day (treats date as UTC). */
val LocalDate.wallClockMillis: Long
    get() = this.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

// Legacy properties from old code
val LocalDate.millis: Long get() = toUtcMillis()
val LocalDate.midnightMillis: Long get() = toUtcMillis()


// ─────────────────────────────────────────────────────────────────────────────
// Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** This date at 00:00:00.000. */
val LocalDate.atMidnight: LocalDateTime get() = atStartOfDay()

/** This date at 23:59:59.999. */
val LocalDate.atEndOfDay: LocalDateTime get() = atTime(LocalTime.MAX)

/** Converts to [LocalDateTime] at a specific time. */
fun LocalDate.atTime(hour: Int, minute: Int): LocalDateTime = atTime(LocalTime.of(hour, minute))


// ─────────────────────────────────────────────────────────────────────────────
// Navigation — step forward / backward
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.nextDay: LocalDate       get() = plusDays(1)
val LocalDate.previousDay: LocalDate   get() = minusDays(1)
val LocalDate.nextWeek: LocalDate      get() = plusWeeks(1)
val LocalDate.previousWeek: LocalDate  get() = minusWeeks(1)
val LocalDate.nextMonth: LocalDate     get() = plusMonths(1)
val LocalDate.previousMonth: LocalDate get() = minusMonths(1)
val LocalDate.nextYear: LocalDate      get() = plusYears(1)
val LocalDate.previousYear: LocalDate  get() = minusYears(1)

/** Advances to next [dayOfWeek] occurrence (skips today if already on it). */
fun LocalDate.next(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.next(dayOfWeek))

/** Advances to next [dayOfWeek], stays if today matches. */
fun LocalDate.nextOrSame(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.nextOrSame(dayOfWeek))

/** Moves back to previous [dayOfWeek] (skips today). */
fun LocalDate.previous(dayOfWeek: DayOfWeek): LocalDate = with(TemporalAdjusters.previous(dayOfWeek))


// ─────────────────────────────────────────────────────────────────────────────
// Navigation — boundaries
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.startOfMonth: LocalDate  get() = with(TemporalAdjusters.firstDayOfMonth())
val LocalDate.endOfMonth: LocalDate    get() = with(TemporalAdjusters.lastDayOfMonth())
val LocalDate.startOfYear: LocalDate   get() = with(TemporalAdjusters.firstDayOfYear())
val LocalDate.endOfYear: LocalDate     get() = with(TemporalAdjusters.lastDayOfYear())
val LocalDate.startOfWeek: LocalDate   get() = with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
val LocalDate.endOfWeek: LocalDate     get() = with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))


// ─────────────────────────────────────────────────────────────────────────────
// Comparison & Helpers
// ─────────────────────────────────────────────────────────────────────────────

fun LocalDate.daysUntil(other: LocalDate): Long = ChronoUnit.DAYS.between(this, other)
fun LocalDate.monthsUntil(other: LocalDate): Long = ChronoUnit.MONTHS.between(this, other)
fun LocalDate.yearsUntil(other: LocalDate): Long = ChronoUnit.YEARS.between(this, other)
fun LocalDate.periodUntil(other: LocalDate): Period = Period.between(this, other)

val LocalDate.isToday: Boolean     get() = this == today()
val LocalDate.isYesterday: Boolean get() = this == today().minusDays(1)
val LocalDate.isTomorrow: Boolean  get() = this == today().plusDays(1)

val LocalDate.isWeekend: Boolean
    get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

val LocalDate.isWeekday: Boolean get() = !isWeekend

val LocalDate.isLastDayOfMonth: Boolean get() = this == endOfMonth

fun LocalDate.isBetween(start: LocalDate, end: LocalDate): Boolean =
    !isBefore(start) && !isAfter(end)


// ─────────────────────────────────────────────────────────────────────────────
// Component helpers
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.monthEnum: Month get() = month
val LocalDate.weekOfYear: Int get() = get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
val LocalDate.isLeapYear: Boolean get() = Year.of(year).isLeap
val LocalDate.daysInMonth: Int get() = month.length(isLeapYear)
val LocalDate.quarter: Int get() = (monthValue - 1) / 3 + 1


// ─────────────────────────────────────────────────────────────────────────────
// Relative labels
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.relativeDay: String
    get() = when {
        isToday     -> "Today"
        isYesterday -> "Yesterday"
        isTomorrow  -> "Tomorrow"
        else        -> format("dd MMM yyyy")
    }


// ─────────────────────────────────────────────────────────────────────────────
// Clamp
// ─────────────────────────────────────────────────────────────────────────────

fun LocalDate.coerceIn(min: LocalDate, max: LocalDate): LocalDate = when {
    isBefore(min) -> min
    isAfter(max)  -> max
    else          -> this
}


// ─────────────────────────────────────────────────────────────────────────────
// Serialization
// ─────────────────────────────────────────────────────────────────────────────

object LocalDateSerializer : KSerializer<LocalDateJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateJson) =
        encoder.encodeString(DateTimeFormatter.ISO_LOCAL_DATE.format(value.value))

    override fun deserialize(decoder: Decoder): LocalDateJson =
        LocalDateJson(LocalDate.parse(decoder.decodeString(), DateTimeFormatter.ISO_LOCAL_DATE))
}

@JvmInline
@Serializable(with = LocalDateSerializer::class)
value class LocalDateJson(val value: LocalDate)

/** Wraps this [LocalDate] in [LocalDateJson] for kotlinx.serialization. */
inline val LocalDate.serialized: LocalDateJson get() = LocalDateJson(this)