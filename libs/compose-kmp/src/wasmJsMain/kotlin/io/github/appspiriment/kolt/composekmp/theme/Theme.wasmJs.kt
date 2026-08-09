package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
actual fun CompositionBaseProvider(
    isDarkTheme: Boolean?,
    font: AppFont,
    content: @Composable () -> Unit
) {
    val dark = isDarkTheme ?: androidx.compose.foundation.isSystemInDarkTheme()
    val colors = baseColors(dark)
    val uiColors = baseUiColors(dark)

    val sizes = createSizes()
    val uiSizes = createUiSizes()

    val fontFamily = remember(font) {
        when (font) {
            AppFont.Roboto -> GoogleFonts.robotoFamily
            AppFont.Noto -> GoogleFonts.notoFamily
            is AppFont.Google -> FontFamily.Default
            is AppFont.Custom -> font.family
        }
    }
    val typography = createBaseTypography(baseSize = sizes, fontFamily = fontFamily)

    val isNotoFont = font is AppFont.Noto
    val flags = BaseFlags(
        isNotoFont = isNotoFont,
        notoFontPadding = if (isNotoFont) 0.dp else 0.dp
    )

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalUiColors provides uiColors,
        LocalSizes provides sizes,
        LocalUiSizes provides uiSizes,
        LocalTypography provides typography,
        LocalFlags provides flags,
        content = content
    )
}
