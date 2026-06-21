package io.github.appspiriment.kolt.composekmp.components.core.badge

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import io.github.appspiriment.kolt.composekmp.theme.SmoothCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

// ── Size tokens ───────────────────────────────────────────────────────────────

// These values have no single theme token — name them explicitly.
private val BADGE_COUNT_MIN_SIZE = 18.dp  // count pill min diameter
private val BADGE_DOT_SIZE = 8.dp         // standalone dot diameter (= paddingSmall)
private val BADGE_OVERFLOW_INSET = 5.dp   // how far the badge bleeds outside the anchor

/**
 * Resolved size set for badge indicators.
 *
 * Obtain the default via [BadgeDefaults.sizes], or construct directly to override
 * individual dimensions without reimplementing the whole indicator.
 *
 * @param dotSize                Diameter of the [BadgeState.Dot] filled circle.
 * @param countMinSize           Minimum width *and* height of the [BadgeState.Count] pill.
 * @param countHorizontalPadding Horizontal padding inside the count pill (to keep digits from
 *                               touching the pill edge when the count grows to two digits).
 */
@Immutable
data class BadgeSizes(
    val dotSize: Dp,
    val countMinSize: Dp,
    val countHorizontalPadding: Dp,
)

/** Default dimension values for [BadgeBox] indicators. */
object BadgeDefaults {
    /**
     * Theme-driven badge sizes.
     *
     * | Token                   | Dp  | Purpose                      |
     * |-------------------------|-----|------------------------------|
     * | `paddingSmall`          | 8   | dot diameter                 |
     * | `BADGE_COUNT_MIN_SIZE`  | 18  | count pill min size          |
     * | `paddingXSmall`         | 4   | count pill horizontal inset  |
     */
    val sizes: BadgeSizes
        @Composable get() = BadgeSizes(
            dotSize = Kolt.sizes.paddingSmall,
            countMinSize = BADGE_COUNT_MIN_SIZE,
            countHorizontalPadding = Kolt.sizes.paddingXSmall,
        )
}

// ── State model ───────────────────────────────────────────────────────────────

/**
 * Describes what a badge overlaying a [BadgeBox] should show.
 *
 * | State          | Visual                                      |
 * |----------------|---------------------------------------------|
 * | [None]         | No badge rendered (passthrough)             |
 * | [Dot]          | 8 dp filled circle — "has new content"      |
 * | [Count]        | Pill with a number; caps at [maxCount]+"+"  |
 */
@Stable
sealed class BadgeState {
    /** No badge — the [BadgeBox] content renders without any overlay. */
    @Immutable
    data object None : BadgeState()

    /** A small filled circle indicating new content without a specific count. */
    @Immutable
    data object Dot : BadgeState()

    /**
     * A numbered badge.
     * @param count    The raw count to display.
     * @param maxCount Counts above this are displayed as "[maxCount]+". Default 99.
     */
    @Immutable
    data class Count(
        val count: Int,
        val maxCount: Int = 99,
    ) : BadgeState() {
        /** Display string — either the raw count or "[maxCount]+". */
        val displayText: String get() = if (count > maxCount) "$maxCount+" else "$count"
    }
}

// ── Container ─────────────────────────────────────────────────────────────────

/**
 * Overlays a badge on [content] at its top-end corner.
 *
 * The badge is offset slightly outside the content boundary to achieve the standard
 * notification-badge appearance. Pass [BadgeState.None] to render [content] unchanged.
 *
 * ```kotlin
 * BadgeBox(badge = BadgeState.Count(3)) {
 *     AppsIcon(icon = bellIcon)
 * }
 * BadgeBox(badge = BadgeState.Dot) {
 *     AppsIcon(icon = messageIcon)
 * }
 * ```
 *
 * @param badge             What to show in the badge overlay.
 * @param badgeColor        Badge background colour — defaults to [Kolt.colors.error].
 * @param badgeContentColor Text / content colour inside the badge.
 * @param badgeOffset       Offset applied to the badge anchor at [Alignment.TopEnd]. Positive x
 *                          moves right (outside the content); negative y moves up.
 *                          Default `DpOffset(BADGE_OVERFLOW_INSET, -BADGE_OVERFLOW_INSET)` for a standard overflow look.
 * @param badgeSizes        Dimension set for the indicator. Override to adjust dot diameter,
 *                          count pill height, or horizontal inset without replacing the whole
 *                          indicator. Defaults to [BadgeDefaults.sizes].
 */
