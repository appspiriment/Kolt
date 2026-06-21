package io.github.appspiriment.kolt.composeutils.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import kotlin.math.max
import kotlin.math.min

// ── Luminance & contrast ───────────────────────────────────────────────────────

/** True if the color is perceptually light (luminance > 0.5). */
fun Color.isLight(): Boolean = ColorUtils.calculateLuminance(toArgb()) > 0.5

/** True if the color is perceptually dark (luminance ≤ 0.5). */
fun Color.isDark(): Boolean = !isLight()

/**
 * Returns [Color.Black] or [Color.White], whichever contrasts better against this color.
 * Useful for automatically picking text/icon color on top of a dynamic background.
 */
fun Color.onColor(): Color = if (isLight()) Color.Black else Color.White

/**
 * WCAG contrast ratio between this color and [other].
 * Values range from 1 (identical) to 21 (black on white).
 * WCAG AA requires ≥ 4.5 for normal text, ≥ 3.0 for large text.
 */
fun Color.contrastRatio(other: Color): Double {
    val l1 = ColorUtils.calculateLuminance(toArgb()) + 0.05
    val l2 = ColorUtils.calculateLuminance(other.toArgb()) + 0.05
    return if (l1 > l2) l1 / l2 else l2 / l1
}

// ── Darken / lighten ──────────────────────────────────────────────────────────

/**
 * Darkens the color by [factor] (0f = unchanged, 1f = black).
 */
fun Color.darken(factor: Float): Color {
    require(factor in 0f..1f) { "factor must be in 0..1, was $factor" }
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[2] = max(0f, hsl[2] * (1f - factor))
    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * Lightens the color by [factor] (0f = unchanged, 1f = white).
 */
fun Color.lighten(factor: Float): Color {
    require(factor in 0f..1f) { "factor must be in 0..1, was $factor" }
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[2] = min(1f, hsl[2] + (1f - hsl[2]) * factor)
    return Color(ColorUtils.HSLToColor(hsl))
}

// ── Blend ─────────────────────────────────────────────────────────────────────

/**
 * Blends this color with [other].
 * [ratio] = 0f → this color entirely, 1f → [other] entirely.
 */
fun Color.blend(other: Color, ratio: Float): Color {
    require(ratio in 0f..1f) { "ratio must be in 0..1, was $ratio" }
    return Color(ColorUtils.blendARGB(toArgb(), other.toArgb(), ratio))
}

// ── Hex string ────────────────────────────────────────────────────────────────

/**
 * Converts this color to a hex string.
 * @param includeAlpha If true, returns `#AARRGGBB` format; otherwise `#RRGGBB`.
 */
fun Color.toHexString(includeAlpha: Boolean = false): String {
    val argb = toArgb()
    return if (includeAlpha) {
        "#%08X".format(argb)
    } else {
        "#%06X".format(argb and 0x00FFFFFF)
    }
}

// ── Alpha ─────────────────────────────────────────────────────────────────────

/** Returns a copy of this color with the given [alpha] (0f–1f). */
fun Color.withAlpha(alpha: Float): Color = copy(alpha = alpha)

// ── Saturation ───────────────────────────────────────────────────────────────

/**
 * Returns a desaturated (greyscale) version of this color.
 * @param amount 0f = unchanged, 1f = fully greyscale.
 */
fun Color.desaturate(amount: Float = 1f): Color {
    require(amount in 0f..1f) { "amount must be in 0..1, was $amount" }
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[1] = max(0f, hsl[1] * (1f - amount))
    return Color(ColorUtils.HSLToColor(hsl))
}
