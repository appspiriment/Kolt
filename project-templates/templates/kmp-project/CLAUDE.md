<!-- KOLT:BEGIN -->
# <AppName> — Claude Code Context

**Read first:** [`docs/CODING_STANDARDS.md`](docs/CODING_STANDARDS.md) — binding rules (Clean + MVI + SOLID + stateless Compose). Follow exactly.

**Read on demand:**
- [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) — layering, DI, offline-first, KMP deltas, `expect/actual`. Read when designing or restructuring.
- [`docs/TESTING.md`](docs/TESTING.md) — test patterns, fakes, coverage targets. Read when writing tests.
- [`docs/KOLT.md`](docs/KOLT.md) — plugin catalog, `kmp {}` / `kmpDataLayer {}` DSL, library APIs, catalog aliases, scaffolding. Read when using or configuring the Kolt suite.
<!-- KOLT:END -->

## Project facts

- **What it is:** <one-line description>
- **Targets:** KMP — Android · <iOS | Desktop | WASM — add as needed>
- **Modules / layers:**
  - `:app` — Android host (thin launcher, `io.github.appspiriment.kolt.kmp.application`)
  - `:shared` — KMP shared (ViewModels, domain, use cases — `io.github.appspiriment.kolt.kmp.library-koin`)
  - `:data` — KMP data layer (`io.github.appspiriment.kolt.kmp.data`)
  - `:core:common` — KMP base classes, `AppResult`, `UseCase`, dispatchers
- **DI:** Koin (auto-wired by `kmp.library-koin*`)
- **Local store:** <SQLDelight | Room KMP | none>
- **Remote:** <Ktor | Ktorfit | Firebase | none>
- **MVI base class:** `MviViewModel<State, Intent, Effect>` from `compose-utils` (or add to `:core:common` for KMP `commonMain`)
- **Dispatch intents:** `vm.dispatch(Intent)` · handle in `override suspend fun onIntent(intent)`
- **One-shot effects:** `sendEffect(Effect)` in VM · `vm.collectEffects { }` in Route composable
- **Platform code boundary:** `expect/actual` — commonMain declares, androidMain/iosMain provides

## Project-specific rules

- **`commonMain` first:** write in commonMain unless you need a platform API. Push only the irreducible platform difference behind `expect/actual`.
- <e.g. all time handling via `kotlinx-datetime`, never `java.util.Date`>
- <e.g. analytics events via `UiEvent.TrackEvent` only>

## Build / test

```bash
./gradlew :app:assembleDebug
./gradlew :shared:testDebugUnitTest     # commonMain + androidMain tests
./gradlew allTests                      # all targets

# One-time setup (runs automatically on first preBuild):
./gradlew scaffoldKoltResources # writes theme XMLs to :app/src/main/res/
./gradlew scaffoldKoltDocs      # writes docs/ and CLAUDE.md/AGENTS.md
```

## Done means

Compiles all targets · reducer + use-case tests pass · no `expect` without `actual` · no TODO/stub in prod paths · follows `CODING_STANDARDS.md`
