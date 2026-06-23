package io.github.appspiriment.kolt.logutils

import java.util.logging.Level
import java.util.logging.Logger

private val logger = Logger.getLogger("Kolt")

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val fullTag = if (tag.isBlank() || tag == "Kolt") "Kolt" else "Kolt-$tag"
    val jLevel = when (level) {
        LogLevel.VERBOSE, LogLevel.DEBUG -> Level.FINE
        LogLevel.INFO                    -> Level.INFO
        LogLevel.WARN                    -> Level.WARNING
        LogLevel.ERROR                   -> Level.SEVERE
    }
    if (throwable != null) logger.log(jLevel, "[$fullTag] $message", throwable)
    else logger.log(jLevel, "[$fullTag] $message")
}
