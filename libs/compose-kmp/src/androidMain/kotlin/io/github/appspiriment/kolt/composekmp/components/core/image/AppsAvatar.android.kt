package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import io.github.appspiriment.kolt.composekmp.components.core.ShimmerBox
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
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(image.model)
            .crossfade(true)
            .build(),
        contentDescription = image.description ?: name,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading,
            is AsyncImagePainter.State.Empty -> {
                if (showShimmerWhileLoading) {
                    ShimmerBox(modifier = Modifier.fillMaxSize(), shape = CircleShape)
                } else {
                    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
                        InitialsContent(initials, contentColor, textStyle)
                    }
                }
            }
            is AsyncImagePainter.State.Error -> {
                Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
                    InitialsContent(initials, contentColor, textStyle)
                }
            }
            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
        }
    }
}
