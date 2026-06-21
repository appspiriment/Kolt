package io.github.appspiriment.kolt.composeutils.components.core.text.textfield

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composeutils.R
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.FieldError
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

/**
 * A themed search text field with a leading search icon and an animated clear button.
 *
 * ### Behaviour
 * - A magnifying-glass icon is always shown on the left.
 * - A close (✕) icon fades in on the right as soon as the user types, and fades out when
 *   the field is empty. Tapping it calls [ValidatedTextFieldState.clear].
 * - The keyboard IME action defaults to [ImeAction.Search]; pressing it invokes [onSearch].
 * - No label or helper text — search fields are identified by their placeholder and icon.
 *
 * ### Live results vs submit-on-action
 * For live results: react to changes inside the ViewModel by collecting a debounced flow
 * from your state, or pass [onValueChange] to be notified on every keystroke:
 * ```kotlin
 * AppsSearchTextField(
 *     state = searchState,
 *     onValueChange = { query -> viewModel.onSearchQueryChanged(query) }
 * )
 * ```
 * For submit-on-action only: use [onSearch] and ignore [onValueChange]:
 * ```kotlin
 * AppsSearchTextField(
 *     state = searchState,
 *     onSearch = { query -> viewModel.search(query) }
 * )
 * ```
 *
 * @param state           Holds the search text. Pass a [ValidatedTextFieldState] with no rules
 *                        for a plain search field.
 * @param placeholder     Hint text shown when the field is empty. Defaults to `"Search"`.
 * @param onSearch        Called when the user presses the Search key on the IME keyboard.
 *                        The current [ValidatedTextFieldState.value] is passed as the argument.
 * @param onValueChange   Called on every keystroke. Useful for live-as-you-type filtering.
 * @param iconTint        Color for both the search and clear icons.
 * @param colors          Visual colors for the field.
 * @param textStyle       Text style for the input text.
 */
@Composable
fun AppsSearchTextField(
    state: ValidatedTextFieldState,
    modifier: Modifier = Modifier,
    placeholderText: UiText = UiText.StringResource(R.string.search),
    onSearch: ((String) -> Unit)? = null,
    onValueChange: ((String) -> Unit)? = null,
    iconTint: Color = Kolt.colors.subText,
    colors: TextFieldColors = AppsTextFieldDefaults.defaultColor(),
    textStyle: TextStyle = Kolt.typography.bodyMediumLarge,
    errorConfig: FieldError = FieldError.Default,
) {
    // The clear button is a @Composable that reads state.value — it lives in the function
    // body (not a default param) so snapshot-state reads happen inside a @Composable scope.
    val context = LocalContext.current
    val trailingIcon: @Composable () -> Unit = {
        AnimatedVisibility(
            visible = state.value.isNotEmpty(),
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
        ) {
            IconButton(onClick = { state.clear() }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = context.getString(R.string.clear_search),
                    tint = iconTint,
                    modifier = Modifier.size(Kolt.sizes.iconSmall),
                )
            }
        }
    }

    AppsValidatedTextField(
        state = state,
        modifier = modifier,
        placeholder = placeholderText,
        textStyle = textStyle,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        onKeyboardAction = { defaultAction ->
            onSearch?.invoke(state.value) ?: defaultAction()
        },
        onValueChange = onValueChange,
        colors = colors,
        errorConfig = errorConfig,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null, // decorative; the field has its own contentDescription
                tint = iconTint,
                modifier = Modifier.size(Kolt.sizes.iconStandard),
            )
        },
        // Always pass trailingIcon so the field width stays stable — AnimatedVisibility
        // handles show/hide internally without changing the field layout.
        trailingIcon = trailingIcon,
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun PreviewSearchTextField() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Empty state
        AppsSearchTextField(
            state = remember { ValidatedTextFieldState() },
        )

        // With text (clear button visible)
        AppsSearchTextField(
            state = remember { ValidatedTextFieldState(initialValue = "Arun Shankar") },
        )

        // Custom placeholder
        AppsSearchTextField(
            state = remember { ValidatedTextFieldState() },
            placeholderText = "Search members…".toUiText(),
        )
    }
}
