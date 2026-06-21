package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

internal expect val platformTextStyleNoPadding: PlatformTextStyle?

object TextStyles {
    val NoPaddingStyle = TextStyle(
        platformStyle = platformTextStyleNoPadding,
    )
}

val TextStyle.noPadding: TextStyle
    get() = this.copy(
        platformStyle = platformTextStyleNoPadding
    )

/**
 * Switches this [TextStyle] to the Roboto font family (bundled with compose-utils).
 *
 * Useful for one-off overrides when the theme-level font is something else:
 * ```kotlin
 * AppspirimentText(
 *     text = "Label".toUiText(),
 *     style = Kolt.typography.textSmall.roboto
 * )
 * ```
 */
val TextStyle.roboto: TextStyle
    get() = this.copy(fontFamily = GoogleFonts.robotoFamily)

/**
 * Switches this [TextStyle] to the Noto Sans font family (bundled with compose-utils).
 *
 * Useful for one-off overrides when you want Noto Sans on a specific text element
 * without changing the whole theme:
 * ```kotlin
 * AppspirimentText(
 *     text = "മലയാളം".toUiText(),
 *     style = Kolt.typography.textMedium.noto
 * )
 * ```
 */
val TextStyle.noto: TextStyle
    get() = this.copy(fontFamily = GoogleFonts.notoFamily)


data class BaseTextStyles(
    val baseTextStyle: TextStyle = TextStyle.Default,
    val textMinimum: TextStyle = TextStyle.Default,
    val textTiny: TextStyle = TextStyle.Default,
    val textXXXSmall: TextStyle = TextStyle.Default,
    val textXXSmall: TextStyle = TextStyle.Default,
    val textXSmall: TextStyle = TextStyle.Default,
    val textXSmallMedium: TextStyle = TextStyle.Default,
    val textSmall: TextStyle = TextStyle.Default,
    val textSmallMedium: TextStyle = TextStyle.Default,
    val textMedium: TextStyle = TextStyle.Default,
    val textMediumMid: TextStyle = TextStyle.Default,
    val textMediumLarge: TextStyle = TextStyle.Default,
    val textLarge: TextStyle = TextStyle.Default,
    val textXLarge: TextStyle = TextStyle.Default,
    val textXXLarge: TextStyle = TextStyle.Default,
    val textXXXLarge: TextStyle = TextStyle.Default,
    val textBig: TextStyle = TextStyle.Default,
    val textXBig: TextStyle = TextStyle.Default,
    val textHuge: TextStyle = TextStyle.Default,
    val textGiant: TextStyle = TextStyle.Default,
) {
    // ── Material3-compatible aliases ──────────────────────────────────────────
    // These allow components designed with M3 typography names to work with the
    // Kolt theme without mechanical find-and-replace in each file.
    // The mappings follow rough visual equivalence (M3 body scale ≈ Kolt
    // text scale shifted by ~2 steps).

    /** M3 alias → [textXXSmall] (≈10 sp) */
    val bodyXXXSmall: TextStyle get() = textXXSmall
    /** M3 alias → [textXSmall] (≈12 sp) */
    val bodyXSmall: TextStyle get() = textXSmall
    /** M3 alias → [textSmall] (≈13 sp) */
    val bodySmall: TextStyle get() = textSmall
    /** M3 alias → [textMedium] (≈14 sp) */
    val bodyMedium: TextStyle get() = textMedium
    /** M3 alias → [textMediumMid] (≈15 sp) */
    val bodyMediumLarge: TextStyle get() = textMediumMid
    /** M3 alias → [textLarge] (≈16 sp) */
    val bodyLarge: TextStyle get() = textLarge
    /** M3 alias → [textSmall] */
    val labelSmall: TextStyle get() = textSmall
    /** M3 alias → [textSmallMedium] */
    val labelMedium: TextStyle get() = textSmallMedium
    /** M3 alias → [textMedium] */
    val labelLarge: TextStyle get() = textMedium
    /** M3 alias → [textMediumLarge] (≈16 sp) */
    val titleSmall: TextStyle get() = textMediumLarge
    /** M3 alias → [textLarge] (≈18 sp) */
    val titleMedium: TextStyle get() = textLarge
    /** M3 alias → [textXLarge] (≈20 sp) */
    val titleLarge: TextStyle get() = textXLarge
    /** M3 alias → [textXXLarge] (≈24 sp) */
    val headlineSmall: TextStyle get() = textXXLarge
    /** M3 alias → [textXXXLarge] (≈28 sp) */
    val headlineMedium: TextStyle get() = textXXXLarge
    /** M3 alias → [textBig] (≈32 sp) */
    val displaySmall: TextStyle get() = textBig
}

