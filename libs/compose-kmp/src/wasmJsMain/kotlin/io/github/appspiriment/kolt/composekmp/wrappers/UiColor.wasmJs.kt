package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
actual fun UiColor.asColor(): Color {
    return when (this) {
        is UiColor.DynamicColor -> value
        is UiColor.ColorResource -> Color.Unspecified
        is UiColor.HexColor -> {
            parseHexColor(hex)
        }
    }
}

private fun parseHexColor(hex: String): Color {
    val cleanHex = hex.removePrefix("#")
    return try {
        when (cleanHex.length) {
            6 -> {
                val r = cleanHex.substring(0, 2).toInt(16)
                val g = cleanHex.substring(2, 4).toInt(16)
                val b = cleanHex.substring(4, 6).toInt(16)
                Color(r, g, b)
            }
            8 -> {
                val a = cleanHex.substring(0, 2).toInt(16)
                val r = cleanHex.substring(2, 4).toInt(16)
                val g = cleanHex.substring(4, 6).toInt(16)
                val b = cleanHex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> Color.Unspecified
        }
    } catch (e: Exception) {
        Color.Unspecified
    }
}
