package io.github.appspiriment.kolt.composekmp.components.core.dropdowns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A plain-text dropdown trigger with an animated arrow indicator.
 *
 * Manages its own expanded/selected state via [DropDownSpinner]. The displayed text updates
 * to the selected item after a selection; before any selection [placeholderText] is shown.
 *
 * The trailing [DropdownArrow] rotates 180° when the menu opens, giving immediate visual
 * feedback that the list is expanded.
 *
 * For a full-featured dropdown with a floating label, error state, and icon support prefer
 * [AppsDropdown] instead.
 *
 * @param items           Options displayed in the dropdown menu.
 * @param modifier        Applied to the outer [DropDownSpinner] box.
 * @param placeholderText Text shown before any selection is made.
 * @param enabled         When `false` the trigger is dimmed and the menu will not open.
 * @param textStyle       Style for the trigger label.
 * @param textColor       Colour for the trigger label.
 * @param arrowTint       Colour of the trailing dropdown arrow.
 * @param arrowSize       Size of the dropdown arrow icon.
 * @param showArrow       Whether to show the trailing [DropdownArrow]. Default `true`.
 * @param containerColor  Background of the trigger pill. [Color.Transparent] = no background.
 * @param shape           Shape of the trigger pill.
 * @param onItemSelected  Called with the selected item index after each selection.
 */
@Composable
fun AppsTextDropDown(
    items: List<UiText>,
    modifier: Modifier = Modifier,
    placeholderText: UiText? = null,
    enabled: Boolean = true,
    textStyle: TextStyle = Kolt.typography.bodyMedium,
    textColor: Color = Kolt.colors.onMainSurface,
    arrowTint: Color = Kolt.colors.subText,
    arrowSize: Dp = Kolt.sizes.iconSmall,
    showArrow: Boolean = true,
    containerColor: Color = Color.Transparent,
    shape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusMedium),
    onItemSelected: (index: Int) -> Unit,
) {
    DropDownSpinner(
        items = items,
        modifier = modifier,
        placeholderText = placeholderText,
        onSelectedIndexChange = { if (enabled) onItemSelected(it) },
    ) { _, displayText, expanded, onClick ->
        Row(
            modifier = Modifier
                .alpha(if (enabled) 1f else 0.38f)   // dim before clipping — visual + ripple
                .clip(shape)
                .background(containerColor)
                .clickable(enabled = enabled) { onClick() }
                .padding(
                    horizontal = Kolt.sizes.paddingXSmall,
                    vertical = Kolt.sizes.paddingXXSmall,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingXXSmall),
        ) {
            AppspirimentText(
                text = displayText,
                style = textStyle,
                color = textColor,
            )
            if (showArrow) {
                DropdownArrow(
                    expanded = expanded,   // ← was always false; now correctly animated
                    tint = arrowTint,
                    size = arrowSize,
                )
            }
        }
    }
}