@Composable
internal fun createBaseTypography(baseSize: Sizes, fontFamily: FontFamily?): BaseTextStyles {
    val baseTextStyle = TextStyle.Default.copy(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        platformStyle = platformTextStyleNoPadding
    )
    return BaseTextStyles(
        baseTextStyle = baseTextStyle,
        textMinimum = baseTextStyle.copy(
            fontSize = baseSize.fontSizeMinimum
        ),
        textTiny = baseTextStyle.copy(
            fontSize = baseSize.fontSizeTiny
        ),
        textXXXSmall = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXXXSmall
        ),
        textXXSmall = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXXSmall
        ),
        textXSmall = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXSmall
        ),
        textXSmallMedium = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXSmallMedium
        ),
        textSmall = baseTextStyle.copy(
            fontSize = baseSize.fontSizeSmall
        ),
        textSmallMedium = baseTextStyle.copy(
            fontSize = baseSize.fontSizeSmallMedium
        ),
        textMedium = baseTextStyle.copy(
            fontSize = baseSize.fontSizeMedium
        ),
        textMediumMid = baseTextStyle.copy(
            fontSize = baseSize.fontSizeMediumMid
        ),
        textMediumLarge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeMediumLarge
        ),
        textLarge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeLarge
        ),
        textXLarge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXLarge
        ),
        textXXLarge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXXLarge
        ),
        textXXXLarge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXXXLarge
        ),
        textBig = baseTextStyle.copy(
            fontSize = baseSize.fontSizeBig
        ),
        textXBig = baseTextStyle.copy(
            fontSize = baseSize.fontSizeXBig
        ),
        textHuge = baseTextStyle.copy(
            fontSize = baseSize.fontSizeHuge
        ),
        textGiant = baseTextStyle.copy(
            fontSize = baseSize.fontSizeGiant
        ),

    )
}

val TextStyle.thin get() = this.copy(fontWeight = FontWeight.Thin)
val TextStyle.extraLight get() = this.copy(fontWeight = FontWeight.ExtraLight)
val TextStyle.light get() = this.copy(fontWeight = FontWeight.Light)
val TextStyle.normal get() = this.copy(fontWeight = FontWeight.Normal)
val TextStyle.medium get() = this.copy(fontWeight = FontWeight.Medium)
val TextStyle.semiBold get() = this.copy(fontWeight = FontWeight.SemiBold)
val TextStyle.bold get() = this.copy(fontWeight = FontWeight.Bold)
val TextStyle.extraBold get() = this.copy(fontWeight = FontWeight.ExtraBold)
val TextStyle.black get() = this.copy(fontWeight = FontWeight.Black)



val TextStyle.italic get() = this.copy(fontStyle = FontStyle.Italic)
val TextStyle.thinItalic get() = this.copy(fontWeight = FontWeight.Thin, fontStyle = FontStyle.Italic)
val TextStyle.extraLightItalic get() = this.copy(fontWeight = FontWeight.ExtraLight, fontStyle = FontStyle.Italic)
val TextStyle.lightItalic get() = this.copy(fontWeight = FontWeight.Light, fontStyle = FontStyle.Italic)
val TextStyle.mediumItalic get() = this.copy(fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic)
val TextStyle.semiBoldItalic get() = this.copy(fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic)
val TextStyle.boldItalic get() = this.copy(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
val TextStyle.extraBoldItalic get() = this.copy(fontWeight = FontWeight.ExtraBold, fontStyle = FontStyle.Italic)
val TextStyle.blackItalic get() = this.copy(fontWeight = FontWeight.Black, fontStyle = FontStyle.Italic)

val LocalTypography  by lazy { staticCompositionLocalOf { BaseTextStyles() } }

