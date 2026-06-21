package io.github.appspiriment.kolt.utils.extensions


fun String.sanitizePhoneNumber(): String {
    val cleaned = filter { it.isDigit() || it == '+' }

    val hasPlus = cleaned.startsWith('+')
    val digitsOnly = cleaned.removePrefix("+")

    // --- Guard Clauses ---
    if (cleaned.isEmpty() || (hasPlus && digitsOnly.isEmpty())) return this
    val minLength = 6
    if (digitsOnly.length < minLength) return this

    // --- Formatting Logic ---
    val result = buildString {
        if (hasPlus) append('+') // '+' is appended here if needed

        // ... (all the when conditions from the previous correct answer)
        when {
            // --- WITH PLUS ---
            hasPlus && digitsOnly.length == 10 && digitsOnly.startsWith('1') -> { // +1 XXX XXX XXXX
                append(digitsOnly, 0, 1).append(' ')
                append(digitsOnly, 1, 4).append(' ')
                append(digitsOnly, 4, 7).append(' ')
                append(digitsOnly, 7, 10)
            }
            hasPlus && digitsOnly.length == 10 -> { // Other +CC (2 or 3) + National (8 or 7)
                val ccLength = if (digitsOnly.length - 8 >= 2) 2 else if (digitsOnly.length - 7 >= 3) 3 else 0
                if (ccLength > 0) {
                    append(digitsOnly, 0, ccLength).append(' ')
                    append(digitsOnly, ccLength, digitsOnly.length)
                } else {
                    append(digitsOnly) // Fallback
                }
            }
            hasPlus && digitsOnly.length == 11 && digitsOnly.startsWith('1') -> {
                append(digitsOnly, 0, 1).append(' ')
                append(digitsOnly, 1, 4).append(' ')
                append(digitsOnly, 4, 7).append(' ')
                append(digitsOnly, 7, 11)
            }
            hasPlus && digitsOnly.length == 11 -> {
                val ccLength = if (digitsOnly.length - 9 >= 2) 2 else if (digitsOnly.length - 8 >= 3) 3 else 0
                if (ccLength > 0) {
                    append(digitsOnly, 0, ccLength).append(' ')
                    val nationalPart = digitsOnly.substring(ccLength)
                    if (nationalPart.length == 9) append(nationalPart, 0, 4).append(' ').append(nationalPart, 4, 9)
                    else if (nationalPart.length == 8) append(nationalPart, 0, 4).append(' ').append(nationalPart, 4, 8)
                    else append(nationalPart)
                } else {
                    append(digitsOnly)
                }
            }
            hasPlus && digitsOnly.length in 12..15 -> {
                val countryCodeLength = digitsOnly.length - 10
                append(digitsOnly, 0, countryCodeLength).append(' ')
                append(digitsOnly, countryCodeLength, countryCodeLength + 5).append(' ')
                append(digitsOnly, countryCodeLength + 5, digitsOnly.length)
            }

            // --- WITHOUT PLUS ---
            !hasPlus && digitsOnly.length == 10 -> {
                append(digitsOnly, 0, 4).append(' ')
                append(digitsOnly, 4, 10)
            }
            !hasPlus && digitsOnly.length > 6 -> {
                val secondPartLength = minOf(digitsOnly.length - 1, 6).coerceAtLeast(0)
                val firstPartLength = digitsOnly.length - secondPartLength
                if (firstPartLength > 0) {
                    append(digitsOnly, 0, firstPartLength).append(' ')
                    append(digitsOnly, firstPartLength, digitsOnly.length)
                } else {
                    append(digitsOnly)
                }
            }
            else -> {
                append(digitsOnly)
            }
        }
    }

    // Corrected final check:
    // If 'hasPlus' is true, the minimum meaningful length for 'result' is 1 (for the '+') + length of formatted digits.
    // If 'hasPlus' is false, the minimum meaningful length is just the length of formatted digits.
    // We return 'this' (original) if the result is effectively empty or just the plus sign.
    return this.takeIf { (hasPlus && result == "+") || (!hasPlus && result.isEmpty()) } ?:  result

}