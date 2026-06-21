package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

// ── Semantic styles ───────────────────────────────────────────────────────────

/**
 * Semantic colour intent for [AppsStatusTag].
 *
 * Each variant maps to a pair of background + content colours from the active theme,
 * using **container** shades (softer) for readability inside tags.
 */
enum class StatusStyle {
    Primary,   // primary / onPrimary
    Secondary, // secondary / onSecondary
    Success,   // successContainer / onSuccessContainer
    Warning,   // warningContainer / onWarningContainer
    Error,     // errorContainer / onErrorContainer
    Info,      // infoContainer / onInfoContainer
    Neutral,   // greyCardContainer / onGreyCardContainer
}

enum class StatusTagSize { Small, Medium, Large }

/** Resolved colours for a [AppsStatusTag]. Pass custom colours via [appsStatusTagColors]. */
@Immutable
data class StatusTagColors(val background: Color, val content: Color)

/**
 * Returns the [StatusTagColors] for [style] from the current theme.
 * Override at call site to use non-semantic colours.
 */
@Composable
fun appsStatusTagColors(style: StatusStyle): StatusTagColors = when (style) {
    StatusStyle.Primary   -> StatusTagColors(Kolt.colors.primary, Kolt.colors.onPrimary)
    StatusStyle.Secondary -> StatusTagColors(Kolt.colors.secondary, Kolt.colors.onSecondary)
    StatusStyle.Success   -> StatusTagColors(Kolt.colors.successContainer, Kolt.colors.onSuccessContainer)
    StatusStyle.Warning   -> StatusTagColors(Kolt.colors.warningContainer, Kolt.colors.onWarningContainer)
    StatusStyle.Error     -> StatusTagColors(Kolt.colors.errorContainer, Kolt.colors.onErrorContainer)
    StatusStyle.Info      -> StatusTagColors(Kolt.colors.infoContainer, Kolt.colors.onInfoContainer)
    StatusStyle.Neutral   -> StatusTagColors(Kolt.colors.greyCardContainer, Kolt.colors.onGreyCardContainer)
}

// ── Size tokens ───────────────────────────────────────────────────────────────

private data class TagDimensions(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val iconSize: Dp,
    val textStyle: TextStyle,
)

@Composable
private fun tagDimensions(size: StatusTagSize): TagDimensions {
    // Read CompositionLocals outside `remember` so the block captures their current
    // values. Keying on all three means a theme switch (rare) correctly rebuilds.
    val sizes = Kolt.sizes
    val typography = Kolt.typography
    return remember(size, sizes, typography) {
        when (size) {
            StatusTagSize.Small  -> TagDimensions(sizes.paddingXSmallPlus, sizes.paddingXXSmall, sizes.iconXXSmall, typography.bodyXSmall)
            StatusTagSize.Medium -> TagDimensions(sizes.paddingSmall,      sizes.paddingXSmall,  sizes.iconXSmall,  typography.bodySmall)
            StatusTagSize.Large  -> TagDimensions(sizes.paddingSmallMedium, sizes.paddingXSmallPlus, sizes.iconSmall, typography.bodyMedium)
        }
    }
}

// ── Component ─────────────────────────────────────────────────────────────────

/**
 * A read-only, pill-shaped status label.
 *
 * Use this for non-interactive status display — order states, content labels, severity
 * indicators, and similar. For tappable chips, use the interactive chip components from
 * Layer 4 instead.
 *
 * The colour pair is resolved automatically from [style] using the current theme, but can
 * be overridden entirely via [colors].
 *
 * ```kotlin
 * AppsStatusTag("Delivered".toUiText(), style = StatusStyle.Success)
 * AppsStatusTag("Overdue".toUiText(),   style = StatusStyle.Error, size = StatusTagSize.Small)
 * AppsStatusTag("Draft".toUiText(),     style = StatusStyle.Neutral, leadingIcon = editIcon)
 * ```
 *
 * @param label        Text displayed inside the tag.
 * @param style        Semantic colour intent — drives background + text colour from theme.
 * @param size         Controls padding, icon size, and text style.
 * @param leadingIcon  Optional icon prepended to [label]. Tinted with [StatusTagColors.content].
 * @param trailingIcon Optional icon appended after [label].
 * @param colors       Explicit colour override. Default resolved from [style].
 * @param maxLines     Maximum lines for [label] text. Default 1 (single-line pill). Set to
 *                     [Int.MAX_VALUE] for wrapping — useful when tags appear in narrow columns.
 */
@Composable
fun AppsStatusTag(
    label: UiText,
    modifier: Modifier = Modifier,
    style: StatusStyle = StatusStyle.Neutral,
    size: StatusTagSize = StatusTagSize.Medium,
    leadingIcon: UiImage? = null,
    trailingIcon: UiImage? = null,
    colors: StatusTagColors = appsStatusTagColors(style),
    maxLines: Int = 1,
) {
    val dims = tagDimensions(size)

    Row(
        modifier = modifier
            .background(colors.background, CircleShape)
            .padding(horizontal = dims.horizontalPadding, vertical = dims.verticalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingXSmall),
    ) {
        if (leadingIcon != null) {
            AppsIcon(
                icon = leadingIcon.setTint(null), // inherit content colour from LocalContentColor
                iconHeight = dims.iconSize,
            )
        }
        AppspirimentText(
            text = label,
            style = dims.textStyle,
            color = colors.content,
            maxLines = maxLines,
        )
        if (trailingIcon != null) {
            AppsIcon(
                icon = trailingIcon.setTint(null),
                iconHeight = dims.iconSize,
            )
        }
    }
}

