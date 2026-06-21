<!-- KOLT:BEGIN -->
# <AppName> — Agent Context (Gemini / Antigravity)

**Convention:** `AGENTS.md` standard, auto-read by Gemini CLI / Antigravity and compatible runners.

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — binding rules (Clean + MVI + SOLID + stateless Compose). Follow exactly; do not skim.

**Read on demand** (only when the task needs it):
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first, abstractions catalog. Read when designing or restructuring.
- [`docs/TESTING.md`](docs/TESTING.md) — test patterns, fakes, coverage targets. Read when writing tests.
- [`docs/KOLT.md`](docs/KOLT.md) — plugin catalog, DSL options, library APIs (logutils/utils/compose-utils/update-utils), catalog aliases, scaffolding. Read when using or configuring the Kolt suite.

## Working rules

- Follow `docs/CODING_STANDARDS.md`. Reuse existing base classes before adding new ones.
- Compile + run unit tests after every change.
- Use `MviViewModel<State, Intent, Effect>` (from compose-utils) as the MVI base — do not rewrite it.
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
- **Async result type:** `AppResult<T>` / `AppError` (in `:core:common`)

## Build / test

```bash
./gradlew :app:assembleDebug
./gradlew test
./gradlew :app:testDebugUnitTest
```

## Done means

Compiles · lint clean · reducer + use-case tests pass · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`
