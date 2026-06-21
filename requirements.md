# UtilsLibs — Requirements & Implementation Plan

## 1. Problem Statement

The Appspiriment toolchain was split across **three separate git repos / Gradle builds**,
causing duplication, a painful local workflow, and diverging versions:

| Old repo | Published artifacts |
|---|---|
| `AndroidConventionPlugins` | 6 plugins `io.github.appspiriment.*` + `appspirimentlibs` catalog |
| `AppspirimentKMPPlugins` | 6 plugins `io.github.appspiriment.kmp.*` + `kmplibs` catalog |
| `AppUtilLibs` | `utils`, `compose-utils`, `logutils`, `update-utils` (all Android-only) |

Confirmed pain points:
- The two plugin projects were near-duplicates (same package, identical boilerplate, overlapping sources).
- Both catalogs duplicated `minSdk/targetSdk/compileSdk/kotlin/coroutines/ksp/...`.
- `logutils`/`update-utils` applied the Android convention plugin, forcing a `publishDev → publishToMavenLocal` round-trip on every plugin tweak.
- The KMP catalog already referenced the same `appspirimentUtils`/`appspirimentLogUtils` versions — the libs wanted cross-use but the repo boundary blocked it.

---

## 2. Goals

1. **One repo, one Gradle build** — easy maintenance, single place to make changes.
2. **Composite build** — convention plugins applied from source, no publish round-trip during development.
3. **Maximum KMP/Android reuse** — `utils` and `logutils` become KMP so shared code in KMP projects can call them.
4. **One unified version** — single `version.properties` drives all plugins and all library artifacts.
5. **Fewer plugins/libraries** — one module declares all 12 convention plugins; `logutils` collapses from two artifacts (`-dev`/`-prod`) to one KMP artifact with auto-gating.
6. **Clean consumer catalogs** — `appspirimentlibs` and `kmplibs` stay separate (Android consumers shouldn't see KMP-only entries and vice versa) but share a common version baseline.

---

## 3. Decisions Made

| Decision | Choice | Rationale |
|---|---|---|
| Build topology | **Composite build** (`includeBuild("build-logic")`) | Eliminates publish round-trip; conventions compile with the libs in one `./gradlew build` |
| Plugin merge | **One `build-logic/conventions` module, all 12 plugins** | Kills duplication; one build file, one Constants generator, one version |
| Toolchain baseline | **KMP baseline (Kotlin 2.1.21 / AGP 8.7.3 / CMP 1.8.0)** | More stable for KMP consumers; Android plugin source needed no code changes |
| `utils` | **KMP** (`commonMain` + `androidMain`) | Pure-Kotlin extensions (Flow, String, Phone) in `commonMain`; android.* / java.time in `androidMain` |
| `logutils` | **KMP** (`commonMain` + `androidMain`) with **auto-gating** | Single artifact callable from shared KMP code. `LogInitializer` (App Startup) reads `ApplicationInfo.FLAG_DEBUGGABLE` and sets `Log.enabled` automatically — debug=on, release=off. Consumer can override via `Log.init(enabled)` or `Log.enabled = ...` |
| `compose-utils` | **Android-only** for now; Compose Multiplatform is a future phase | 75+ files, heavy `res/`, M2 + M3 usage — CMP port is significant |
| `update-utils` | **Android-only** permanently | Wraps Firebase Remote Config + in-app update API; inherently Android |
| Version catalogs | **Two thin catalogs** generated from common baseline | `appspirimentlibs` (Android), `kmplibs` (KMP) — each clean for its consumers |
| Unified versioning | **Single `version.properties`** (`MAJOR` + `DEV` counter) | All plugins + libs ship together under one version string |
| logutils gating | **Option B: auto-gate via plugin + user-override** | Zero consumer boilerplate; `LogInitializer` runs at app startup; `Log.init(bool)` overrides |

---

## 4. Repository Layout

```
UtilsLibs/
├── version.properties              # SINGLE truth: MAJOR + DEV counter
├── build.gradle.kts                # Root: reads version, sets allprojects group/version
├── settings.gradle.kts             # includeBuild("build-logic"); registers two consumer catalogs
├── gradle.properties               # AndroidX flags, POM metadata, kotlin.code.style
├── local.properties                # sdk.dir (gitignored, machine-specific)
│
├── gradle/
│   ├── libs.versions.toml          # Library module build deps (vanniktech only)
│   ├── appspirimentlibs.versions.toml  # Android consumer catalog (GENERATED content baked into plugin JAR)
│   └── kmplibs.versions.toml       # KMP consumer catalog (GENERATED content baked into plugin JAR)
│
├── build-logic/                    # Included build — convention plugins
│   ├── settings.gradle.kts
│   ├── gradle.properties
│   ├── gradle/
│   │   └── libs.versions.toml      # Internal build deps (AGP, kotlin, ksp, CMP gradle plugin)
│   └── conventions/
│       ├── build.gradle.kts        # Declares all 12 plugins; generic Constants generator × 2
│       └── src/main/java/com/appspiriment/conventions/
│           ├── extensions/         # Shared: versioning, Extensions, KmpExtensions, Dependencies,
│           │                       #   KmpLibraryCoordinates, CustomExtensions, KmpCustomExtensions,
│           │                       #   KotlinAndroid, KotlinKmp, ProjectConfiguration,
│           │                       #   KmpProjectConfiguration, LibsData
│           ├── plugins/            # Android: Application, Base, Library (+ Hilt/Compose variants)
│           │   ├── ScaffoldComposeThemeTask.kt
│           │   ├── feature/        # AndroidDataLayerConventionPlugin, KmpDataLayerConventionPlugin
│           │   └── kmp/            # KmpBase, KmpLibrary (+ Compose/Koin/KoinCompose variants),
│           │                       #   KmpApplication
│           └── src/theme-templates/ # Fallback XML resources when compose-utils not present
│
└── libs/
    ├── utils/                      # KMP: commonMain (pure Kotlin) + androidMain (java.time, Context)
    ├── logutils/                   # KMP: commonMain API + androidMain (android.util.Log + App Startup)
    ├── compose-utils/              # Android-only: 75 Compose files + res/; CMP is future phase
    └── update-utils/               # Android-only: Firebase Remote Config + in-app update
```

---

## 5. Convention Plugin Catalog

### Android plugins (`io.github.appspiriment.*`)

| Plugin ID | Class | Description |
|---|---|---|
| `io.github.appspiriment.application` | `AndroidApplicationConventionPlugin` | Standard Android app module (Compose + Hilt by default) |
| `io.github.appspiriment.library` | `AndroidLibraryConventionPlugin` | Minimal Android library |
| `io.github.appspiriment.library-hilt` | `AndroidLibraryHiltConventionPlugin` | Library + Hilt |
| `io.github.appspiriment.library-compose` | `AndroidLibraryComposeConventionPlugin` | Library + Compose |
| `io.github.appspiriment.library-hilt-compose` | `AndroidLibraryHiltComposeConventionPlugin` | Library + Hilt + Compose |
| `io.github.appspiriment.data` | `AndroidDataLayerConventionPlugin` | Data layer: opt-in Room, Retrofit, DataStore, Security, WorkManager |

### KMP plugins (`io.github.appspiriment.kmp.*`)

| Plugin ID | Class | Description |
|---|---|---|
| `io.github.appspiriment.kmp.library` | `KmpLibraryConventionPlugin` | Base KMP shared module (Android always on; iOS/Desktop/WASM opt-in via `kmp { }`) |
| `io.github.appspiriment.kmp.library-compose` | `KmpLibraryComposeConventionPlugin` | KMP library + Compose Multiplatform |
| `io.github.appspiriment.kmp.library-koin` | `KmpLibraryKoinConventionPlugin` | KMP library + Koin DI |
| `io.github.appspiriment.kmp.library-koin-compose` | `KmpLibraryKoinComposeConventionPlugin` | KMP library + Koin + Compose MP |
| `io.github.appspiriment.kmp.data` | `KmpDataLayerConventionPlugin` | KMP data layer: opt-in SQLDelight, Ktor, Ktorfit, DataStore, Serialization, Room 3 |
| `io.github.appspiriment.kmp.application` | `KmpApplicationConventionPlugin` | Android host app module for a KMP project |

---

## 6. Library Artifacts

All artifacts share group `io.github.appspiriment` and the unified version from `version.properties`.

| Artifact | Type | Targets | Notes |
|---|---|---|---|
| `utils` | KMP | commonMain + androidMain | Pure-Kotlin extensions in common; java.time/Context in androidMain |
| `logutils` | KMP | commonMain + androidMain | `Log` API in common; `android.util.Log` + `LogInitializer` in androidMain |
| `compose-utils` | Android | release AAR | Compose components, theme, wrappers; CMP port is a future phase |
| `update-utils` | Android | release AAR | Firebase Remote Config-driven update prompts; permanently Android |

### `logutils` auto-gating behaviour

```
┌─────────────────────────────────────────────────────────┐
│  App startup (App Startup library)                      │
│  LogInitializer reads ApplicationInfo.FLAG_DEBUGGABLE   │
│  → Log.enabled = true  (debug builds)                   │
│  → Log.enabled = false (release builds)                 │
│                                                         │
│  Consumer override (any time, any target):              │
│    Log.init(true / false)                               │
│    Log.enabled = true / false                           │
└─────────────────────────────────────────────────────────┘
```

---

## 7. Toolchain Baseline

| Tool | Version |
|---|---|
| Kotlin | 2.1.21 |
| AGP (Android Gradle Plugin) | 8.7.3 |
| Compose Multiplatform | 1.8.0 |
| KSP | 2.1.21-2.0.1 |
| compileSdk / targetSdk | 35 |
| minSdk | 26 |
| Java / JVM target | 17 |
| Gradle | 8.13 (wrapper) |
| Koin | 4.0.4 |
| Ktor | 3.1.3 |
| Coroutines | 1.10.1 |
| SQLDelight | 2.0.2 |
| DataStore | 1.1.4 |
| kotlinx.serialization | 1.8.1 |

---

## 8. Versioning Workflow

Single `version.properties` at repo root:

```properties
MAJOR=1.0.0
DEV=1
```

Published version string: `1.0.0.dev-01` (dev) / `1.0.0` (release, when `-PisRelease` is passed).

```bash
# Dev publish workflow (local)
./gradlew publishDev                    # Step 1: bumps DEV counter in version.properties
./gradlew publishToMavenLocal           # Step 2: publishes with new version

# Release publish
./gradlew publish -PisRelease           # Publishes MAJOR only (no .dev-XX suffix) to Maven Central
```

---

## 9. Implementation Plan

### Phase 1 — Scaffold composite build ✅ DONE
- [x] Create repo skeleton at `UtilsLibs/` (empty, separate from the 3 source repos)
- [x] Copy Gradle wrapper from `AndroidConventionPlugins`
- [x] `settings.gradle.kts`: `pluginManagement { includeBuild("build-logic") }`, register `appspirimentlibs` and `kmplibs` catalogs
- [x] `build.gradle.kts`: read unified version, `allprojects { group = ...; version = ... }`
- [x] `version.properties`: `MAJOR=1.0.0` / `DEV=1`
- [x] `gradle.properties`: AndroidX, POM metadata, Kotlin code style
- [x] `build-logic/settings.gradle.kts`
- [x] `local.properties` (sdk.dir, gitignored)

### Phase 2 — Merge conventions into `build-logic/conventions` ✅ DONE
- [x] Port all Android convention sources verbatim (`AndroidApplication`, `AndroidBase`, `AndroidLibrary*`, `ScaffoldComposeThemeTask`, `AndroidDataLayerConventionPlugin`)
- [x] Port all KMP convention sources into `plugins/kmp/` and `plugins/feature/` (`KmpBase`, `KmpLibrary*`, `KmpApplication`, `KmpDataLayerConventionPlugin`)
- [x] De-duplicate shared extensions: kept Android's `Extensions.kt` (id-based `applyPluginFromLibs`), extracted KMP's alias-based variant as `KmpExtensions.kt` (`applyKmpPluginFromLibs`), renamed all KMP plugin call sites
- [x] Resolved `RoomConfig` collision: KMP version renamed to `KmpRoomConfig` in `KmpCustomExtensions.kt`
- [x] Dropped KMP's dead `Dependency`/`ImplType`/`implementDependency` (unused, conflicted with Android's)
- [x] Single `build-logic/conventions/build.gradle.kts`: one `gradlePlugin { }` block (12 plugins), one generic `registerConstantsGenerator()` called twice (Android + KMP), single `bumpDevVersion`, publishing, signing
- [x] `configureKmpEarly()` enhanced: now also applies `compileSdk`/`minSdk` to the Android `LibraryExtension` and calls `publishLibraryVariants("release")` — consumers only need to declare `namespace`
- [x] `generateThemeTemplates` task now reads from `../libs/compose-utils/src/main/res` (in-repo path), falls back to committed templates
- [x] **Build validated**: `./gradlew -p build-logic :conventions:assemble` → BUILD SUCCESSFUL

