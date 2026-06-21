package io.github.appspiriment.kolt.composeutils.components.containers.types

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import io.github.appspiriment.kolt.composekmp.wrappers.UiColor
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

data class AppsTopBarButton(
    val icon: UiImage,
    val modifier: Modifier = Modifier,
    val tint: Color = Color.Gray,
    val onClick: () -> Unit,
)
