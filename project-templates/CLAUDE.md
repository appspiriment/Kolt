<!-- KOLT:BEGIN -->
# <PROJECT> — Claude Code Context

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — **binding** coding rules (Clean + MVI + SOLID + stateless Compose). It is the rule index; follow it exactly. This file adds only project-specific facts.

**Read on demand** (don't load unless the task needs it):
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first, layer wiring, Android/KMP deltas, abstractions catalog. Read when designing or structuring.
- [`docs/TESTING.md`](docs/TESTING.md) — what to test + patterns. Read when writing tests.
- [`docs/KOLT.md`](docs/KOLT.md) — plugin catalog, DSL options (`kolt {}` / `kmp {}` / `dataLayer {}`), library APIs (logutils / utils / compose-utils / update-utils), catalog aliases, scaffolding. Read when using or configuring the Kolt suite.
<!-- KOLT:END -->

## Project facts (fill in)

- **What it is:** <one line>
- **Targets:** <Android-only | KMP: Android/iOS/Desktop>
- **Modules / layers:** <e.g. :app · :feature:* · :domain · :data · :core:*  — or packages domain/ data/ ui/>
- **Key libs:** DI=<Hilt|Koin|kotlin-inject> · Local=<Room|SQLDelight|DataStore> · Remote=<Retrofit|Ktor|Firebase|none> · Async result type=<Result|Outcome|Either>
- **MVI base class:** <BaseViewModel<S,I,E> location>
- **One-shot VM→UI channel name in this project:** Effect  *(or UiEvent/SideEffect — match the code)*

## Project-specific rules (only deltas from the standard)

- <e.g. all monetary values use Money value object>
- <e.g. analytics events emitted via XEffect only>

## Build / test

```
<./gradlew :app:assembleDebug>
<./gradlew test>      # or :module:testDebugUnitTest
<./gradlew ktlintCheck detekt>
```

## Done means

Compiles · lint clean · reducer + use-case unit tests pass · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`.
