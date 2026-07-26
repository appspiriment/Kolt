package io.github.appspiriment.kolt.locationpicker

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable

actual object LocationPicker {
    /**
     * Compose entry point: registers an [LocationPickerContract] launcher and returns a
     * function that starts it. Must be called from a composable that's present for the
     * lifetime of the launch (same rules as `rememberLauncherForActivityResult`).
     */
    @Composable
    fun rememberLauncher(
        config: LocationPickerConfig = defaultLocationPickerConfig(),
        onResult: (LocationPickerResult?) -> Unit,
    ): () -> Unit {
        val launcher = rememberLauncherForActivityResult(LocationPickerContract(), onResult)
        return { launcher.launch(config) }
    }

    /** For View-system callers: `registerForActivityResult(LocationPicker.contract()) { ... }`. */
    fun contract(): LocationPickerContract = LocationPickerContract()
}
