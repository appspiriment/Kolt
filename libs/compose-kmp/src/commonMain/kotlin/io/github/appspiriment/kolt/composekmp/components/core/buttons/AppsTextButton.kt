package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A flat text-only button backed by Material 3 [TextButton].
 *
 * Unlike [AppsButton] (which always has a container background), [AppsTextButton] renders no
 * background surface — the ripple spreads over the text bounds only. This gives it the lowest
 * visual emphasis and makes it suitable for inline or secondary actions that must not compete
 * with a nearby primary button.
 *
 * ### When to use
 * - Dismiss / Cancel actions paired with a filled primary button.
 * - In-line "Learn more" or "See all" links inside cards where a full button would feel heavy.
 * - Any two-button row where one action is clearly secondary (e.g. "Skip" + "Continue").
 *
 * Prefer [AppsLinkButton] when the action should look like a hyperlink (underlined, no padding,
 * no ripple container).
 *
 * ### Style
 * Defaults to [ButtonStyle.transparent()]. Swap in [ButtonStyle.danger()] for low-emphasis
 * destructive actions.
 *
 * @param text        Button label.
 * @param modifier    Applied to the [TextButton] itself.
 * @param textModifier Applied to the inner [AppspirimentText].
 * @param enabled     When `false`, renders with disabled colours and ignores clicks.
 * @param buttonStyle Visual style — only [ButtonStyle.contentColor], [ButtonStyle.disabledContentColor],
 *                    [ButtonStyle.shape], and [ButtonStyle.textStyle] are used.
 *                    [ButtonStyle.containerColor] is forwarded but typically [Color.Transparent].
 * @param contentPadding Inner padding of the button. Defaults to a compact horizontal-only inset.
 * @param onClick     Called on tap.
 */
@Composable
fun AppsTextButton(
    text: UiText,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.transparent(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Kolt.sizes.paddingMedium,
        vertical = Kolt.sizes.noPadding,
    ),
    onClick: () -> Unit,
) {
    val contentColor = if (enabled) buttonStyle.contentColor else buttonStyle.disabledContentColor

    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = buttonStyle.shape,
        colors = ButtonDefaults.textButtonColors(
            containerColor = buttonStyle.containerColor,
            contentColor = buttonStyle.contentColor,
            disabledContainerColor = buttonStyle.disabledContainerColor,
            disabledContentColor = buttonStyle.disabledContentColor,
        ),
        contentPadding = contentPadding,
    ) {
        AppspirimentText(
            text = text,
            style = buttonStyle.textStyle,
            color = contentColor,
            textAlign = TextAlign.Center,
            modifier = textModifier,
        )
    }
}

