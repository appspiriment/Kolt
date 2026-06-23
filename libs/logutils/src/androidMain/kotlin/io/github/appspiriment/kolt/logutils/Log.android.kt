package io.github.appspiriment.kolt.logutils

import android.util.Log as AndroidLog

internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val fullTag = if (tag.isBlank() || tag == "Kolt") "Kolt" else "Kolt-$tag"
    when (level) {
        LogLevel.VERBOSE -> AndroidLog.v(fullTag, message, throwable)
        LogLevel.DEBUG -> AndroidLog.d(fullTag, message, throwable)
        LogLevel.INFO -> AndroidLog.i(fullTag, message, throwable)
        LogLevel.WARN -> AndroidLog.w(fullTag, message, throwable)
        LogLevel.ERROR -> AndroidLog.e(fullTag, message, throwable)
    }
}
