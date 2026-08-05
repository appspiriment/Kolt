# Android + Compose Agent Steering

This is the **Android-only** steering set — the current project has no
`commonMain`/`iosMain`/`kotlin("multiplatform")` plugin, just Android
Gradle modules. If the project is Kotlin Multiplatform, stop and read
`steering/kmp/AGENTS.md` instead — see the root `AGENTS.md` for the
detection rule.

Read this before generating any code here. These rules are mandatory, not
suggestions — an AI agent (Claude, Gemini, or otherwise) must follow them by
default without being re-asked per task.

## Load steering files on demand, not upfront

The tl;dr below is enough to keep code compliant in general. Don't read any
file in this directory speculatively or "just in case" — each one is only
worth its tokens right before you touch the area it covers, and skip it
entirely otherwise:

| Read this file... | ...right before you | Skip if |
|---|---|---|
| [architecture.md](architecture.md) | create/restructure a module, place a class in `domain`/`data`/`presentation`, write a UseCase/Repository/DataSource, wire DI, or you're about to write a second/third copy of some logic | you're only editing inside an existing correctly-placed file |
| [presentation-mvi.md](presentation-mvi.md) | write/edit a `ViewModel`, `State`, `Intent`, or `Effect`, or extract ViewModel logic into a delegate | the change is UI-only inside an already-compliant screen |
| [navigation.md](navigation.md) | add a route/screen, touch `NavDisplay`/back stack/deep links, or the question is Activity-per-journey vs single-Activity | navigation isn't part of the task |
| [theming.md](theming.md) | add a color/dimension/font/user-facing string/token, or touch the theme module | you're just referencing `AppTheme.colors.x` that already exists |
| [kolt-libs.md](kolt-libs.md) | about to add a dependency or write a new util/component, **and** `Kolt/libs` exists in this workspace — check first, its vetted pieces are mandatory to reuse | `Kolt/libs` isn't present — the rest of this steering set doesn't need it |
| [testing.md](testing.md) | write a unit test for a UseCase/ViewModel, or reach for a mocking library | the task has no test-writing in it |

If this file was reached through a `.standards` symlink in a consuming app
repo (see the root `AGENTS.md`'s `scripts/link-standards.sh`), the table
links above (`kolt-libs.md`, `architecture.md`, etc.) resolve to
`.standards/<file>.md` from that app's root, not `<file>.md` — the symlinked
folder, not the app root, is what they're sibling to.

## Non-negotiables (tl;dr)

1. Clean architecture, strict call chain, no skipped layers: `ViewModel → UseCase → Repository (interface, domain) → RepositoryImpl → DataSource → originator (Retrofit/Room/DataStore)`. `UseCase` depends only on `Repository` interfaces (any number); `RepositoryImpl` depends only on its `DataSource`(s), never a network client/DAO/DataStore directly. Domain has zero Android/Compose imports. Platform exceptions (Retrofit/Room/etc.) get mapped to a shared `DomainException` at the `DataSource` boundary — never caught raw above it.
2. No same-layer dependencies: `UseCase` never depends on another `UseCase`; `RepositoryImpl` never depends on another `Repository`; `DataSource` never depends on another `DataSource`. Shared logic between peers is a plain, non-`UseCase`/non-`Repository`-suffixed function or class. See [architecture.md](architecture.md#data-layer-repository--datasource--originator).
3. MVI: `State` (data class), `Intent` (sealed interface), `Effect` (sealed interface, one-shot via `Channel`), all three together in one `Contract.kt`.
4. One package per screen under `presentation/` (e.g. `presentation/list/`, `presentation/detail/`), holding that screen's `Contract.kt`, `ViewModel`, `Route`, and `Screen` — never a flat `presentation/` shared by every screen in the feature.
5. Navigation is **Navigation 3** (`androidx.navigation3`, stable since Nov 2025) — not the old `androidx.navigation:navigation-compose`. Caller-owned back stack (`NavKey` list), not a `NavController`.
6. ViewModel is instantiated inside the composable named in `entryProvider` for its route (the "Route" composable), obtained via `hiltViewModel()` — never inside the stateless screen composable, never above the back stack.
7. The Route composable is the **only** collector of the effect `Flow`. It resolves nav effects itself (`backStack.add/removeLast`) and forwards non-nav effects (snackbar text, etc.) to the screen as plain lambdas/state — never as a raw `Flow`. Note: current official Android guidance argues against `Channel`-based effects entirely, in favor of modeling events as `State`; this project keeps `Channel` deliberately — see [presentation-mvi.md](presentation-mvi.md#known-deviation-from-current-official-guidance-channel-based-effects).
8. The stateless root composable receives only `state`, `onIntent: (Intent) -> Unit`, and resolved navigation lambdas. It never sees a `ViewModel`, a back stack, or an effect `Flow`. It must be `@Preview`-able with fake data alone.
9. Any `List`/`Map`/`Set` in `State` is `ImmutableList`/`ImmutableMap`/`ImmutableSet` (`kotlinx.collections.immutable`), never the plain `kotlin.collections` type — Compose treats plain collections as unstable and over-recomposes.
10. Single Activity, one back stack/`NavDisplay` for the whole app. Feature modules contribute routes and `entryProvider` fragments, not their own `NavDisplay`. Splitting a journey into its own Activity is opt-in, for a concrete reason only (SDK-style embedding, own process/task, deep-link-only entry) — see [navigation.md](navigation.md).
11. Theme lives in its own module, exposed via `CompositionLocal`. No hardcoded colors/dimens/fonts/user-facing strings (use `stringResource`) outside their respective modules.
12. Accessibility is required, not optional polish: `contentDescription` on every meaningful image/icon (explicit `null` if decorative), ≥48.dp touch targets, no layout that breaks at large system font scales.
13. `Kolt/libs` is optional — but only whether it's *present*. If it is, using its vetted pieces (see [kolt-libs.md](kolt-libs.md)'s "Reuse as-is") is mandatory: don't write a new theme module/`AsyncState`/UI component that already exists there. On an Android-only project, most of Kolt is usable (including `compose-utils`, which the KMP set restricts) — the doc says what to still skip and why. Everything else in this steering set is self-contained and works without Kolt.
14. Extract a delegate/util/component once real duplication shows up (rule of three — two if it's error-prone) — not speculatively, and not left unused after extraction. See [architecture.md](architecture.md#reuse-over-duplication).
15. Unit tests for `domain`/`presentation` are plain JVM local unit tests (JUnit4/5) — no Robolectric/instrumentation unless the test genuinely needs a real Android framework class. Fakes for small interfaces, MockK for large/volatile ones. See [testing.md](testing.md).
16. **DI is Hilt**, not Koin — no multiplatform constraint here, and Hilt is the officially recommended choice for Android apps of this shape.
