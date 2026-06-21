package io.github.appspiriment.kolt.composeutils.components.core

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale


@Composable
fun rememberSpeechToText(
    onFailure: ((Exception) -> Unit)? = null
): Pair<() -> Unit, StateFlow<String?>> {
    val context = LocalContext.current
    val voiceTextStateFlow = remember { MutableStateFlow<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val results: ArrayList<String>? = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            voiceTextStateFlow.value = results?.firstOrNull()

        } else {
            voiceTextStateFlow.value = null
        }
    }

    // Function to launch the voice search intent
    val launchVoiceToText = remember<(String) -> Unit>(context) {
        { prompt ->
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
            }

            try {
                voiceTextStateFlow.value = null
                launcher.launch(intent)
            } catch (a: ActivityNotFoundException) {
                onFailure?.invoke(a)?: run {
                    Toast.makeText(
                        context,
                        "Sorry, your device does not support speech recognition.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                voiceTextStateFlow.value = null
            }
        }
    }

    val defaultLaunch: () -> Unit = remember { { launchVoiceToText("Speak now...") } }
    return Pair(defaultLaunch, voiceTextStateFlow)
}
