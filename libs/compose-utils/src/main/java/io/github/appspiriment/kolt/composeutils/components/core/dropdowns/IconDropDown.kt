package io.github.appspiriment.kolt.composeutils.components.core.dropdowns

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText


@Composable
fun IconDropDown(
    items: List<UiText>,
    icon: UiImage,
    modifier: Modifier = Modifier,
    onItemSelected: (index: Int) -> Unit
) {
    DropDownSpinner(
        items = items,
        onSelectedIndexChange = { onItemSelected(it) },
    ) { _, _, _, onClick ->

        AppsIcon(
            icon = icon,
                modifier = modifier.clickable { onClick() }
            )
    }
}