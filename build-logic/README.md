# build-logic — Convention Plugins

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![AGP](https://img.shields.io/badge/AGP-8.13.1-green?style=flat-square&logo=android)](https://developer.android.com/build)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

Gradle convention plugins for Android and Kotlin Multiplatform projects. Applied via the composite build — no `publishToMavenLocal` needed during development.

---

## Available Plugins

### Android Plugins

| Plugin ID | Description |
|-----------|-------------|
| `io.github.appspiriment.kolt.application` | Base Android application: compileSdk, minSdk, Kotlin, proguard |
| `io.github.appspiriment.kolt.library` | Base Android library: same defaults as application |
| `io.github.appspiriment.kolt.library-compose` | Library + Jetpack Compose + Compose compiler |
| `io.github.appspiriment.kolt.library-hilt` | Library + Hilt dependency injection |
| `io.github.appspiriment.kolt.library-hilt-compose` | Library + Hilt + Compose (most common) |
| `io.github.appspiriment.kolt.data` | Android data-layer module: Room, Retrofit, serialization |

### KMP Plugins

| Plugin ID | Description |
|-----------|-------------|
| `io.github.appspiriment.kolt.kmp.library` | KMP library: Android + JVM targets, commonMain/androidMain |
| `io.github.appspiriment.kolt.kmp.application` | KMP application module |
| `io.github.appspiriment.kolt.kmp.data` | KMP data-layer: Ktor, serialization, coroutines |

---

## Usage

In a consumer project's module `build.gradle.kts`:

```kotlin
plugins {
    // From the koltlibs catalog
    alias(koltlibs.plugins.kolt.library.hilt.compose)
}
```

The plugin sets:
- `compileSdk`, `targetSdk`, `minSdk` from the catalog
- Kotlin JVM target to 21
- Compose compiler (via Kotlin 2.x plugin)
- KSP for annotation processing where applicable
- Sensible `buildFeatures` defaults

---

## Tasks

| Task | Description |
|------|-------------|
| `bumpDevVersion` | Increments `DEV` counter in `version.properties` |
| `generateThemeTemplates` | Scaffolds `appspiriment_colors.xml` / `appspiriment_dimens.xml` into a consumer's `res/` |
| `generateConsumerCatalogs` | Regenerates `koltlibs.versions.toml` and `kmplibs.versions.toml` from shared version data |

---

## Project Templates & Steering Standards

`project-templates/templates/` contains ready-to-use project templates copied by `scripts/new-project.sh`:

- `android-project/` — Compose + Hilt Android app scaffold
- `kmp-project/` — KMP app with shared + data modules

`Standards/` contains canonical AI-agent steering documents scaffolded into new projects:
- `CLAUDE.md` / `AGENTS.md` / `GEMINI.md` — AI agent working instructions
- `Standards/steering/android/` — Android-only steering set
- `Standards/steering/kmp/` — KMP steering set
- `Standards/KOLT.md` — Library reference for AI agents

---

## Structure

```
build-logic/
├── conventions/
│   ├── build.gradle.kts                        # Plugin declarations + publishing
│   └── src/main/kotlin/io/github/appspiriment/kolt/conventions/
│       ├── extensions/
│       │   ├── Dependencies.kt                 # Shared dependency helpers
│       │   ├── Extensions.kt                   # Android DSL extensions
│       │   ├── KmpDependencies.kt              # KMP dependency helpers
│       │   ├── KmpExtensions.kt                # KMP DSL extensions
│       │   ├── LibsData.kt                     # Version catalog data class
│       │   ├── KotlinAndroid.kt                # Kotlin/Android config helper
│       │   ├── KotlinKmp.kt                    # Kotlin/KMP config helper
│       │   └── ProjectConfiguration.kt         # AGP project DSL
│       └── plugins/
│           ├── AndroidApplicationConventionPlugin.kt
│           ├── AndroidBaseConventionPlugin.kt
│           ├── AndroidLibraryConventionPlugin.kt
│           ├── ScaffoldComposeThemeTask.kt
│           ├── ScaffoldSteeringDocsTask.kt
│           ├── ScaffoldVersionCatalogTask.kt
│           ├── feature/
│           │   ├── AndroidDataLayerConventionPlugin.kt
│           │   └── KmpDataLayerConventionPlugin.kt
│           └── kmp/
│               ├── KmpApplicationConventionPlugin.kt
│               ├── KmpBaseConventionPlugin.kt
│               └── KmpLibraryConventionPlugin.kt
└── gradle/
    └── libs.versions.toml                      # build-logic's own dependencies
```
