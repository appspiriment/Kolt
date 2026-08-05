package io.github.appspiriment.kolt.composekmp.components.core.modifiers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// ── Conditional application ───────────────────────────────────────────────────

/**
 * Applies [ifTrue] when [condition] is true, or [ifFalse] when false.
 *
 * Usage:
 * ```
 * Modifier.conditional(isSelected) { background(selectedColor) }
 * ```
 */
fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: (Modifier.() -> Modifier)? = null,
): Modifier = if (condition) then(ifTrue(Modifier))
else if (ifFalse != null) then(ifFalse(Modifier))
else this

/**
 * Makes the composable clickable only when [enabled] is true.
 * When disabled, no ripple or interaction is shown.
 */
fun Modifier.clickableIf(enabled: Boolean, onClick: (() -> Unit)?): Modifier =
    if (enabled && onClick != null) clickable(onClick = onClick) else this

// ── Visibility ────────────────────────────────────────────────────────────────

/**
 * Hides the composable (alpha = 0) when [visible] is false, but it still occupies space.
 */
fun Modifier.visibleIf(visible: Boolean): Modifier = alpha(if (visible) 1f else 0f)

/**
 * Removes the composable from the layout completely when [gone] is true — no space reserved.
 * The content is still composed (for state preservation), but its measured size is 0×0.
 */
fun Modifier.goneIf(gone: Boolean): Modifier = layout { measurable, constraints ->
    if (gone) {
        measurable.measure(Constraints())
        layout(0, 0) {}
    } else {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

// ── Private constants ─────────────────────────────────────────────────────────

/** Conventional fading-edge length for list scroll fade-outs. No theme token at this value. */
private val FADE_EDGE_DEFAULT_LENGTH = 32.dp

// ── RTL support ───────────────────────────────────────────────────────────────

/**
 * Flips the composable horizontally in RTL layouts.
 * Useful for directional icons (arrows, chevrons, back buttons).
 */
fun Modifier.mirrorForRtl(): Modifier = composed {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    if (isRtl) graphicsLayer { rotationY = 180f } else this
}

// ── Drawing ───────────────────────────────────────────────────────────────────

/**
 * Draws a dashed border around the composable.
 *
 * Useful for upload zones, drop targets, or placeholder cards.
 */
fun Modifier.dashedBorder(
    strokeWidth: Dp = 1.dp,
    color: Color,
    cornerRadius: Dp = 0.dp,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 4.dp,
): Modifier = drawWithContent {
    drawContent()
    drawRoundRect(
        color = color,
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                phase = 0f,
            ),
        ),
        cornerRadius = CornerRadius(cornerRadius.toPx()),
    )
}

// ponytail: coloredShadow (Android-only, used android.graphics.Paint/nativeCanvas directly)
// dropped from the commonMain port — not portable without a per-platform drawIntoCanvas
// expect/actual. Add an androidMain/desktopMain/iosMain actual only if a real caller needs it.

/**
 * Adds a gradient fade on one or more edges — ideal for horizontally/vertically scrolling lists.
 *
 * Usage:
 * ```
 * LazyRow(
 *     modifier = Modifier.fadingEdge(FadeEdge.Start, FadeEdge.End)
 * )
 * ```
 */
enum class FadeEdge { Top, Bottom, Start, End }

fun Modifier.fadingEdge(
    vararg edges: FadeEdge,
    length: Dp = FADE_EDGE_DEFAULT_LENGTH,
): Modifier = graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        for (edge in edges) {
            val brush = when (edge) {
                FadeEdge.Top -> Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startY = 0f,
                    endY = length.toPx(),
                )
                FadeEdge.Bottom -> Brush.verticalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startY = size.height - length.toPx(),
                    endY = size.height,
                )
                FadeEdge.Start -> Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                    startX = 0f,
                    endX = length.toPx(),
                )
                FadeEdge.End -> Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent),
                    startX = size.width - length.toPx(),
                    endX = size.width,
                )
            }
            drawRect(brush = brush, blendMode = BlendMode.DstIn)
        }
    }

// ── Interaction animations ────────────────────────────────────────────────────

/**
 * Scales down on tap, springs back on release, and triggers [onClick].
 * Replaces a standard `clickable` — do not combine both.
 */
fun Modifier.bounceClick(
    scaleTo: Float = 0.93f,
    onClick: () -> Unit,
): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
        .pointerInput(onClick) {
            detectTapGestures(
                onPress = {
                    scope.launch {
                        scale.animateTo(scaleTo, spring(stiffness = Spring.StiffnessHigh))
                    }
                    tryAwaitRelease()
                    scope.launch {
                        scale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                },
                onTap = { onClick() },
            )
        }
}

/**
 * Scales down while the pointer is pressed, without consuming the click.
 * Safe to combine with `clickable {}` or `combinedClickable {}`.
 */
fun Modifier.scaleOnPress(scaleTo: Float = 0.96f): Modifier = composed {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    graphicsLayer { scaleX = scale.value; scaleY = scale.value }
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    scope.launch {
                        scale.animateTo(scaleTo, spring(stiffness = Spring.StiffnessHigh))
                    }
                    tryAwaitRelease()
                    scope.launch {
                        scale.animateTo(1f, spring(stiffness = Spring.StiffnessMediumLow))
                    }
                },
            )
        }
}

/**
 * Triggers a horizontal shake animation whenever [trigger] changes to a non-null value.
 * Typically used to highlight a validation error.
 *
 * Usage:
 * ```
 * var errorTrigger by remember { mutableStateOf<Any?>(null) }
 * TextField(modifier = Modifier.shake(errorTrigger))
 * Button(onClick = { errorTrigger = Any() }) { ... }
 * ```
 */
fun Modifier.shake(
    trigger: Any?,
    amplitude: Dp = 8.dp,
): Modifier = composed {
    val offsetX = remember { Animatable(0f) }

    LaunchedEffect(trigger) {
        if (trigger != null) {
            repeat(3) {
                offsetX.animateTo(amplitude.value, spring(stiffness = Spring.StiffnessHigh))
                offsetX.animateTo(-amplitude.value, spring(stiffness = Spring.StiffnessHigh))
            }
            offsetX.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
        }
    }

    offset(x = offsetX.value.dp)
}
