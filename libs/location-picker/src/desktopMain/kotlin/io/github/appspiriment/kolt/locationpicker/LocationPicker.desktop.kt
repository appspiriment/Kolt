package io.github.appspiriment.kolt.locationpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

actual object LocationPicker {
    /** Desktop has no Activity concept — a window is the closest equivalent. */
    @Composable
    fun showDialog(
        config: LocationPickerConfig = defaultLocationPickerConfig(),
        onCancel: () -> Unit,
        onResult: (LocationPickerResult) -> Unit,
    ) {
        DialogWindow(
            onCloseRequest = onCancel,
            title = config.title,
            state = rememberDialogState(width = 480.dp, height = 640.dp),
        ) {
            LocationPickerScreen(config = config, onCancel = onCancel, onResult = onResult)
        }
    }
}
