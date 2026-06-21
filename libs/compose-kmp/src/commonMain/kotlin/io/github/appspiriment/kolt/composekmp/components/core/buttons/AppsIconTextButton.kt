package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A button that can display a label with an **independent** leading icon, trailing icon, or both
 * at the same time.
 *
 * This is the more powerful sibling of [AppsImageButton]: while [AppsImageButton] supports either
 * a leading *or* trailing icon, [AppsIconTextButton] renders all three slots ([leadingIcon],
 * [text], [trailingIcon]) simultaneously, each independently optional.
 *
 * ### Typical patterns
 * ```kotlin
 * // Download button — trailing icon only
 * AppsIconTextButton(
 *     text   = "Download".toUiText(),
 *     trailingIcon = uiImageResource(R.drawable.ic_download),
 * ) { /* download */ }
 *
 * // Navigation button — leading back-arrow, trailing forward-arrow
 * AppsIconTextButton(
 *     text         = "Continue".toUiText(),
 *     leadingIcon  = uiImageResource(R.drawable.ic_arrow_back),
 *     trailingIcon = uiImageResource(R.drawable.ic_arrow_forward),
 * ) { /* next */ }
 * ```
 *
 * @param text            Button label.
 * @param modifier        Applied to the [Button] itself.
 * @param leadingIcon     Optional icon rendered to the left of [text].
 * @param trailingIcon    Optional icon rendered to the right of [text].
 * @param iconSize        Width and height applied to both icons.
 * @param iconPadding     Gap between each icon and [text].
 * @param textModifier    Applied to the inner [AppspirimentText].
 * @param enabled         When `false`, renders disabled colours and ignores clicks.
 * @param buttonStyle     Visual style token bag. See [ButtonStyle].
 * @param contentPadding  Internal padding of the button content area.
 * @param onClick         Called on tap.
 */
@Composable
fun AppsIconTextButton(
    text: UiText,
    modifier: Modifier = Modifier,
    leadingIcon: UiImage? = null,
    trailingIcon: UiImage? = null,
    iconSize: Dp = sizes.iconSmall,
    iconPadding: Dp = sizes.paddingSmall,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.primary(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = sizes.paddingMedium,
        vertical = sizes.noPadding,
    ),
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor = when {
        !enabled -> buttonStyle.disabledContainerColor
        isPressed -> buttonStyle.pressedContainerColor
        else -> buttonStyle.containerColor
    }
    val contentColor = if (enabled) buttonStyle.contentColor else buttonStyle.disabledContentColor

    val borderModifier = if (
        buttonStyle.borderColor != Color.Transparent && buttonStyle.borderWidth > 0.dp
    ) {
        Modifier.border(
            width = buttonStyle.borderWidth,
            color = if (enabled) buttonStyle.borderColor
                    else buttonStyle.borderColor.copy(alpha = 0.38f),
            shape = buttonStyle.shape,
        )
    } else Modifier

    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.then(borderModifier),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = buttonStyle.disabledContainerColor,
            disabledContentColor = buttonStyle.disabledContentColor,
        ),
        contentPadding = contentPadding,
        shape = buttonStyle.shape,
        interactionSource = interactionSource,
    ) {
        leadingIcon?.let {
            // padding(end) goes on the modifier (outside), size goes through AppsIcon's own param
            // (inside). Order matters: padding wraps the sized icon, not the reverse.
            AppsIcon(
                icon = it,
                size = iconSize,
                iconHeight = null,
                tint = contentColor,
                modifier = Modifier.padding(end = iconPadding),
            )
        }

        AppspirimentText(
            text = text,
            style = buttonStyle.textStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = textModifier,
        )

        trailingIcon?.let {
            AppsIcon(
                icon = it,
                size = iconSize,
                iconHeight = null,
                tint = contentColor,
                modifier = Modifier.padding(start = iconPadding),
            )
        }
    }
}

