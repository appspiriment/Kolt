package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
actual fun UiDimen.asDp(): Dp {
    return when (this) {
        is UiDimen.DynamicDp -> value
        is UiDimen.DimenResource -> 0.dp // fallback
        is UiDimen.DynamicTextUnit -> throw RuntimeException("For Dp, DynamicTextUnit type should not be used")
    }
}

@Composable
actual fun UiDimen.asSp(): TextUnit {
    return when (this) {
        is UiDimen.DynamicTextUnit -> value
        is UiDimen.DimenResource -> 0.sp // fallback
        is UiDimen.DynamicDp -> throw RuntimeException("For Sp, DynamicDp type should not be used")
    }
}
