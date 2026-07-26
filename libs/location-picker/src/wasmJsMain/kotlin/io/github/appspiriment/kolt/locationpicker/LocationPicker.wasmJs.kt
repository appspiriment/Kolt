package io.github.appspiriment.kolt.locationpicker

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

actual object LocationPicker {
    /** No OS window/modal concept on web — uses a Compose [Dialog] overlay instead, matching
     *  the "own surface" pattern the other platforms use (Activity / modal view controller /
     *  DialogWindow): the host page's own content stays composed underneath. */
    @Composable
    fun Embed(
        config: LocationPickerConfig = defaultLocationPickerConfig(),
        onCancel: () -> Unit,
        onResult: (LocationPickerResult) -> Unit,
    ) {
        Dialog(
            onDismissRequest = onCancel,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            LocationPickerScreen(
                config = config,
                modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.9f),
                onCancel = onCancel,
                onResult = onResult,
            )
        }
    }
}
