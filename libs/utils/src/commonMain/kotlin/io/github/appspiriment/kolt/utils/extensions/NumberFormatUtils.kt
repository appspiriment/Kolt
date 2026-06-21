package io.github.appspiriment.kolt.utils.extensions

import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow

// ── Internal helpers ──────────────────────────────────────────────────────────

/** Formats [this] to [decimals] decimal places using pure Kotlin (no String.format). */
private fun Double.toFixed(decimals: Int): String {
    val factor = 10.0.pow(decimals.toDouble())
    val absVal = if (this < 0.0) -this else this
    val shifted = absVal * factor
    val rounded = if (shifted - shifted.toLong() >= 0.5) shifted.toLong() + 1L else shifted.toLong()
    val intPart = rounded / factor.toLong()
    val decPart = rounded % factor.toLong()
    val sign = if (this < 0.0 && (intPart > 0L || decPart > 0L)) "-" else ""
    return if (decimals == 0) "$sign$intPart"
    else "$sign$intPart.${decPart.toString().padStart(decimals, '0')}"
}

/** "1.0" → "1",  "1.50" → "1.5" */
private fun String.trimTrailingZero(): String =
    if (contains('.')) trimEnd('0').trimEnd('.') else this

// ── Thousands-separated ───────────────────────────────────────────────────────

/**
 * Formats a number with comma thousands-separators (English locale format).
 * 1000 → "1,000"
 *
 * For locale-aware formatting on JVM/Android, use java.text.NumberFormat directly.
 */
fun Int.toFormattedString(): String = toLong().toFormattedString()

fun Long.toFormattedString(): String {
    val neg = this < 0
    val digits = if (neg) toString().substring(1) else toString()
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return if (neg) "-$grouped" else grouped
}

fun Double.toFormattedString(decimals: Int = 2): String {
    val fixed = toFixed(decimals)
    val dot = fixed.indexOf('.')
    val intStr = if (dot >= 0) fixed.substring(0, dot) else fixed
    val decStr = if (dot >= 0) fixed.substring(dot) else ""
    val neg = intStr.startsWith('-')
    val digits = if (neg) intStr.substring(1) else intStr
    val grouped = digits.reversed().chunked(3).joinToString(",").reversed()
    return "${if (neg) "-" else ""}$grouped$decStr"
}

fun Float.toFormattedString(decimals: Int = 2): String = toDouble().toFormattedString(decimals)

// ── Compact notation ──────────────────────────────────────────────────────────

/**
 * Formats a large number as a compact string.
 * 1_200         → "1.2K"
 * 1_500_000     → "1.5M"
 * 2_300_000_000 → "2.3B"
 */
fun Long.toCompactString(): String {
    if (this == Long.MIN_VALUE) return "-9.2E"
    val absVal = abs(this)
    val sign = if (this < 0) "-" else ""
    return when {
        absVal < 1_000L -> "$this"
        absVal < 1_000_000L -> "$sign${(absVal / 1_000.0).toFixed(1).trimTrailingZero()}K"
        absVal < 1_000_000_000L -> "$sign${(absVal / 1_000_000.0).toFixed(1).trimTrailingZero()}M"
        absVal < 1_000_000_000_000L -> "$sign${(absVal / 1_000_000_000.0).toFixed(1).trimTrailingZero()}B"
        else -> "$sign${(absVal / 1_000_000_000_000.0).toFixed(1).trimTrailingZero()}T"
    }
}

fun Int.toCompactString(): String = toLong().toCompactString()

// ── File sizes ────────────────────────────────────────────────────────────────

/**
 * Formats a byte count as a human-readable file size.
 * 0           → "0 B"
 * 1_024       → "1 KB"
 * 1_572_864   → "1.5 MB"
 */
fun Long.toFileSizeString(): String {
    if (this <= 0L) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
    val digitGroups = (log10(toDouble()) / log10(1024.0)).toInt()
        .coerceIn(0, units.size - 1)
    val value = this / 1024.0.pow(digitGroups.toDouble())
    return if (digitGroups == 0) "$this B"
    else "${value.toFixed(1).trimTrailingZero()} ${units[digitGroups]}"
}

fun Int.toFileSizeString(): String = toLong().toFileSizeString()

// ── Ordinals ──────────────────────────────────────────────────────────────────

/**
 * Returns the English ordinal string for this integer.
 * 1 → "1st", 2 → "2nd", 3 → "3rd", 11 → "11th", 21 → "21st"
 */
fun Int.toOrdinal(): String {
    val suffix = when {
        this % 100 in 11..13 -> "th"
        this % 10 == 1 -> "st"
        this % 10 == 2 -> "nd"
        this % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$this$suffix"
}

// ── Percentage ────────────────────────────────────────────────────────────────

/** 0.425 → "42.5%",  1.0 → "100%" */
fun Double.toPercentString(decimals: Int = 1): String =
    "${(this * 100).toFixed(decimals).trimTrailingZero()}%"

fun Float.toPercentString(decimals: Int = 1): String =
    toDouble().toPercentString(decimals)

/** 42 (out of 100) → "42%" */
fun Int.toPercentString(total: Int, decimals: Int = 1): String =
    if (total == 0) "0%" else (toDouble() / total).toPercentString(decimals)

// ── Duration ──────────────────────────────────────────────────────────────────

/**
 * Formats a duration in seconds as a human-readable string.
 * 65    → "1m 5s"
 * 3661  → "1h 1m"
 * 90000 → "25h 0m"
 */
fun Long.toDurationString(): String {
    val seconds = this % 60
    val minutes = (this / 60) % 60
    val hours = this / 3600
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

fun Int.toDurationString(): String = toLong().toDurationString()
