package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A text composable that truncates long content and reveals a "Show more / Show less"
 * toggle at the end of the collapsed text.
 *
 * @param text             The full text to display.
 * @param modifier         Applied to the outer layout.
 * @param style            Text style.
 * @param color            Text colour.
 * @param collapsedMaxLines Maximum lines shown while collapsed.
 * @param expandLabel      Label appended to the truncated text. Default "Show more".
 * @param collapseLabel    Label shown when fully expanded. Default "Show less".
 * @param toggleStyle      Style for [expandLabel] / [collapseLabel].
 * @param toggleColor      Colour for [expandLabel] / [collapseLabel].
 * @param expanded         External expanded state. `null` = manage state internally.
 * @param onExpandChange   Called when the toggle is tapped. `null` = internal management.
 */
@Composable
fun AppsExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.bodyMedium,
    color: Color = Kolt.colors.onMainSurface,
    collapsedMaxLines: Int = 3,
    expandLabel: UiText = UiText.DynamicString("Show more"),
    collapseLabel: UiText = UiText.DynamicString("Show less"),
    toggleStyle: TextStyle = Kolt.typography.bodySmall,
    toggleColor: Color = Kolt.colors.primary,
    expanded: Boolean? = null,
    onExpandChange: ((Boolean) -> Unit)? = null,
) {
    // Internal state used when caller doesn't control it
    var internalExpanded by remember { mutableStateOf(false) }
    val isExpanded = expanded ?: internalExpanded

    // Whether the text actually overflows at collapsedMaxLines.
    // Keyed to `text` so stale overflow state doesn't persist when content changes.
    var hasOverflow by remember(text) { mutableStateOf(false) }

    val toggleAction: () -> Unit = {
        val next = !isExpanded
        if (onExpandChange != null) onExpandChange(next)
        else internalExpanded = next
    }

    Column(
        modifier = modifier
            .animateContentSize(animationSpec = tween(durationMillis = 200)),
        verticalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingXXSmall),
    ) {
        AppspirimentText(
            text = text,
            style = style,
            color = color,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (!isExpanded) {
                    hasOverflow = result.hasVisualOverflow
                }
            },
        )

        if (hasOverflow || isExpanded) {
            AppspirimentText(
                text = if (isExpanded) collapseLabel else expandLabel,
                style = toggleStyle,
                color = toggleColor,
                modifier = Modifier.clickable(onClick = toggleAction),
            )
        }
    }
}
