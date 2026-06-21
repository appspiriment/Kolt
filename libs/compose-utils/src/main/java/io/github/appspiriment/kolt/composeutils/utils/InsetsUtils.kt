package io.github.appspiriment.kolt.composeutils.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp

// ── Inset heights as Dp ───────────────────────────────────────────────────────
// Compose already provides Modifier.statusBarsPadding() etc., but not the raw
// Dp values. These are needed for custom headers, parallax offsets, animations,
// and any layout that must know the size rather than just apply padding.

/** The current status bar height in Dp. Reacts to configuration changes. */
@Composable
fun rememberStatusBarHeightDp(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

/** The current navigation bar height in Dp (bottom of screen). */
@Composable
fun rememberNavigationBarHeightDp(): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

/** The current software keyboard (IME) height in Dp. 0 when keyboard is hidden. */
@Composable
fun rememberImeHeightDp(): Dp =
    WindowInsets.ime.asPaddingValues().calculateBottomPadding()

// ── Combined system bar data ───────────────────────────────────────────────────

/**
 * All four system bar inset values in one snapshot.
 * Useful when you need multiple values without separate composable calls.
 */
data class SystemBarInsets(
    val top: Dp,
    val bottom: Dp,
    val start: Dp,
    val end: Dp,
) {
    /** The total vertical inset (top + bottom). */
    val vertical: Dp get() = top + bottom
    /** The total horizontal inset (start + end). */
    val horizontal: Dp get() = start + end
}

@Composable
fun rememberSystemBarInsets(): SystemBarInsets {
    val layoutDir = LocalLayoutDirection.current
    val padding = WindowInsets.systemBars.asPaddingValues()
    return SystemBarInsets(
        top = padding.calculateTopPadding(),
        bottom = padding.calculateBottomPadding(),
        start = padding.calculateStartPadding(layoutDir),
        end = padding.calculateEndPadding(layoutDir),
    )
}

/** System bars as a [PaddingValues] — drop-in for `contentPadding` in lazy lists. */
@Composable
fun rememberSystemBarPaddingValues(): PaddingValues =
    WindowInsets.systemBars.asPaddingValues()

/** Display cutout insets as a [PaddingValues] — for notch/punch-hole aware layouts. */
@Composable
fun rememberDisplayCutoutPaddingValues(): PaddingValues =
    WindowInsets.displayCutout.asPaddingValues()
