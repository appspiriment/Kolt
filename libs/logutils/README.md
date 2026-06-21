# libs/logutils — KMP Structured Logging

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/logutils)
[![Kotlin Multiplatform](https://img.shields.io/badge/KMP-commonMain%20%2B%20androidMain-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)

A thin KMP logging wrapper using `expect`/`actual` to switch between a full debug logger (dev) and a silent no-op (prod) — with zero runtime overhead in release builds.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:logutils:0.1.0")
    // Kolt convention plugins add this automatically — opt out with `kolt { enableUtils.set(false) }`
}
```

---

## Usage

```kotlin
import io.github.appspiriment.kolt.logutils.Log

// All standard log levels
Log.d("MyTag", "Debug message")
Log.i("MyTag", "Info message")
Log.w("MyTag", "Warning message")
Log.e("MyTag", "Error message", throwable)
Log.v("MyTag", "Verbose message")

// Initialise once in Application.onCreate() (Android)
LogInitializer.init(isDebug = BuildConfig.DEBUG)
```

---

## How it Works

| Source set | Behaviour |
|-----------|-----------|
| `commonMain` | Declares `expect object Log` with `d`, `i`, `w`, `e`, `v` functions |
| `androidMain` (debug) | `actual` delegates to `android.util.Log` |
| `androidMain` (release) | `actual` is a no-op — zero overhead, no log leakage |

The dev/prod switching is handled by `LogInitializer.init(isDebug)` — no build flavors or product variants needed.
