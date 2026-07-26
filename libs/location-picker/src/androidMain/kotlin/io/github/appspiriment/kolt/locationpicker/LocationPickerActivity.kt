package io.github.appspiriment.kolt.locationpicker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.appspiriment.kolt.composekmp.theme.CompositionBaseProvider
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Standalone Activity host for [LocationPickerScreen]. Launch it via
 * [LocationPickerContract] (`registerForActivityResult(LocationPickerContract())`) or the
 * Compose-friendly `LocationPicker.rememberLauncher`.
 *
 * To override its base theme, declare the same `<activity>` element with your own
 * `android:theme` in your app's AndroidManifest.xml — the app manifest's value wins over this
 * module's default (`Theme.LocationPicker`) during manifest merging, no code changes needed.
 */
class LocationPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config = intent.getStringExtra(LocationPickerContract.EXTRA_CONFIG)
            ?.let { Json.decodeFromString<LocationPickerConfig>(it) }
            ?: LocationPickerConfig()

        setContent {
            CompositionBaseProvider {
                LocationPickerScreen(
                    config = config,
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    },
                    onResult = { result ->
                        setResult(
                            RESULT_OK,
                            Intent().putExtra(LocationPickerContract.EXTRA_RESULT, Json.encodeToString(result))
                        )
                        finish()
                    },
                )
            }
        }
    }
}
