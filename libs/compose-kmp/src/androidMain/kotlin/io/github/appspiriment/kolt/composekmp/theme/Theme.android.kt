package io.github.appspiriment.kolt.composekmp.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.R

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

@Composable
actual fun CompositionBaseProvider(
    isDarkTheme: Boolean?,
    font: AppFont,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current

    val (forcedContext, forcedConfiguration) = remember(isDarkTheme, context, configuration) {
        if (isDarkTheme != null) {
            val targetNightMode = if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            val newConfig = Configuration(configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or targetNightMode
            }

            val configContext = context.createConfigurationContext(newConfig)

            val wrappedContext = object : android.content.ContextWrapper(context) {
                override fun getResources(): android.content.res.Resources = configContext.resources
                override fun getAssets(): android.content.res.AssetManager = configContext.assets
                override fun getTheme(): android.content.res.Resources.Theme = configContext.theme
            }

            wrappedContext to newConfig
        } else {
            context to configuration
        }
    }

    CompositionLocalProvider(
        LocalContext provides forcedContext,
        LocalConfiguration provides forcedConfiguration
    ) {
        val resolvedDark = (forcedConfiguration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        val colors = baseColors(resolvedDark)
        val uiColors = baseUiColors(resolvedDark)

        val sizes = createSizes()
        val uiSizes = createUiSizes()

        val fontFamily = remember(font) { font.toFontFamily(provider) }
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
}
