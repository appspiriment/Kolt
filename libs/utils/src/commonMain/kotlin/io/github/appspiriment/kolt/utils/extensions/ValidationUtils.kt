package io.github.appspiriment.kolt.utils.extensions

// ── Email ─────────────────────────────────────────────────────────────────────

private val EMAIL_REGEX = Regex(
    "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$"
)

fun String.isValidEmail(): Boolean = EMAIL_REGEX.matches(trim())

// ── Phone ─────────────────────────────────────────────────────────────────────

/** Validates that a string looks like a phone number (digits, spaces, dashes, +, parens). */
private val PHONE_REGEX = Regex(
    "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{3,5}[-\\s.]?[0-9]{3,5}$"
)

fun String.isValidPhone(): Boolean = PHONE_REGEX.matches(filter { it.isDigit() || it == '+' })

// ── URL ───────────────────────────────────────────────────────────────────────

private val URL_REGEX = Regex(
    "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$",
    RegexOption.IGNORE_CASE
)

fun String.isValidUrl(): Boolean = URL_REGEX.matches(trim())

// ── Pin / postal code ─────────────────────────────────────────────────────────

/** True if this string is a 6-digit Indian PIN code. */
fun String.isValidIndianPinCode(): Boolean = matches(Regex("^[1-9][0-9]{5}$"))

/** True if this string is a 5-digit US ZIP code (optionally with +4 extension). */
fun String.isValidUsZipCode(): Boolean = matches(Regex("^[0-9]{5}(-[0-9]{4})?$"))

// ── Length ────────────────────────────────────────────────────────────────────

fun String.hasMinLength(min: Int): Boolean = length >= min
fun String.hasMaxLength(max: Int): Boolean = length <= max
fun String.hasLengthInRange(min: Int, max: Int): Boolean = length in min..max

// ── Character set ─────────────────────────────────────────────────────────────

fun String.hasOnlyLetters(): Boolean = all { it.isLetter() }
fun String.hasOnlyDigits(): Boolean = all { it.isDigit() }
fun String.hasOnlyAlphanumeric(): Boolean = all { it.isLetterOrDigit() }
fun String.hasNoSpecialChars(): Boolean = all { it.isLetterOrDigit() || it.isWhitespace() }

// ── Password strength ─────────────────────────────────────────────────────────

fun String.hasUppercase(): Boolean = any { it.isUpperCase() }
fun String.hasLowercase(): Boolean = any { it.isLowerCase() }
fun String.hasDigit(): Boolean = any { it.isDigit() }
fun String.hasSpecialChar(): Boolean = any { !it.isLetterOrDigit() }

/**
 * Returns a password strength score from 0 (weakest) to 4 (strongest).
 * Checks: length ≥ 8, has uppercase, has lowercase + digit, has special char.
 */
fun String.passwordStrength(): Int {
    var score = 0
    if (length >= 8) score++
    if (hasUppercase()) score++
    if (hasLowercase() && hasDigit()) score++
    if (hasSpecialChar()) score++
    return score
}

// ── Credit card ───────────────────────────────────────────────────────────────

/**
 * Validates a credit card number using the Luhn algorithm.
 * Accepts spaces and dashes as separators.
 */
fun String.isValidCreditCard(): Boolean {
    val digits = filter { it.isDigit() }
    if (digits.length < 13) return false
    var sum = 0
    var alternate = false
    for (i in digits.length - 1 downTo 0) {
        var n = digits[i].digitToInt()
        if (alternate) {
            n *= 2
            if (n > 9) n -= 9
        }
        sum += n
        alternate = !alternate
    }
    return sum % 10 == 0
}
