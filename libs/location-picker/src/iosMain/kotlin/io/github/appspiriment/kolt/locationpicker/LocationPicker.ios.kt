package io.github.appspiriment.kolt.locationpicker

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController

actual object LocationPicker {
    /** Presents [LocationPickerScreen] modally over [from]; dismisses itself on cancel/confirm. */
    fun present(
        from: UIViewController,
        config: LocationPickerConfig = LocationPickerConfig(),
        onResult: (LocationPickerResult?) -> Unit,
    ) {
        lateinit var picker: UIViewController
        picker = ComposeUIViewController {
            LocationPickerScreen(
                config = config,
                onCancel = {
                    picker.dismissViewControllerAnimated(true, completion = null)
                    onResult(null)
                },
                onResult = { result ->
                    picker.dismissViewControllerAnimated(true, completion = null)
                    onResult(result)
                },
            )
        }
        picker.modalPresentationStyle = UIModalPresentationFullScreen
        from.presentViewController(picker, animated = true, completion = null)
    }
}