### Phase 3 — Catalog consolidation ✅ DONE (pragmatic form)
- [x] `gradle/appspirimentlibs.versions.toml`: aligned to KMP toolchain baseline (Kotlin 2.1.21, AGP 8.7.3, compileSdk/targetSdk 35, javaVersion 17); all lib versions set to `LIBVERSION` placeholder
- [x] `gradle/kmplibs.versions.toml`: lib versions set to `LIBVERSION` placeholder
- [x] Added missing entries required by library modules: `compose-bom`, `ui-tooling`, `ui-test-manifest`, `firebase-bom`, `firebase-config-ktx`, `appspiriment-compose`, `lottie-compose`, `ui-text-google-fonts`, `material` (M2), `kotlin-compose`, `kotlinx-serialization` plugins
- [x] `gradle/libs.versions.toml` (lib module build deps): `vanniktech-publish` plugin
- [ ] **Future**: extract a `shared.versions.toml` overlay to formally de-dup common version numbers shared between the two consumer catalogs

### Phase 4 — Port libs ✅ DONE
- [x] **`logutils`** — KMP module, new architecture:
  - `commonMain`: `Log.kt` — `Log` object (enabled flag, `init()`), `LogLevel` enum, `expect fun platformLog(...)`, public `printLog` functions
  - `androidMain`: `Log.android.kt` — `actual fun platformLog(...)` using `android.util.Log`
  - `androidMain`: `LogInitializer.kt` — App Startup `Initializer<Unit>` reads `FLAG_DEBUGGABLE`, sets `Log.enabled`
  - `androidMain/AndroidManifest.xml` — registers `LogInitializer` via `InitializationProvider`
  - `build.gradle.kts` — applies `io.github.appspiriment.kmp.library`, `enableUtils.set(false)`
  - **Build validated**: `:libs:logutils:build` → BUILD SUCCESSFUL
