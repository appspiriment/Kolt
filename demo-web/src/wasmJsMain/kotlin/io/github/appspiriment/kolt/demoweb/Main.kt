package io.github.appspiriment.kolt.demoweb

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import io.github.appspiriment.kolt.locationpicker.LocationPicker
import io.github.appspiriment.kolt.locationpicker.LocationPickerConfig
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        MaterialTheme {
            LocationPickerDemo()
        }
    }
}

@Composable
private fun LocationPickerDemo() {
    var showPicker by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf("No location picked yet") }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column {
            Text("Kolt Location Picker — Web demo (search / current-location / manual entry).")
            Text(lastResult)
            Button(onClick = { showPicker = true }) {
                Text("Open Location Picker")
            }
        }
    }

    if (showPicker) {
        LocationPicker.Embed(
            config = LocationPickerConfig(showMap = false, title = "Pick a location"),
            onCancel = { showPicker = false },
            onResult = { result ->
                lastResult = "${result.label}: ${result.latitude}, ${result.longitude} (${result.timezoneId})"
                showPicker = false
            },
        )
    }
}
