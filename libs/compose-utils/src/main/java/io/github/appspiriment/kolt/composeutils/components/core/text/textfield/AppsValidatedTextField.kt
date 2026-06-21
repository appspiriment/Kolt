
package io.github.appspiriment.kolt.composeutils.components.core.text.textfield
import io.github.appspiriment.kolt.composekmp.wrappers.asString
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.maxLength
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.FieldError
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.theme.Kolt.typography
import io.github.appspiriment.kolt.composekmp.theme.normal
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

@Composable
fun AppsValidatedTextField(
    state: ValidatedTextFieldState,
    modifier: Modifier = Modifier,
    label: UiText? = null,
    placeholder: UiText? = null,
    helperText: UiText? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = typography.textMediumMid.semiBold,
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
        unselectedTextStyle = typography.textMedium.normal
    ),
    helperStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultHelper(),
    errorConfig: FieldError = FieldError.Default,
    outputTransformation: OutputTransformation? = null,
    onValueChange: ((String) -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isError = state.error != null
    val context = LocalContext.current

    // Observe value changes and forward to external callback + validation
    if (onValueChange != null) {
        LaunchedEffect(state.textFieldState) {
            snapshotFlow { state.value }
                .drop(1)
                .onEach { text ->
                    state.onValueChange(text)
                    onValueChange(text)
                }
                .launchIn(this)
        }
    }

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
                    if (isError) error(state.error?.asString(context) ?: "")
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
            outputTransformation = outputTransformation,
            decorator = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(sizes.paddingMedium)
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
                            style = if (isError) typography.textSmall else helperStyle.unselectedTextStyle,
                            color = if (isError) (errorConfig.color ?: colors.errorColor) else helperStyle.unselectedColor,
                            modifier = helperStyle.modifier
                        )
                    }
                }
            }

            if (state.showCounter && state.maxLength < Int.MAX_VALUE) {
                AppspirimentText(
                    text = UiText.DynamicString("${state.value.length} / ${state.maxLength}"),
                    style = typography.textSmall,
                    color = if (state.value.length >= state.maxLength) colors.errorColor else Kolt.colors.subText
                )
            }
        }
    }
}
