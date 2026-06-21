package io.github.appspiriment.kolt.composekmp.theme

import android.content.res.Configuration

internal fun computeWindowInfo(configuration: Configuration): WindowInfo {
    val screenWidthDp = configuration.screenWidthDp
    val screenHeightDp = configuration.screenHeightDp
    val smallestWidthDp = configuration.smallestScreenWidthDp
    val uiModeType = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK

    val widthSizeClass = when {
        screenWidthDp < 600 -> WidthSizeClass.Compact
        screenWidthDp < 840 -> WidthSizeClass.Medium
        else -> WidthSizeClass.Expanded
    }

    val heightSizeClass = when {
        screenHeightDp < 480 -> HeightSizeClass.Compact
        screenHeightDp < 900 -> HeightSizeClass.Medium
        else -> HeightSizeClass.Expanded
    }

    val formFactor = when {
        uiModeType == Configuration.UI_MODE_TYPE_TELEVISION -> FormFactor.TV
        smallestWidthDp >= 600 -> FormFactor.Tablet
        else -> FormFactor.Phone
    }

    // Use Configuration.orientation directly — accurate on all form factors.
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    return WindowInfo(widthSizeClass, heightSizeClass, formFactor, isLandscape)
}
