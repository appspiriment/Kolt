package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.staticCompositionLocalOf

// ── Size classes ──────────────────────────────────────────────────────────────
// Material3 standard breakpoints.

enum class WidthSizeClass {
    /** < 600dp  — phone portrait, small phone landscape */
    Compact,
    /** 600–839dp — tablet portrait, large phone landscape, unfolded foldable */
    Medium,
    /** ≥ 840dp  — tablet landscape, desktop */
    Expanded,
}

enum class HeightSizeClass {
    /** < 480dp  — phone landscape */
    Compact,
    /** 480–899dp — most phones portrait, small tablets */
    Medium,
    /** ≥ 900dp  — large tablets, desktops */
    Expanded,
}

// ── Form factor ───────────────────────────────────────────────────────────────

enum class FormFactor {
    /** smallestScreenWidthDp < 600 */
    Phone,
    /** smallestScreenWidthDp ≥ 600 — includes unfolded foldables */
    Tablet,
    /** UI_MODE_TYPE_TELEVISION */
    TV,
}

// ── Navigation style ──────────────────────────────────────────────────────────

/**
 * The recommended navigation pattern for the current window.
 *
 * - [BottomBar] — compact width (phone): `NavigationBar` at the bottom
 * - [Rail]      — medium width (tablet portrait / large phone landscape): `NavigationRail` on the side
 * - [Drawer]    — expanded width (tablet landscape): `NavigationDrawer` on the left
 */
enum class NavigationStyle { BottomBar, Rail, Drawer }

// ── WindowInfo ────────────────────────────────────────────────────────────────

data class WindowInfo(
    val widthSizeClass: WidthSizeClass,
    val heightSizeClass: HeightSizeClass,
    val formFactor: FormFactor,
    /**
     * True when the device is in landscape orientation.
     * Derived from [Configuration.orientation] — accurate even on large tablets where
     * landscape height is still [HeightSizeClass.Medium] (not Compact).
     */
    val isLandscape: Boolean = false,
) {
    val isPortrait: Boolean
        get() = !isLandscape

    val isPhone: Boolean get() = formFactor == FormFactor.Phone
    val isTablet: Boolean get() = formFactor == FormFactor.Tablet
    val isTV: Boolean get() = formFactor == FormFactor.TV

    val isCompact: Boolean get() = widthSizeClass == WidthSizeClass.Compact
    val isMedium: Boolean get() = widthSizeClass == WidthSizeClass.Medium
    val isExpanded: Boolean get() = widthSizeClass == WidthSizeClass.Expanded

    /**
     * The recommended navigation component for this window.
     * Components and scaffolds should use this to switch between
     * BottomBar, NavigationRail, and NavigationDrawer.
     */
    val navigationStyle: NavigationStyle
        get() = when (widthSizeClass) {
            WidthSizeClass.Compact -> NavigationStyle.BottomBar
            WidthSizeClass.Medium -> NavigationStyle.Rail
            WidthSizeClass.Expanded -> NavigationStyle.Drawer
        }

    /**
     * True when the layout should show two content panes side-by-side
     * (e.g., list + detail). Typically enabled on expanded width.
     */
    val shouldUseTwoPanes: Boolean
        get() = widthSizeClass == WidthSizeClass.Expanded

    companion object {
        val Default = WindowInfo(
            widthSizeClass = WidthSizeClass.Compact,
            heightSizeClass = HeightSizeClass.Medium,
            formFactor = FormFactor.Phone,
        )
    }
}

// ── Composition local ─────────────────────────────────────────────────────────

val LocalWindowInfo = staticCompositionLocalOf<WindowInfo> {
    error("LocalWindowInfo not provided — wrap your content in CompositionBaseProvider")
}

