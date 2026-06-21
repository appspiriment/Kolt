package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage.RemoteImage
import io.github.appspiriment.kolt.composekmp.wrappers.asColor
import io.github.appspiriment.kolt.composekmp.wrappers.getPainter

actual class RemoteImageState(val state: AsyncImagePainter.State)

@Composable
actual fun AppsImage(
    image: UiImage,
    modifier: Modifier,
    colorFilter: ColorFilter?,
    alignment: Alignment,
    contentScale: ContentScale,
    alpha: Float,
    usePainter: Boolean,
    onRemoteState: ((RemoteImageState) -> Unit)?,
) {
    when (image) {
        is RemoteImage -> {
            val tintFilter = image.tint?.asColor()?.let { ColorFilter.tint(it) } ?: colorFilter
            AsyncImage(
                model              = image.model,
                contentDescription = image.description,
                modifier           = modifier,
                alignment          = alignment,
                contentScale       = contentScale,
                alpha              = alpha,
                colorFilter        = tintFilter,
                placeholder        = image.placeholder?.getPainter(),
                error              = image.error?.getPainter(),
                fallback           = image.fallback?.getPainter(),
                onLoading          = { onRemoteState?.invoke(RemoteImageState(it)) },
                onSuccess          = { onRemoteState?.invoke(RemoteImageState(it)) },
                onError            = { onRemoteState?.invoke(RemoteImageState(it)) },
            )
        }

        else -> {
            if (usePainter || image.getImageVector() == null) {
                Image(
                    painter            = image.getPainter(),
                    modifier           = modifier,
                    contentDescription = image.description,
                    alignment          = alignment,
                    contentScale       = contentScale,
                    alpha              = alpha,
                    colorFilter        = image.tint?.asColor()?.let { ColorFilter.tint(it) } ?: colorFilter,
                )
            } else {
                AppsIcon(
                    icon     = image,
                    modifier = modifier,
                )
            }
        }
    }
}
