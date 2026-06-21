# AGENTS.md — Architecture & Coding Guide for UtilsLibs

This file is the authoritative guide for any AI agent or contributor working in this repository.
Read it fully before making any changes.

---

## 1. Project Overview

`UtilsLibs` is a **Kotlin Multiplatform** monorepo that consolidates:
- **Convention Gradle plugins** (Android + KMP) published to Maven Central
- **Runtime utility libraries** (`utils`, `logutils`, `compose-utils`, `update-utils`)

Artifacts share group `io.github.appspiriment.kolt` with **per-artifact independent versions**
tracked in `version.properties` (e.g. `PLUGIN_MAJOR`, `UTILS_MAJOR`, `BOM_VERSION`).
The repo uses a **composite Gradle build** — `build-logic/` is an included build with two
subprojects: `:conventions` (all 12 convention plugins) and `:publish` (lean vanniktech-only
module for classloader isolation). Convention plugins apply from source — no publish round-trip.

Refer to `requirements.md` for full requirements, decisions, and implementation plan.

---

## 2. Repo Structure — What Lives Where

```
build-logic/conventions/src/main/…/conventions/
    extensions/      ← shared helpers used by both Android and KMP plugins
    plugins/         ← Android convention plugins
    plugins/kmp/     ← KMP convention plugins
    plugins/feature/ ← AndroidDataLayer + KmpDataLayer

libs/
    utils/           ← KMP: commonMain (pure Kotlin) + androidMain (android.*, java.time)
    logutils/        ← KMP: commonMain API + androidMain (android.util.Log + App Startup)
    compose-utils/   ← Android-only: Compose components + theme + wrappers
    update-utils/    ← Android-only: Firebase-driven in-app update

gradle/
    koltlibs.versions.toml   ← published Android consumer catalog (VERSIONS + plugins only)
    kmplibs.versions.toml            ← published KMP consumer catalog (VERSIONS + plugins only)
    libs.versions.toml               ← library module build deps (vanniktech etc.)

build-logic/gradle/
    libs.versions.toml               ← internal build deps for the convention plugins themselves
```

**Never** put Android-only imports into `commonMain`. Never put `build-logic` runtime deps into
the repo-root `libs.versions.toml` — they live in `build-logic/gradle/libs.versions.toml`.

---

## 3. Convention Plugins — Rules

### 3.1 Applying plugins

- **Android plugins** use `applyPluginFromLibs(catalog to listOf("plugin-id", ...))` (resolves by plugin ID).
- **KMP plugins** use `applyKmpPluginFromLibs(catalog to listOf("alias", ...))` (resolves by catalog alias). Do **not** cross these — the `kmplibs` catalog uses aliases, `koltlibs` uses IDs.

### 3.2 Catalog design — versions in the catalog, coordinates in the plugin

The consumer catalogs (`koltlibs.versions.toml`, `kmplibs.versions.toml`) contain
**only `[versions]` and `[plugins]`** — no `[libraries]`, no `[bundles]`. Library *coordinates*
(`group:name`) are hardcoded in the plugin; the plugin reads the matching *version* back from the
catalog by alias. This means a consumer can bump a single dependency (e.g. raise `composeBom`)
for their project without changing the plugin version, and the plugin stays the single source of
truth for which artifacts a module gets.

- **Coordinates** (group:name): hardcoded in the plugin —
  `extensions/Dependencies.kt` (Android, as `List<Dependency>` vals) and
  `extensions/KmpLibraryCoordinates.kt` (KMP, as `DependencyHandlerScope` extension functions).
- **Versions**: read from the catalog via `libs.getVersion("alias")` / `kmpLibs.getVersion("alias")`,
  or — for the Android `Dependency` model — via `Dependency(notation = "group:name", versionRef = "alias")`.
  Compose/Firebase artifacts pinned by a BOM platform carry no `versionRef`.
- **Kolt runtime libs** (utils/logutils/compose-utils/update-utils) carry no catalog version
  at all — the plugin injects the Kolt BOM (version baked into `Constants.kt` from
  `version.properties`) and references the artifacts without a version.
- The **lib modules themselves** (`libs/compose-utils`, `libs/update-utils`) BUILD published
  artifacts, so they are not plugin consumers — they declare their own coordinates directly,
  pulling versions from the catalog (`koltlibs.versions.<alias>.get()`).

### 3.3 Adding a new plugin

