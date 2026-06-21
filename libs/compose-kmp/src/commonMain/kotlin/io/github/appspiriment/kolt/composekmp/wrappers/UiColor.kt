package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A sealed class to handle different types of colors in Compose UI.
 *
 * This class allows you to represent colors as a raw Color, a color resource ID,
 * or a hex string. It provides a unified way to manage colors in your UI,
 * regardless of its source.
 */
sealed class UiColor {
    companion object {
        val Black = DynamicColor(Color.Black)
        val White = DynamicColor(Color.White)
        val Transparent = DynamicColor(Color.Transparent)
        val Unspecified = DynamicColor(Color.Unspecified)
    }

    /**
     * Represents a raw Color.
     *
     * @property value The raw Color value.
     */
    data class DynamicColor(val value: Color) : UiColor()

    /**
     * Represents a color resource ID.
     *
     * @property resId The color resource ID.
     */
    class ColorResource(val resId: Int) : UiColor()

    /**
     * Represents a color defined by a hex string.
     *
     * @property hex The hex string representation of the color (e.g., "#FF0000" for red).
     */
    data class HexColor(val hex: String) : UiColor() {
        init {
            require(hex.matches(Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8})$"))) {
                "Invalid hex color format. Must be #RRGGBB or #AARRGGBB"
            }
        }
    }
}

/**
 * Extension function to convert a Color to a UiColor.
 */
fun Color.toUiColor() = UiColor.DynamicColor(this)

/**
 * Extension function to convert a color resource id to a UiColor.
 */
fun Int.toUiColorResource() = UiColor.ColorResource(this)

/**
 * Extension function to convert a hex string to a UiColor.
 */
fun String.toUiColorHex() = UiColor.HexColor(this)

/**
 * Composable function to get a UiColor from a color resource ID.
 *
 * @param id The color resource ID.
 * @return The UiColor representing the color resource.
 */
@Composable
fun uiColorResource(id: Int): UiColor {
    return UiColor.ColorResource(id)
}

@Composable
expect fun UiColor.asColor(): Color