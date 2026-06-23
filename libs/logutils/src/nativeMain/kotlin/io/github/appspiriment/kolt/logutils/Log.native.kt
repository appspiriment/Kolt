package io.github.appspiriment.kolt.logutils

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val fullTag = if (tag.isBlank() || tag == "Kolt") "Kolt" else "Kolt-$tag"
    val prefix = when (level) {
        LogLevel.VERBOSE -> "V"
        LogLevel.DEBUG   -> "D"
        LogLevel.INFO    -> "I"
        LogLevel.WARN    -> "W"
        LogLevel.ERROR   -> "E"
    }
    println("$prefix/$fullTag: $message")
    throwable?.printStackTrace()
}
