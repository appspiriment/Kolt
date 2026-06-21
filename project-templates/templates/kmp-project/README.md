# KMP Project Template

Ready-to-use Gradle scaffolding for a new Kotlin Multiplatform project using Kolt convention plugins.

## Usage

1. Copy these files into your new project root.
2. Replace all `<placeholder>` values:
   - `settings.gradle.kts` → `rootProject.name`
   - `app/build.gradle.kts` → `namespace` and `applicationId`
   - `CLAUDE.md` / `AGENTS.md` → fill in Project facts, enable targets
3. Update both catalog versions in `settings.gradle.kts` to the latest release.
4. Run `./gradlew :app:assembleDebug` — first build scaffolds theme XMLs and steering docs automatically.

## Module structure

```
<AppName>/
├── app/                              ← Android host (io.github.appspiriment.kolt.kmp.application)
│   └── build.gradle.kts
├── shared/                           ← KMP ViewModels + domain (kmp.library-koin)
│   └── build.gradle.kts
├── data/                             ← KMP data layer (kmp.data + kmpDataLayer {})
│   └── build.gradle.kts
└── core/
    └── common/                       ← KMP base classes: AppResult, UseCase, dispatchers
        └── build.gradle.kts          ← (kmp.library)
```

## Adding iOS support

In `shared/build.gradle.kts`:
```kotlin
kmp { enableIos.set(true) }
```

In `settings.gradle.kts`, add:
```kotlin
// (no change needed — the plugin creates the iosMain source set automatically)
```

Create an Xcode project and add a `iosApp/` directory; set the KMP framework as a dependency.

## Data layer snippet (`kmp.data`)

```kotlin
// data/build.gradle.kts
plugins { id("io.github.appspiriment.kolt.kmp.data") }
kmpDataLayer {
    sqlDelight { enabled.set(true) }    // recommended for KMP
    ktor { enabled.set(true) }
    dataStore { enabled.set(true) }
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(project(":core:common"))
        }
    }
}
```

## KMP-specific rules

- **Write everything in `commonMain` by default.** Move code to `androidMain`/`iosMain` only when it uses a platform API.
- **`expect/actual`** — declare the interface in `commonMain`, provide implementations in each platform source set.
- **No `java.*` in `commonMain`** — use `kotlinx-datetime` for time, `kotlinx-io` for I/O, `kotlinx-serialization` for JSON.
- **Test in `commonTest`** — reducers, use cases, and mappers are all platform-agnostic; test them there.

See `docs/KOLT.md` §2–§5 for the full plugin and DSL reference.
See `docs/ARCHITECTURE.md` §5 for the full Android-only vs KMP deltas table.
