package io.github.appspiriment.kolt.composeutils.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.InterceptPlatformTextInput
import kotlinx.coroutines.awaitCancellation

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DisableSoftKeyboard(
    disable: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (disable) {
        InterceptPlatformTextInput(
            interceptor = { _, _ ->
                // Swallow keyboard activation requests
                awaitCancellation()
            },
            content = content,
        )
    } else {
        // Do not intercept anything, allow keyboard to show normally
        content()
    }
}