1. Write the plugin class in `plugins/` (Android) or `plugins/kmp/` (KMP).
2. Register it in `build-logic/conventions/build.gradle.kts` under `gradlePlugin { plugins { create(...) } }`.
3. Add the plugin alias to both the relevant consumer catalog (`koltlibs.versions.toml` or `kmplibs.versions.toml`) **and** the internal `build-logic/gradle/libs.versions.toml` if it's a build-time dependency.
4. The Constants generator runs automatically — no manual update needed.

### 3.4 Convention plugin DSL blocks

- Android: `appspiriment { enableUtils = true; enableMinify = false; ... }` → `AppspirimentExtension`
- KMP library: `kmp { enableIos.set(true); enableDesktop.set(false); enableUtils.set(true) }` → `KmpExtension`
- KMP data layer: `kmpDataLayer { sqlDelight { enabled.set(true) }; ktor { ... }; retrofit { enabled.set(true) } }` → `KmpDataLayerExtension`
  - Persistence: `sqlDelight` (full KMP) or `room` (alpha, no WASM; configures `schemaVersion`/`schemaDirectory`)
  - Networking: `ktor` / `ktorfit` (full KMP) or `retrofit` (androidMainImplementation only)
  - Room uses `androidx.room:room-runtime` (group `androidx.room`, NOT `androidx.room3`)
  - `sqlite-bundled` has its own version (`sqliteBundled` in kmplibs) separate from Room

---

## 4. KMP Library Modules — Rules

### 4.1 Source set discipline

| Source set | What goes here |
|---|---|
| `commonMain` | Pure Kotlin: stdlib, coroutines, kotlinx.serialization, domain logic |
| `androidMain` | `android.*`, `androidx.*`, `java.time`, Compose (Android side) |
| `iosMain` | iOS-specific `actual` implementations |
| `commonTest` | Pure tests using `kotlin-test`, coroutines-test, Turbine |
| `androidUnitTest` | Tests that depend on Android SDK / java.time |

**Never** import `android.*` in `commonMain`. If you need platform behaviour in common code, use `expect`/`actual`.

### 4.2 expect/actual pattern

```kotlin
// commonMain
internal expect fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?)

// androidMain
internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    // android.util.Log
}
```

Use `expect`/`actual` only when the platform difference is real. Don't over-abstract.

### 4.3 Minimal `build.gradle.kts` for KMP modules

```kotlin
plugins {
    id("io.github.appspiriment.kolt.kmp.library")   // or kmp.library-compose, kmp.library-koin, etc.
    id("io.github.appspiriment.kolt.publish")       // vanniktech publish — from :publish included build
}

kmp {
    enableUtils.set(true)           // default: true — adds utils + logutils
    // enableIos.set(true)          // opt-in
    // enableDesktop.set(true)      // opt-in
}

android {
    namespace = "io.github.appspiriment.kolt.yourmodule"    // REQUIRED — the only Android config you need
}

mavenPublishing {
    coordinates(artifactId = "your-module")
    pom {
        name = "Kolt Your Module"
        description = "..."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
    // publishToMavenCentral + signAllPublications are configured in the publish plugin
    // when -PisRelease is present — do NOT add them here.
}
```

The plugin fills in `compileSdk`, `minSdk`, `jvmTarget`, and `publishLibraryVariants("release")`.

### 4.4 Self-referencing modules

Modules that ARE a util library must opt out of auto-wiring themselves:
```kotlin
kmp { enableUtils.set(false) }
```
This applies to `:libs:utils` and `:libs:logutils`.

---

## 5. Android-Only Library Modules — Rules

### 5.1 Minimal `build.gradle.kts`

For libraries that use Kolt convention plugins:
```kotlin
plugins {
    alias(koltlibs.plugins.kolt.library)   // or .library-compose, .library-hilt, etc.
    alias(libs.plugins.vanniktech.publish)
}

android {
    namespace = "io.github.appspiriment.kolt.yourmodule"
}

kolt {
    enableUtils = true          // default: true
    enableMinify = false        // default: false
}
```

For libraries that don't use convention plugins (e.g. `compose-utils`):
```kotlin
plugins {
    alias(koltlibs.plugins.google.android.library)
    alias(koltlibs.plugins.kotlin.android)
    alias(koltlibs.plugins.kotlin.compose)      // if using Compose
    alias(libs.plugins.vanniktech.publish)
}
android {
    namespace = "..."
    compileSdk = koltlibs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = koltlibs.versions.minSdk.get().toInt() }
}
```

---

## 6. Logging — `logutils`

### 6.1 API (callable from commonMain)

