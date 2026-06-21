package io.github.appspiriment.kolt.composeutils.components.messages

import io.github.appspiriment.kolt.composekmp.wrappers.asText
import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

@Composable
fun ToastMessage(
    message: UiText,
    duration: Int = Toast.LENGTH_SHORT,
) {
    val context = LocalContext.current
    Toast.makeText(context, message.asText(context), duration).show()
}


fun Context.showToast(
    message: UiText,
    duration: Int = Toast.LENGTH_SHORT,
) {
    Toast.makeText(this, message.asText(this), duration).show()
}