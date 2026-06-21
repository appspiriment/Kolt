package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Encapsulates all visual properties of an inline field-error row.
 *
 * A single [FieldError] instance controls the **message**, the **leading icon**, and the
 * **color** of the error row rendered below a form field. Using one object keeps call-sites
 * tidy and makes it easy to share a consistent error style across an entire form.
 *
 * ### Text field (validation-state driven)
 * [AppsValidatedTextField] derives its error message from [ValidatedTextFieldState] — pass
 * [FieldError] for icon/color customisation only and leave [message] as `null`:
 * ```kotlin
 * AppsValidatedTextField(
 *     state = emailState,
 *     errorConfig = FieldError(icon = Icons.Outlined.WarningAmber),
 * )
 * // No icon — text only
 * AppsValidatedTextField(
 *     state = nameState,
 *     errorConfig = FieldError(icon = null),
 * )
 * ```
 *
 * ### Dropdown / external error
 * [AppsDropdown] does not manage validation state; the caller owns the full error:
 * ```kotlin
 * AppsDropdown(
 *     ...,
 *     error = FieldError(message = "Please select a value".toUiText()),
 * )
 * // Custom icon + brand color
 * AppsDropdown(
 *     ...,
 *     error = FieldError(
 *         message = "Selection is required".toUiText(),
 *         icon    = Icons.Outlined.WarningAmber,
 *         color   = Color(0xFFF57C00),
 *     ),
 * )
 * ```
 *
 * @param message  The error text displayed in the row.
 *                 - **Dropdowns / external state**: set this to the error string.
 *                 - **[AppsValidatedTextField]**: leave as `null` — the text field reads its
 *                   message from [ValidatedTextFieldState.error] and ignores this field.
 * @param icon     Leading icon rendered before [message]. Pass `null` to suppress the icon
 *                 and show only text. Defaults to [Icons.Default.Warning].
 * @param color    Tint applied to both the icon and the message text. `null` = inherit the
 *                 component's built-in error color token (recommended for theme consistency).
 */
@Immutable
data class FieldError(
    val message: UiText? = null,
    val icon: ImageVector? = Icons.Default.Warning,
    val color: Color? = null,
) {
    companion object {
        /**
         * Default display config used by [AppsValidatedTextField] when no [FieldError] is
         * provided: standard error icon, theme error color.
         */
        val Default = FieldError()
 
        /**
         * Convenience factory for a text-only error row (no icon).
         *
         * ```kotlin
         * AppsDropdown(..., error = FieldError.textOnly("Required".toUiText()))
         * ```
         */
        fun textOnly(message: UiText, color: Color? = null) =
            FieldError(message = message, icon = null, color = color)
 
        /**
         * Convenience factory for a fully custom error row.
         *
         * ```kotlin
         * FieldError.of("Out of stock".toUiText(), Icons.Outlined.RemoveShoppingCart)
         * ```
         */
        fun of(
            message: UiText,
            icon: ImageVector? = Icons.Default.Warning,
            color: Color? = null,
        ) = FieldError(message = message, icon = icon, color = color)
    }
}
