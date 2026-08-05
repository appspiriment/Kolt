@file:OptIn(ExperimentalTime::class)

package io.github.appspiriment.kolt.utils.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
// Factory helpers
// ─────────────────────────────────────────────────────────────────────────────

/** Current date in [timeZone] (defaults to the system default zone). */
fun today(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Clock.System.now().toLocalDateTime(timeZone).date

/**
 * Converts ABSOLUTE epoch-millis (UTC) to [LocalDate] based on [timeZone].
 */
fun fromUtcMillisToLocalDate(millis: Long, timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone).date

/** Legacy name from old implementation */
fun millisToLocalDate(millis: Long): LocalDate = fromUtcMillisToLocalDate(millis)

/**
 * Converts WALL-CLOCK millis (local time reading) directly to [LocalDate].
 * Interprets the millis as if they were UTC/Zone-blind.
 */
fun fromWallClockMillisToLocalDate(millis: Long): LocalDate =
    Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date


// ─────────────────────────────────────────────────────────────────────────────
// Formatting
// ─────────────────────────────────────────────────────────────────────────────

// ponytail: plain (non-thread-safe) cache — java.time's original used ConcurrentHashMap,
// but formatter construction here is cheap; add synchronization if profiling shows contention.
private val localDateFormatterCache = mutableMapOf<String, DateTimeFormat<LocalDate>>()

/** Returns a [DateTimeFormat] for [pattern], built with English month/weekday names. */
fun localDateFormatterFor(pattern: String): DateTimeFormat<LocalDate> =
    localDateFormatterCache.getOrPut(pattern) {
        LocalDate.Format { byUnicodePattern(pattern) }
    }

/** Formats this [LocalDate] with [pattern] in English locale. */
fun LocalDate.format(pattern: String): String = localDateFormatterFor(pattern).format(this)

/** Formats this [LocalDate] with an explicit [DateTimeFormat]. */
fun LocalDate.format(formatter: DateTimeFormat<LocalDate>): String = formatter.format(this)

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
    runCatching { localDateFormatterFor(pattern).parse(this) }.getOrNull()

/** Parses an ISO-8601 string (`yyyy-MM-dd`) to [LocalDate], or `null` on failure. */
fun String.toLocalDateIsoOrNull(): LocalDate? =
    runCatching { LocalDate.parse(this) }.getOrNull()

/** Legacy convenience from old code */
fun String.toLocalDateOrNow(pattern: String): LocalDate =
    toLocalDateOrNull(pattern) ?: today()


// ─────────────────────────────────────────────────────────────────────────────
// Epoch / millis
// ─────────────────────────────────────────────────────────────────────────────

/** Absolute UTC millis for the start of this day (00:00) in [timeZone]. */
fun LocalDate.toUtcMillis(timeZone: TimeZone = TimeZone.currentSystemDefault()): Long =
    this.atStartOfDayIn(timeZone).toEpochMilliseconds()

/** Wall-clock millis for the start of this day (treats date as UTC). */
val LocalDate.wallClockMillis: Long
    get() = this.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

// Legacy properties from old code
val LocalDate.millis: Long get() = toUtcMillis()
val LocalDate.midnightMillis: Long get() = toUtcMillis()


// ─────────────────────────────────────────────────────────────────────────────
// Conversions
// ─────────────────────────────────────────────────────────────────────────────

/** This date at 00:00:00.000. */
val LocalDate.atMidnight: LocalDateTime get() = LocalDateTime(this, LocalTime(0, 0))

/** This date at 23:59:59.999999999. */
val LocalDate.atEndOfDay: LocalDateTime get() = LocalDateTime(this, LocalTime(23, 59, 59, 999_999_999))

/** Converts to [LocalDateTime] at a specific time. */
fun LocalDate.atTime(hour: Int, minute: Int): LocalDateTime = LocalDateTime(this, LocalTime(hour, minute))


// ─────────────────────────────────────────────────────────────────────────────
// Navigation — step forward / backward
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.nextDay: LocalDate       get() = plus(1, DateTimeUnit.DAY)
val LocalDate.previousDay: LocalDate   get() = minus(1, DateTimeUnit.DAY)
val LocalDate.nextWeek: LocalDate      get() = plus(1, DateTimeUnit.WEEK)
val LocalDate.previousWeek: LocalDate  get() = minus(1, DateTimeUnit.WEEK)
val LocalDate.nextMonth: LocalDate     get() = plus(1, DateTimeUnit.MONTH)
val LocalDate.previousMonth: LocalDate get() = minus(1, DateTimeUnit.MONTH)
val LocalDate.nextYear: LocalDate      get() = plus(1, DateTimeUnit.YEAR)
val LocalDate.previousYear: LocalDate  get() = minus(1, DateTimeUnit.YEAR)

/** Advances to next [dayOfWeek] occurrence (skips today if already on it). */
fun LocalDate.next(dayOfWeek: DayOfWeek): LocalDate {
    val diff = (dayOfWeek.isoDayNumber - this.dayOfWeek.isoDayNumber + 7) % 7
    return plus(if (diff == 0) 7 else diff, DateTimeUnit.DAY)
}

/** Advances to next [dayOfWeek], stays if today matches. */
fun LocalDate.nextOrSame(dayOfWeek: DayOfWeek): LocalDate {
    val diff = (dayOfWeek.isoDayNumber - this.dayOfWeek.isoDayNumber + 7) % 7
    return plus(diff, DateTimeUnit.DAY)
}

/** Moves back to previous [dayOfWeek] (skips today). */
fun LocalDate.previous(dayOfWeek: DayOfWeek): LocalDate {
    val diff = (this.dayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber + 7) % 7
    return minus(if (diff == 0) 7 else diff, DateTimeUnit.DAY)
}

/** Moves back to previous [dayOfWeek], stays if today matches. */
fun LocalDate.previousOrSame(dayOfWeek: DayOfWeek): LocalDate {
    val diff = (this.dayOfWeek.isoDayNumber - dayOfWeek.isoDayNumber + 7) % 7
    return minus(diff, DateTimeUnit.DAY)
}


// ─────────────────────────────────────────────────────────────────────────────
// Navigation — boundaries
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.startOfMonth: LocalDate  get() = LocalDate(year, monthNumber, 1)
val LocalDate.endOfMonth: LocalDate    get() = startOfMonth.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)
val LocalDate.startOfYear: LocalDate   get() = LocalDate(year, 1, 1)
val LocalDate.endOfYear: LocalDate     get() = LocalDate(year, 12, 31)
val LocalDate.startOfWeek: LocalDate   get() = previousOrSame(DayOfWeek.MONDAY)
val LocalDate.endOfWeek: LocalDate     get() = nextOrSame(DayOfWeek.SUNDAY)


