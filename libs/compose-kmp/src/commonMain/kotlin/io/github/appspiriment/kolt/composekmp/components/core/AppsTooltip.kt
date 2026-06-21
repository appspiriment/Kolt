package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * Wraps [content] in a Material 3 **plain tooltip** that appears on long-press.
 *
 * This is the standard use-case: one short label, no actions, no title.
 * For richer tooltips with a title and action buttons use [AppsRichTooltip].
 *
 * ```kotlin
 * AppsTooltip(tooltip = "Refresh".toUiText()) {
 *     AppsIconButton(icon = refreshIcon, onClick = { /* ... */ })
 * }
 * ```
 *
 * @param tooltip          Text shown inside the tooltip bubble.
 * @param modifier         Applied to the [TooltipBox] anchor wrapper.
 * @param enableUserInput  When `true` (default), the tooltip responds to long-press / hover.
 *                         Pass `false` to suppress gesture-triggered display
 *                         (the tooltip can still be shown programmatically via [TooltipState]).
 * @param tooltipStyle     Text style for the tooltip label.
 * @param containerColor   Tooltip bubble background colour.
 *                         Defaults to `MaterialTheme.colorScheme.inverseSurface`.
 * @param contentColor     Tooltip text colour.
 *                         Defaults to `MaterialTheme.colorScheme.inverseOnSurface`.
 * @param content          The anchor composable that the tooltip is attached to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsTooltip(
    tooltip: UiText,
    modifier: Modifier = Modifier,
    enableUserInput: Boolean = true,
    tooltipStyle: TextStyle = Kolt.typography.bodySmall,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            PlainTooltip(
                containerColor = containerColor,
                contentColor = contentColor,
            ) {
                AppspirimentText(text = tooltip, style = tooltipStyle)
            }
        },
        state = rememberTooltipState(),
        modifier = modifier,
        enableUserInput = enableUserInput,
    ) {
        content()
    }
}

/**
 * Wraps [content] in a Material 3 **rich tooltip** with an optional [title], a [message],
 * and an optional trailing [action] slot.
 *
 * Rich tooltips are suited for contextual help that needs a bit more explanation than a
 * single-line label. Prefer [AppsTooltip] for simple cases.
 *
 * ```kotlin
 * AppsRichTooltip(
 *     title = "Auto-save".toUiText(),
 *     message = "Your changes are saved automatically every 30 seconds.".toUiText(),
 *     action = { TextButton(onClick = { /* learn more */ }) { Text("Learn more") } },
 * ) {
 *     AppsIcon(icon = infoIcon)
 * }
 * ```
 *
 * @param message          Main body text of the tooltip.
 * @param modifier         Applied to the [TooltipBox] anchor wrapper.
 * @param enableUserInput  When `true` (default), the tooltip responds to long-press / hover.
 *                         Pass `false` to suppress gesture-triggered display.
 * @param title            Optional bold title shown above [message].
 * @param titleStyle       Text style for [title].
 * @param messageStyle     Text style for [message].
 * @param containerColor   Tooltip bubble background colour.
 *                         Defaults to `MaterialTheme.colorScheme.surfaceContainerHigh`.
 * @param contentColor     Tooltip text colour.
 *                         Defaults to `MaterialTheme.colorScheme.onSurface`.
 * @param shape            Shape of the tooltip bubble.
 * @param isPersistent     When `true` (default), the tooltip stays visible after the user lifts
 *                         their finger, requiring an explicit dismiss gesture. Set to `false`
 *                         for tooltips that should disappear automatically on pointer-up.
 * @param action           Optional composable shown below [message] (e.g. a `TextButton`).
 * @param content          The anchor composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsRichTooltip(
    message: UiText,
    modifier: Modifier = Modifier,
    enableUserInput: Boolean = true,
    title: UiText? = null,
    titleStyle: TextStyle = Kolt.typography.labelMedium,
    messageStyle: TextStyle = Kolt.typography.bodySmall,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    shape: Shape = TooltipDefaults.richTooltipContainerShape,
    isPersistent: Boolean = true,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberRichTooltipPositionProvider(),
        tooltip = {
            RichTooltip(
                title = if (title != null) {
                    { AppspirimentText(text = title, style = titleStyle) }
                } else null,
                action = action,
                shape = shape,
                colors = TooltipDefaults.richTooltipColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                ),
            ) {
                AppspirimentText(text = message, style = messageStyle)
            }
        },
        state = rememberTooltipState(isPersistent = isPersistent),
        modifier = modifier,
        enableUserInput = enableUserInput,
    ) {
        content()
    }
}

