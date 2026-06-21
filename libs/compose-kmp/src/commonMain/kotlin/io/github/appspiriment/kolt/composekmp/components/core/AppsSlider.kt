package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import kotlin.math.roundToInt

/**
 * A premium, custom-drawn slider component.
 * Features a sleek thin track, gradient progress fill, glowing thumb, and floating value tooltip.
 * Provides tactile haptic feedback on snaps/changes.
 *
 * @param value The current progress value.
 * @param onValueChange Callback to trigger when the slider value changes.
 * @param modifier Modifier to be applied to the slider box layout.
 * @param valueRange Range of values that this slider can represent. Defaults to 0f..1f.
 * @param steps If greater than 0, specifies the number of discrete snap points.
 * @param onValueChangeFinished Optional callback when drag/tap gesture finishes.
 * @param enabled True if interactive, false otherwise.
 * @param activeTrackGradient Gradient brush for the active progress track.
 * @param inactiveTrackColor Background color of the inactive track.
 * @param thumbColor Fill color of the inner thumb circle.
 * @param thumbGlowColor Color of the thumb glow background.
 */
@Composable
fun AppsSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    enabled: Boolean = true,
    activeTrackGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Kolt.colors.primary, Kolt.colors.primary.copy(alpha = 0.6f))
    ),
    inactiveTrackColor: Color = Kolt.colors.dividerColor.copy(alpha = 0.4f),
    thumbColor: Color = Kolt.colors.primary,
    thumbGlowColor: Color = Kolt.colors.primary.copy(alpha = 0.2f),
) {
    BoxWithConstraints(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .requiredHeightIn(min = 36.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val density = LocalDensity.current
        val hapticFeedback = LocalHapticFeedback.current
        val disabledIconColor = Kolt.colors.disabledIconTint

        var isDragging by remember { mutableStateOf(false) }
        val dragProgress = ((value - valueRange.start) / (valueRange.endInclusive - valueRange.start)).coerceIn(0f, 1f)
        val thumbX = dragProgress * widthPx

        val tooltipAlpha by animateFloatAsState(
            targetValue = if (isDragging) 1f else 0f,
            animationSpec = tween(durationMillis = 150)
        )
        val thumbScale by animateFloatAsState(
            targetValue = if (isDragging) 1.25f else 1f,
            animationSpec = tween(durationMillis = 150)
        )

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(enabled, valueRange, steps, widthPx) {
                    if (!enabled) return@pointerInput
                    detectTapGestures(
                        onPress = { offset ->
                            isDragging = true
                            val newValue = calculateValue(offset.x, widthPx, valueRange, steps)
                            if (newValue != value) {
                                onValueChange(newValue)
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            tryAwaitRelease()
                            isDragging = false
                            onValueChangeFinished?.invoke()
                        }
                    )
                }
                .pointerInput(enabled, valueRange, steps, widthPx) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = {
                            isDragging = false
                            onValueChangeFinished?.invoke()
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentThumbX = (dragProgress * widthPx) + dragAmount.x
                            val newValue = calculateValue(currentThumbX, widthPx, valueRange, steps)
                            if (newValue != value) {
                                onValueChange(newValue)
                                if (steps > 0) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                } else if (newValue.roundToInt() != value.roundToInt()) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                        }
                    )
                }
        ) {
            val trackY = size.height / 2f
            val trackHeight = 4.dp.toPx()

            // Draw Inactive Track
            drawLine(
                color = inactiveTrackColor,
                start = Offset(0f, trackY),
                end = Offset(size.width, trackY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Draw Active Track with horizontal gradient
            drawLine(
                brush = activeTrackGradient,
                start = Offset(0f, trackY),
                end = Offset(thumbX, trackY),
                strokeWidth = trackHeight,
                cap = StrokeCap.Round
            )

            // Draw thumb glow bubble
            drawCircle(
                color = thumbGlowColor,
                radius = 16.dp.toPx() * thumbScale,
                center = Offset(thumbX, trackY)
            )

            // Draw inner sleek thumb
            drawCircle(
                color = if (enabled) thumbColor else disabledIconColor,
                radius = 8.dp.toPx() * thumbScale,
                center = Offset(thumbX, trackY)
            )
        }

        // Draw floating value tooltip
        if (tooltipAlpha > 0f) {
            val tooltipWidth = 48.dp
            val halfTooltipWidth = tooltipWidth / 2
            val thumbXDp = with(density) { thumbX.toDp() }
            val maxOffsetDp = (this.maxWidth - tooltipWidth).coerceAtLeast(0.dp)
            val tooltipXDp = (thumbXDp - halfTooltipWidth).coerceIn(0.dp, maxOffsetDp)

            Box(
                modifier = Modifier
                    .offset(x = tooltipXDp, y = (-28).dp)
                    .alpha(tooltipAlpha)
                    .background(
                        color = Kolt.colors.primary,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                AppspirimentText(
                    text = UiText.DynamicString(value.roundToInt().toString()),
                    style = Kolt.typography.bodyXXXSmall.semiBold,
                    color = Kolt.colors.onPrimary
                )
            }
        }
    }
}

/**
 * A sleek, custom horizontal progress bar with a thin elegant layout and gradient progress indicator.
 *
 * @param progress Current progress value between 0f and 1f.
 * @param modifier Modifier to apply to the progress bar container.
 * @param trackGradient Gradient brush for active progress.
 * @param inactiveTrackColor Color of the background empty track.
 */
@Composable
fun AppsProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    trackGradient: Brush = Brush.horizontalGradient(
        colors = listOf(Kolt.colors.primary, Kolt.colors.primary.copy(alpha = 0.6f))
    ),
    inactiveTrackColor: Color = Kolt.colors.dividerColor.copy(alpha = 0.4f),
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300)
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp) // padded bounds for click targets / alignments
    ) {
        val trackY = size.height / 2f
        val trackHeight = 4.dp.toPx()

        // Draw Inactive Track
        drawLine(
            color = inactiveTrackColor,
            start = Offset(0f, trackY),
            end = Offset(size.width, trackY),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )

        // Draw Active Track
        drawLine(
            brush = trackGradient,
            start = Offset(0f, trackY),
            end = Offset(size.width * animatedProgress, trackY),
            strokeWidth = trackHeight,
            cap = StrokeCap.Round
        )
    }
}

private fun calculateValue(
    x: Float,
    widthPx: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int
): Float {
    val progress = (x / widthPx).coerceIn(0f, 1f)
    val rawValue = valueRange.start + progress * (valueRange.endInclusive - valueRange.start)
    if (steps > 0) {
        val stepSize = (valueRange.endInclusive - valueRange.start) / (steps + 1)
        val stepIndex = ((rawValue - valueRange.start) / stepSize).roundToInt()
        return (valueRange.start + stepIndex * stepSize).coerceIn(valueRange)
    }
    return rawValue
}
