package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class UiImage(
    open val description: String?,
    open val tint: UiColor?
) {
    abstract fun setDescription(contentDescription: String?): UiImage
    abstract fun setTint(tint: UiColor? = null): UiImage

    data class ImageVectorIcon(
        val imageVector: ImageVector,
        override val description: String? = null,
        override val tint: UiColor? = null
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage {
            return copy(description = contentDescription)
        }
        override fun setTint(tint: UiColor?): UiImage {
            return copy(tint = tint)
        }
    }

    data class DrawableResourceIcon(
        val resId: Int,
        override val description: String? = null,
        override val tint: UiColor? = null
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage {
            return DrawableResourceIcon(resId, contentDescription, tint)
        }
        override fun setTint(tint: UiColor?): UiImage {
            return DrawableResourceIcon(resId, description, tint)
        }
    }

    data class VectorResourceIcon(
        val resId: Int,
        override val description: String? = null,
        override val tint: UiColor? = null
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage {
            return VectorResourceIcon(resId, contentDescription, tint)
        }
        override fun setTint(tint: UiColor?): UiImage {
            return VectorResourceIcon(resId, description, tint)
        }
    }

    data class DrawableIcon(
        val drawable: Any,
        override val description: String? = null,
        override val tint: UiColor? = null
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage {
            return DrawableIcon(drawable, contentDescription, tint)
        }
        override fun setTint(tint: UiColor?): UiImage {
            return DrawableIcon(drawable, description, tint)
        }
    }

    data class PainterIcon(
        val painter: Painter,
        override val description: String? = null,
        override val tint: UiColor? = null
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage {
            return PainterIcon(painter, contentDescription, tint)
        }
        override fun setTint(tint: UiColor?): UiImage {
            return PainterIcon(painter, description, tint)
        }
    }

    /**
     * A remote or local-file image loaded asynchronously via Coil.
     *
     * [model] accepts anything Coil understands: `String` URL, [Uri], [File],
     * `okhttp3.HttpUrl`, `Int` drawable-res, `ByteArray`, etc.
     */
    data class RemoteImage(
        val model: Any?,
        val placeholder: UiImage? = null,
        val error: UiImage? = null,
        val fallback: UiImage? = null,
        val crossfade: Boolean = true,
        val memoryCacheKey: String? = null,
        val diskCacheKey: String? = null,
        val allowHardware: Boolean = true,
        override val description: String? = null,
        override val tint: UiColor? = null,
    ) : UiImage(description, tint) {
        override fun setDescription(contentDescription: String?): UiImage =
            copy(description = contentDescription)
        override fun setTint(tint: UiColor?): UiImage =
            copy(tint = tint)
    }

    @Composable
    fun getImageVector(): ImageVector? {
        return when (this) {
            is ImageVectorIcon -> imageVector
            is VectorResourceIcon -> getImageVectorResource()
            is DrawableResourceIcon, is DrawableIcon, is PainterIcon, is RemoteImage -> null
        }
    }
}

@Composable
expect fun UiImage.VectorResourceIcon.getImageVectorResource(): ImageVector?

fun ImageVector.toUiImage(
    description: String? = null,
    tint: UiColor? = null
): UiImage = UiImage.ImageVectorIcon(this, description, tint)

fun uiImageResource(
    resId: Int,
    description: String? = null,
    tint: UiColor? = null
): UiImage = UiImage.DrawableResourceIcon(resId, description, tint)

/** Backward-compat alias for [uiImageResource] (historical typo preserved for existing callers). */
fun uiImageResouce(
    resId: Int,
    description: String? = null,
    tint: UiColor? = null
): UiImage = uiImageResource(resId, description, tint)

fun uiVectorResource(
    resId: Int,
    description: String? = null,
    tint: UiColor? = null
): UiImage = UiImage.VectorResourceIcon(resId, description, tint)

fun Painter.toUiImage(
    description: String? = null,
    tint: UiColor? = null
): UiImage = UiImage.PainterIcon(this, description, tint)

// ── Remote / URI image factories ─────────────────────────────────────────────

fun uiImageFromUrl(
    url: String?,
    placeholder: UiImage? = null,
    error: UiImage? = null,
    fallback: UiImage? = null,
    crossfade: Boolean = true,
    description: String? = null,
    tint: UiColor? = null,
): UiImage = UiImage.RemoteImage(
    model       = url,
    placeholder = placeholder,
    error       = error,
    fallback    = fallback,
    crossfade   = crossfade,
    description = description,
    tint        = tint,
)

/**
 * Generic factory — pass any model type Coil accepts (String, Uri, File,
 * HttpUrl, ByteArray, DrawableRes Int, etc.).
 */
fun uiImageRemote(
    model: Any?,
    placeholder: UiImage? = null,
    error: UiImage? = null,
    fallback: UiImage? = null,
    crossfade: Boolean = true,
    memoryCacheKey: String? = null,
    diskCacheKey: String? = null,
    allowHardware: Boolean = true,
    description: String? = null,
    tint: UiColor? = null,
): UiImage = UiImage.RemoteImage(
    model          = model,
    placeholder    = placeholder,
    error          = error,
    fallback       = fallback,
    crossfade      = crossfade,
    memoryCacheKey = memoryCacheKey,
    diskCacheKey   = diskCacheKey,
    allowHardware  = allowHardware,
    description    = description,
    tint           = tint,
)

@Composable
expect fun UiImage.getPainter(): Painter
