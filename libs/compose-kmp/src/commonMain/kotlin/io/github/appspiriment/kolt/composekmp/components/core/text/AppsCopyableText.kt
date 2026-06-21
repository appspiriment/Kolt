package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import kotlinx.coroutines.delay

// Icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

// Custom implementation of ContentCopy vector to avoid large material-icons-extended dependency
private val ContentCopyIcon: ImageVector
    get() {
        if (_contentCopy != null) {
            return _contentCopy!!
        }
        _contentCopy = ImageVector.Builder(
            name = "ContentCopy",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(16f, 1f)
                lineTo(4f, 1f)
                curveTo(2.9f, 1f, 2f, 1.9f, 2f, 3f)
                verticalLineTo(17f)
                horizontalLineTo(4f)
                verticalLineTo(3f)
                horizontalLineTo(16f)
                verticalLineTo(1f)
                close()
                moveTo(19f, 5f)
                horizontalLineTo(8f)
                curveTo(6.9f, 5f, 6f, 5.9f, 6f, 7f)
                verticalLineTo(21f)
                curveTo(6f, 22.1f, 6.9f, 23f, 8f, 23f)
                horizontalLineTo(19f)
                curveTo(20.1f, 23f, 21f, 22.1f, 21f, 21f)
                verticalLineTo(7f)
                curveTo(21f, 5.9f, 20.1f, 5f, 19f, 5f)
                close()
                moveTo(19f, 21f)
                horizontalLineTo(8f)
                verticalLineTo(7f)
                horizontalLineTo(19f)
                verticalLineTo(21f)
                close()
            }
        }.build()
        return _contentCopy!!
    }

private var _contentCopy: ImageVector? = null

/**
 * A text composable that copies its content to the clipboard when tapped.
 *
 * ### Feedback
 * After a successful copy the text colour briefly shifts to [copiedColor] and,
 * if [showIcon] is `true`, a checkmark icon replaces the copy icon for
 * [feedbackDurationMs] milliseconds — then both revert automatically.
 *
 * @param text               Text to display and copy.
 * @param modifier           Applied to the root Row.
 * @param style              Text style.
 * @param color              Normal text colour.
 * @param copiedColor        Colour briefly shown after copying. Defaults to [Kolt.colors.success].
 * @param maxLines           Maximum visible lines.
 * @param overflow           Text overflow strategy.
 * @param showIcon           Whether to show a trailing copy/check icon.
 * @param iconSize           Size of the trailing icon.
 * @param feedbackDurationMs How long the "copied" state is shown (ms). Default 1 500.
 * @param onCopied           Optional callback invoked after the text is placed on the clipboard.
 */
@Composable
fun AppsCopyableText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.bodyMedium,
    color: Color = Kolt.colors.onMainSurface,
    copiedColor: Color = Kolt.colors.success,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    showIcon: Boolean = true,
    iconSize: Dp = Kolt.sizes.iconXSmall,
    feedbackDurationMs: Long = 1_500L,
    onCopied: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val hapticFeedback = LocalHapticFeedback.current
    var copied by remember { mutableStateOf(false) }

    // Auto-reset after feedbackDurationMs
    LaunchedEffect(copied) {
        if (copied) {
            delay(feedbackDurationMs)
            copied = false
        }
    }

    val animatedColor by animateColorAsState(
        targetValue = if (copied) copiedColor else color,
        animationSpec = tween(durationMillis = 200),
        label = "copyColorAnim",
    )

    Row(
        modifier = modifier.clickable {
            clipboardManager.setText(AnnotatedString(text))
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            copied = true
            onCopied?.invoke()
        },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Kolt.sizes.paddingXSmall),
    ) {
        AppspirimentText(
            text = text,
            style = style,
            color = animatedColor,
            maxLines = maxLines,
            overflow = overflow,
            modifier = Modifier.weight(1f, fill = false),
        )

        if (showIcon) {
            val iconVector = if (copied) Icons.Filled.Check else ContentCopyIcon
            AppsIcon(
                icon = iconVector.toUiImage(),
                iconHeight = iconSize,
            )
        }
    }
}
