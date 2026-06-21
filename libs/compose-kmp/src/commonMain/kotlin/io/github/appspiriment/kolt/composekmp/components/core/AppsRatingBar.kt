package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt

private val StarHalf: ImageVector by lazy {
    ImageVector.Builder(
        name = "StarHalf",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes("M22 9.24l-7.19-.62L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21 12 17.27 18.18 21l-1.63-7.03L22 9.24zM12 15.4V6.1l1.71 4.04 4.38.38-3.32 2.88.99 4.29L12 15.4z"),
        fill = SolidColor(Color.Black)
    ).build()
}

private val StarBorder: ImageVector by lazy {
    ImageVector.Builder(
        name = "StarBorder",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).addPath(
        pathData = addPathNodes("M22,9.24l-7.19,-0.62L12,2 9.19,8.63 2,9.24l5.46,4.73L5.82,21 12,17.27 18.18,21l-1.63,-7.03L22,9.24zM12,15.4l-3.76,2.27 1,-4.28 -3.32,-2.88 4.38,-0.38L12,6.1l1.71 4.04 4.38,0.38 -3.32,2.88 1,4.28L12,15.4z"),
        fill = SolidColor(Color.Black)
    ).build()
}




/**
 * A star rating bar — read-only or interactive.
 *
 * ### Display
 * Stars are rendered as filled / half / empty based on [rating] and [allowHalfStars].
 *
 * ### Interaction
 * Set `readOnly = false` and provide [onRatingChange] to enable tap + drag input.
 * The gesture updates the rating continuously while dragging so the caller can react
 * immediately (e.g., store it in a `mutableStateOf`).
 *
 * ```kotlin
 * // Read-only display
 * AppsRatingBar(rating = 3.5f)
 *
 * // Interactive input
 * var rating by remember { mutableFloatStateOf(0f) }
 * AppsRatingBar(rating = rating, readOnly = false, onRatingChange = { rating = it })
 * ```
 *
 * @param rating          Current rating value. Clamped to `0..maxRating`.
 * @param maxRating       Number of stars shown. Default 5.
 * @param starSize        Size of each star icon.
 * @param spacing         Space between adjacent stars.
 * @param filledColor     Colour of a fully filled star.
 * @param halfColor       Colour of a half-filled star (only shown when [allowHalfStars]).
 * @param emptyColor      Colour of an empty star outline.
 * @param allowHalfStars  Whether to render / accept half-star values.
 * @param readOnly        When `true`, gestures are ignored.
 * @param onRatingChange  Callback invoked with the new rating after tap or drag.
 */
@Composable
fun AppsRatingBar(
    rating: Float,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = Kolt.sizes.iconMedium,
    spacing: Dp = Kolt.sizes.paddingXXSmall,
    filledColor: Color = Kolt.colors.accentedGold,
    halfColor: Color = Kolt.colors.accentedGoldLight,
    emptyColor: Color = Kolt.colors.outline,
    allowHalfStars: Boolean = true,
    allowHalfSelection: Boolean = allowHalfStars,
    readOnly: Boolean = true,
    onRatingChange: (Float) -> Unit = {},
) {
    val clampedRating = rating.coerceIn(0f, maxRating.toFloat())

    val gestureModifier = if (!readOnly) {
        Modifier.pointerInput(maxRating, allowHalfSelection) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val newRating = ratingFromOffset(
                    offsetX = down.position.x,
                    starSizePx = starSize.toPx(),
                    spacingPx = spacing.toPx(),
                    maxRating = maxRating,
                    allowHalfStars = allowHalfSelection,
                )
                onRatingChange(newRating)

                drag(pointerId = down.id) { change ->
                    change.consume()
                    onRatingChange(
                        ratingFromOffset(
                            offsetX = change.position.x,
                            starSizePx = starSize.toPx(),
                            spacingPx = spacing.toPx(),
                            maxRating = maxRating,
                            allowHalfStars = allowHalfSelection,
                        )
                    )
                }
            }
        }
    } else Modifier

    Row(
        modifier = modifier
            .then(gestureModifier)
            .semantics { contentDescription = "$clampedRating out of $maxRating stars" },
        horizontalArrangement = Arrangement.spacedBy(spacing),
    ) {
        repeat(maxRating) { index ->
            val starValue = clampedRating - index // how much of this star is filled

            val (icon, tint) = when {
                starValue >= 1f -> Icons.Filled.Star to filledColor
                allowHalfStars && starValue >= 0.5f -> StarHalf to halfColor
                else -> StarBorder to emptyColor
            }

            Icon(
                imageVector = icon,
                contentDescription = null, // described by parent semantics
                tint = tint,
                modifier = Modifier.size(starSize),
            )
        }
    }
}

// ── Rating calculation ────────────────────────────────────────────────────────

private fun ratingFromOffset(
    offsetX: Float,
    starSizePx: Float,
    spacingPx: Float,
    maxRating: Int,
    allowHalfStars: Boolean,
): Float {
    val totalStarWidth = starSizePx + spacingPx
    val starIndex = (offsetX / totalStarWidth).toInt().coerceIn(0, maxRating - 1)
    val positionInStar = offsetX - starIndex * totalStarWidth
    val fraction = positionInStar / starSizePx

    return if (allowHalfStars && fraction < 0.5f) {
        (starIndex + 0.5f).coerceAtLeast(0.5f)
    } else {
        (starIndex + 1f).coerceAtMost(maxRating.toFloat())
    }
}

