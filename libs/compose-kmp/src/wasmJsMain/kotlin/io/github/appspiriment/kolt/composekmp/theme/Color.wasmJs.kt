package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable

@Composable
actual fun baseColors(isDarkTheme: Boolean): BaseColors =
    if (isDarkTheme) getDarkColors() else getLightColors()

@Composable
actual fun baseUiColors(isDarkTheme: Boolean): BaseUiColors =
    baseColors(isDarkTheme).toUiColors()
