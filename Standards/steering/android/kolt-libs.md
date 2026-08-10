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
forbids.

`Kolt/libs` lives at `/Users/arunshankar/Projects/KMP/Kolt/libs`. It's
KMP-authored, but that's a non-issue for an Android-only project — a
multiplatform Gradle module is a perfectly normal Android dependency, you
just never touch its `iosMain`/`desktopMain` source sets. **Because this
project has no non-Android target, almost everything in Kolt is usable here**
— including the `compose-utils` module, which the KMP steering set excludes
for being "Android-only despite living in a KMP repo." Here, Android-only is
exactly the target, so that objection doesn't apply. Checked each module on
its own merits below anyway — being usable isn't the same as being good.

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
| `compose-kmp` theme | `compose-kmp/src/{commonMain,androidMain}/.../theme/` | `Kolt` object + `LocalColors`/`LocalTypography`/`LocalSizes` + `CompositionBaseProvider` is exactly the pattern in [theming.md](theming.md). You only need the `commonMain` + `androidMain` pieces. |
| `compose-kmp` component library | `compose-kmp/src/commonMain/.../components/` — buttons, text/text fields, containers (card, accordion, tooltip, divider), messages (snackbar/banner/dialog), image, badges, progress, rating bar, slider, stepper, shimmer | The reusable-component target from [architecture.md](architecture.md#reuse-over-duplication) already built — check here before writing a new `AppsButton`/`AppsCard`/etc. |
| `compose-utils` components | dropdowns, `AppsTextField`/`AppsValidatedTextField`, `PageScaffold`, `AppsTopBar`, `AppsDrawerScaffold`, swipe-actions box, `Lottie`, `RememberSpeechToText`, photo picker | Genuinely Android-only, which is exactly this project's target — no mismatch. Same "check before writing a new one" rule as the `compose-kmp` set above. |
| `utils` → `AsyncState` | `utils/src/commonMain/.../state/AsyncState.kt` | Plain sealed class (`Idle`/`Loading`/`Success`/`Error`) with `map`/`onSuccess`/`getOrElse`. Use it in `State` fields per [presentation-mvi.md](presentation-mvi.md). |
| `logutils` | `logutils/` | Auto debug/release gating on Android via App Startup. Fine to use as-is. |
| `location` | `location/` | Fine if a journey needs location. |
| `location-picker` | `location-picker/` | All-in-one Search/Map/Current-location/Manual-entry picker UI built on `location` — `LocationPickerActivity` + `LocationPickerContract` for `registerForActivityResult`, or `LocationPicker.rememberLauncher` from Compose. Fine to consume as a black-box UI dependency for a "pick a location" screen. |

On Android, `location-picker` follows this steering set:
`LocationPickerViewModel` extends the `MviViewModel` base from
[presentation-mvi.md](presentation-mvi.md); `State`/`Intent`/`Effect` live in
their own `LocationPickerMviContract.kt`; `LocationPickerScreenContent` is a
stateless renderer owned by `LocationPickerScreen`, which collects its
effects; its list field is `ImmutableList`; its UI reuses `compose-kmp`'s
theme/components instead of a hand-rolled theme; and `LocationPickerViewModel`
has test coverage (`LocationPickerViewModelTest` in `commonTest`, via a
`LocationPickerGateway` fake seam wrapping `libs/location`'s otherwise
unfakeable `CurrentLocationProvider`/`searchPlaces`/etc.). Safe to treat as a
reference example here, not just a black box.

It also handles its own runtime-permission flow — `rememberLocationAccessGate`
gates "use current location" behind a permission check, showing a
`compose-kmp` `AppsBottomSheet` rationale (not a dialog), falling back to an
"open app settings" bottomsheet if permanently denied. Once permission is
granted, it resolves "location services off" via `libs/location`'s own
`rememberLocationSettingsResolver` — a Google Play Services `SettingsClient`
check that, in the common case, shows the system's in-app "Turn on location"
dialog directly (no bottomsheet, no navigation out to Settings); only a
device Play Services can't resolve automatically falls back to a bottomsheet
deep-linking to the device's location settings. Every title/message/button
label across all of this is configurable via `LocationPickerConfig`. You do
**not** need to request `ACCESS_FINE_LOCATION`/`ACCESS_COARSE_LOCATION`
yourself before using this module — it's the one caller `libs/location`'s own
doc defers that responsibility to.

**Don't import `compose-utils`' `AppsBanner`/`AppsBottomSheet`/`AppsSnackbar`/
`DialogButtonStyle`/`MessageDialog`/`ColorUtils`** even though those
filenames exist there too — they're 10-line backward-compat `typealias`
stubs pointing at the `compose-kmp` versions above (each file says so in a
header comment: "Moved to compose-kmp. This file is a backward-compatibility
stub."). Import from `compose-kmp` directly.

## Don't reuse

| Module | Path | Problem |
|---|---|---|
| `compose-utils` ViewModel bases | `compose-utils/.../utils/base/UiStateEventsViewModel.kt`, `UiStateEventsAndroidViewModel.kt`, `UiEventsViewModel.kt`, `UiEventsAndroidViewModel.kt` | Not excluded for being Android-only (that's fine here) — excluded because `UiStateEventsAndroidViewModel`/`UiEventsAndroidViewModel` extend `android.app.AndroidViewModel`, which current official guidance says to avoid (inject `Application`/context via DI instead), and the naming is confusing (`UiEventType` is used for one-shot *effects*, reads like *events*). Use the `MviViewModel` in [presentation-mvi.md](presentation-mvi.md) instead — same shape, cleaner naming, no `AndroidViewModel`. |
| `compose-utils` nav helpers | `AppsBottomNavigationNavHost.kt`, `AppsNavType.kt` | Built on Navigation 2 (`NavController`/`NavGraphBuilder`), which [navigation.md](navigation.md) has this project moving off in favor of Navigation 3. Not an Android-only-vs-KMP issue — Nav3 supersedes Nav2 for every target. |
| `utils` → `FlowUtils.collectState` / `collectFlows` | `utils/src/commonMain/.../extensions/FlowUtils.kt` | Callback-listener style predating structured concurrency (`successListener`/`errorListener`/`onLoading` triads), and `collect()` bakes in a hardcoded `delay(200)` after every emission — every consumer inherits 200ms of invisible latency. Use `stateIn` + `collectAsStateWithLifecycle` instead. |

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
their own merits if/when a journey needs in-app update prompts. `update-utils`
in particular is worth a look for an Android-only project specifically — it's
solving an Android Play-Store-update problem that doesn't apply to iOS at
all, so the KMP steering set never had reason to flag it either way.