// ─────────────────────────────────────────────────────────────────────────────
// Comparison & Helpers
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.isToday: Boolean     get() = this == today()
val LocalDate.isYesterday: Boolean get() = this == today().minus(1, DateTimeUnit.DAY)
val LocalDate.isTomorrow: Boolean  get() = this == today().plus(1, DateTimeUnit.DAY)

val LocalDate.isWeekend: Boolean
    get() = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY

val LocalDate.isWeekday: Boolean get() = !isWeekend

val LocalDate.isLastDayOfMonth: Boolean get() = this == endOfMonth

fun LocalDate.isBetween(start: LocalDate, end: LocalDate): Boolean =
    this >= start && this <= end


// ─────────────────────────────────────────────────────────────────────────────
// Component helpers
// ─────────────────────────────────────────────────────────────────────────────

val LocalDate.monthEnum: Month get() = month

/** ISO-8601 week number (week containing this date's Thursday). */
val LocalDate.weekOfYear: Int
    get() {
        val thursdayOfThisWeek = plus(4 - dayOfWeek.isoDayNumber, DateTimeUnit.DAY)
        val firstDayOfYear = LocalDate(thursdayOfThisWeek.year, 1, 1)
        return firstDayOfYear.daysUntil(thursdayOfThisWeek) / 7 + 1
    }

val LocalDate.isLeapYear: Boolean get() = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
val LocalDate.daysInMonth: Int get() = endOfMonth.dayOfMonth
val LocalDate.quarter: Int get() = (monthNumber - 1) / 3 + 1


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
// Serialization
// ─────────────────────────────────────────────────────────────────────────────

object LocalDateSerializer : KSerializer<LocalDateJson> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateJson) =
        encoder.encodeString(value.value.toString())

    override fun deserialize(decoder: Decoder): LocalDateJson =
        LocalDateJson(LocalDate.parse(decoder.decodeString()))
}

@JvmInline
@Serializable(with = LocalDateSerializer::class)
value class LocalDateJson(val value: LocalDate)

/** Wraps this [LocalDate] in [LocalDateJson] for kotlinx.serialization. */
inline val LocalDate.serialized: LocalDateJson get() = LocalDateJson(this)
