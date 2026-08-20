# libs/bom — Kolt Bill of Materials (BOM)

[![Maven Central](https://img.shields.io/badge/Maven%20Central-2026.06.1-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/kolt-bom)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

The **Kolt Bill of Materials (BOM)** manages consistent version alignment across all Kolt runtime libraries (`utils`, `logutils`, `compose-utils`, `compose-kmp`, `update-utils`, `location`, and `location-picker`). 

By importing `kolt-bom`, you declare library dependencies without specifying individual version strings, ensuring zero version mismatch across your project modules.

---

## Installation

In your target project's module-level `build.gradle.kts`:

```kotlin
dependencies {
    // Import the Kolt BOM
    implementation(platform("io.github.appspiriment.kolt:kolt-bom:2026.06.1"))

    // Add Kolt libraries without explicit versions — versions are managed by BOM
    implementation("io.github.appspiriment.kolt:utils")
    implementation("io.github.appspiriment.kolt:logutils")
    implementation("io.github.appspiriment.kolt:compose-kmp")
    implementation("io.github.appspiriment.kolt:location")
    implementation("io.github.appspiriment.kolt:location-picker")
}
```

> **Note:** Kolt Gradle convention plugins (`io.github.appspiriment.kolt.*`) automatically apply the Kolt BOM platform to your build configuration.

---

## Managed Libraries

| Artifact ID | Description | Targets |
|-------------|-------------|---------|
| `utils` | Pure-Kotlin & Android utility extensions (Flow, String, List, Time) | KMP |
| `logutils` | expect/actual logging with auto-gating on Android | KMP |
| `compose-kmp` | Core Compose Multiplatform UI components & design system tokens | KMP |
| `compose` | Android-only Compose UI components, form fields, and scaffolding | Android |
| `update-utils` | Firebase & Play Store in-app update sheets | Android |
| `location` | Location geolocator wrappers & reverse geocoding systems | KMP |
| `location-picker` | Themeable location picker UI (Search, Map, Current Location, Manual) | KMP |

---

## Version Scheme

The BOM version follows a calendar-based versioning scheme (`YYYY.MM.patch`), updated whenever any underlying member library releases a new version.
