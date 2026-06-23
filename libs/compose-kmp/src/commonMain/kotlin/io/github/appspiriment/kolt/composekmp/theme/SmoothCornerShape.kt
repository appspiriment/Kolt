package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A custom [Shape] that draws squircles (superellipses) using cubic Bezier curves to achieve
 * smooth, premium corner rounding (G2 continuity).
 * This mathematical implementation does not require external dependencies and compiles for all platforms.
 *
 * @param topLeftRadius The corner radius for the top-left corner.
 * @param topRightRadius The corner radius for the top-right corner.
 * @param bottomRightRadius The corner radius for the bottom-right corner.
 * @param bottomLeftRadius The corner radius for the bottom-left corner.
 * @param smoothness Curvature continuity factor between 0f (standard rounded corner) and 1f (continuous smooth corner). Default is 0.55f (iOS-style).
 */
class SmoothCornerShape(
    val topLeftRadius: Dp,
    val topRightRadius: Dp,
    val bottomRightRadius: Dp,
    val bottomLeftRadius: Dp,
    val smoothness: Float = 0.55f
) : Shape {

    /**
     * Primary uniform constructor.
     *
     * @param cornerRadius Corner radius applied to all four corners.
     * @param smoothness Curvature continuity factor between 0f and 1f. Default is 0.55f.
     */
    constructor(cornerRadius: Dp = 12.dp, smoothness: Float = 0.55f) : this(
        topLeftRadius = cornerRadius,
        topRightRadius = cornerRadius,
        bottomRightRadius = cornerRadius,
        bottomLeftRadius = cornerRadius,
        smoothness = smoothness
    )

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        val s = smoothness.coerceIn(0f, 1f)

        // Convert Dp to pixels
        val tl = with(density) { topLeftRadius.toPx() }.coerceAtLeast(0f)
        val tr = with(density) { topRightRadius.toPx() }.coerceAtLeast(0f)
        val br = with(density) { bottomRightRadius.toPx() }.coerceAtLeast(0f)
        val bl = with(density) { bottomLeftRadius.toPx() }.coerceAtLeast(0f)

        // Ensure corners do not overlap (taking smoothness factor into account)
        val maxRadius = (w / 2f).coerceAtMost(h / 2f) / (1f + s)
        val rTl = tl.coerceAtMost(maxRadius)
        val rTr = tr.coerceAtMost(maxRadius)
        val rBr = br.coerceAtMost(maxRadius)
        val rBl = bl.coerceAtMost(maxRadius)

        val path = Path().apply {
            moveTo(w / 2f, 0f)

            // Constant for circular arc Bezier approximation: (4/3) * (sqrt(2) - 1)
            val kCircle = 0.5522847f
            val kBlend = kCircle + s * (1f - kCircle)

            // Top-right corner
            if (rTr > 0f) {
                val startOffset = rTr * (1f + s)
                val controlOffset = rTr * kBlend
                lineTo(w - startOffset, 0f)
                cubicTo(
                    x1 = w - startOffset + controlOffset, y1 = 0f,
                    x2 = w, y2 = startOffset - controlOffset,
                    x3 = w, y3 = startOffset
                )
            } else {
                lineTo(w, 0f)
            }

            // Bottom-right corner
            if (rBr > 0f) {
                val startOffset = rBr * (1f + s)
                val controlOffset = rBr * kBlend
                lineTo(w, h - startOffset)
                cubicTo(
                    x1 = w, y1 = h - startOffset + controlOffset,
                    x2 = w - startOffset + controlOffset, y2 = h,
                    x3 = w - startOffset, y3 = h
                )
            } else {
                lineTo(w, h)
            }

            // Bottom-left corner
            if (rBl > 0f) {
                val startOffset = rBl * (1f + s)
                val controlOffset = rBl * kBlend
                lineTo(startOffset, h)
                cubicTo(
                    x1 = startOffset - controlOffset, y1 = h,
                    x2 = 0f, y2 = h - startOffset + controlOffset,
                    x3 = 0f, y3 = h - startOffset
                )
            } else {
                lineTo(0f, h)
            }

            // Top-left corner
            if (rTl > 0f) {
                val startOffset = rTl * (1f + s)
                val controlOffset = rTl * kBlend
                lineTo(0f, startOffset)
                cubicTo(
                    x1 = 0f, y1 = startOffset - controlOffset,
                    x2 = startOffset - controlOffset, y2 = 0f,
                    x3 = startOffset, y3 = 0f
                )
            } else {
                lineTo(0f, 0f)
            }

            close()
        }

        return Outline.Generic(path)
    }
}
