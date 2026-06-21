// Moved to compose-kmp. This file is a backward-compatibility wrapper.
// Please migrate imports to: io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog
@file:Suppress("unused")

package io.github.appspiriment.kolt.composeutils.components.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.DialogProperties
import io.github.appspiriment.kolt.composekmp.components.core.messages.DialogButtonStyle
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold

/**
 * @deprecated Moved to compose-kmp. Use [io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog].
 */
@Deprecated(
    message = "MessageDialog has been moved to compose-kmp. Use io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog",
    replaceWith = ReplaceWith(
        "MessageDialog",
        "io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog"
    ),
    level = DeprecationLevel.WARNING
)
@Composable
fun MessageDialog(
    modifier: Modifier = Modifier,
    title: UiText? = null,
    message: UiText? = null,
    titleStyle: TextStyle = Kolt.typography.textLarge.semiBold,
    messageStyle: TextStyle = Kolt.typography.textMedium,
    titleAlign: TextAlign = TextAlign.Center,
    messageAlign: TextAlign = TextAlign.Center,
    messageContent: (@Composable (UiText) -> Unit)? = null,
    positiveText: UiText? = UiText.DynamicString("OK"),
    negativeText: UiText? = null,
    buttonStyle: DialogButtonStyle = DialogButtonStyle.primary(),
    listener: (Boolean) -> Unit = {},
    cancellable: Boolean = true,
    dialogBackground: Color = Kolt.colors.mainSurface,
    onDismissRequest: () -> Unit,
    dialogProperties: DialogProperties = DialogProperties(),
    customviewContent: (@Composable () -> Unit)? = null
) {
    io.github.appspiriment.kolt.composekmp.components.core.messages.MessageDialog(
        modifier = modifier,
        title = title,
        message = message,
        titleStyle = titleStyle,
        messageStyle = messageStyle,
        titleAlign = titleAlign,
        messageAlign = messageAlign,
        messageContent = messageContent,
        positiveText = positiveText,
        negativeText = negativeText,
        buttonStyle = buttonStyle,
        listener = listener,
        cancellable = cancellable,
        dialogBackground = dialogBackground,
        onDismissRequest = onDismissRequest,
        dialogProperties = dialogProperties,
        customviewContent = customviewContent,
    )
}
