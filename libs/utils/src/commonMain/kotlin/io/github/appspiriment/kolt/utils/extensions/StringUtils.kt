package io.github.appspiriment.kolt.utils.extensions

// ── Initials ──────────────────────────────────────────────────────────────────

/**
 * Extracts initials from a name string.
 * "John Doe"     → "JD"
 * "Alice"        → "A"
 * "van der Berg" → "VB"  (skips lowercase-only tokens like "van", "der")
 * Falls back to first [maxChars] characters if no capitalised tokens are found.
 */
fun String.toInitials(maxChars: Int = 2): String {
    val tokens = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    val initials = tokens
        .filter { it.first().isUpperCase() }
        .take(maxChars)
        .joinToString("") { it.first().toString() }
    return initials.ifEmpty {
        tokens.take(maxChars).joinToString("") { it.first().uppercaseChar().toString() }
    }
}

// ── Truncation ────────────────────────────────────────────────────────────────

/**
 * Truncates the string to [maxLength] characters, appending [ellipsis] if truncated.
 * The total length of the returned string will never exceed [maxLength].
 */
fun String.truncate(maxLength: Int, ellipsis: String = "…"): String {
    if (length <= maxLength) return this
    val cutAt = (maxLength - ellipsis.length).coerceAtLeast(0)
    return take(cutAt) + ellipsis
}

/**
 * Truncates the middle of a long string, keeping the start and end visible.
 * "very_long_file_name.kt" → "very_lon…me.kt"
 * Useful for file paths, URLs, or IDs.
 */
fun String.ellipsizeMiddle(maxLength: Int, ellipsis: String = "…"): String {
    if (length <= maxLength) return this
    val keep = (maxLength - ellipsis.length).coerceAtLeast(2)
    val front = keep / 2
    val back = keep - front
    return take(front) + ellipsis + takeLast(back)
}

// ── Whitespace ────────────────────────────────────────────────────────────────

/** Collapses multiple consecutive whitespace characters into a single space and trims. */
fun String.normalizeSpaces(): String = trim().replace(Regex("\\s+"), " ")

// ── Null / blank convenience ──────────────────────────────────────────────────

/** Returns null if this string is blank, otherwise the string itself. */
fun String.nullIfBlank(): String? = ifBlank { null }

/** Returns null if this string is empty, otherwise the string itself. */
fun String.nullIfEmpty(): String? = ifEmpty { null }

/** Returns this string if not null and not blank, otherwise [default]. */
fun String?.orDefault(default: String): String =
    if (isNullOrBlank()) default else this

/** Returns this string if not null and not blank, otherwise the result of [provider]. */
fun String?.orDefault(provider: () -> String): String =
    if (isNullOrBlank()) provider() else this

// ── Numeric check ─────────────────────────────────────────────────────────────

/** True if the string represents a valid integer (e.g. "42", "-7"). */
fun String.isInteger(): Boolean = toIntOrNull() != null

/** True if the string represents any valid number (int or decimal). */
fun String.isNumeric(): Boolean = toDoubleOrNull() != null
