package io.github.appspiriment.kolt.composekmp.wrappers

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource

@Composable
actual fun UiColor.asColor(): Color {
    val context = LocalContext.current
    return asColor(context = context)
}

@Composable
fun UiColor.asColor(context: Context): Color {
    return when (this) {
        is UiColor.DynamicColor -> value
        is UiColor.ColorResource -> colorResource(resId)
        is UiColor.HexColor -> Color(android.graphics.Color.parseColor(hex))
    }
}

fun UiColor.getColor(context: Context): Color {
    return when (this) {
        is UiColor.DynamicColor -> value
        is UiColor.ColorResource -> Color(context.resources.getColor(resId))
        is UiColor.HexColor -> Color(android.graphics.Color.parseColor(hex))
    }
}
