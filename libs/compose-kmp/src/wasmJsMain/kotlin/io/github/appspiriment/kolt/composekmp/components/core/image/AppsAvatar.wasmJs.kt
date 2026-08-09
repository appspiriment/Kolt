package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

@Composable
actual fun RemoteAvatarImage(
    image: UiImage.RemoteImage,
    name: String,
    initials: String,
    bgColor: Color,
    contentColor: Color,
    textStyle: TextStyle,
    modifier: Modifier,
    showShimmerWhileLoading: Boolean,
) {
    InitialsCircle(initials, bgColor, contentColor, textStyle, modifier)
}
