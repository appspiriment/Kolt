package io.github.appspiriment.kolt.composeutils.components.core.text.textfield

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composeutils.R
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.FieldError
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

/**
 * A password text field with a built-in visibility toggle.
 *
 * ### Behaviour
 * - By default, characters are masked with `•` (U+2022) via [OutputTransformation].
 *   The actual text in [ValidatedTextFieldState.value] is **never modified** — masking is
 *   purely visual.
 * - Tapping the trailing eye icon toggles masking on/off. Pass `showVisibilityToggle = false`
 *   to remove the toggle entirely (e.g., for a "confirm password" field where revealing
 *   is a security concern).
 * - The default keyboard type is [KeyboardType.Password] so the system keyboard suppresses
 *   auto-correct and disables predictive text suggestions.
 *
 * ### Validation
 * Wire up [ValidatedTextFieldState] with whatever [ValidationRule]s are appropriate.
 * A "minimum 8 characters" rule is typical:
 * ```kotlin
 * val passwordState = remember {
 *     ValidatedTextFieldState(
 *         rules = listOf(
 *             ValidationRules.required,
 *             ValidationRules.minLength(8),
 *         )
 *     )
 * }
 * AppsPasswordTextField(state = passwordState, label = "Password".toUiText())
 * ```
 *
 * @param state                 Holds the password text, rules, and error state.
 * @param label                 Label shown above the field.
 * @param placeholder           Hint shown when the field is empty.
 * @param helperText            Supplementary text below the field (replaced by error when invalid).
 * @param errorConfig         Icon/color config for the inline error row. See [FieldError].
 * @param showVisibilityToggle  Whether to show the eye icon. Default `true`.
 * @param keyboardOptions       Defaults to [KeyboardType.Password] + [ImeAction.Done].
 */
@Composable
fun AppsPasswordTextField(
    state: ValidatedTextFieldState,
    modifier: Modifier = Modifier,
    label: UiText? = null,
    placeholder: UiText? = null,
    helperText: UiText? = null,
    enabled: Boolean = true,
    textStyle: TextStyle = Kolt.typography.bodyMediumLarge.semiBold,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Password,
        imeAction = ImeAction.Done,
    ),
    onKeyboardAction: ((defaultAction: () -> Unit) -> Unit)? = null,
    showVisibilityToggle: Boolean = true,
    colors: TextFieldColors = AppsTextFieldDefaults.defaultColor(),
    labelStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultLabel(),
    placeHolderStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultItem(
        unselectedColor = Kolt.colors.subText.copy(alpha = 0.8f),
        unselectedTextStyle = Kolt.typography.bodyMedium,
    ),
    helperStyle: TextFieldItemStyle = AppsTextFieldDefaults.defaultHelper(),
    errorConfig: FieldError = FieldError.Default,
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    AppsValidatedTextField(
        state = state,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        helperText = helperText,
        enabled = enabled,
        textStyle = textStyle,
        keyboardOptions = keyboardOptions,
        onKeyboardAction = onKeyboardAction,
        colors = colors,
        labelStyle = labelStyle,
        placeHolderStyle = placeHolderStyle,
        helperStyle = helperStyle,
        errorConfig = errorConfig,
        outputTransformation = if (passwordVisible) null else PasswordMaskTransformation,
        trailingIcon = if (showVisibilityToggle) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible)
                            context.getString(R.string.hide_password)
                        else
                            context.getString(R.string.show_password),
                        tint = Kolt.colors.subText,
                    )
                }
            }
        } else null,
    )
}

// ── OutputTransformation ──────────────────────────────────────────────────────

/**
 * Visually replaces every character with a bullet `•` (U+2022).
 * Applied as [OutputTransformation] so the actual text in [TextFieldState] is never changed —
 * copy/paste and accessibility services always see the real value.
 */
private object PasswordMaskTransformation : OutputTransformation {
    private const val MASK = '•' // •

    override fun TextFieldBuffer.transformOutput() {
        if (length > 0) replace(0, length, MASK.toString().repeat(length))
    }
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewPasswordTextField() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Default — masked with toggle
        AppsPasswordTextField(
            state = remember {
                ValidatedTextFieldState(
                    rules = listOf(ValidationRules.required, ValidationRules.minLength(8))
                )
            },
            label = "Password".toUiText(),
            placeholder = "Enter password".toUiText(),
            helperText = "Must be at least 8 characters".toUiText(),
        )

        // No toggle (e.g., confirm password)
        AppsPasswordTextField(
            state = remember { ValidatedTextFieldState() },
            label = "Confirm password".toUiText(),
            showVisibilityToggle = false,
        )
    }
}
