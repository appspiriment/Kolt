package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.asAnnotatedString

@Composable
fun AppspirimentText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.textMedium,
    color: Color = Kolt.colors.onMainSurface,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val textContent = @Composable {
        Text(
            text = text,
            modifier = modifier.offset(y = Kolt.flags.notoFontPadding),
            style = style,
            letterSpacing = letterSpacing,
            color = color,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout
        )
    }
    if (selectable) {
        SelectionContainer { textContent() }
    } else {
        textContent()
    }
}

@Composable
fun AppspirimentText(
    text: UiText,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.textMedium,
    color: Color = Kolt.colors.onMainSurface,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    isHtml: Boolean = false,
    selectable: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val textContent = @Composable {
        Text(
            text = text.asAnnotatedString(isHtml),
            modifier = modifier.offset(y = Kolt.flags.notoFontPadding),
            style = style,
            letterSpacing = letterSpacing,
            color = color,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout
        )
    }
    if (selectable) {
        SelectionContainer { textContent() }
    } else {
        textContent()
    }
}

@Composable
fun AppspirimentText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.textMedium,
    color: Color = Kolt.colors.onMainSurface,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    selectable: Boolean = false,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
    val textContent = @Composable {
        Text(
            text = text,
            modifier = modifier.offset(y = Kolt.flags.notoFontPadding),
            style = style,
            letterSpacing = letterSpacing,
            color = color,
            textDecoration = textDecoration,
            textAlign = textAlign,
            lineHeight = lineHeight,
            overflow = overflow,
            softWrap = softWrap,
            maxLines = maxLines,
            onTextLayout = onTextLayout,
        )
    }
    if (selectable) {
        SelectionContainer { textContent() }
    } else {
        textContent()
    }
}
