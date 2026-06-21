<!-- KOLT:BEGIN -->
# <PROJECT> — Agent Context (Gemini / Antigravity)

**Convention:** `AGENTS.md` standard, auto-read by Gemini CLI / Antigravity and compatible agent runners.

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — **binding** coding rules (Clean + MVI + SOLID + stateless Compose). It is the rule index; follow it exactly, do not skim. This file adds only project-specific facts.

**Read on demand** (only when the task needs it, to save context):
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first data layer, layer wiring, Android/KMP deltas, abstractions catalog. Read when designing or structuring.
- [`docs/TESTING.md`](docs/TESTING.md) — what to test + patterns. Read when writing tests.

> Paths assume the standards live in `docs/` (copy or submodule); adjust if elsewhere.

## Working rules

- Follow `docs/CODING_STANDARDS.md`. Reuse existing base classes before adding new ones; compile + run unit tests after a change.
<!-- KOLT:END -->

## Project facts (fill in)

- **What it is:** <one line>
- **Targets:** <Android-only | KMP: Android/iOS/Desktop>
- **Modules / layers:** <:app · :feature:* · :domain · :data · :core:*  — or packages domain/ data/ ui/>
- **Key libs:** DI=<Hilt|Koin|kotlin-inject> · Local=<Room|SQLDelight|DataStore> · Remote=<Retrofit|Ktor|Firebase|none> · Async result type=<Result|Outcome|Either>
- **MVI base class:** <BaseViewModel<S,I,E> location>
- **One-shot VM→UI channel name in this project:** Effect  *(or UiEvent/SideEffect — match the code)*

## Build / test

```
<./gradlew :app:assembleDebug>
<./gradlew test>
<./gradlew ktlintCheck detekt>
```

## Done means

Compiles · lint clean · reducer + use-case unit tests pass · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`.