- [x] **`utils`** — KMP module:
  - `commonMain`: `FlowUtils`, `ListUtils`, `PhoneNumberUtils`, `StringExtns` (pure coroutines/Kotlin)
  - `androidMain`: `Utils`, `ContextExtensions`, `FormatUtils` (android.*), `time/` package (java.time + kotlinx.serialization)
  - `androidUnitTest`: `MillisToMMddHmaTimeTest`
  - `build.gradle.kts` — applies `io.github.appspiriment.kmp.library` + serialization plugin, `enableUtils.set(false)`
  - **Build validated**: `:libs:utils:build` → BUILD SUCCESSFUL
- [x] **`compose-utils`** — Android library:
  - Sources ported; `build.gradle.kts` applies `google.android.library` + `kotlin.android` + `kotlin.compose`
  - Material 2 (`pullrefresh`, `BottomNavigation`, `DropdownMenu`) added via `androidx-material` in bundle
  - Added missing API: `TextButton.kt`, `MalayalamText.kt`, `uiImageResouce()` alias, `ButtonStyle.primaryNegative/primaryPositive()`
  - Lint disabled (`tasks.configureEach`) — AGP 8.7.3 + Kotlin 2.x Analysis API crashes several compose-runtime lint detectors; fix propagated to all compose modules via the `setupCompose()` convention
  - **Build validated**: `:libs:compose-utils:build` → BUILD SUCCESSFUL
