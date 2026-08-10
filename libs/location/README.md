# libs/location — Location & Geocoding

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.1.dev--02-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/location)
[![KMP](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

Cross-platform current-location fetching and place lookup, no UI attached — pair it with [`libs/location-picker`](../location-picker) for a ready-made picker screen, or use it standalone.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:location:0.2.1.dev-02")
}
```

---

## Current location

```kotlin
val provider = CurrentLocationProvider(PlatformLocationContext(/* Android: context */))

when (val result = provider.getCurrentLocation()) {
    is LocationResult.Success -> {
        val (lat, lng, label, timezoneId) = result.location
    }
    LocationResult.PermissionDenied -> { /* prompt for permission, or explain */ }
    is LocationResult.Unavailable -> { /* result.message */ }
}
```

Platform behavior is intentionally not unified into one flow:

| Platform | Source | Permission handling |
|---|---|---|
| Android | Native GPS (`FusedLocationProvider`) | Caller must already hold `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` — returns `PermissionDenied` otherwise |
| iOS | `CLLocationManager` | Self-contained — requests authorization itself if not yet determined |
| Desktop | IP-based geolocation | No OS permission concept — always attempts the lookup; show your own consent UI first if desired |
| Web | Browser Geolocation API | Browser's native permission prompt |

`PlatformLocationContext` is an `expect class`: on Android it wraps `android.content.Context` (`PlatformLocationContext(context)`), on iOS/Desktop/Web it takes no arguments.

`getCurrentLocation()` also fails with `Unavailable("Location services are turned off")` on Android if the device's GPS/network positioning is off (independent of permission). Check `isLocationServicesEnabled(context)` upfront if you want to prompt the user to turn services on before calling `getCurrentLocation()` — `libs/location-picker`'s Android flow does exactly this (see its README).

```kotlin
val servicesOn: Boolean = isLocationServicesEnabled(PlatformLocationContext(/* Android: context */))
```

`isLocationServicesEnabled` is `true` unconditionally on iOS (`CLLocationManager.locationServicesEnabled()`), Desktop, and Web — only Android has a separate services toggle to check.

For the Android in-app "and let the user turn it on" step, use `rememberLocationSettingsResolver`
instead of a raw `isLocationServicesEnabled` check + a `Settings` deep-link — it wraps Google Play
Services' `SettingsClient`, which shows the system's own "Turn on location" dialog in the current
`Activity`, no navigation out to the Settings app:

```kotlin
val resolveLocationSettings = rememberLocationSettingsResolver(
    context = PlatformLocationContext(/* Android: context */),
    onResolved = { /* settings were already on, or the user just turned them on */ },
    onUnresolvable = { message -> /* rare — a device Play Services can't resolve automatically */ },
    onDeclined = { /* user dismissed the system dialog without enabling */ },
)
// call resolveLocationSettings() to trigger the check-then-resolve flow, e.g. from a button onClick
```

`isLocationServicesEnabled` and `rememberLocationSettingsResolver` are complementary, not
alternatives: the former is a plain synchronous check usable anywhere including
`CurrentLocationProvider`'s own suspend fast-fail path (no `Activity`/launcher needed); the latter
needs both and is Compose-only, for the UI layer that can act on the answer. Requires
`com.google.android.gms:play-services-location` on the device (bundled by Play Services, which
`location-picker`'s Android target already pulls in).

---

## Place search & reverse geocoding

```kotlin
val matches: List<PlaceSearchResult> = searchPlaces(query = "Kochi", limit = 8)
val place: PlaceSearchResult? = reverseGeocode(latitude = 9.9312, longitude = 76.2673)
val timezoneId: String? = lookupTimezone(latitude = 9.9312, longitude = 76.2673)
```

`searchPlaces`, `reverseGeocode`, and `lookupTimezone` are plain suspend functions backed by Ktor — no platform actuals needed, work the same on every target.
