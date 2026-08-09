# libs/compose-utils — Compose UI Component Library

[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.2.1.dev--00-blue?style=flat-square)](https://central.sonatype.com/artifact/io.github.appspiriment.kolt/compose)
[![Android](https://img.shields.io/badge/Android-only-green?style=flat-square&logo=android)](https://developer.android.com)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2.3.10-4285F4?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange?style=flat-square)](../../LICENSE)
[![Changelog](https://img.shields.io/badge/Changelog-view-lightgrey?style=flat-square)](CHANGELOG.md)

A production-ready Compose UI component library and theme system for Kolt Android apps. Provides 100+ composables, a fully customisable design token system, ViewModel base classes, and a rich set of utility wrappers.

---

## Installation

```kotlin
dependencies {
    implementation("io.github.appspiriment.kolt:compose:0.2.1.dev-00")
    // Kolt convention plugins add this automatically — opt out with `kolt { enableUtils.set(false) }`
}
```

---

## Theme System

The theme is token-based. All design decisions are centralised in `AppspirimentTheme` and accessed via `Kolt.*`:

```kotlin
AppspirimentTheme(
    colors = myLightColors,     // BaseColors
    darkColors = myDarkColors,
    sizes = mySizes,            // Sizes (dp + sp)
    typography = myTypography,  // BaseTextStyles
) {
    // Your app content
}

// Inside any composable
val color = Kolt.colors.primary
val textStyle = Kolt.typography.bodyMedium
val padding = Kolt.sizes.paddingMedium
```

### Design Tokens

| Token class | Accessed via | Examples |
|-------------|-------------|---------|
| `BaseColors` | `Kolt.colors` | `primary`, `onMainSurface`, `successContainer`, `errorContainer` |
| `BaseTextStyles` | `Kolt.typography` | `bodySmall`, `bodyMedium`, `titleLarge`, `headlineSmall` |
| `Sizes` | `Kolt.sizes` | `paddingSmall`, `iconStandard`, `cornerRadiusMedium` |
| `Flags` | `Kolt.flags` | `notoFontPadding`, `isRtl` |

---

## Components

### Text

| Composable | Description |
|-----------|-------------|
| `AppspirimentText(text: String/UiText/AnnotatedString)` | Themed text with font-padding correction |
| `AppsExpandableText` | Truncated text with "Show more / Show less" toggle |
| `AppsCopyableText` | Text with a one-tap copy-to-clipboard action |
| `AppsHighlightText` | Highlights all occurrences of a search query within text |
| `MalayalamText` | Text forced to Noto Sans for Malayalam script rendering |

### Buttons

| Composable | Description |
|-----------|-------------|
| `AppsButton` | Filled button with pressed-state animation |
| `AppsTextButton` | Ghost / text-only button |
| `AppsIconButton` | Square icon-only button |
| `AppsIconTextButton` | Icon + label button |
| `AppsLinkButton` | Underlined inline link |
| `AppsCircularButton` | Round FAB-style button |

All buttons accept a `ButtonStyle` — pre-built factories: `ButtonStyle.primary()`, `.outlined()`, `.transparent()`, `.danger()`.

### Text Fields

| Composable | Description |
|-----------|-------------|
| `AppsValidatedTextField` | `TextFieldState`-based field with validation, error display, counter |
| `AppsPasswordTextField` | Password field with animated visibility toggle |
| `AppsSearchTextField` | Search field with animated clear button |
| `AppsTextField` | Simple unmanaged string field |

Pair with `ValidatedTextFieldState` + `ValidationRules`:

```kotlin
val emailState = remember {
    ValidatedTextFieldState(
        rules = listOf(ValidationRules.required, ValidationRules.emailFormat)
    )
}

AppsValidatedTextField(
    state = emailState,
    label = "Email".toUiText(),
)

// On form submit:
if (emailState.validate()) { /* proceed */ }
```

### Dropdowns

| Composable | Description |
|-----------|-------------|
| `DropDownSpinner` | Base spinner with fully customisable item composable |
| `TextDropDown` | Spinner rendered as a text field |
| `IconDropDown` | Spinner rendered as an icon button |
| `ChipDropDown` | Spinner rendered as a chip |
| `AppsTextDropDown` | Themed text dropdown with animated arrow |
| `AppsIconDropDown` | Themed icon dropdown |

### Images & Avatars

| Composable | Description |
|-----------|-------------|
| `AppsImage` | Coil-powered image with placeholder/error states |
| `AppsIcon` | Icon from `UiImage` (vector, resource, or remote) |
| `AppsAvatar` | Circular avatar with initials fallback |
| `CircleIconBox` | Icon inside a themed circle |

### Feedback & Status

| Composable | Description |
|-----------|-------------|
| `AppsBanner` | Inline alert banner (Info / Success / Warning / Error / Neutral) |
| `AppsSnackbarHost` | Themed snackbar with icon and semantic colour variants |
| `AsyncStateBox` | Renders loading / error / empty / content based on `AsyncState<T>` |
| `AppsEmptyState` | Full empty-state with illustration, title, message, CTA |
| `ShimmerBox` | Animated shimmer placeholder skeleton |
| `AppsProgressIndicator` | Circular/linear progress with theme colours |
| `AppsStatusTag` | Coloured pill tag for status labels |
| `AppsBadge` | Notification count badge with overflow cap |

### Containers & Scaffolding

| Composable | Description |
|-----------|-------------|
| `PageScaffold` | Standard screen scaffold with top bar + snackbar host |
| `AppsTopBar` | Themed `TopAppBar` with back button and action slots |
| `AppsBottomNavigation` | Bottom navigation bar wired to a `NavHost` |
| `AppsDrawerScaffold` | Navigation drawer scaffold |
| `TitledCardView` | Card with an optional title header |
| `SwipeableActionsBox` | Swipe-to-reveal action row (delete, archive, etc.) |
| `PullToRefreshBox` | Pull-to-refresh wrapper |
| `AppsBottomSheet` | Themed `ModalBottomSheet` |

### Utilities & Modifiers

```kotlin
// Modifier extensions
Modifier.conditional(isSelected) { background(color) }
Modifier.clickableIf(enabled) { onClick() }
Modifier.visibleIf(isVisible)
Modifier.goneIf(isGone)           // removes from layout, preserves state
Modifier.dashedBorder(color = red)
Modifier.coloredShadow(color = shadow)
Modifier.fadingEdge(FadeEdge.Bottom)
Modifier.bounceClick { onClick() }
Modifier.shake(errorTrigger)
Modifier.mirrorForRtl()
```

```kotlin
// Compose state helpers
rememberMutableStateOf(initial)
produceUiState { repository.data() }
```

```kotlin
// Inset helpers
WindowInsetsUtils.ime              // IME insets as State<Dp>
WindowInsetsUtils.statusBarHeight
```

### ViewModel Base Classes

```kotlin
// Stateless — just UI effects
class MyViewModel : UiEventsViewModel<MyEffect>() {
    fun doSomething() = sendUiEffect(MyEffect.ShowToast("Done"))
}

// Stateful — state + effects
class MyViewModel : UiStateEventsViewModel<MyState, MyEffect>(MyState()) {
    fun load() = launchIO {
        updateUiState { it.copy(loading = true) }
        val data = repository.fetch()
        updateUiState { it.copy(loading = false, data = data) }
    }
}

// Same but extends AndroidViewModel (for Application context)
class MyViewModel(app: Application) : UiEventsAndroidViewModel<MyEffect>(app)
```

---

## Wrappers

| Wrapper | Description |
|---------|-------------|
| `UiText` | Sealed type: `DynamicString`, `StringResource`, `AnnotatedStringBased` — safely pass text across ViewModel → UI boundary |
| `UiImage` | Sealed type: `VectorImage`, `ResourceImage`, `RemoteImage` — unified image source |
| `UiColor` | Sealed type for theme-agnostic color references |
| `FieldError` | Icon + color config for inline validation error rows |

```kotlin
// UiText
UiText.DynamicString("Hello")
UiText.StringResource(R.string.greeting, "David Lee Kolt")
"Hello".toUiText()

// UiImage
Icons.Default.Search.toUiImage()
R.drawable.ic_logo.toUiImage()
"https://example.com/photo.jpg".toUiImage()
```

---

## InputTransformations

Pre-built `InputTransformation` objects for `TextFieldState`-based fields:

```kotlin
AppsValidatedTextField(
    state = state,
    inputTransformation = InputTransformations.digitsOnly
        .then(InputTransformation.maxLength(10)),
)
```

| Transformation | Effect |
|---------------|--------|
| `InputTransformations.digitsOnly` | Strips non-digit characters |
| `InputTransformations.digitsAndDecimal` | Allows digits + one decimal point |
| `InputTransformations.lettersOnly` | Strips non-letter characters |
| `InputTransformations.alphanumericOnly` | Letters and digits only |
| `InputTransformations.alphanumericWithSpaces` | Letters, digits, and spaces |
| `InputTransformations.noWhitespace` | Removes all whitespace |
| `InputTransformations.upperCase` | Converts to uppercase |
| `InputTransformations.lowerCase` | Converts to lowercase |
