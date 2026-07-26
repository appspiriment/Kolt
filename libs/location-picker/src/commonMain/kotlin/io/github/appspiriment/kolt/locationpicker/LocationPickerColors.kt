package io.github.appspiriment.kolt.locationpicker

import androidx.compose.ui.graphics.Color

/**
 * The handful of colors [LocationPickerScreen] needs to blend into a host app's own theme.
 * Pass this to [LocationPickerScreen]'s `colors` parameter to override — leave it `null` (the
 * default) to inherit whatever ambient theme each platform renderer would otherwise use
 * (compose-kmp's `Kolt.colors` on Android/iOS/Desktop, `MaterialTheme.colorScheme` on Web).
 */
data class LocationPickerColors(
    val accent: Color,
    val onAccent: Color,
    val background: Color,
    val surface: Color,
    val onSurface: Color,
    val subText: Color,
    val border: Color,
    val error: Color,
)