```kotlin
// Top-level functions — import io.github.appspiriment.kolt.logutils
printLog("something happened", tag = "MyClass")          // WARN by default
printLog("error occurred", tag = "MyClass", isError = true)   // ERROR
printLog("verbose info", tag = "MyClass", level = LogLevel.DEBUG)
exception.printLog(tag = "MyClass")                       // logs stack trace at ERROR

// Direct flag control
Log.enabled = true
Log.init(BuildConfig.DEBUG)      // same as Log.enabled = BuildConfig.DEBUG
```

### 6.2 Auto-gating

On Android, `LogInitializer` (App Startup) runs before `Application.onCreate()` and sets
`Log.enabled` from `ApplicationInfo.FLAG_DEBUGGABLE`:
- **Debug builds** → `Log.enabled = true`
- **Release builds** → `Log.enabled = false`

Consumer apps can override _after_ startup:
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.init(BuildConfig.DEBUG)   // optional explicit override
    }
}
```

Non-Android targets default to `Log.enabled = false`. Call `Log.init(true)` in your
platform entry point to enable logging.

### 6.3 Disabling App Startup auto-gating

To turn off `LogInitializer` in a specific module (e.g., you want manual control only):
```xml
<!-- AndroidManifest.xml in that module -->
<provider
    android:name="androidx.startup.InitializationProvider"
    android:authorities="${applicationId}.androidx-startup"
    tools:node="merge">
    <meta-data
        android:name="io.github.appspiriment.kolt.logutils.LogInitializer"
        android:value="androidx.startup"
        tools:node="remove" />
