package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BaseFlags(
    val isNotoFont: Boolean = false,
    val notoFontPadding: Dp = 0.dp
)
val LocalFlags  by lazy { staticCompositionLocalOf { BaseFlags() } }
