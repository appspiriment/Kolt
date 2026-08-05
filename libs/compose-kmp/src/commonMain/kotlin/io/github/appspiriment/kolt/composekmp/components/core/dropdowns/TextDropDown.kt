package io.github.appspiriment.kolt.composekmp.components.core.dropdowns

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.wrappers.UiText


@Composable
fun TextDropDown(
    items: List<UiText>,
    onItemSelected: (index: Int) -> Unit
) {
    DropDownSpinner(
        items = items,
        onSelectedIndexChange = { onItemSelected(it) },
    ) { index, item, _, onClick ->

        AppspirimentText(
            text = item,
            modifier = Modifier.clickable { onClick() }
        )
    }
}