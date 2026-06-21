# UtilsLibs — Claude Code Context

**Read first:** [`build-logic/conventions/src/steering-templates/docs/CODING_STANDARDS.md`](build-logic/conventions/src/steering-templates/docs/CODING_STANDARDS.md) — binding rules (Clean + MVI + SOLID + stateless Compose). Follow exactly.

**Read on demand:**
- [`build-logic/conventions/src/steering-templates/docs/ARCHITECTURE.md`](build-logic/conventions/src/steering-templates/docs/ARCHITECTURE.md) — layering, DI, offline-first, abstractions catalog.
- [`build-logic/conventions/src/steering-templates/docs/TESTING.md`](build-logic/conventions/src/steering-templates/docs/TESTING.md) — test patterns, fakes, coverage targets.
- [`build-logic/conventions/src/steering-templates/docs/KOLT.md`](build-logic/conventions/src/steering-templates/docs/KOLT.md) — plugin & library API reference; also the consumer-facing guide agents read in downstream projects.
- [`AGENTS.md`](AGENTS.md) — **full UtilsLibs architecture guide**: composite build layout, plugin internals, catalog system, library APIs, publishing workflow, task reference. Read when working on anything in this repo.

## Project facts

- **What it is:** Appspiriment convention plugins + runtime utility libraries (Android + KMP)
- **Targets:** Plugin JAR (JVM) + Android libraries + KMP libraries
- **Current plugin version:** `0.1.6` (`PLUGIN_MAJOR=0.1.6`, `PLUGIN_DEV=0` in `version.properties`)
- **Repo layout:**
  - `build-logic/conventions/` — all convention plugins (included build, applies from source)
  - `libs/logutils/` — KMP logging (commonMain + androidMain)
  - `libs/utils/` — KMP utilities (commonMain + androidMain)
  - `libs/compose-utils/` — Android Compose components, theme, wrappers, MVI ViewModel base
  - `libs/update-utils/` — Android update dialog (Firebase Remote Config)
- **Per-artifact versions:** `version.properties` → `PLUGIN_MAJOR=0.1.1 PLUGIN_DEV=1` → `0.1.1.dev-01` (dev) / `0.1.1` (release with `-PisRelease`)
- **No publish round-trip:** convention plugins apply from source via `includeBuild("build-logic")`
- **Steering docs source:** `build-logic/conventions/src/steering-templates/` — edit here, ship with plugin
- **MVI base (compose-utils):** `MviViewModel<State, Intent, Effect>` — `dispatch(intent)` entry, `onIntent()` handler, `updateState {}`, `sendEffect()`, `collectEffects {}` in Route

## Build / test

```bash
./gradlew build                                        # full build (all libs + plugin)
./gradlew :build-logic:conventions:test                # plugin TestKit tests
./gradlew :libs:compose-utils:assembleDebug            # single lib
./gradlew bumpDevVersion                               # increment DEV counter
./gradlew :build-logic:conventions:publishToMavenLocal # local test publish
```

## Done means

Compiles · plugin tests pass · no unresolved references in libs · `docs/` steering files accurate · follows `CODING_STANDARDS.md`
