package io.github.appspiriment.kolt.composekmp.utils

import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * WCAG Relative Luminance formula: L = 0.2126 * R + 0.7152 * G + 0.0722 * B
 */
fun Color.luminance(): Float {
    fun linearize(component: Float): Float {
        return if (component <= 0.03928f) {
            component / 12.92f
        } else {
            ((component + 0.055f) / 1.055f).pow(2.4f)
        }
    }
    return 0.2126f * linearize(red) + 0.7152f * linearize(green) + 0.0722f * linearize(blue)
}

/** True if the color is perceptually light (luminance > 0.5). */
fun Color.isLight(): Boolean = luminance() > 0.5f

/** True if the color is perceptually dark (luminance <= 0.5). */
fun Color.isDark(): Boolean = !isLight()

/**
 * Returns [Color.Black] or [Color.White], whichever contrasts better against this color.
 * Useful for automatically picking text/icon color on top of a dynamic background.
 */
fun Color.onColor(): Color = if (isLight()) Color.Black else Color.White
