package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.asColor

/**
 * A circular icon button with a solid background fill.
 *
 * Unlike [AppsIconButton] (which delegates to Material 3's [IconButton] and renders a transparent
 * background), [AppsCircularButton] draws a coloured disc behind the icon. It is suitable for
 * FAB-style triggers, colour-branded actions, and any place where the icon needs a distinct
 * background tray.
 *
 * The **entire circle** — not just the icon — is the touch target and clips the ripple.
 *
 * ### Size
 * Use [size] to control the diameter. Defaults to [Kolt.sizes.floatingButtonSize]
 * (standard FAB diameter). For small variants pass [Kolt.sizes.floatingButtonSizeSmall]:
 * ```kotlin
 * AppsCircularButton(
 *     icon = uiImageResource(R.drawable.ic_add),
 *     size = Kolt.sizes.floatingButtonSizeSmall,
 * ) { /* add */ }
 * ```
 *
 * @param icon               Icon rendered at the centre of the circle. Set [UiImage.tint] on the
 *                           icon to control its colour independently of [buttonColor].
 * @param modifier           Applied to the outer [Box] (controls positioning, not size).
 * @param size               Diameter of the circle. Defaults to [Kolt.sizes.floatingButtonSize].
 * @param iconModifier       Applied to [AppsIcon] inside the circle. Defaults to a small padding
 *                           so the icon doesn't touch the circle edge.
 * @param buttonColor        Circle background fill colour. Defaults to [Kolt.colors.primary].
 * @param enabled            When false, disables interactions and styles it with neutral disabled colors.
 * @param contentDescription Accessibility label. Defaults to [UiImage.description].
 * @param onClick            Called when the circle is tapped.
 */
@Composable
fun AppsCircularButton(
    icon: UiImage,
    modifier: Modifier = Modifier,
    size: Dp = Kolt.sizes.floatingButtonSize,
    iconModifier: Modifier = Modifier.padding(Kolt.sizes.paddingXSmall),
    buttonColor: Color = Kolt.colors.primary,
    enabled: Boolean = true,
    contentDescription: String? = icon.description,
    onClick: () -> Unit,
) {
    val backgroundColor = if (enabled) buttonColor else Kolt.colors.onMainSurface.copy(alpha = 0.12f)
    val iconTint = if (enabled) icon.tint?.asColor() else Kolt.colors.onMainSurface.copy(alpha = 0.38f)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClickLabel = contentDescription,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        AppsIcon(
            icon = icon,
            modifier = iconModifier,
            tint = iconTint,
            contentDescription = contentDescription,
        )
    }
}
