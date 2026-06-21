package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.layout.ContentScale
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage.RemoteImage
import io.github.appspiriment.kolt.composekmp.wrappers.asColor
import io.github.appspiriment.kolt.composekmp.wrappers.getPainter

actual typealias RemoteImageState = Any

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
            val placeholder = image.placeholder
            if (placeholder != null) {
                AppsImage(
                    image = placeholder,
                    modifier = modifier,
                    colorFilter = colorFilter,
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    usePainter = usePainter,
                    onRemoteState = null
                )
            } else {
                Box(modifier = modifier)
            }
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
