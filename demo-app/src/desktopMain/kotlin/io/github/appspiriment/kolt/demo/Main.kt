package io.github.appspiriment.kolt.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.github.appspiriment.kolt.composekmp.theme.CompositionBaseProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Kolt Compose KMP Demo"
    ) {
        var isDarkTheme by remember { mutableStateOf(false) }
        CompositionBaseProvider(isDarkTheme = isDarkTheme) {
            DemoApp(
                platformName = "Desktop",
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme }
            ) {
                DesktopShowcaseContent()
            }
        }
    }
}

@Composable
fun DesktopShowcaseContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to the Desktop host. Android-specific components (like permission dialogs and Firebase in-app updates) are disabled on this target.",
            modifier = Modifier.padding(16.dp)
        )
    }
}