- [x] **`update-utils`** — Android library:
  - `build.gradle.kts` applies `io.github.appspiriment.library-compose` + firebase deps
  - Fixed: `org.jetbrains.kotlin.plugin.serialization` removed from `composePluginList` (it was incorrectly included)
  - Fixed: JVM target mismatch (hardcoded `JVM_21` → `JvmTarget.fromTarget(projectConfigs.javaVersion.toString())`)
  - Fixed: `AppUpdateHelperUtil.kt` API migration (`uiColors → colors`, removed `padding(it)` from `ColumnScope` lambda)
  - **Build validated**: `:libs:update-utils:build` → BUILD SUCCESSFUL

### Phase 5 — Wire publishing + full build ✅ DONE
- [x] All 4 modules included in `settings.gradle.kts`
- [x] Dependency substitution in root `build.gradle.kts` — `io.github.appspiriment:*` → local projects during development
- [x] `./gradlew build` at repo root → **BUILD SUCCESSFUL** (295 tasks)
- [x] `./gradlew :build-logic:conventions:test` → **BUILD SUCCESSFUL** (TestKit tests pass)
- [x] `./gradlew publishToMavenLocal` → **BUILD SUCCESSFUL**; all 4 artifacts at `io.github.appspiriment:*:1.0.0.dev-01`; `Constants.kt` + `KmpConstants.kt` contain `libVersion = "1.0.0.dev-01"` (LIBVERSION substituted correctly)
- [x] Plugin IDs resolve from included build — no `publishToMavenLocal` round-trip needed
- [x] `.gitignore` added (excludes `build/`, `.gradle/`, `.kotlin/`, `local.properties`, `*.jks`)
- [x] Initial git commit on `main` branch (191 files)

