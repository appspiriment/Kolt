package io.github.appspiriment.kolt.composekmp.components.core.image

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.asColor
import io.github.appspiriment.kolt.composekmp.wrappers.getPainter
import io.github.appspiriment.kolt.composekmp.theme.Kolt

/**
 * Renders a [UiImage] as an icon using `Icon()`.
 *
 * @param icon               The image to display — vector, painter, or resource.
 * @param modifier           Applied to the Icon. Do NOT pass `.size()` here AND provide [size]
 *                           at the same time; pick one mechanism.
 * @param iconHeight         Constrains the height of the icon. Pass `null` to let [modifier]
 *                           or [size] fully control the dimensions.
 * @param tint               Explicit tint override. `null` → use [UiImage.tint] → fall back to
 *                           [LocalContentColor].
 * @param contentDescription Accessibility label; overrides [UiImage.description] when provided.
 * @param size               If non-null, applies `Modifier.size(size)` after [modifier].
 */
@Composable
fun AppsIcon(
    icon: UiImage,
    modifier: Modifier = Modifier,
    iconHeight: Dp? = Kolt.sizes.iconStandard,
    tint: Color? = null,
    contentDescription: String? = null,
    size: Dp? = null,
) {
    val resolvedTint = tint
        ?: icon.tint?.asColor()
        ?: LocalContentColor.current

    val resolvedDesc = contentDescription ?: icon.description

    val baseModifier = when {
        size != null -> modifier.size(size)
        iconHeight != null -> modifier.height(iconHeight)
        else -> modifier
    }

    icon.getImageVector()?.let {
        Icon(
            imageVector = it,
            contentDescription = resolvedDesc,
            tint = resolvedTint,
            modifier = baseModifier,
        )
    } ?: Icon(
        painter = icon.getPainter(),
        contentDescription = resolvedDesc,
        tint = resolvedTint,
        modifier = baseModifier,
    )
}
