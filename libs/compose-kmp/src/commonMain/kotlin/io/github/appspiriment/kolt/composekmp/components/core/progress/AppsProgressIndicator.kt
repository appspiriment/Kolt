package io.github.appspiriment.kolt.composekmp.components.core.progress

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt

// 4 dp has no single theme token — name it explicitly.
private val CIRCULAR_PROGRESS_STROKE_WIDTH = 4.dp

// ── Circular ──────────────────────────────────────────────────────────────────

/**
 * Themed circular progress indicator.
 *
 * @param progress `null` for indeterminate; `0f–1f` for determinate.
 * @param color    Fill colour — defaults to [Kolt.colors.primary].
 * @param trackColor Background ring colour — defaults to transparent.
 */
@Composable
fun AppsCircularProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = Kolt.colors.primary,
    trackColor: Color = Color.Transparent,
    strokeWidth: Dp = CIRCULAR_PROGRESS_STROKE_WIDTH,
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = strokeCap,
        )
    } else {
        CircularProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeWidth = strokeWidth,
            strokeCap = strokeCap,
        )
    }
}

// ── Linear ────────────────────────────────────────────────────────────────────

/**
 * Themed linear progress indicator.
 *
 * @param progress `null` for indeterminate; `0f–1f` for determinate.
 * @param color    Fill colour — defaults to [Kolt.colors.primary].
 * @param trackColor Track background colour — defaults to 24 % alpha of [color].
 */
@Composable
fun AppsLinearProgress(
    modifier: Modifier = Modifier,
    progress: Float? = null,
    color: Color = Kolt.colors.primary,
    trackColor: Color = color.copy(alpha = 0.24f),
    strokeCap: StrokeCap = StrokeCap.Round,
) {
    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
        )
    } else {
        LinearProgressIndicator(
            modifier = modifier,
            color = color,
            trackColor = trackColor,
            strokeCap = strokeCap,
        )
    }
}
