package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.appspiriment.kolt.composekmp.R

actual object GoogleFonts {
    actual val robotoFamily: FontFamily = FontFamily(
        Font(R.font.font_roboto_thin, FontWeight.Thin),
        Font(R.font.font_roboto_extra_light, FontWeight.ExtraLight),
        Font(R.font.font_roboto_light, FontWeight.Light),
        Font(R.font.font_roboto_medium, FontWeight.Medium),
        Font(R.font.font_roboto_regular, FontWeight.Normal),
        Font(R.font.font_roboto_semi_bold, FontWeight.SemiBold),
        Font(R.font.font_roboto_bold, FontWeight.Bold),
        Font(R.font.font_roboto_extra_bold, FontWeight.ExtraBold),
        Font(R.font.font_roboto_black, FontWeight.Black),
    )
    actual val notoFamily: FontFamily = FontFamily(
        Font(R.font.noto_thin, FontWeight.Thin),
        Font(R.font.noto_extra_light, FontWeight.ExtraLight),
        Font(R.font.noto_light, FontWeight.Light),
        Font(R.font.noto_normal, FontWeight.Normal),
        Font(R.font.noto_medium, FontWeight.Medium),
        Font(R.font.noto_semi_bold, FontWeight.SemiBold),
        Font(R.font.noto_bold, FontWeight.Bold),
        Font(R.font.noto_extra_bold, FontWeight.ExtraBold),
        Font(R.font.noto_black, FontWeight.Black),
    )
}
