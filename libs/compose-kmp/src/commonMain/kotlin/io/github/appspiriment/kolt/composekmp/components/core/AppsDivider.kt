package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

/**
 * Themed horizontal divider. Defaults to [Kolt.colors.dividerColor] so every
 * divider in the app automatically tracks theme changes without per-call colour arguments.
 *
 * [thickness] defaults to [Kolt.sizes.paddingTiny] (1 dp — the standard thin-divider
 * token used throughout the design system).
 */
@Composable
fun AppsDivider(
    modifier: Modifier = Modifier,
    color: Color = Kolt.colors.dividerColor,
    thickness: Dp = Kolt.sizes.paddingTiny,
) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

/**
 * Themed vertical divider. Requires a bounded height — either via [modifier] or by being
 * inside a [Row] with `fillMaxHeight()`.
 *
 * [thickness] defaults to [Kolt.sizes.paddingTiny] (1 dp).
 */
@Composable
fun AppsVerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = Kolt.colors.dividerColor,
    thickness: Dp = Kolt.sizes.paddingTiny,
) {
    VerticalDivider(modifier = modifier, thickness = thickness, color = color)
}