</provider>
```

---

## 7. Version Catalogs — How to Use

### In Android library modules (`libs/compose-utils`, `libs/update-utils`)

The catalog is versions-only, so plugins come by alias but library coordinates are written
directly with the version pulled from the catalog:

```kotlin
// Catalog alias: koltlibs  (from gradle/koltlibs.versions.toml)
alias(koltlibs.plugins.google.android.library)
implementation(platform("androidx.compose:compose-bom:${'$'}{koltlibs.versions.composeBom.get()}"))
implementation("androidx.compose.material3:material3")          // version via the BOM above
implementation("io.coil-kt:coil-compose:${'$'}{koltlibs.versions.coil.get()}")
```

### In KMP library modules (`libs/utils`, `libs/logutils`)

```kotlin
// Catalog alias: kmplibs  (from gradle/kmplibs.versions.toml)
// The publish plugin is applied by ID, not from a catalog:
id("io.github.appspiriment.kolt.publish")
"androidMainImplementation"("androidx.startup:startup-runtime:1.2.0")
```

### In `build-logic` (plugin code)

```kotlin
// Catalog alias: libs  (from build-logic/gradle/libs.versions.toml — internal build deps)
implementation(libs.android.gradle.plugin)
implementation(libs.kotlin.gradle.plugin)
```

**Rule:** Never reference `koltlibs` or `kmplibs` inside `build-logic`. Those are
consumer-facing catalogs whose content is baked into the plugin JAR as `Constants.kt`.

---

## 8. Versioning — How It Works

Each artifact has an independent version track in `version.properties` (e.g. `PLUGIN_MAJOR`, `UTILS_MAJOR`, `BOM_VERSION`). Example:

```properties
PLUGIN_MAJOR=0.2.0
PLUGIN_DEV=0
```

Version string at build time:
- Dev: `0.2.0.dev-01` (MAJOR + padded DEV)
- Release (`-PisRelease`): `0.2.0`

The `build-logic/conventions/build.gradle.kts` reads this file via the parent path
(`rootDir.parentFile.resolve("version.properties")`) since it is an included build.
`build.gradle.kts` (repo root) also reads it and propagates via `allprojects { version = ... }`.

The `PLUGIN_VERSION` token (and the `*_VERSION` / `BOM_VERSION` tokens, plus the legacy
`LIBVERSION` alias) in both consumer catalogs is replaced at code-gen time to embed the current
versions in the baked-in TOML — so consumer projects that run `scaffoldKoltResources` get
the exact matching catalog versions.

### 8.1 Versioning Rules (SemVer guidelines starting at `0.2.0`)

*   **Runtime Libraries (`utils`, `logutils`, `compose-utils`, etc.):**
    *   **Patch Bump (Micro: `X.Y.Z` → `X.Y.Z+1`):** Bug fixes, internal performance tweaks, dependency upgrades within the module. No API change.
    *   **Minor Bump (`X.Y.Z` → `X.Y+1.0`):** Backward-compatible new features (e.g., new extension functions, composables, or helper classes).
    *   **Major Bump (`X.Y.Z` → `X+1.0.0`):** Breaking API changes (e.g., signature changes, removals of public methods, or significant refactoring).
*   **BOM (`kolt-bom`):**
    *   Uses calendar versioning format `YYYY.MM.PATCH` (e.g., `2026.06.0`).
    *   **Patch Bump:** Increment the `PATCH` counter after every library publish.
    *   **Monthly Reset:** Roll into new `MM`/`YYYY` and reset `PATCH` to `0` when publishing the first release of a new month.
*   **Convention Plugins (`conventions`):**
    *   **Patch Bump:** Bushed for plugin bug fixes, OR whenever a library/BOM version changes (needed to compile and bake the new version constants into the plugin).
    *   **Minor Bump:** Adding new plugins, catalog entries, or new properties to custom DSL blocks (`kmp { }` or `appspiriment { }`).
    *   **Major Bump:** Removing plugins, renaming plugin IDs, removing DSL properties, or upgrading base tooling requiring consumer migration.

**Crucial Dependency Rule:** Any library version bump requires a plugin version bump and a BOM version bump.

---

## 9. Coding Style

### 9.1 Kotlin

- **Kotlin 2.3.10** — language version `2.0`, API version `2.0` (set by convention plugins).
- Prefer `val` over `var`. Prefer expression functions for single-expression bodies.
- Use `internal` visibility for anything not part of the public API.
- Extension functions over utility classes. Group related extensions in a single file per domain.
- `data class` for pure value holders. `object` for singletons with no mutable state.
- Avoid platform types (`!`-annotated): always be explicit about nullability when interoperating with Java.
- `@Suppress` annotations must include a reason comment above them.

### 9.2 Coroutines & Flow

- Expose `Flow<T>` from repositories/data sources; collect in ViewModels only.
- Never `GlobalScope` — always pass `CoroutineScope` or use `viewModelScope`.
- Prefer `StateFlow`/`SharedFlow` over `LiveData` in new code.
- Use `flowOn(Dispatchers.IO)` for IO-bound flows; keep `viewModelScope.launch` on the default dispatcher.
- Wrap all Flow collection with `catch { }` before returning from a public API.

### 9.3 Compose (Android + compose-utils)

- **One composable per file** for anything with significant logic. Simple helper composables may co-locate.
- Composables are **stateless by default** — hoist state to the caller.
- Preview functions are named `@Preview fun <Name>Preview()` in the same file, in a `@PreviewParameterProvider` where multiple states are needed.
- Use `Modifier` as the **first non-required parameter** (after content lambdas go at the end).
- Never do I/O or business logic inside a `@Composable` function. Use `LaunchedEffect`, `rememberCoroutineScope`, or a ViewModel.
- Theme values come from `MaterialTheme.colorScheme`, `MaterialTheme.typography`, `MaterialTheme.shapes` — never hardcode colors or text sizes.

### 9.4 Naming

| Thing | Convention | Example |
|---|---|---|
| Composable | PascalCase, noun or noun-phrase | `AppsButton`, `PermissionHandlerUtils` |
| ViewModel | `<Feature>ViewModel` | `UpdateViewModel` |
| Extension functions | camelCase verbs | `printLog()`, `toFormattedDate()` |
| Expect/actual files | `<Name>.<platform>.kt` | `Log.android.kt` |
| Gradle catalog version aliases | kebab-case or camelCase | `material-icons`, `composeBom` |
| Convention plugin IDs | `io.github.appspiriment.kolt[.kmp].<feature>` | `io.github.appspiriment.kolt.kmp.library-koin` |

### 9.5 File / Package organisation

- Package root: `io.github.appspiriment.kolt.<module>` (e.g. `io.github.appspiriment.kolt.logutils`).
- One top-level class/object/interface per file, file name matches the type name.
- Exception: tightly-related sealed class + subclasses may share a file.
- `extensions/` subpackage for extension-function files. `utils/` for stateless helpers.
- Do **not** create a `util/` or `helper/` package inside `extensions/` — just name the file clearly.

---

## 10. Publishing

### Maven coordinates

```
groupId:    io.github.appspiriment.kolt
artifactId: utils | logutils | compose-utils | update-utils
version:    <from version.properties>
```

### Per-module `mavenPublishing` block

```kotlin
mavenPublishing {
    coordinates(artifactId = "my-module")     // group + version come from allprojects
    pom {
        name = "Kolt My Module"
        description = "..."
        url = "https://github.com/appspiriment/UtilsLibs"
    }
    // Do NOT call publishToMavenCentral() or signAllPublications() here.
    // Both are configured in AppspirimentPublishConventionPlugin (the :publish
    // included-build module) and gated behind -PisRelease — calling them per-module
    // registers SonatypeRepositoryBuildService from each module's classloader scope
    // and causes a type-identity conflict at configuration time.
}
```

### Classloader isolation — why we have two build-logic subprojects

- `:conventions` — carries KGP/AGP/KSP/Compose as `compileOnly`. Also applies vanniktech
  for its own catalog publications. Root `buildscript {}` puts these on a shared ClassLoader
  so all lib modules get the same `KotlinNativeBundleBuildService` type.
- `:publish` — carries only vanniktech. Sourcing it from here (via `id("io.github.appspiriment.kolt.publish")`)
  means all lib modules load the vanniktech plugin from ONE shared classloader, and
  `SonatypeRepositoryBuildService` is registered exactly once.

### Release workflow

```bash
# MavenLocal (for testing — no credentials needed):
./gradlew publishToMavenLocal

