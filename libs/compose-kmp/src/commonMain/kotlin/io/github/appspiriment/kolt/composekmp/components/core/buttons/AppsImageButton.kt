package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes


@Composable
fun AppsImageButton(
    icon: UiImage?,
    iconPosition: IconPosition = IconPosition.Start,
    text: UiText,
    modifier: Modifier = Modifier,
    iconPadding: Dp = Kolt.sizes.paddingXXXXLarge,
    textModifier: Modifier = Modifier,
    enabled: Boolean = true,
    buttonStyle: ButtonStyle = ButtonStyle.primary(),
    contentPadding: PaddingValues = PaddingValues(horizontal = sizes.paddingMedium, vertical = sizes.noPadding),
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor = if(isPressed) buttonStyle.buttonPressedColor else buttonStyle.buttonColor
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
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = buttonStyle.textColor,
        ),
        enabled = enabled,
        modifier = modifier.then(borderModifier),
        contentPadding = contentPadding,
        shape = buttonStyle.buttonShape,
        onClick = { onClick.invoke() }
    ) {

        icon?.takeIf { iconPosition == IconPosition.Start }?.let {
            AppsImage(image = it, modifier = Modifier.padding(end = iconPadding))
        }

        AppspirimentText(
            text = text,
            style = buttonStyle.textStyle,
            color = buttonStyle.textColor,
            textAlign = TextAlign.Center,
            modifier = textModifier
        )
        icon?.takeIf { iconPosition == IconPosition.End }?.let {
            AppsImage(image = it, modifier = Modifier.padding(start = iconPadding))
        }

    }
}

sealed interface IconPosition{
    data object Start: IconPosition
    data object End: IconPosition
}