@Composable
fun BadgeBox(
    badge: BadgeState,
    modifier: Modifier = Modifier,
    badgeColor: Color = Kolt.colors.error,
    badgeContentColor: Color = Kolt.colors.onError,
    badgeOffset: DpOffset = DpOffset(x = BADGE_OVERFLOW_INSET, y = -BADGE_OVERFLOW_INSET),
    badgeSizes: BadgeSizes = BadgeDefaults.sizes,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier) {
        content()
        if (badge != BadgeState.None) {
            BadgeIndicator(
                badge = badge,
                color = badgeColor,
                contentColor = badgeContentColor,
                sizes = badgeSizes,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = badgeOffset.x, y = badgeOffset.y),
            )
        }
    }
}

// ── Badge indicator ───────────────────────────────────────────────────────────

@Composable
private fun BadgeIndicator(
    badge: BadgeState,
    color: Color,
    contentColor: Color,
    sizes: BadgeSizes,
    modifier: Modifier = Modifier,
) {
    when (badge) {
        is BadgeState.Dot -> {
            Box(
                modifier = modifier
                    .size(sizes.dotSize)
                    .background(color, SmoothCornerShape(sizes.dotSize / 2)),
            )
        }

        is BadgeState.Count -> {
            Box(
                modifier = modifier
                    .defaultMinSize(minWidth = sizes.countMinSize, minHeight = sizes.countMinSize)
                    .background(color, SmoothCornerShape(sizes.countMinSize / 2))
                    .padding(horizontal = sizes.countHorizontalPadding),
                contentAlignment = Alignment.Center,
            ) {
                AppspirimentText(
                    text = badge.displayText,
                    style = Kolt.typography.bodyXXXSmall,
                    color = contentColor,
                    maxLines = 1,
                )
            }
        }

        BadgeState.None -> Unit
    }
}

// ── Standalone badges ─────────────────────────────────────────────────────────

/**
 * A standalone numeric badge pill — not attached to any anchor.
 *
 * Use this when you need the badge as a visual element on its own, for example
 * in a list row to show an unread count next to a label.
 *
 * ```kotlin
 * Row {
 *     Text("Messages")
 *     Spacer(Modifier.weight(1f))
 *     AppsCountBadge(count = 5)
 * }
 * ```
 *
 * @param count        Number to display. Counts above [maxCount] show as "[maxCount]+".
 * @param modifier     Applied to the badge shape.
 * @param maxCount     Cap for the displayed number. Default 99.
 * @param color        Badge background colour.
 * @param contentColor Badge text colour.
 * @param sizes        Dimension overrides for the pill. Defaults to [BadgeDefaults.sizes].
 */
@Composable
fun AppsCountBadge(
    count: Int,
    modifier: Modifier = Modifier,
    maxCount: Int = 99,
    color: Color = Kolt.colors.error,
    contentColor: Color = Kolt.colors.onError,
    sizes: BadgeSizes = BadgeDefaults.sizes,
) {
    BadgeIndicator(
        badge = BadgeState.Count(count = count, maxCount = maxCount),
        color = color,
        contentColor = contentColor,
        sizes = sizes,
        modifier = modifier,
    )
}

/**
 * A standalone dot badge — an 8 dp filled circle indicating new content.
 *
 * ```kotlin
 * Row(verticalAlignment = Alignment.CenterVertically) {
 *     Text("Notifications")
 *     Spacer(Modifier.width(4.dp))
 *     AppsDotBadge()
 * }
 * ```
 *
 * @param modifier Applied to the dot.
 * @param color    Dot fill colour.
 * @param size     Dot diameter. Default 8 dp.
 */
@Composable
fun AppsDotBadge(
    modifier: Modifier = Modifier,
    color: Color = Kolt.colors.error,
    size: Dp = BADGE_DOT_SIZE,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, SmoothCornerShape(size / 2)),
    )
}

