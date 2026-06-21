package io.github.appspiriment.kolt.composeutils.components.core.dropdowns

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsIconButton
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * An icon-button dropdown trigger.
 *
 * Tapping the icon opens a [DropDownSpinner] menu. The button is backed by [AppsIconButton]
 * which guarantees a minimum 48 × 48 dp touch target and uses the app's themed icon tint
 * by default.
 *
 * For a full-featured dropdown (floating label, error state, item icons, sub-labels)
 * prefer [AppsDropdown] instead.
 *
 * @param items              Options displayed in the dropdown menu.
 * @param icon               The icon shown as the trigger.
 * @param modifier           Applied to the [DropDownSpinner] outer box.
 * @param tint               Explicit tint for the icon. `null` = inherit from [UiImage.tint]
 *                           or [Kolt.colors.iconTint].
 * @param contentDescription Accessibility label for the button. Defaults to [icon.description].
 * @param enabled            When `false` the icon is dimmed and the menu will not open.
 * @param onItemSelected     Called with the selected item index after each selection.
 */
@Composable
fun AppsIconDropDown(
    items: List<UiText>,
    icon: UiImage,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    contentDescription: String? = icon.description,
    enabled: Boolean = true,
    onItemSelected: (index: Int) -> Unit,
) {
    DropDownSpinner(
        items = items,
        modifier = modifier,
        onSelectedIndexChange = { if (enabled) onItemSelected(it) },
    ) { _, _, _, onClick ->
        AppsIconButton(
            icon = icon,
            enabled = enabled,
            iconHeight = null,
            iconModifier = Modifier.size(Kolt.sizes.iconStandard),
            onClick = onClick,
        )
    }
}
