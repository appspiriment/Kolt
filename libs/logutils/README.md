# libs/logutils — KMP Structured Logging

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.1.dev--00-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/logutils)
[![Kotlin Multiplatform](https://img.shields.io/badge/KMP-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Native-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

A thin Kotlin Multiplatform logging wrapper using `expect`/`actual` to switch between debug logging and silent production output — with zero runtime overhead in release builds. Automatically gates logs on Android via App Startup.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:logutils:0.2.1.dev-00")
    // Kolt convention plugins add this automatically — opt out with `kolt { enableUtils.set(false) }`
}
```

---

## Usage

```kotlin
import io.github.appspiriment.kolt.logutils.Log
import io.github.appspiriment.kolt.logutils.printLog

// Direct method calls
Log.d("MyTag", "Debug message")
Log.i("MyTag", "Info message")
Log.w("MyTag", "Warning message")
Log.e("MyTag", "Error message", throwable)

// Or extension functions on String / Throwable
"User signed in".printLog(tag = "Auth", level = LogLevel.INFO)
exception.printLog(tag = "Database")
```

---

## How it Works

| Source set | Behaviour |
|-----------|-----------|
| `commonMain` | Declares `expect object Log` and extension functions (`printLog`) |
| `androidMain` | `actual` delegates to `android.util.Log`. Auto-gated by `LogInitializer` (App Startup) which sets `Log.enabled = FLAG_DEBUGGABLE` at launch. |
| `desktopMain` | `actual` outputs to stdout/stderr |
| `nativeMain` | `actual` outputs via native platform loggers |

On Android, zero manual `Log.init(...)` boilerplate is required — App Startup checks `FLAG_DEBUGGABLE` on startup. You can manually override `Log.enabled` or call `Log.init(enabled)` anywhere at runtime.