# Maven Central (checks Central first, only publishes changed versions):
./scripts/check-and-publish.sh --release
```

### GitHub Actions Automated Releases

A GitHub Actions workflow is triggered automatically when a new GitHub Release is published (`release.published`).
The workflow:
1. Injects credentials via `ORG_GRADLE_PROJECT_*` environment variables (Gradle automatically
   maps these to project properties — no flag-passing needed).
2. Checks Maven Central for already published artifact versions.
3. Automatically publishes the newly changed library/plugin versions to Maven Central.

Required repository secrets (`Settings → Secrets → Actions`):

| Secret | Value |
|--------|-------|
| `MAVEN_CENTRAL_USERNAME` | Central Portal user token username |
| `MAVEN_CENTRAL_PASSWORD` | Central Portal user token password |
| `GPG_SIGNING_KEY` | `gpg --export-secret-keys --armor <KEY_ID> \| base64` |
| `GPG_KEY_PASSWORD` | Passphrase for the GPG key |

### Secrets & Credentials — Local Development

Never commit credentials. Configure them in `~/.gradle/gradle.properties` (machine-global,
never in the project):

```properties
# Maven Central Portal token (https://central.sonatype.com → Account → User Token)
mavenCentralUsername=<token-username>
mavenCentralPassword=<token-password>

# GPG in-memory signing (no gpg-agent needed)
# Export: gpg --export-secret-keys --armor <KEY_ID> | base64
signingInMemoryKey=<base64-armored-private-key>
signingInMemoryKeyPassword=<passphrase>
```

These property names are read directly by vanniktech's `InMemoryGpgSigner` — no additional
Gradle configuration required. The signing block in `build.gradle.kts` is managed entirely
by vanniktech when `-PisRelease` is present.

**To generate your GPG key export:**
```bash
gpg --list-secret-keys          # find your KEY_ID
gpg --export-secret-keys --armor <KEY_ID> | base64 | pbcopy   # copies to clipboard
```

---

## 11. What NOT to Do

- Do **not** add `mavenLocal()` as a consumer repository in any `settings.gradle.kts` — the composite build makes it unnecessary for local development.
- Do **not** duplicate version numbers in multiple TOML files — add new versions to both `koltlibs` and `kmplibs` only if genuinely needed on both sides.
- Do **not** apply plugins by hardcoded string IDs inside convention plugins — use `applyPluginFromLibs` / `applyKmpPluginFromLibs` so the version catalog is the single source.
- Do **not** add Android-only library dependencies to `kmplibs.versions.toml` and vice versa.
- Do **not** create product flavors in library modules — use `expect`/`actual` for platform variation in KMP, and build-type configuration for debug/release variation.
- Do **not** put `local.properties` in version control (it's machine-specific).
- Do **not** commit `build/`, `.gradle/`, `.kotlin/` directories.
- Do **not** modify the generated `Constants.kt` or `KmpConstants.kt` — they are overwritten on every build.
- Do **not** hardcode credentials in any `.kts`, `.kt`, `.yml`, or `.properties` file in the repo.
  Use `~/.gradle/gradle.properties` locally and `ORG_GRADLE_PROJECT_*` env vars in CI.
- Do **not** call `publishToMavenCentral()` or `signAllPublications()` in individual module
  `mavenPublishing {}` blocks — this is handled centrally by `AppspirimentPublishConventionPlugin`.
