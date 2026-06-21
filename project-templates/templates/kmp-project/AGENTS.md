<!-- KOLT:BEGIN -->
# <AppName> — Agent Context (Gemini / Antigravity)

**Convention:** `AGENTS.md` standard, auto-read by Gemini CLI / Antigravity and compatible runners.

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — binding rules (Clean + MVI + SOLID + stateless Compose). Follow exactly; do not skim.

**Read on demand** (only when the task needs it):
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first, KMP deltas, `expect/actual`. Read when designing or restructuring.
- [`docs/TESTING.md`](docs/TESTING.md) — test patterns, fakes, coverage targets. Read when writing tests.
- [`docs/KOLT.md`](docs/KOLT.md) — plugin catalog, `kmp {}` / `kmpDataLayer {}` DSL, library APIs, catalog aliases, scaffolding. Read when using or configuring the Kolt suite.

## Working rules

- Follow `docs/CODING_STANDARDS.md`. Reuse existing base classes (MVI base, `AppResult`, `UseCase`) before adding new ones.
- Default to `commonMain`. Use `androidMain`/`iosMain` only for irreducibly platform-specific code.
- Compile + run all-target tests after every change.
<!-- KOLT:END -->

## Project facts

- **What it is:** <one-line description>
- **Targets:** KMP — Android · <iOS | Desktop | WASM — add as needed>
- **Modules / layers:**
  - `:app` — Android host (`io.github.appspiriment.kolt.kmp.application`)
  - `:shared` — KMP ViewModels + domain (`io.github.appspiriment.kolt.kmp.library-koin`)
  - `:data` — KMP data layer (`io.github.appspiriment.kolt.kmp.data`)
  - `:core:common` — KMP base classes
- **DI:** Koin (auto-wired by `kmp.library-koin*`)
- **Local store:** <SQLDelight | Room KMP | none>
- **Remote:** <Ktor | Ktorfit | Firebase | none>
- **MVI base class:** `MviViewModel<State, Intent, Effect>` from `compose-utils` (or add to `:core:common`)
- **Dispatch intents:** `vm.dispatch(Intent)` · handle in `override suspend fun onIntent(intent)`
- **One-shot effects:** `sendEffect(Effect)` in VM · `vm.collectEffects { }` in Route composable
- **Platform boundary:** `expect/actual` in `commonMain` + `androidMain`/`iosMain`

## Build / test

```bash
./gradlew :app:assembleDebug
./gradlew allTests
./gradlew :shared:testDebugUnitTest
```

## Done means

Compiles all targets · tests pass · no unimplemented `expect` · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`
