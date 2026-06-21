package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp

// ── Generic value picker ──────────────────────────────────────────────────────

/**
 * Returns one of the three values based on the current window's [WidthSizeClass].
 *
 * [medium] defaults to [compact], [expanded] defaults to [medium] — so you only need to
 * specify the values that differ.
 *
 * Usage:
 * ```kotlin
 * val columns = adaptiveOf(compact = 1, medium = 2, expanded = 3)
 * val contentPadding = adaptiveOf(compact = 16.dp, expanded = 32.dp)
 * val showSidePanel = adaptiveOf(compact = false, expanded = true)
 * ```
 */
@Composable
@ReadOnlyComposable
fun <T> adaptiveOf(
    compact: T,
    medium: T = compact,
    expanded: T = medium,
): T = when (LocalWindowInfo.current.widthSizeClass) {
    WidthSizeClass.Compact -> compact
    WidthSizeClass.Medium -> medium
    WidthSizeClass.Expanded -> expanded
}

// ── Typed shorthands ──────────────────────────────────────────────────────────

/** Picks an [Int] (e.g., column count, item span) based on width size class. */
@Composable
@ReadOnlyComposable
fun adaptiveColumns(
    compact: Int,
    medium: Int = compact,
    expanded: Int = medium,
): Int = adaptiveOf(compact, medium, expanded)

/** Picks a [Dp] value (padding, spacing, size) based on width size class. */
@Composable
@ReadOnlyComposable
fun adaptivePadding(
    compact: Dp,
    medium: Dp = compact,
    expanded: Dp = medium,
): Dp = adaptiveOf(compact, medium, expanded)

/** Picks a [Float] (fraction, weight, scale) based on width size class. */
@Composable
@ReadOnlyComposable
fun adaptiveFraction(
    compact: Float,
    medium: Float = compact,
    expanded: Float = medium,
): Float = adaptiveOf(compact, medium, expanded)

// ── WindowInfo extensions (non-Composable, operate on a captured WindowInfo) ──

/**
 * Returns [ifTrue] when [predicate] matches this [WindowInfo], otherwise [ifFalse].
 * Useful for non-Composable logic that has already captured the window info.
 *
 * ```kotlin
 * val config = Kolt.windowInfo
 * val itemWidth = config.adaptive(compact = 200.dp, expanded = 320.dp)
 * ```
 */
fun <T> WindowInfo.adaptive(
    compact: T,
    medium: T = compact,
    expanded: T = medium,
): T = when (widthSizeClass) {
    WidthSizeClass.Compact -> compact
    WidthSizeClass.Medium -> medium
    WidthSizeClass.Expanded -> expanded
}

// ── Navigation-aware content fraction ────────────────────────────────────────

/**
 * The fraction of the screen width that content (non-navigation) should occupy.
 *
 * On [NavigationStyle.Rail] and [NavigationStyle.Drawer] layouts, this accounts
 * for the navigation component's width so content doesn't overflow.
 *
 * Apps can use this with `Modifier.fillMaxWidth(windowInfo.contentWidthFraction)`.
 */
val WindowInfo.contentWidthFraction: Float
    get() = when (navigationStyle) {
        NavigationStyle.BottomBar -> 1f
        NavigationStyle.Rail -> 0.9f       // approximate rail width ~56dp on typical screen
        NavigationStyle.Drawer -> 0.75f    // modal drawer typically 256–360dp
    }
