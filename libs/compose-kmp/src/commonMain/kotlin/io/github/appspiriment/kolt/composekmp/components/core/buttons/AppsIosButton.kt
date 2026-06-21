package io.github.appspiriment.kolt.composekmp.components.core.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * iOS Button Style options matching UIKit / SwiftUI configurations.
 */
enum class IosButtonStyle {
    Filled,
    Tinted,
    Gray,
    Plain
}

/**
 * Holds colors resolved for an [AppsIosButton].
 */
data class IosButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

/**
 * Resolves colors for an iOS button style based on a tint color and the theme's colors.
 */
@Composable
fun iosButtonColors(
    style: IosButtonStyle,
    tintColor: Color = Kolt.colors.primary
): IosButtonColors {
    val isDark = isSystemInDarkTheme()
    return when (style) {
        IosButtonStyle.Filled -> IosButtonColors(
            containerColor = tintColor,
            contentColor = Color.White, // Typically white on iOS for filled primary buttons
            disabledContainerColor = Kolt.colors.disabledIconTint.copy(alpha = 0.12f),
            disabledContentColor = Kolt.colors.disabledText
        )
        IosButtonStyle.Tinted -> IosButtonColors(
            containerColor = tintColor.copy(alpha = 0.15f),
            contentColor = tintColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Kolt.colors.disabledText
        )
        IosButtonStyle.Gray -> IosButtonColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
            contentColor = tintColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Kolt.colors.disabledText
        )
        IosButtonStyle.Plain -> IosButtonColors(
            containerColor = Color.Transparent,
            contentColor = tintColor,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Kolt.colors.disabledText
        )
    }
}

/**
 * An iOS-style button implementing standard iOS configurations, shapes,
 * typography, and custom pressed feedback (fade effect instead of Material ripples).
 */
@Composable
fun AppsIosButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: UiText? = null,
    leadingIcon: UiImage? = null,
    trailingIcon: UiImage? = null,
    style: IosButtonStyle = IosButtonStyle.Filled,
    tintColor: Color = Kolt.colors.primary,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(10.dp),
    iconSize: Dp = Kolt.sizes.iconSmall,
    iconPadding: Dp = Kolt.sizes.paddingSmall,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val resolvedColors = iosButtonColors(style = style, tintColor = tintColor)
    
    val (containerColor, contentColor) = when {
        !enabled -> resolvedColors.disabledContainerColor to resolvedColors.disabledContentColor
        else -> resolvedColors.containerColor to resolvedColors.contentColor
    }
    
    val alpha = when {
        !enabled -> 0.38f
        isPressed -> 0.45f
        else -> 1.0f
    }
    
    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(shape)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable Material ripple for iOS-style button
                enabled = enabled,
                onClick = onClick
            )
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingIcon?.let {
                AppsIcon(
                    icon = it,
                    size = iconSize,
                    iconHeight = null,
                    tint = contentColor,
                    modifier = Modifier.padding(end = if (text != null) iconPadding else 0.dp)
                )
            }
            
            text?.let {
                AppspirimentText(
                    text = it,
                    style = Kolt.typography.textMedium.copy(fontWeight = FontWeight.Medium),
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
            }
            
            trailingIcon?.let {
                AppsIcon(
                    icon = it,
                    size = iconSize,
                    iconHeight = null,
                    tint = contentColor,
                    modifier = Modifier.padding(start = if (text != null) iconPadding else 0.dp)
                )
            }
        }
    }
}
