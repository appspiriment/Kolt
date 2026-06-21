package io.github.appspiriment.kolt.utils.extensions

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Changes the date format of a string.
 *
 * <p>This function attempts to parse the input string as a date using the specified input format
 * and then formats the parsed date into the specified output format. If the input string
 * cannot be parsed as a date, it is returned as is.
 *
 * <p>The input and output formats should be valid date format patterns as defined by
 * [java.text.SimpleDateFormat].
 *
 * @param inputFormat The format of the input date string. Defaults to "yyyy-MM-dd".
 * @param outputFormat The desired format of the output date string. Defaults to "dd MMM yyyy".
 * @return The date string in the specified output format, or null if the input string
 * cannot be parsed as a date.
 */
fun String.changeDateFormat(
    inputFormat: String = "yyyy-MM-dd",
    outputFormat: String = "dd MMM yyyy"
): String? {
    return try {
        SimpleDateFormat(inputFormat, Locale.ENGLISH)
            .parse(this)?.changeDateFormat(outputFormat) ?: this
    } catch (e: Exception) {
        null
    }
}

/**
 * Changes the date format of a [Date] object.
 *
 * <p>This function attempts to format the given [Date] object into the specified output format.
 * If an exception occurs during formatting, the original date object's string representation
 * is returned.
 *
 * <p>The output format should be a valid date format pattern as defined by
 * [java.text.SimpleDateFormat].
 *
 * @param outputFormat The desired format of the output date string. Defaults to "dd MMM yyyy".
 * @return The date string in the specified output format, or the original date object's
 * string representation if formatting fails.
 */
fun Date.changeDateFormat(
    outputFormat: String = "dd MMM yyyy"
): String {
    return try {
        SimpleDateFormat(outputFormat, Locale.ENGLISH).format(this)
    } catch (e: Exception) {
        "$this"
    }
}


/**
 * Converts a string to a [Date] object using the specified input format.
 *
 * <p>This function attempts to parse the input string as a date using the specified input format.
 * If the input string is a valid date and is not empty, it is parsed and returned as a [Date]
 * object. Otherwise, null is returned.
 *
 * <p>The input format should be a valid date format pattern as defined by
 * [java.text.SimpleDateFormat].
 *
 * @param inputFormat The format of the input date string. Defaults to "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".
 * @return The parsed [Date] object, or null if the input string cannot be parsed as a date.
 */
fun String.toDate(inputFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"): Date? {
    try {
        if (isValidDateInput() && isNotEmpty()) {
            SimpleDateFormat(inputFormat, Locale.ENGLISH).let {
                return it.parse(this)
            }
        }
    } catch (e: Exception) {
        return null
    }
    return null
}

/**
 * Converts a date string to milliseconds since the epoch.
 *
 * This function attempts to parse the input string as a date using the specified input format
 * and then converts it to milliseconds since the epoch. If the input string is not empty,
 * it is parsed and converted to milliseconds. Otherwise, null is returned.
 *
 * The input format should be a valid date format pattern as defined by
 * [java.time.format.DateTimeFormatter].
 *
 * @param inputFormat The format of the input date string. Defaults to "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'".
 * @return The milliseconds since the epoch, or null if the input string cannot be parsed as a date.
 */
fun String.toMillis(inputFormat: String = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"): Long? {
    return try {
        if (isNotEmpty()) {
            return LocalDateTime.parse(this, DateTimeFormatter.ofPattern(inputFormat, Locale.ENGLISH))?.let{
                it.toEpochSecond(ZoneOffset.UTC) * 1000
            }
        } else null
    } catch (e: Exception) {
        null
    }
}


/**
 * Converts a date string to a [LocalDate] object with the current year.
 *
 * <p>This function takes a date string that may not include the year and converts it to a
 * [LocalDate] object with the current year. It does this by prepending the current year
 * to the input string and then parsing it using the specified date format.
 *
 * <p>The input date string should be in a format that can be parsed by
 * [java.time.format.DateTimeFormatter] after the current year is prepended.
 *
 * @param dateFormat The format of the input date string. Defaults to "yyyy MMM dd hh:mm a".
 * @return The [LocalDate] object representing the date with the current year.
 */
fun String.toCurrentYearLocalDate(dateFormat: String = "yyyy MMM dd hh:mm a"): LocalDate{
    val year = Calendar.getInstance().get(Calendar.YEAR)
    return "$year $this".toLocalDate(dateFormat)
}

/**
 * Converts a date string to a [LocalDate] object.
 *
 * <p>This function parses the input date string using the specified date format and returns
 * a [LocalDate] object representing the date.
 *
 * <p>The input date string should be in a format that can be parsed by
 * [java.time.format.DateTimeFormatter].
 *
 * @param dateFormat The format of the input date string. Defaults to "yyyy MMM dd hh:mm a".
 * @return The [LocalDate] object representing the date.
 */
fun String.toLocalDate(dateFormat: String = "yyyy MMM dd hh:mm a"): LocalDate{
    return LocalDate.parse(
        this,
        DateTimeFormatter.ofPattern(dateFormat, Locale.US)
    )
}


/**
 * Checks if a string is a valid date input in the specified format.
 *
 * <p>This function validates the input string against the specified date format. It checks if
 * the string can be split into three parts (day, month, year) using the "/" delimiter and
 * if the day, month, and year values are within valid ranges.
 *
 * <p>The input date string should be in the format "dd/MM/yyyy" by default.
 *
 * The expected date format is "dd/MM/yyyy".
 * @return True if the input string is a valid date, false otherwise.
 */
fun String.isValidDateInput(): Boolean {
    val dateArr = this.split("/")
    if (dateArr.size == 3) {
        val day = dateArr[0].toInt()
        val month = dateArr[1].toInt()
        val year = dateArr[2].toInt()
        if (day in 1..31 && month in 1..12 && year > 0) {
            return true
        }
    }
    return false
}



/**
 * Checks if all the provided options are not null and not blank (for strings).
 *
 * <p>This function takes a variable number of arguments of any type and checks if all of them
 * are not null. If any of the arguments are strings, it also checks if they are not blank.
 * If all the arguments satisfy these conditions, it returns a list of the non-null arguments.
 * Otherwise, it returns null.
 *
 * @param <T> The type of the options.
 * @param options The options to check.
 * @return A list of the non-null options if all of them are not null and not blank (for strings),
 * or null otherwise.
 */
fun <T : Any> checkAllNotNullOrBlank(vararg options: T?): List<T>? {
    return if (options.all { it != null && (if (it is String) it.isNotBlank() else true) }) {
        options.toList().filterNotNull()
    } else null
}
