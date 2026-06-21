package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A themed tonal button with low-to-medium emphasis.
 *
 * It uses [ButtonStyle.tonal()] by default.
 */
@Composable
fun AppsTonalButton(
    text: UiText,
    modifier: Modifier = Modifier,
    leadingIcon: UiImage? = null,
    trailingIcon: UiImage? = null,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.tonal(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = sizes.paddingMedium,
        vertical = sizes.noPadding,
    ),
    onClick: () -> Unit,
) {
    AppsIconTextButton(
        text = text,
        modifier = modifier,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        textModifier = textModifier,
        enabled = enabled,
        buttonStyle = buttonStyle,
        contentPadding = contentPadding,
        onClick = onClick,
    )
}
