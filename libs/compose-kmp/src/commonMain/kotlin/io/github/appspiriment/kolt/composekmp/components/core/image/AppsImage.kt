package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage

expect class RemoteImageState

@Composable
expect fun AppsImage(
    image: UiImage,
    modifier: Modifier = Modifier,
    colorFilter: ColorFilter? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    usePainter: Boolean = true,
    onRemoteState: ((RemoteImageState) -> Unit)? = null,
)
