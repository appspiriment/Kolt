<!-- KOLT:BEGIN -->
# <AppName> — Claude Code Context

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — binding rules (Clean + MVI + SOLID + stateless Compose). Follow exactly.

**Read on demand:**
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first. Read when designing or restructuring.
- [`docs/TESTING.md`](docs/TESTING.md) — test patterns, fakes, coverage targets. Read when writing tests.
- [`docs/KOLT.md`](docs/KOLT.md) — plugin & library API reference. Read when using compose-utils components, configuring data layers, or wiring new modules.
<!-- KOLT:END -->

## Project facts

- **What it is:** <one-line description>
- **Targets:** Android-only
- **Modules / layers:** `:app` · `:feature:<name>` · `:domain` · `:data` · `:core:common`
- **DI:** Hilt (auto-wired by `io.github.appspiriment.kolt.application`)
- **Local store:** <Room | DataStore | none>
- **Remote:** <Retrofit | Firebase | none>
- **MVI base class:** `MviViewModel<State, Intent, Effect>` from `compose-utils`
- **Dispatch intents:** `vm.dispatch(Intent)` · handle in `override suspend fun onIntent(intent)`
- **One-shot effects:** `sendEffect(Effect)` in VM · `vm.collectEffects { }` in Route composable
- **Async result type:** `AppResult<T>` / `AppError` (add to `:core:common`)

## Project-specific rules (deltas only)

- <e.g. all monetary amounts use a `Money` value object, never a raw Double>
- <e.g. analytics events emitted via `UiEvent.TrackEvent` only>

## Build / test

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew :app:testDebugUnitTest

# One-time setup (runs automatically on first preBuild):
./gradlew scaffoldKoltResources   # writes theme XMLs to src/main/res/
./gradlew scaffoldKoltDocs        # writes docs/ and CLAUDE.md/AGENTS.md
```

## Done means

Compiles · lint clean · reducer + use-case unit tests pass · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`
