# libs/location-picker — Location Picker UI

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.0.dev--01-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/location-picker)
[![KMP](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

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

## Current-location permission & services (Android)

`libs/location`'s Android `CurrentLocationProvider` assumes the caller already holds the location
permission — `location-picker` handles that assumption itself so callers don't have to. Tapping
"Use current location" on Android:

1. Checks `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION`. If neither is granted, shows a
   bottomsheet rationale (not a dialog) before the system permission prompt.
2. If the system prompt comes back denied and can no longer be shown again (the user checked
   "Don't ask again", or denied it once already), shows a second bottomsheet deep-linking to the
   app's settings screen instead of retrying the system prompt.
3. Once granted, resolves the device's location settings via `libs/location`'s
   `rememberLocationSettingsResolver` (Google Play Services `SettingsClient`) — the common case
   (settings already on, or resolvable) shows the system's own in-app "Turn on location" dialog
   directly, **no bottomsheet of ours first**, and no navigation out to the Settings app. Only the
   rare unresolvable case (a device/OEM Play Services can't fix automatically) falls back to a
   third bottomsheet that deep-links to the device's location settings screen.
4. Only then fetches the location.

Every title/message/button label for all three bottomsheets is part of `LocationPickerConfig` —
override via `defaultLocationPickerConfig(locationPermissionRationaleMessage = "…", …)` (see
`LocationPickerConfig`'s `locationPermissionRationale*`/`locationPermissionSettings*`/
`locationServicesDisabled*` fields for the full list) the same way any other label in this module
is customized. `locationServicesDisabled*` specifically now backs only step 3's rare fallback
sheet, not the common path.

iOS and Desktop don't need this: iOS's `CurrentLocationProvider` requests its own permission via
`CLLocationManager`, and Desktop's IP-based lookup has no permission/services concept.

---

## Platform notes

| Platform | Current-location | Map |
|---|---|---|
| Android | Native GPS via `libs/location` | Google Maps |
| iOS | Native GPS via `libs/location` | MapKit (`UIKitView` interop) |
| Desktop | IP-based geolocation via `libs/location` | Custom tile-based canvas renderer |
| Web | Browser Geolocation API via `libs/location` | Not yet ported — disable `showMap` in `config`, use Search/Manual entry |
