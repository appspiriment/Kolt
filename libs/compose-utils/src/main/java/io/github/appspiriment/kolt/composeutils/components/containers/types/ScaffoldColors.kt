package io.github.appspiriment.kolt.composeutils.components.containers.types

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.github.appspiriment.kolt.composekmp.theme.Kolt


data class ScaffoldColors (
    val backgroundColor: Color,
    val drawerBackgroundColor: Color,
    val drawerItemColor: Color,
    val scrimColor: Color,
    val topBarBackground: Color,
    val onTopBarColor: Color
){
    companion object {
        @Composable
        fun defaults(
            backgroundColor: Color = Kolt.colors.background,
            drawerBackgroundColor: Color = Kolt.colors.background,
            drawerItemColor: Color = Kolt.colors.drawerItem,
            topBarBackground: Color = Kolt.colors.topAppBar,
            onTopBarColor: Color = Kolt.colors.onTopAppBar,
            scrimColor: Color = Kolt.colors.scrimColor
        ) : ScaffoldColors = ScaffoldColors (
            backgroundColor= backgroundColor,
            drawerBackgroundColor= drawerBackgroundColor,
            drawerItemColor= drawerItemColor,
            topBarBackground = topBarBackground,
            onTopBarColor = onTopBarColor,
            scrimColor = scrimColor
        )
    }
}