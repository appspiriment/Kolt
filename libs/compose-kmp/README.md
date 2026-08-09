# libs/compose-kmp — Compose Multiplatform UI

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.1.dev--00-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/compose-kmp)
[![KMP](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop-7F52FF?style=flat-square&logo=kotlin)](https://kotlinlang.org/docs/multiplatform.html)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

Core Compose Multiplatform UI components and design-system tokens (`commonMain`, real `expect`/`actual` per platform — Android, iOS, Desktop). This is the shared foundation [`libs/compose-utils`](../compose-utils) (Android-only) builds on top of.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:compose-kmp:0.2.1.dev-00")
}
```

---

## Theming

Wrap your app root in a `CompositionBaseProvider`, then read tokens off `Kolt`:

```kotlin
CompositionBaseProvider(isDarkTheme = isSystemInDarkTheme(), font = AppFont.Roboto) {
    Text("Hello", color = Kolt.colors.onBackground, style = Kolt.typography.textMedium)
}
```

`Kolt.colors` / `Kolt.sizes` / `Kolt.typography` / `Kolt.flags` are `CompositionLocal`-backed — no hardcoded colors, dimens, or fonts outside this module (see the root steering docs' `theming.md`).

---

## Component inventory

| Package | Contents |
|---|---|
| `components/core/buttons` | `AppsButton`, `AppsOutlinedButton`, `AppsTextButton`, `AppsTonalButton`, `AppsLinkButton`, `AppsIosButton`, `AppsIconButton`, `AppsIconTextButton`, `AppsImageButton`, `AppsCircularButton` |
| `components/core/text` | `AppspirimentText`, `AppsExpandableText`, `AppsHighlightText`, `AppsCopyableText`, `AppsImageText`, `KeyValuePairText`, `PrefixedText` |
| `components/core/text/textfield` | `AppsValidatedTextField` (stateful, `ValidatedTextFieldState`), `AppsPasswordTextField`, `AppsSearchTextField`, `InputTransformations` |
| `components/core/dropdowns` | `AppsDropDown`, `AppsTextDropDown`, `AppsIconDropDown`, `ChipDropDown`, `DropDownSpinner`, `TextDropDown`, `IconDropDown` |
| `components/core/messages` | `MessageDialog`, `AppsBanner`, `AppsSnackbar`, `AppsBottomSheet` |
| `components/core/image` | `AppsImage`, `AppsAvatar`, `AppsIcon`, `CircleIconBox` |
| `components/core/progress` | `AppsProgressIndicator`, `FullscreenLoader` |
| `components/core/selectors` | `AppsSelectableText` |
| `components/core/modifiers`, `components/modifiers` | `CircleBackground`, `ModifierExtensions`, `ShimmerEffect` |
| `components/core` (misc) | `AppsAccordion`, `AppsDivider`, `AppsEmptyState`, `AppsRatingBar`, `AppsSlider`, `AppsStatusTag`, `AppsStepper`, `AppsTooltip`, `AsyncStateBox`, `ShimmerBox`, `Spacers` |
| `components/containers` | `PageScaffold`, `AppsTopBar`, `AppsDrawerScaffold`, `PullToRefreshBox`, `TitledCardView`, `bottomnavigation/AppsBottomNavigation` |
| `components/containers/swipeactionbox` | `SwipeableActionsBox` and its `SwipeAction`/`ActionFinder`/`SwipeableActionsState` support |
| `wrappers` | `UiText`, `UiColor`, `UiDimen`, `UiImage`, `FieldError` — platform-agnostic wrappers for string resources, colors, dimens, and images across Android/iOS/Desktop |

`AsyncStateBox` requires `AsyncState` from [`libs/utils`](../utils), which this module depends on directly.

---

## Not ported here

Anything built on `androidx.navigation` (`NavHost`, `NavGraphBuilder`, nav-scoped `AnimatedContent`) stays Android-only in `compose-utils` — this repo's steering standardizes on Navigation 3's caller-owned back stack instead of the old Navigation-Compose API, so those components weren't force-abstracted into commonMain.
