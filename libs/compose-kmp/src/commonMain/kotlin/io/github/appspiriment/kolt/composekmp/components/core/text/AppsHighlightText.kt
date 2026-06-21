package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import io.github.appspiriment.kolt.composekmp.theme.Kolt

/**
 * Displays [text] with all occurrences of [query] highlighted using [highlightStyle].
 *
 * Matching is case-insensitive by default (controlled by [ignoreCase]).
 * When [query] is blank, the text is rendered without any highlights.
 *
 * @param text            Full text to display.
 * @param query           Substring to highlight. Blank → no highlighting.
 * @param modifier        Applied to the text composable.
 * @param style           Base text style.
 * @param color           Base text colour.
 * @param highlightStyle  [SpanStyle] applied to every [query] match.
 * @param ignoreCase      Whether the search is case-insensitive. Default `true`.
 * @param maxLines        Maximum number of visible lines.
 * @param overflow        Text overflow strategy.
 */
@Composable
fun AppsHighlightText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Kolt.typography.bodyMedium,
    color: Color = Kolt.colors.onMainSurface,
    highlightStyle: SpanStyle = SpanStyle(
        background = Kolt.colors.primary.copy(alpha = 0.20f),
        fontWeight = FontWeight.Bold,
        color = Kolt.colors.primary,
    ),
    ignoreCase: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    val annotated = remember(text, query, ignoreCase, highlightStyle) {
        if (query.isBlank()) {
            buildAnnotatedString { append(text) }
        } else {
            buildAnnotatedString {
                append(text)
                var startIndex = 0
                while (true) {
                    val matchStart = text.indexOf(query, startIndex, ignoreCase = ignoreCase)
                    if (matchStart == -1) break
                    val matchEnd = matchStart + query.length
                    addStyle(highlightStyle, matchStart, matchEnd)
                    startIndex = matchEnd
                }
            }
        }
    }

    AppspirimentText(
        text = annotated,
        modifier = modifier,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
    )
}
