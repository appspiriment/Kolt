# Theme Module

Self-contained — the shape below works with zero Kolt dependency, so if
`Kolt/libs` isn't in the workspace, implement the contract directly. **If it
is present, use it instead of writing this module from scratch** —
`compose-kmp/src/*/theme/` (`Theme.kt`, `Color.kt`, `Dimens.kt`, `AppFont.kt`,
`TextStyles.kt`) already implements this exact pattern, and per
[kolt-libs.md](kolt-libs.md) it's a vetted "reuse as-is" — not optional once
it's there. It's KMP-authored (real `expect`/`actual` across `commonMain`/
`androidMain`/`iosMain`/`desktopMain`), which is irrelevant baggage for an
Android-only app but not a problem — it compiles and runs as a normal
dependency, you just never touch the non-`androidMain` source sets.

## Shape

No `expect`/`actual` needed here — this is a single-platform module, so a
plain `object`/interface set is enough. `CompositionLocal` is still the
right mechanism (testable overrides, no global mutable state, scoped
overrides for one screen without touching every call site):

```kotlin
// :theme module
val LocalColors = staticCompositionLocalOf<BaseColors> { error("No colors provided") }
val LocalTypography = staticCompositionLocalOf<BaseTextStyles> { error("No typography provided") }
val LocalSizes = staticCompositionLocalOf<Sizes> { error("No sizes provided") }

object AppTheme {
    val colors: BaseColors @Composable @ReadOnlyComposable get() = LocalColors.current
    val typography: BaseTextStyles @Composable @ReadOnlyComposable get() = LocalTypography.current
    val sizes: Sizes @Composable @ReadOnlyComposable get() = LocalSizes.current
}

@Composable
fun AppThemeProvider(
    isDarkTheme: Boolean? = null,   // null = follow system
    content: @Composable () -> Unit,
) {
    val darkTheme = isDarkTheme ?: isSystemInDarkTheme()
    val colors = if (darkTheme) DarkColors else LightColors
    CompositionLocalProvider(LocalColors provides colors, LocalTypography provides AppTypography, LocalSizes provides AppSizes) {
        MaterialTheme(colorScheme = colors.toMaterialColorScheme(), typography = AppTypography.toMaterialTypography()) {
            content()
        }
    }
}
```

If using Kolt's version instead, `Theme.android.kt` is the worked example
(dark/light resolution, font loading) — just note it's the `actual` for one
platform among several; you only need what's in that file, not the
`iosMain`/`desktopMain` siblings.

## Material 3 Expressive

Current Material3 (`developer.android.com/develop/ui/compose/designsystems/material3`,
updated June 2026) adds `MaterialExpressiveTheme` and a `motionScheme`
parameter (`MotionScheme.expressive()` / `.standard()`) alongside the usual
`colorScheme`/`typography`/`shapes`. Treat `motionScheme` as another token
this module owns — expose it through the same provider (`AppThemeProvider`)
and `CompositionLocal` set as colors/typography, don't hardcode
`MaterialExpressiveTheme` vs `MaterialTheme` per screen.

## Strings / i18n

Same "no hardcoded token outside its module" shape as colors/dimens, applied
to user-facing text: use Android's standard resource system —
`stringResource(R.string.x)` in Compose, backed by `res/values/strings.xml`
(and `res/values-<locale>/strings.xml` per locale) — not a literal string
typed into a composable. (Compose Multiplatform resources
(`compose.resources`) are the KMP steering set's equivalent; don't reach for
that dependency here, it's solving a cross-platform problem this app doesn't
have.)

## Rules

- No `Color(0xFF...)`, `.sp`, `.dp` literals outside the `:theme` module.
  Screens reference `AppTheme.colors.primary`, `AppTheme.sizes.spacingM`, etc.
- No string literals for user-facing text in a composable — `stringResource(R.string.x)`.
  (Log messages, internal tags, and test-only strings are fine as literals;
  this rule is about text a user sees.)
- Conditions (dark mode, dynamic color, locale-driven font) are parameters to
  the provider composable, not `if` branches scattered through screens.
- One `AppThemeProvider` call, at the root of the single Activity (see
  [navigation.md](navigation.md)) — not per screen, not per journey.
- Adding a new design token (spacing, radius, elevation): add it to the
  `Sizes`/`Dimens` interface in `:theme`, not as a local `val` in a screen.