---

## 10. Completed — What Was Shipped

All goals from §2 are met:
1. **One repo, one Gradle build** ✅ — `UtilsLibs/` consolidates all three old repos
2. **Composite build** ✅ — `includeBuild("build-logic")`, plugins apply from source with no publish round-trip
3. **Maximum KMP/Android reuse** ✅ — `utils` and `logutils` are KMP; shared code in KMP projects calls them from `commonMain`
4. **One unified version** ✅ — `version.properties` drives all 12 plugins + 4 libraries
5. **Fewer plugins/libraries** ✅ — `logutils` is now a single KMP artifact (vs old `-dev`/`-prod` split); one `build-logic/conventions` module declares all 12 plugins
6. **Clean consumer catalogs** ✅ — `appspirimentlibs` (Android) and `kmplibs` (KMP) stay separate; `LIBVERSION` placeholder substituted at code-gen time

### Known limitations / future work
- The two consumer catalogs still share common versions (sdk levels, kotlin, etc.) by copy rather than a shared overlay TOML. This was recorded as Phase 3 future work.
- `compose-utils` Compose Multiplatform port is deferred — Android-only for now.
- Lint is disabled on all Compose modules (AGP 8.7.3 + Kotlin 2.x Analysis API incompatibility). Will resolve when upgrading AGP or when the Compose lint detectors are updated.
5. Write `update-utils/build.gradle.kts`
6. Port/fix TestKit tests in `build-logic`
7. `git init` + `.gitignore`
8. Tag `1.0.0` and publish to Maven Central

---

## 11. Out of Scope / Future Phases

- `compose-utils` → Compose Multiplatform (significant; blocked on CMP stabilisation for M2 replacements)
- `shared.versions.toml` overlay formally de-duplicating common version numbers
- Migration guide for existing consumers of `logutils-dev`/`logutils-prod` → `logutils`
- CI/CD pipeline (GitHub Actions) for automated publishing
