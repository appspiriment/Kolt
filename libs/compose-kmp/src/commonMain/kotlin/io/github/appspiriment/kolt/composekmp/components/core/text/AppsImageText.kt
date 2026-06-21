package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.HorizontalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsImage
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

@Composable
fun AppsImageText(
    text: UiText,
    modifier: Modifier = Modifier,
    color: Color = Kolt.colors.onMainSurface,
    style: TextStyle = Kolt.typography.textMedium,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    startingImage: UiImage? = null,
    trailingImage: UiImage? = null,
    startingImageHeight: Dp? = null,
    trailingImageHeight: Dp? = null,
    rowModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    iconPadding: Dp? = 8.dp,
    usePainter: Boolean = false,
    isHtml: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier.apply {
            onClick?.let { clickable { it.invoke() } }
        },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = rowModifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            startingImage?.let {
                AppsImage(
                    image = startingImage,
                    modifier = startingImageHeight?.let { h ->
                        Modifier.height(h)
                    } ?: Modifier,
                    usePainter = usePainter
                )

                iconPadding?.let { iconPadding ->
                    HorizontalSpacer(iconPadding)
                }
            }
            AppspirimentText(
                text = text,
                color = color,
                style = style,
                letterSpacing = letterSpacing,
                textDecoration = textDecoration,
                textAlign = textAlign,
                lineHeight = lineHeight,
                overflow = overflow,
                softWrap = softWrap,
                maxLines = maxLines,
                onTextLayout = onTextLayout,
                modifier = textModifier,
                isHtml = isHtml
            )

            trailingImage?.let {

                iconPadding?.let { padding ->
                    HorizontalSpacer(padding)
                }
                AppsImage(
                    image = it,
                    modifier = trailingImageHeight?.let { h ->
                        Modifier
                            .height(h)
                            .offset(y = (-1).dp)
                    } ?: Modifier,
                    usePainter = usePainter
                )
            }
        }
    }
}
