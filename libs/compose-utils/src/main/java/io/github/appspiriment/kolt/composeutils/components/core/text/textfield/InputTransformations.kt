package io.github.appspiriment.kolt.composeutils.components.core.text.textfield

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer

/**
 * Pre-built [InputTransformation] implementations for common input constraints.
 *
 * These run **at the IME level** — rejected characters never reach [TextFieldState],
 * so no cleanup is needed in the ViewModel.
 *
 * ### Composing transformations
 * Use the [InputTransformation.then] operator to chain multiple rules:
 * ```kotlin
 * // Only digits, at most 10 characters
 * inputTransformation = InputTransformations.digitsOnly
 *     .then(InputTransformation.maxLength(10))
 *
 * // Uppercase letters, no whitespace
 * inputTransformation = InputTransformations.lettersOnly
 *     .then(InputTransformations.upperCase)
 * ```
 *
 * ### Keyboard type hint
 * Transformations do **not** change the keyboard layout. Pair numeric transformations with
 * `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)` for the best UX.
 */
object InputTransformations {

    /**
     * Accepts only decimal digit characters `[0–9]`.
     *
     * Pair with `KeyboardType.Number` or `KeyboardType.NumberPassword`.
     */
    val digitsOnly: InputTransformation = InputTransformation {
        removeChars { !it.isDigit() }
    }

    /**
     * Accepts decimal digits `[0–9]` and at most one decimal separator `.`.
     *
     * Useful for price/quantity fields. Pair with `KeyboardType.Decimal`.
     */
    val digitsAndDecimal: InputTransformation = InputTransformation {
        // First pass: strip any character that is neither a digit nor a period
        removeChars { !it.isDigit() && it != '.' }
        // Second pass: keep only the first period
        var seenDecimal = false
        for (i in length - 1 downTo 0) {
            if (charAt(i) == '.') {
                if (seenDecimal) replace(i, i + 1, "") else seenDecimal = true
            }
        }
    }

    /**
     * Accepts only Unicode letter characters. Strips digits, symbols, and punctuation.
     *
     * Pair with `KeyboardType.Text` and disable auto-correct when used for codes/IDs.
     */
    val lettersOnly: InputTransformation = InputTransformation {
        removeChars { !it.isLetter() }
    }

    /**
     * Accepts letters and digits; strips whitespace, symbols, and punctuation.
     */
    val alphanumericOnly: InputTransformation = InputTransformation {
        removeChars { !it.isLetterOrDigit() }
    }

    /**
     * Accepts letters, digits, and whitespace.
     * Useful for full-name fields where hyphens and special characters should be blocked.
     */
    val alphanumericWithSpaces: InputTransformation = InputTransformation {
        removeChars { !it.isLetterOrDigit() && !it.isWhitespace() }
    }

    /**
     * Removes all whitespace characters from the input.
     * Useful for username, email, or token fields where spaces are never valid.
     */
    val noWhitespace: InputTransformation = InputTransformation {
        removeChars { it.isWhitespace() }
    }

    /**
     * Converts every typed character to its uppercase equivalent.
     *
     * Note: the entire buffer is replaced on each change to apply the transformation,
     * which may cause the cursor to jump to the end on some IMEs. Acceptable for short
     * codes and reference numbers; avoid for long prose fields.
     */
    val upperCase: InputTransformation = InputTransformation {
        transformChars { it.uppercaseChar() }
    }

    /**
     * Converts every typed character to its lowercase equivalent.
     *
     * Has the same cursor caveat as [upperCase].
     */
    val lowerCase: InputTransformation = InputTransformation {
        transformChars { it.lowercaseChar() }
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

/**
 * Deletes every character at positions where [predicate] returns `true`.
 * Iterates from the end to avoid index shifts after deletion.
 */
private fun TextFieldBuffer.removeChars(predicate: (Char) -> Boolean) {
    for (i in length - 1 downTo 0) {
        if (predicate(charAt(i))) replace(i, i + 1, "")
    }
}

/**
 * Replaces each character with the result of [transform] when the result differs.
 * Replaces the entire buffer at once to minimise edit operations.
 */
private inline fun TextFieldBuffer.transformChars(crossinline transform: (Char) -> Char) {
    val original = toString()
    val transformed = original.map(transform).joinToString("")
    if (original != transformed) replace(0, length, transformed)
}
