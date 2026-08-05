# libs/location-picker — Location Picker UI

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0.dev--01-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/location-picker)
[![KMP](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)

All-in-one, configurable, themeable location-picker UI — Search / Map / Current-location / Manual-entry tabs — for Android, iOS, Desktop and Web. Built on top of [`libs/location`](../location).

Try it live in the [Web demo](https://kolt-kmp.web.app/) (see [`demo-web`](../../demo-web)), or run the Android/Desktop demo in [`demo-app`](../../demo-app).

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:location-picker:0.1.0.dev-01")
}
```

---

## Usage

```kotlin
@Composable
fun PickLocationScreen(onDone: () -> Unit) {
    LocationPickerScreen(
        config = defaultLocationPickerConfig(),
        onCancel = onDone,
        onResult = { result ->
            // result.latitude / result.longitude / result.label / result.timezoneId
            onDone()
        },
    )
}
```

`LocationPickerScreen` owns its own `ViewModel` and effect handling — embed it directly in a screen, a full-screen `Activity` (Android), a `DialogWindow` (Desktop), or a modal `UIViewController` (iOS). `config` controls which tabs (search / map / current-location / manual entry) are enabled; `colors` lets you override the default theme.

---

## Platform notes

| Platform | Current-location | Map |
|---|---|---|
| Android | Native GPS via `libs/location` | Google Maps |
| iOS | Native GPS via `libs/location` | MapKit (`UIKitView` interop) |
| Desktop | IP-based geolocation via `libs/location` | Custom tile-based canvas renderer |
| Web | Browser Geolocation API via `libs/location` | Not yet ported — disable `showMap` in `config`, use Search/Manual entry |
