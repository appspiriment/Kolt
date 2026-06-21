package io.github.appspiriment.kolt.composekmp.wrappers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

@Composable
actual fun UiImage.getPainter(): Painter {
    return when (this) {
        is UiImage.ImageVectorIcon -> rememberVectorPainter(image = imageVector)
        is UiImage.PainterIcon -> painter
        is UiImage.DrawableResourceIcon,
        is UiImage.VectorResourceIcon,
        is UiImage.DrawableIcon,
        is UiImage.RemoteImage -> {
            ColorPainter(Color.Transparent)
        }
    }
}

@Composable
actual fun UiImage.VectorResourceIcon.getImageVectorResource(): ImageVector? {
    return null
}
