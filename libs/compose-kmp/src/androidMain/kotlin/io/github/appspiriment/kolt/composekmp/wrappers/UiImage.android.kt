package io.github.appspiriment.kolt.composekmp.wrappers

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.core.graphics.drawable.toBitmap
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import java.io.File

@Composable
actual fun UiImage.getPainter(): Painter {
    return when (this) {
        is UiImage.ImageVectorIcon -> rememberVectorPainter(image = imageVector)
        is UiImage.VectorResourceIcon -> painterResource(id = resId)
        is UiImage.DrawableResourceIcon -> painterResource(id = resId)
        is UiImage.DrawableIcon -> remember(drawable) {
            val androidDrawable = drawable as Drawable
            BitmapPainter(androidDrawable.toBitmap().asImageBitmap())
        }
        is UiImage.PainterIcon -> painter
        is UiImage.RemoteImage -> {
            val context = LocalContext.current
            rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(model)
                    .crossfade(crossfade)
                    .apply {
                        memoryCacheKey?.let { memoryCacheKey(it) }
                        diskCacheKey?.let { diskCacheKey(it) }
                        if (!allowHardware) allowHardware(false)
                    }
                    .build(),
                placeholder = placeholder?.getPainter(),
                error = error?.getPainter(),
                fallback = fallback?.getPainter(),
            )
        }
    }
}

@Composable
actual fun UiImage.VectorResourceIcon.getImageVectorResource(): ImageVector? {
    return ImageVector.vectorResource(id = resId)
}

// Android-only factories/extensions to maintain compatibility:

fun uiImageDrawable(
    drawable: Drawable,
    description: String? = null,
    tint: UiColor? = null
): UiImage = UiImage.DrawableIcon(drawable, description, tint)

fun uiImageFromUri(
    uri: Uri?,
    placeholder: UiImage? = null,
    error: UiImage? = null,
    fallback: UiImage? = null,
    crossfade: Boolean = true,
    description: String? = null,
    tint: UiColor? = null,
): UiImage = UiImage.RemoteImage(
    model       = uri,
    placeholder = placeholder,
    error       = error,
    fallback    = fallback,
    crossfade   = crossfade,
    description = description,
    tint        = tint,
)

fun uiImageFromFile(
    file: File?,
    placeholder: UiImage? = null,
    error: UiImage? = null,
    fallback: UiImage? = null,
    crossfade: Boolean = true,
    description: String? = null,
): UiImage = UiImage.RemoteImage(
    model       = file,
    placeholder = placeholder,
    error       = error,
    fallback    = fallback,
    crossfade   = crossfade,
    description = description,
)

@Composable
fun uiImageFromDrawableName(
    drawableName: String,
): UiImage? {
    val context = LocalContext.current
    val resources = context.resources
    val packageName = context.packageName

    val resourceId = remember(drawableName, resources, packageName) {
        try {
            resources.getIdentifier(drawableName, "drawable", packageName)
        } catch (e: Exception) {
            println("Error getting resource ID for $drawableName: ${e.message}")
            null
        }
    }
    return resourceId?.let {
        uiImageResource(it)
    }
}
