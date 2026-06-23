package io.github.appspiriment.kolt.logutils

import kotlin.concurrent.Volatile

/**
 * Log severity levels (platform-agnostic).
 */
enum class LogLevel { VERBOSE, DEBUG, INFO, WARN, ERROR }

/**
 * Global logging switch, usable from KMP commonMain.
 *
 * On Android the state is auto-gated by [LogInitializer] (App Startup): logging is
 * enabled when the host app is debuggable (debug builds) and disabled in release.
 * Consumers can override at any time via [enabled] or [init].
 *
 * On non-Android targets the default is `false`; call [init] (e.g. `Log.init(true)`)
 * to turn logging on.
 */
object Log {
    @Volatile
    var enabled: Boolean = false

    /** Override the auto-gated state (e.g. `Log.init(BuildConfig.DEBUG)`). */
    fun init(enabled: Boolean) {
        this.enabled = enabled
    }
}

/**
 * Platform log sink. Implemented per target (android.util.Log on Android).
 */
internal expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)

/** Logs a throwable's stack trace at ERROR level (no-op when [Log.enabled] is false). */
fun Throwable.printLog(tag: String = "Kolt") {
    if (Log.enabled) platformLog(LogLevel.ERROR, tag, message ?: toString(), this)
}

/**
 * Logs [message] at WARN (or ERROR when [isError]). Throwables are logged with their
 * stack trace. No-op when [Log.enabled] is false.
 */
fun printLog(message: Any?, tag: String = "Kolt", isError: Boolean = false) {
    if (!Log.enabled) return
    platformLog(
        level = if (isError) LogLevel.ERROR else LogLevel.WARN,
        tag = tag,
        message = message.toString(),
        throwable = message as? Throwable
    )
}

/**
 * Logs [message] at an explicit [level], optionally prefixed with [className].
 * No-op when [Log.enabled] is false.
 */
fun printLog(message: Any?, tag: String = "Kolt", level: LogLevel, className: String? = null) {
    if (!Log.enabled) return
    val text = if (!className.isNullOrBlank()) "$className - $message" else "$message"
    platformLog(level, tag, text, message as? Throwable)
}
