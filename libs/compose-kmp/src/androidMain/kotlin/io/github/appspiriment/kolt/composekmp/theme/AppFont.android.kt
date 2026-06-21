package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont

internal fun AppFont.toFontFamily(provider: GoogleFont.Provider): FontFamily = when (this) {
    AppFont.Roboto  -> GoogleFonts.robotoFamily
    AppFont.Noto    -> GoogleFonts.notoFamily
    is AppFont.Google -> {
        val googleFont = GoogleFont(this.name)
        FontFamily(
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Thin),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.ExtraLight),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Light),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Normal),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Medium),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.SemiBold),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Bold),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.ExtraBold),
            Font(googleFont = googleFont, fontProvider = provider, weight = FontWeight.Black),
        )
    }
    is AppFont.Custom -> this.family
}
