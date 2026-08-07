# libs/update-utils — Play Store In-App Updates

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/update-utils)
[![Android](https://img.shields.io/badge/Android-only-green?style=flat-square&logo=android)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

A thin wrapper around the [Play Core In-App Updates API](https://developer.android.com/guide/playcore/in-app-updates) that handles both **immediate** and **flexible** update flows with minimal boilerplate.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:update-utils:0.1.0")
}
```

---

## Usage

```kotlin
class MainActivity : ComponentActivity() {

    private val updateHelper by lazy { AppUpdateHelperUtil(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check and trigger flexible update
        updateHelper.checkForUpdate(
            onUpdateAvailable = { /* show banner or dialog */ },
            onUpdateNotAvailable = { /* nothing to do */ },
        )
    }

    override fun onResume() {
        super.onResume()
        // Complete a flexible update that downloaded in the background
        updateHelper.completeFlexibleUpdate()
    }
}
```

---

## Update Types

| Type | Behaviour | When to use |
|------|-----------|-------------|
| **Flexible** | Downloads in background, user continues using app, completes on restart | Minor/patch versions |
| **Immediate** | Full-screen blocking UI, app restarts automatically | Critical security or breaking updates |
