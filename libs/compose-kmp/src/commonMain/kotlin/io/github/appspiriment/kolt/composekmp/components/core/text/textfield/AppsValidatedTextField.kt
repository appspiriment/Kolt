package io.github.appspiriment.kolt.composekmp.components.core.text.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.medium
import io.github.appspiriment.kolt.composekmp.theme.normal
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.asString

@Composable
fun AppsValidatedTextField(
    state: ValidatedTextFieldState,
    modifier: Modifier = Modifier,
    label: UiText? = null,
    placeholder: UiText? = null,
    helperText: UiText? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = Kolt.typography.textMediumMid.semiBold,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onKeyboardAction: ((defaultAction: () -> Unit) -> Unit)? = null,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.SingleLine,
    inputTransformation: InputTransformation? = if (state.maxLength < Int.MAX_VALUE) {
        InputTransformation.maxLength(state.maxLength)
    } else null,
    colors: TextFieldColors = AppsTextFieldDefaults.defaultColor(),
    labelStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultLabel(),
    placeHolderStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultItem(
        unselectedColor = Kolt.colors.subText.copy(alpha = 0.8f),
        unselectedTextStyle = Kolt.typography.textMedium.normal
    ),
    helperStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultHelper(),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isError = state.error != null
    val errorDescription = state.error?.asString() ?: ""

    Column(modifier = modifier) {
        // Label Section
        label?.let {
            AppspirimentText(
                text = it,
                style = labelStyle.unselectedTextStyle,
                color = labelStyle.unselectedColor,
                modifier = labelStyle.modifier
            )
        }

        // The Modern BasicTextField (v2)
        BasicTextField(
            state = state.textFieldState,
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.background, colors.borderShape)
                .border(
                    border = when {
                        isError -> BorderStroke(1.dp, colors.errorBorderColor)
                        isFocused -> colors.focusedBorder
                        else -> colors.border
                    },
                    shape = colors.borderShape
                )
                .semantics {
                    if (isError) error(errorDescription)
                },
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(color = colors.contentColor),
            keyboardOptions = keyboardOptions,
            onKeyboardAction = onKeyboardAction,
            lineLimits = lineLimits,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(colors.cursorColor),
            inputTransformation = inputTransformation,
            decorator = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(Kolt.sizes.paddingMedium)
                ) {
                    leadingIcon?.let { it() }

                    Box(modifier = Modifier.weight(1f)) {
                        if (state.value.isEmpty() && placeholder != null) {
                            AppspirimentText(
                                text = placeholder,
                                style = placeHolderStyle.unselectedTextStyle,
                                color = placeHolderStyle.unselectedColor,
                                modifier = placeHolderStyle.modifier
                            )
                        }
                        innerTextField()
                    }

                    trailingIcon?.let { it() }
                }
            }
        )

        // Helper and Counter Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp)
        ) {
            val footerText = state.error ?: helperText

            Box(modifier = Modifier.weight(1f)) {
                this@Row.AnimatedVisibility(
                    visible = footerText != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    footerText?.let {
                        AppspirimentText(
                            text = it,
                            style = if (isError) Kolt.typography.textSmall else helperStyle.unselectedTextStyle,
                            color = if (isError) colors.errorColor else helperStyle.unselectedColor,
                            modifier = helperStyle.modifier
                        )
                    }
                }
            }

            if (state.showCounter && state.maxLength < Int.MAX_VALUE) {
                AppspirimentText(
                    text = UiText.DynamicString("${state.value.length} / ${state.maxLength}"),
                    style = Kolt.typography.textSmall,
                    color = if (state.value.length >= state.maxLength) colors.errorColor else Kolt.colors.subText
                )
            }
        }
    }
}

data class TextFieldColors(
    val background: Color,
    val contentColor: Color,
    val border: BorderStroke,
    val focusedBorder: BorderStroke,
    val errorBorderColor: Color,
    val errorColor: Color,
    val cursorColor: Color,
    val borderShape: Shape
)

data class TextFieldItemStyle(
    val unselectedColor: Color,
    val unselectedTextStyle: TextStyle,
    val errorColor: Color,
    val modifier: Modifier = Modifier,
)

object AppsTextFieldDefaults {
    @Composable
    fun defaultItem(
        unselectedColor: Color = Kolt.colors.onBackground.copy(alpha = 0.7f),
        unselectedTextStyle: TextStyle = Kolt.typography.textMediumMid.medium,
        errorColor: Color = Kolt.colors.error,
        modifier: Modifier = Modifier
    ): TextFieldItemStyle {
        return TextFieldItemStyle(
            unselectedColor = unselectedColor,
            unselectedTextStyle = unselectedTextStyle,
            errorColor = errorColor,
            modifier = modifier,
        )
    }

    @Composable
    fun defaultColor(
        background: Color = Kolt.colors.mainSurface,
        contentColor: Color = Kolt.colors.onMainSurface,
        borderWidth: Dp = 1.dp,
        borderColor: Color = Kolt.colors.onMainSurface.copy(alpha = 0.1f),
        focusedBorderWidth: Dp = 1.dp,
        focusedBorderColor: Color = Kolt.colors.primary,
        errorBorderColor: Color = Kolt.colors.error,
        errorColor: Color = Kolt.colors.error,
        cursorColor: Color = Kolt.colors.primary,
        borderShape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusNormal)
    ): TextFieldColors {
        return TextFieldColors(
            background = background,
            contentColor = contentColor,
            border = BorderStroke(width = borderWidth, color = borderColor),
            focusedBorder = BorderStroke(width = focusedBorderWidth, color = focusedBorderColor),
            errorBorderColor = errorBorderColor,
            cursorColor = cursorColor,
            errorColor = errorColor,
            borderShape = borderShape
        )
    }

    @Composable
    fun defaultLabel(
        color: Color = Kolt.colors.subText,
        textStyle: TextStyle = Kolt.typography.textSmallMedium,
        modifier: Modifier = Modifier.padding(
            start = Kolt.sizes.paddingXSmall,
            bottom = Kolt.sizes.paddingXSmall
        )
    ): TextFieldItemStyle {
        return TextFieldItemStyle(
            unselectedColor = color,
            unselectedTextStyle = textStyle,
            errorColor = color,
            modifier = modifier,
        )
    }

    @Composable
    fun defaultHelper(
        color: Color = Kolt.colors.subText,
        textStyle: TextStyle = Kolt.typography.textSmall,
        modifier: Modifier = Modifier.padding(
            start = Kolt.sizes.paddingXSmall,
            bottom = Kolt.sizes.paddingXSmall
        )
    ): TextFieldItemStyle {
        return TextFieldItemStyle(
            unselectedColor = color,
            unselectedTextStyle = textStyle,
            errorColor = color,
            modifier = modifier,
        )
    }
}
