package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.wrappers.UiText


@Composable
fun AppsButton(
    text: UiText,
    modifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.primary(),
    onClick: () -> Unit
) {
    AppsImageButton(
        icon = null,
        text = text,
        modifier = modifier,
        textModifier = textModifier,
        enabled = enabled,
        buttonStyle = buttonStyle,
        onClick = onClick
    )
}