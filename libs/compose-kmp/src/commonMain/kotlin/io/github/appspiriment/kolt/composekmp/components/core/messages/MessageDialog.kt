package io.github.appspiriment.kolt.composekmp.components.core.messages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.SmoothCornerShape
import io.github.appspiriment.kolt.composekmp.theme.semiBold

/**
 * A configurable dialog with optional title, message, positive/negative buttons and custom content.
 *
 * All text values accept [UiText] so both raw strings (`DynamicString`) and Android string
 * resources (`StringResource`) work transparently — making this component fully KMP-compatible
 * while still supporting Android resource-based localisation on the Android target.
 *
 * @param title              Optional dialog title.
 * @param message            Optional body message.
 * @param titleStyle         Text style for [title].
 * @param messageStyle       Text style for [message].
 * @param titleAlign         Text alignment for [title].
 * @param messageAlign       Text alignment for [message].
 * @param messageContent     Optional composable override for the [message] area.
 * @param positiveText       Label for the positive / confirm button. Pass `null` to hide.
 * @param negativeText       Label for the negative / cancel button. Pass `null` to hide.
 * @param buttonStyle        Combined style for both buttons (colour, arrangement).
 * @param listener           Called with `true` for positive, `false` for negative.
 * @param cancellable        If `false`, tapping outside or pressing back does nothing.
 * @param dialogBackground   Background colour of the dialog card.
 * @param onDismissRequest   Called when the dialog should close (back press / outside tap).
 * @param dialogProperties   [DialogProperties] forwarded to the underlying [Dialog].
 * @param customviewContent  Optional composable rendered below the message and above buttons.
 */
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
    Dialog(
        onDismissRequest = {
            if (cancellable) onDismissRequest()
        },
        properties = dialogProperties
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .background(
                    color = dialogBackground,
                    shape = SmoothCornerShape(12.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            title?.let {
                AppspirimentText(
                    text = it,
                    style = titleStyle,
                    textAlign = titleAlign,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            message?.let {
                messageContent?.invoke(it) ?: AppspirimentText(
                    text = it,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    style = messageStyle,
                    textAlign = messageAlign,
                )
            }

            customviewContent?.invoke()

            Row(
                horizontalArrangement = buttonStyle.arrangement,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
            ) {
                negativeText?.let {
                    AppsButton(
                        text = it,
                        buttonStyle = buttonStyle.negativeStyle,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .defaultMinSize(minWidth = 96.dp)
                    ) {
                        onDismissRequest()
                        listener.invoke(false)
                    }
                }
                positiveText?.let {
                    AppsButton(
                        text = it,
                        buttonStyle = buttonStyle.positiveStyle,
                        modifier = Modifier.defaultMinSize(minWidth = 96.dp)
                    ) {
                        onDismissRequest()
                        listener.invoke(true)
                    }
                }
            }
        }
    }
}
