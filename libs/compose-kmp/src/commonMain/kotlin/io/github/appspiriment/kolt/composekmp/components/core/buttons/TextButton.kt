package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A text-only button with no background or border by default.
 * Uses [ButtonStyle.transparent] as the default style; pass a custom [ButtonStyle] to override.
 */
@Composable
fun TextButton(
    text: UiText,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.transparent(),
    onClick: () -> Unit,
) {
    AppsButton(
        text = text,
        modifier = modifier,
        textModifier = textModifier,
        enabled = enabled,
        buttonStyle = buttonStyle,
        onClick = onClick,
    )
}
