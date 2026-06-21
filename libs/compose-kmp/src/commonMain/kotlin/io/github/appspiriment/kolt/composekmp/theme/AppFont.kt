package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.ui.text.font.FontFamily

/**
 * Describes which font family the library should use for all text styles.
 *
 * Pass an instance to [CompositionBaseProvider] (or [MalayalamCompositionBaseProvider]) once
 * at your root composable and every component in the tree will pick it up automatically.
 *
 * ## Built-in fonts (zero network / GMS dependency)
 * ```kotlin
 * CompositionBaseProvider(font = AppFont.Roboto) { … }   // default
 * CompositionBaseProvider(font = AppFont.Noto)   { … }   // Noto Sans (Indic scripts)
 * ```
 *
 * ## Any Google Font by name (requires GMS on the device)
 * ```kotlin
 * CompositionBaseProvider(font = AppFont.Google("Inter"))   { … }
 * CompositionBaseProvider(font = AppFont.Google("Poppins")) { … }
 * CompositionBaseProvider(font = AppFont.Google("Nunito"))  { … }
 * ```
 * All nine standard weights (Thin → Black) are registered automatically so weight
 * extensions like `.semiBold` or `.bold` work without extra setup.
 *
 * ## Your own pre-built FontFamily
 * ```kotlin
 * val myFont = FontFamily(
 *     Font(R.font.my_regular, FontWeight.Normal),
 *     Font(R.font.my_bold,    FontWeight.Bold),
 * )
 * CompositionBaseProvider(font = AppFont.Custom(myFont)) { … }
 * ```
 */
sealed class AppFont {

    /**
     * Roboto — bundled with compose-utils, all 9 weights available offline.
     * This is the **default** when no font is specified.
     */
    data object Roboto : AppFont()

    /**
     * Noto Sans — bundled with compose-utils, all 9 weights available offline.
     * Recommended for apps that display Indic scripts (Malayalam, Hindi, Tamil, etc.).
     * [MalayalamCompositionBaseProvider] defaults to this font automatically.
     */
    data object Noto : AppFont()

    /**
     * Loads any font from the [Google Fonts](https://fonts.google.com) catalog at runtime
     * via the GMS font provider. Requires Google Play Services on the device.
     *
     * All nine standard weights (Thin → Black) are registered so that weight
     * extension properties (`.semiBold`, `.bold`, etc.) work correctly.
     *
     * @param name The exact Google Fonts family name, e.g. `"Inter"`, `"Poppins"`, `"Nunito"`.
     */
    data class Google(val name: String) : AppFont()

    /**
     * Brings your own pre-built [FontFamily].
     * Use this for locally bundled assets, downloadable fonts you manage yourself, or
     * system font families.
     *
     * @param family The [FontFamily] to apply to all text styles.
     */
    data class Custom(val family: FontFamily) : AppFont()
}

