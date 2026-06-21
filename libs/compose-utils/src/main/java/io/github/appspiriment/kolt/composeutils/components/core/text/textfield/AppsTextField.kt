package io.github.appspiriment.kolt.composeutils.components.core.text.textfield

import io.github.appspiriment.kolt.composekmp.wrappers.asString
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.colors
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.theme.Kolt.typography
import io.github.appspiriment.kolt.composekmp.theme.medium
import io.github.appspiriment.kolt.composekmp.theme.normal
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import kotlinx.coroutines.flow.collectLatest

@Deprecated("Use new stateful textfield")
@Composable
fun AppsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: UiText? = null,
    placeholder: UiText? = null,
    errorText: UiText? = null,
    helperText: UiText? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = typography.textMediumMid.semiBold,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    colors: TextFieldColors = AppsTextFieldDefaults.defaultColor(),
    labelStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultLabel(),
    placeHolderStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultItem(
        unselectedColor = Kolt.colors.subText.copy(alpha = 0.8f),
        unselectedTextStyle = typography.textMedium.normal
    ),
    helperStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultHelper(),
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    maxLength: Int = 0,
    currentLength: Int = 0,
    showCounter: Boolean = false,
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }

    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collectLatest { interaction ->
            when (interaction) {
                is FocusInteraction.Focus -> isFocused = true
                is FocusInteraction.Unfocus -> isFocused = false
            }
        }
    }

    Column(modifier = modifier) {
        label?.let {
            AppspirimentText(
                text = it,
                style = labelStyle.unselectedTextStyle,
                color = labelStyle.unselectedColor,
                modifier = labelStyle.modifier
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                }
                .background(
                    color = colors.background,
                    shape = colors.borderShape
                )
                .border(
                    border = when {
                        errorText != null -> BorderStroke(1.dp, colors.errorBorderColor)
                        isFocused -> colors.focusedBorder
                        else -> colors.border
                    },
                    shape = colors.borderShape
                )
                .semantics {
                    contentDescription = label?.asString(context = context) ?: placeholder?.asString(context) ?: "Text field"
                    if (errorText != null) {
                        error(errorText.asString(context))
                    }
                },
            interactionSource = interactionSource,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(color = colors.contentColor),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(colors.cursorColor),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = sizes.paddingMedium, vertical = sizes.paddingMedium)
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder != null) {
                            AppspirimentText(
                                text = placeholder,
                                style = placeHolderStyle.unselectedTextStyle,
                                color = placeHolderStyle.unselectedColor,
                                modifier = placeHolderStyle.modifier
                            )
                        }
                        innerTextField()
                    }

                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 4.dp, end = 4.dp)
        ) {
            AnimatedVisibility(
                visible = errorText != null || helperText != null,
                enter = expandVertically(),
                exit = shrinkVertically(),
                modifier = Modifier.weight(1f)
            ) {
                val textToShow = errorText ?: helperText
                val styleToUse = if (errorText != null) typography.textSmall else helperStyle.unselectedTextStyle
                val colorToUse = if (errorText != null) colors.errorColor else helperStyle.unselectedColor

                textToShow?.let {
                    AppspirimentText(
                        text = it,
                        style = styleToUse,
                        color = colorToUse,
                        modifier = helperStyle.modifier
                    )
                }
            }

            if (showCounter) {
                AppspirimentText(
                    text = UiText.DynamicString("$currentLength / $maxLength"),
                    style = typography.textSmall,
                    color = if (currentLength > maxLength) colors.errorColor else Kolt.colors.subText,
                    modifier = Modifier.align(Alignment.Bottom)
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
        unselectedColor: Color = colors.onBackground.copy(alpha = 0.7f),
        unselectedTextStyle: TextStyle = typography.textMediumMid.medium,
        errorColor: Color = colors.error,
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
        background: Color = colors.mainSurface,
        contentColor: Color = colors.onMainSurface,
        borderWidth: Dp = 1.dp,
        borderColor: Color = colors.onMainSurface.copy(alpha = 0.1f),
        focusedBorderWidth: Dp = 1.dp,
        focusedBorderColor: Color = colors.primary,
        errorBorderColor: Color = colors.error,
        errorColor: Color = colors.error,
        cursorColor: Color = colors.primary,
        borderShape: Shape = RoundedCornerShape(sizes.cornerRadiusNormal)
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
        color: Color = colors.subText,
        textStyle: TextStyle = typography.textSmallMedium,
        modifier: Modifier = Modifier.padding(
            start = sizes.paddingXSmall,
            bottom = sizes.paddingXSmall
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
        color: Color = colors.subText,
        textStyle: TextStyle = typography.textSmall,
        modifier: Modifier = Modifier.padding(
            start = sizes.paddingXSmall,
            bottom = sizes.paddingXSmall
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