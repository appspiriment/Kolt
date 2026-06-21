package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

/**
 * Theme provider pre-configured for Noto Sans — recommended for apps that display
 * Indic scripts (Malayalam, Hindi, Tamil, etc.).
 *
 * You can override the font with any [AppFont] value:
 * ```kotlin
 * // Still use Noto (default for this provider)
 * MalayalamCompositionBaseProvider { … }
 *
 * // Switch to a Google Font while keeping other Malayalam-friendly defaults
 * MalayalamCompositionBaseProvider(font = AppFont.Google("Noto Serif Malayalam")) { … }
 * ```
 */
@Composable
fun MalayalamCompositionBaseProvider(
    isDarkTheme: Boolean? = null,
    font: AppFont = AppFont.Noto,
    content: @Composable () -> Unit
) {
    CompositionBaseProvider(isDarkTheme = isDarkTheme, font = font, content = content)
}

/**
 * Root theme provider for all `compose-utils` components.
 */
@Composable
expect fun CompositionBaseProvider(
    isDarkTheme: Boolean? = null,
    font: AppFont = AppFont.Roboto,
    content: @Composable () -> Unit
)

object Kolt {
    val colors: BaseColors
        @Composable @ReadOnlyComposable get() = LocalColors.current
    val uiColors: BaseUiColors
        @Composable @ReadOnlyComposable get() = LocalUiColors.current
    val sizes: Sizes
        @Composable @ReadOnlyComposable get() = LocalSizes.current
    val uiSizes: UiSizes
        @Composable @ReadOnlyComposable get() = LocalUiSizes.current
    val typography: BaseTextStyles
        @Composable @ReadOnlyComposable get() = LocalTypography.current
    val flags: BaseFlags
        @Composable @ReadOnlyComposable get() = LocalFlags.current
}
