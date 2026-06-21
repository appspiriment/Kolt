# Android Project Template

Ready-to-use Gradle scaffolding for a new Android project using Kolt convention plugins.

## Usage

1. Copy these files into your new project root.
2. Replace all `<placeholder>` values:
   - `settings.gradle.kts` → `rootProject.name`
   - `app/build.gradle.kts` → `namespace` and `applicationId`
   - `CLAUDE.md` / `AGENTS.md` → fill in the Project facts section
3. Update the `appspiriment-catalog` version in `settings.gradle.kts` to the latest release.
4. Run `./gradlew :app:assembleDebug` — first build scaffolds theme XMLs and steering docs automatically.

## Typical module structure

```
<AppName>/
├── app/                              ← host app (io.github.appspiriment.kolt.application)
│   └── build.gradle.kts
├── feature/
│   └── home/
│       └── build.gradle.kts         ← (io.github.appspiriment.kolt.library-hilt-compose)
├── domain/
│   └── build.gradle.kts             ← (io.github.appspiriment.kolt.library) — no DI annotations
├── data/
│   └── build.gradle.kts             ← (io.github.appspiriment.kolt.data) + dataLayer {}
└── core/
    └── common/
        └── build.gradle.kts         ← (io.github.appspiriment.kolt.library) — shared base classes
```

## Module build.gradle.kts snippets

**Feature module (`library-hilt-compose`):**
```kotlin
plugins { id("io.github.appspiriment.kolt.library-hilt-compose") }
android { namespace = "com.example.myapp.feature.home" }
dependencies {
    implementation(project(":domain"))
    // compose-utils (artifact: compose) is added automatically by library-hilt-compose
}
```

**Domain module (`library`):**
```kotlin
plugins { id("io.github.appspiriment.kolt.library") }
android { namespace = "com.example.myapp.domain" }
// No DI, no Compose — pure Kotlin interfaces + entities
```

**Data module (`data`):**
```kotlin
plugins { id("io.github.appspiriment.kolt.data") }
android { namespace = "com.example.myapp.data" }
dataLayer {
    room { enabled.set(true) }
    retrofit { enabled.set(true); useChucker.set(true) }
}
dependencies { implementation(project(":domain")) }
```

See `docs/KOLT.md` §2–§5 for the full plugin and DSL reference.
