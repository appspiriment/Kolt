package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.modifiers.shimmerEffect
import io.github.appspiriment.kolt.composekmp.theme.Kolt

// Shimmer placeholder heights approximate typical text line-box heights.
// There is no 14 dp theme token; name the value so it is traceable.
private val SHIMMER_LINE_TALL = 14.dp   // ≈ bodyMediumLarge line height
private val SHIMMER_LINE_SHORT = 12.dp  // = paddingSmallMedium; ≈ bodySmall line height

/**
 * A pre-built shimmer placeholder for skeleton screens.
 *
 * Size the box via [modifier]; the animated shimmer fills it completely.
 * [shape] defaults to [Kolt.sizes.cornerRadiusMedium] which matches the library's
 * standard card/surface rounding.
 *
 * ```kotlin
 * // Skeleton for a text line
 * ShimmerBox(modifier = Modifier.fillMaxWidth().height(16.dp))
 *
 * // Skeleton for a circular avatar
 * ShimmerBox(modifier = Modifier.size(40.dp), shape = CircleShape)
 *
 * // Skeleton with custom colors
 * ShimmerBox(modifier = Modifier.fillMaxWidth().height(160.dp), color = Color.Gray)
 * ```
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Kolt.sizes.cornerRadiusMedium),
    color: Color = Color.LightGray,
    shimmerColors: List<Color>? = null,
) {
    val resolvedColors = shimmerColors ?: listOf(
        color.copy(alpha = 0.6f),
        color.copy(alpha = 0.2f),
        color.copy(alpha = 0.6f),
    )
    Box(modifier = modifier.clip(shape).shimmerEffect(resolvedColors))
}

// ── Skeleton layout helpers ───────────────────────────────────────────────────

/**
 * Pre-built skeleton row: circular avatar on the start + two text lines on the end.
 * Drop it into a `LazyColumn` while your list data loads.
 */
@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(
            horizontal = Kolt.sizes.paddingMedium,
            vertical = Kolt.sizes.paddingSmallMedium,
        ),
        horizontalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingSmallMedium),
    ) {
        ShimmerBox(modifier = Modifier.size(Kolt.sizes.iconXLarge), shape = CircleShape)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingSmall),
        ) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(SHIMMER_LINE_TALL))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.9f).height(SHIMMER_LINE_SHORT))
        }
    }
}
