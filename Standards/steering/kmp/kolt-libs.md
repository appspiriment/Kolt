# Kolt/libs — what's reusable and what isn't (optional)

**This whole file only applies if `Kolt/libs` is actually present in the
workspace.** Nothing elsewhere in this steering set requires it — every
pattern it documents (theme module, `AsyncState`, `MviViewModel`) is defined
standalone in [theming.md](theming.md) / [presentation-mvi.md](presentation-mvi.md)
and works with zero Kolt dependency. That's the only thing optional here —
*whether Kolt/libs exists in this workspace at all.* Once it's present, using
what's in [Reuse as-is](#reuse-as-is-mandatory-when-present) below is not
optional: don't write a new `AppsButton`, a new theme module, a new
`AsyncState`, etc. once a vetted one already exists a few files over — that's
exactly the duplication [architecture.md](architecture.md#reuse-over-duplication)
forbids. "Evaluated on its actual merits" still applies in full, though:
mandatory means *use the parts that passed the check below*, not *trust
everything because it's sitting in the repo* — the `Don't reuse`/`Dead code`
sections are exclusions from that mandate, not suggestions.

`Kolt/libs` lives at `/Users/arunshankar/Projects/KMP/Kolt/libs`. It's marketed
as a KMP util collection, but not every module in it actually is KMP. Checked
each module's source-set layout and dependencies before recommending
anything — don't assume "it's in the KMP repo" means "it works on iOS."

## How to consume it — ask once, at project creation

Before wiring the first Kolt dependency into a new project, check whether
consumption is already decided: look for an `includeBuild(...)` pointing at
`Kolt/libs` in `settings.gradle.kts`, a copied module directory, or an
`io.appspiriment` coordinate in the version catalog. If none of those exist
yet, **ask the user which mode to use — always, exactly once per project,
never silently default:**

1. **Composite build / direct link from disk** — `includeBuild("/Users/arunshankar/Projects/KMP/Kolt/libs")` in `settings.gradle.kts`. Not a copy: a live reference to the same files on this machine, edits in Kolt show up immediately with no publish step. Only works on this machine, at this path.
2. **Copy the module(s) in** — vendor the source into this project's tree. Portable across machines/CI, but you own drift from the source from that point on; no free updates.
3. **Gradle dependency on a published artifact** — `implementation("io.github.appspiriment.kolt:<module>:<version>")` (note: `compose-utils` is published under artifact ID `compose`, i.e. `io.github.appspiriment.kolt:compose:<version>`). Requires Kolt actually published (Maven local or remote) under that coordinate. Most portable/CI-friendly option, if publishing is set up.

Once answered, treat it as decided for the life of the project — don't
re-ask on later tasks; re-check the project state above instead.

## Reuse as-is (mandatory when present)

If `Kolt/libs` is in the workspace, use these — don't hand-write a new
equivalent and don't treat this as a "pick whichever you feel like" menu.

| Module | Path | Why it's safe |
|---|---|---|
| `compose-kmp` theme | `compose-kmp/src/{commonMain,androidMain,iosMain,desktopMain}/.../theme/` | Genuine `expect`/`actual` KMP. `Kolt` object + `LocalColors`/`LocalTypography`/`LocalSizes` + `CompositionBaseProvider` is exactly the pattern in [theming.md](theming.md). |
| `compose-kmp` component library | `compose-kmp/src/commonMain/.../components/` — buttons, text/text fields, containers (card, accordion, tooltip, divider), messages (snackbar/banner/dialog), image, badges, progress, rating bar, slider, stepper, shimmer | `commonMain`-first; only `image/` drops to `androidMain`/`iosMain`/`desktopMain` `actual` (platform image loading — Coil on Android). This is the reusable-component target from [architecture.md](architecture.md#reuse-over-duplication) already built — check here before writing a new `AppsButton`/`AppsCard`/etc. |
| `utils` → `AsyncState` | `utils/src/commonMain/.../state/AsyncState.kt` | Plain `commonMain` sealed class (`Idle`/`Loading`/`Success`/`Error`) with `map`/`onSuccess`/`getOrElse`. Clean, no platform deps. Use it in `State` fields per [presentation-mvi.md](presentation-mvi.md). |
| `logutils` | `logutils/` | Real `commonMain`/`androidMain`/`desktopMain`/`nativeMain` KMP logging, auto debug/release gating on Android via App Startup. Fine to use as-is. |
| `location` | `location/` | Real KMP (`commonMain` + per-platform `actual`, including `wasmJs` via a hand-written `navigator.geolocation` binding — `kotlinx-browser` doesn't cover that API). Fine if a journey needs location. |
| `location-picker` | `location-picker/` | Real KMP (`commonMain` + android/ios/desktop/wasmJs `actual`). All-in-one Search/Map/Current-location/Manual-entry picker UI built on `location` — `LocationPicker.rememberLauncher`/`.present`/`.showDialog`/`.Embed` per platform. Fine to consume as a black-box UI dependency for a "pick a location" screen. |

`location-picker` follows this steering set on Android/iOS/Desktop:
`LocationPickerViewModel` extends the `MviViewModel` base from
[presentation-mvi.md](presentation-mvi.md); `State`/`Intent`/`Effect` live in
their own `LocationPickerMviContract.kt`; `LocationPickerScreenContent` is a
stateless renderer (`LocationPickerScreen` is the composable that owns the
ViewModel and collects its effects, the Route-equivalent for a module with no
Navigation-3 back stack); `State.placeResults` is `ImmutableList`; those three
platforms' UI reuses the `compose-kmp` theme/components above instead of a
hand-rolled theme; and `LocationPickerViewModel` has `commonTest` coverage
(`LocationPickerViewModelTest`, via a `LocationPickerGateway` fake seam —
`libs/location`'s `CurrentLocationProvider`/`searchPlaces`/etc. aren't
fakeable on their own).

**Android permission/services handling is self-contained** — `location-picker`
gates its own "use current location" trigger through
`rememberLocationAccessGate` (Android `actual` only, no expect/actual
counterpart needed): permission rationale and "permanently denied → open app
settings" are each a `compose-kmp` `AppsBottomSheet` (not a dialog), with
every title/message/button label configurable via `LocationPickerConfig`'s
`locationPermissionRationale*`/`locationPermissionSettings*` fields. A
consuming app does **not** need to request
`ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION` itself before using this
module on Android — that's the one case where `libs/location`'s own doc
("Android: assumes the caller has already requested the permission") doesn't
apply, because `location-picker` is that caller. iOS/Desktop don't need this:
iOS's `CurrentLocationProvider` requests its own `CLLocationManager`
authorization, and Desktop has no permission/services concept.

Once permission is granted, "location services off" is resolved via
`libs/location`'s own `rememberLocationSettingsResolver` — a Google Play
Services `SettingsClient` check that, in the common resolvable case, shows the
system's in-app "Turn on location" dialog directly (no bottomsheet of
`location-picker`'s own, no navigation out to the Settings app). This
Composable lives in `libs/location` itself (not `location-picker`), Android
`androidMain` only, alongside `isLocationServicesEnabled` — the two are
complementary: `isLocationServicesEnabled` is a synchronous, no-`Activity`
check (used in `CurrentLocationProvider`'s own suspend fast-fail path);
`rememberLocationSettingsResolver` is the Compose/`Activity`-launcher-backed
"and let the user turn it on in-app" step. `LocationPickerConfig`'s
`locationServicesDisabled*` fields still exist, but now back only the rare
fallback bottomsheet for a device Play Services can't resolve automatically —
not the common path.

**One legitimate, documented exception**: `compose-kmp` has no `wasmJs`
target, and a `commonMain` dependency must resolve for every enabled target —
so `LocationPickerScreenContent`'s Web `actual` is a separately-maintained,
plain-Material3 duplicate of the android/ios/desktop one, not the same
`compose-kmp`-based code shared via `commonMain`. This is exactly the kind of
per-module platform gap this doc already tracks elsewhere (`location`'s own
`wasmJs` Geolocation binding) — not a reason to avoid the module, just don't
expect Web's rendering code to be the same file as the other three. Its
`commonTest` suite also doesn't run under `wasmJsTest` (a Skiko/webpack
test-bundling gap in the browser test runner, unrelated to the tests
themselves — they pass on Android and Desktop, which compile and run the
exact same `commonTest` source).

**Don't import `compose-utils`' `AppsBanner`/`AppsBottomSheet`/`AppsSnackbar`/
`DialogButtonStyle`/`MessageDialog`/`ColorUtils`** even though those
filenames exist there too — they're 10-line backward-compat `typealias`
stubs pointing at the `compose-kmp` versions above (each file says so in a
header comment: "Moved to compose-kmp. This file is a backward-compatibility
stub."). Import from `compose-kmp` directly.

## Don't reuse — Android-only despite living here

| Module | Path | Problem |
|---|---|---|
| `compose-utils` ViewModel bases | `compose-utils/.../utils/base/UiStateEventsViewModel.kt`, `UiStateEventsAndroidViewModel.kt`, `UiEventsViewModel.kt`, `UiEventsAndroidViewModel.kt` | Module has no `commonMain` at all — everything is under `src/main/java`. `UiStateEventsAndroidViewModel` extends `android.app.AndroidViewModel`. Building a real KMP presentation layer on these breaks the moment iOS is added. Use the `commonMain` `MviViewModel` in [presentation-mvi.md](presentation-mvi.md) instead. |
| `compose-utils` nav helpers | `AppsBottomNavigationNavHost.kt`, `AppsNavType.kt` | `AppsNavType.genericNavType` imports `android.net.Uri` directly. The module's `build.gradle.kts` pulls the Android `navigation-compose` artifact via an Android-only convention plugin, not the KMP coordinate. Not usable outside Android — and independent of that, it's built on Navigation 2 (`NavController`/`NavGraphBuilder`), which [navigation.md](navigation.md) has this project moving off in favor of Navigation 3. |
| `utils` → `FlowUtils.collectState` / `collectFlows` | `utils/src/commonMain/.../extensions/FlowUtils.kt` | *Is* commonMain, but the pattern itself is one to avoid: callback-listener style predating structured concurrency (`successListener`/`errorListener`/`onLoading` triads), and `collect()` bakes in a hardcoded `delay(200)` after every emission — every consumer inherits 200ms of invisible latency. Use `stateIn` + `collectAsStateWithLifecycle` instead. |
| `compose-utils` remaining components | dropdowns, `AppsTextField`/`AppsValidatedTextField`, `PageScaffold`, `AppsTopBar`, `AppsDrawerScaffold`, swipe-actions box, `Lottie`, `RememberSpeechToText`, photo picker | Genuinely useful, but Android-only (`src/main/java`, no `commonMain`) and *not* migrated to `compose-kmp` like the messages/ components were — no compat stub, no KMP equivalent exists yet. Fine to reuse only on a screen that will never ship iOS/desktop. Otherwise this is exactly the "extract once you need it" case from [architecture.md](architecture.md#reuse-over-duplication) — port the specific component you need into the shared KMP component module, don't block on porting all of them upfront. |

## Dead code — don't copy the pattern

`compose-utils/.../base/ViewModelDelegate.kt` is an `internal class` meant to
de-duplicate `UiStateEventsViewModel` and `UiStateEventsAndroidViewModel` (its
own doc comment says so). Nothing in the repo constructs it — grep confirms
zero usages. `UiStateEventsAndroidViewModel` reimplements the same
state/effect-channel logic by hand instead of delegating to it. Net effect:
two divergent copies of the same logic plus one unused third copy. Don't
carry this inconsistency into a new project; the `MviViewModel` in
[presentation-mvi.md](presentation-mvi.md) is the single version to use.

## Not evaluated

`update-utils`, `bom` — out of scope for this steering set (app-update flow
and Maven BOM packaging, not presentation/theme/navigation). Check them on
their own merits if/when a journey needs in-app update prompts.
