package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.utils.onColor
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.toUiColor

/**
 * An icon centered inside a filled circle — a common pattern for feature tiles,
 * action buttons, and avatar-style indicators.
 *
 * ### Icon tint
 * When no explicit [iconTint] is given, [io.github.appspiriment.kolt.composekmp.utils.onColor]
 * automatically picks [Color.Black] or [Color.White] based on the luminance of
 * [backgroundColor], ensuring the icon is always legible.
 *
 * ```kotlin
 * // Automatic tint from background
 * CircleIconBox(
 *     icon = uiVectorResource(R.drawable.ic_star),
 *     backgroundColor = Kolt.colors.primary,
 * )
 *
 * // Fixed icon tint
 * CircleIconBox(
 *     icon = uiVectorResource(R.drawable.ic_bell),
 *     backgroundColor = Color(0xFFFF6B6B),
 *     iconTint = Color.White,
 * )
 * ```
 *
 * @param icon            Icon to render inside the circle.
 * @param modifier        Applied to the outer [Box].
 * @param backgroundColor Fill color of the circle.
 * @param circleSize      Diameter of the circle.
 * @param iconSize        Size of the icon inside the circle. Defaults to 60 % of [circleSize].
 * @param iconTint        Tint applied to the icon. `null` = derived from [backgroundColor].
 */
@Composable
fun CircleIconBox(
    icon: UiImage,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Kolt.colors.primaryContainer,
    circleSize: Dp = Kolt.sizes.iconXLarge,
    iconSize: Dp = circleSize * 0.6f,
    iconTint: Color? = null,
) {
    val resolvedTint = iconTint ?: backgroundColor.onColor()

    Box(
        modifier = modifier
            .size(circleSize)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center,
    ) {
        AppsIcon(
            icon = icon.setTint(resolvedTint.toUiColor()),
            iconHeight = iconSize,
        )
    }
}

