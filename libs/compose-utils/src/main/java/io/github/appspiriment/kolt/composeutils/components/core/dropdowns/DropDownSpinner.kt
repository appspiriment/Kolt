package io.github.appspiriment.kolt.composeutils.components.core.dropdowns

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composeutils.components.core.dropdowns.models.DropDownItem
import io.github.appspiriment.kolt.composeutils.components.core.dropdowns.models.SpinnerStyle
import io.github.appspiriment.kolt.composeutils.components.core.dropdowns.models.SpinnerStyleDefaults
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt

@Composable
fun DropDownSpinner(
    items: List<UiText>,
    modifier: Modifier = Modifier,
    dropdownModifier: Modifier = Modifier,
    placeholderText: UiText? = null,
    onSelectedIndexChange: (Int) -> Unit = {},
    itemStyle: SpinnerStyle = SpinnerStyleDefaults.defaultSpinner,
    spinnerComposable: @Composable (index: Int, text: UiText, expanded: Boolean, onClick: ()->Unit) -> Unit
) {
    DropDownModelSpinner(
        items = items.map { DropDownItem(label = it) },
        modifier = modifier,
        dropdownModifier = dropdownModifier,
        placeholderText = placeholderText,
        onSelectedIndexChange = onSelectedIndexChange,
        itemStyle = itemStyle,
        spinnerComposable = spinnerComposable
    )
}

@Composable
fun DropDownModelSpinner(
    items: List<DropDownItem>,
    modifier: Modifier = Modifier,
    dropdownModifier: Modifier = Modifier,
    containerColor: Color = Kolt.colors.background,
    placeholderText: UiText? = null,
    onSelectedIndexChange: (Int) -> Unit = {},
    itemStyle: SpinnerStyle = SpinnerStyleDefaults.defaultSpinner,
    spinnerComposable: @Composable (index: Int, text: UiText, expanded: Boolean, onClick: () -> Unit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val selectedText by remember {
        derivedStateOf { items.getOrNull(selectedIndex)?.label ?: placeholderText }
    }
    Box(contentAlignment = Alignment.Center) {
        Box {
            spinnerComposable(selectedIndex, selectedText ?: "Not Selected".toUiText(), expanded) {
                expanded = true
            }
        }
        DropdownMenu(
            modifier = dropdownModifier,
            expanded = expanded,
            containerColor = containerColor,
            onDismissRequest = {
                expanded = false
            }) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        itemStyle.run {
                            AppspirimentText(
                                text = item.label,
                                modifier = modifier,
                                style = textStyle.copy(color = textColor),
                                textAlign =textAlign,
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        selectedIndex = index
                        onSelectedIndexChange(index)
                    }
                )
            }
        }
    }
}

/**
 * An animated dropdown arrow that rotates 180° when [expanded] is `true`.
 *
 * Used as the trailing decoration in [AppsTextDropDown] and any other trigger
 * that needs visual feedback of the open/closed state.
 */
@Composable
fun DropdownArrow(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.ArrowDropDown,
    tint: Color = io.github.appspiriment.kolt.composekmp.theme.Kolt.colors.subText,
    size: Dp = io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes.iconStandard,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "DropdownArrowRotation",
    )
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(size)
            .graphicsLayer { rotationZ = rotation },
    )
}
