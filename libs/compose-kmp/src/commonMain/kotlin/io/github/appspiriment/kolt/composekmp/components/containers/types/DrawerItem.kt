package io.github.appspiriment.kolt.composekmp.components.containers.types

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.containers.DrawerIdentifier
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt

data class DrawerItem(
    val menuTitle: UiText,
    val icon: UiImage? = null,
    val trailingIcon: UiImage? = null,
    val showTopDivider: Boolean = false,
    val showBottomDivider: Boolean = false,
    val closeDrawer: Boolean = true,
    val textStyle: TextStyle? = null,
    val verticalPadding: Dp? = null,
    val drawerIdentifier: () -> DrawerIdentifier,
) {
    companion object {
        @Composable
        fun <T>from(
            menuTitle: UiText,
            icon: UiImage? = null,
            trailingIcon: UiImage? = null,
            showTopDivider: Boolean = false,
            showBottomDivider: Boolean = false,
            closeDrawer: Boolean = true,
            textStyle: TextStyle = Kolt.typography.textMedium,
            verticalPadding: Dp = Kolt.sizes.paddingSmallMedium,
            drawerIdentifier: () -> DrawerIdentifier,
        ) = DrawerItem(
            menuTitle = menuTitle,
            icon = icon,
            trailingIcon = trailingIcon,
            showTopDivider = showTopDivider,
            showBottomDivider = showBottomDivider,
            closeDrawer = closeDrawer,
            textStyle = textStyle,
            verticalPadding = verticalPadding,
            drawerIdentifier = drawerIdentifier
        )
    }
}
