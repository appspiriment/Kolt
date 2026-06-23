package io.github.appspiriment.kolt.composekmp.components.core.buttons.types

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.SmoothCornerShape


data class ButtonStyle(
    val textStyle : TextStyle,
    val buttonColor: Color,
    val buttonPressedColor: Color = buttonColor,
    val textColor: Color,
    val strokeColor: Color,
    val buttonShape: Shape,
    // ── M3-compatible aliases (used by AppsIconTextButton / AppsTextButton) ──
    val disabledContainerColor: Color = Color.Unspecified,
    val disabledContentColor: Color = Color.Unspecified,
    val borderWidth: Dp = 0.dp,
) {
    // Aliases so ported files can reference either naming convention.
    val containerColor: Color get() = buttonColor
    val pressedContainerColor: Color get() = buttonPressedColor
    val contentColor: Color get() = textColor
    val borderColor: Color get() = strokeColor
    val shape: Shape get() = buttonShape
    companion object {
        @Composable
        fun primary(
            textStyle : TextStyle = Kolt.typography.textMedium,
            buttonColor: Color = Kolt.colors.primary,
            buttonPressedColor: Color = Kolt.colors.primary,
            textColor: Color = Kolt.colors.onPrimary,
            strokeColor: Color = Color.Transparent,
            buttonShape: Shape = CircleShape,
            disabledContainerColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.12f),
            disabledContentColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.38f),
        ): ButtonStyle = ButtonStyle(
            textStyle = textStyle,
            buttonColor = buttonColor,
            buttonPressedColor = buttonPressedColor,
            textColor = textColor,
            strokeColor = strokeColor,
            buttonShape = buttonShape,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

        @Composable
        fun outlined(
            textStyle : TextStyle = Kolt.typography.textMedium,
            buttonColor: Color = Color.Transparent,
            buttonPressedColor: Color = Kolt.colors.primary,
            textColor: Color = Kolt.colors.primary,
            strokeColor: Color = Kolt.colors.primary,
            buttonShape: Shape = CircleShape,
            disabledContainerColor: Color = Color.Transparent,
            disabledContentColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.38f),
            borderWidth: Dp = 1.dp,
        ): ButtonStyle = ButtonStyle(
            textStyle = textStyle,
            buttonColor = buttonColor,
            buttonPressedColor = buttonPressedColor,
            textColor = textColor,
            strokeColor = strokeColor,
            buttonShape = buttonShape,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
            borderWidth = borderWidth,
        )

        @Composable
        fun transparent(
            textStyle : TextStyle = Kolt.typography.textMedium,
            buttonColor: Color = Color.Transparent,
            buttonPressedColor: Color = Kolt.colors.primary,
            textColor: Color = Kolt.colors.primary,
            strokeColor: Color = Color.Transparent,
            buttonShape: Shape = CircleShape,
            disabledContainerColor: Color = Color.Transparent,
            disabledContentColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.38f),
        ): ButtonStyle = ButtonStyle(
            textStyle = textStyle,
            buttonColor = buttonColor,
            buttonPressedColor = buttonPressedColor,
            textColor = textColor,
            strokeColor = strokeColor,
            buttonShape = buttonShape,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )

        /** Destructive / danger action button — transparent background, error-coloured text. */
        @Composable
        fun danger(
            textStyle: TextStyle = Kolt.typography.textMedium,
            textColor: Color = Kolt.colors.error,
            buttonShape: Shape = CircleShape,
        ): ButtonStyle = transparent(
            textStyle = textStyle,
            textColor = textColor,
            buttonShape = buttonShape,
        )

        /** Negative text-button style (e.g. "No thanks") — transparent background, muted text. */
        @Composable
        fun primaryNegative(
            textStyle: TextStyle = Kolt.typography.textMedium,
            textColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.6f),
            buttonShape: Shape = CircleShape,
        ): ButtonStyle = transparent(
            textStyle = textStyle,
            textColor = textColor,
            buttonShape = buttonShape,
        )

        /** Positive text-button style (e.g. "Update") — transparent background, accented text. */
        @Composable
        fun primaryPositive(
            textStyle: TextStyle = Kolt.typography.textMedium,
            textColor: Color = Kolt.colors.primary,
            buttonShape: Shape = CircleShape,
        ): ButtonStyle = transparent(
            textStyle = textStyle,
            textColor = textColor,
            buttonShape = buttonShape,
        )

        /** Filled Tonal style — uses secondary container colors for subtle emphasis. */
        @Composable
        fun tonal(
            textStyle: TextStyle = Kolt.typography.textMedium,
            buttonColor: Color = Kolt.colors.secondaryCardContainer,
            buttonPressedColor: Color = Kolt.colors.secondaryCardContainer.copy(alpha = 0.85f),
            textColor: Color = Kolt.colors.onSecondaryCardContainer,
            buttonShape: Shape = CircleShape,
            disabledContainerColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.12f),
            disabledContentColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.38f),
        ): ButtonStyle = ButtonStyle(
            textStyle = textStyle,
            buttonColor = buttonColor,
            buttonPressedColor = buttonPressedColor,
            textColor = textColor,
            strokeColor = Color.Transparent,
            buttonShape = buttonShape,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = disabledContentColor,
        )
    }
}