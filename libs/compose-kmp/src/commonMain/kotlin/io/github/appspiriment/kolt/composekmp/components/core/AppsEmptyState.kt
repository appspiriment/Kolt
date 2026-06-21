package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.components.core.buttons.AppsButton
import io.github.appspiriment.kolt.composekmp.components.core.buttons.types.ButtonStyle
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

/**
 * A centred empty / zero-state screen with an optional illustration, title, body message,
 * and a single call-to-action button.
 *
 * Use this whenever a list or content area has nothing to show — search results with no
 * matches, a feed that hasn't been populated yet, an error recovery screen, and so on.
 *
 * ```kotlin
 * AppsEmptyState(
 *     illustration = Icons.Default.Warning.toUiImage(),
 *     title = "No results found".toUiText(),
 *     message = "Try adjusting your filters.".toUiText(),
 *     action = "Clear filters".toUiText(),
 *     onAction = { viewModel.clearFilters() },
 * )
 * ```
 *
 * @param title              Primary heading shown below the illustration.
 * @param modifier           Applied to the outer [Column].
 * @param message            Optional explanatory body text below [title].
 * @param illustration       Optional icon or image displayed above [title].
 * @param action             Label for the call-to-action button. `null` hides the button.
 * @param onAction           Called when [action] is tapped.
 * @param illustrationSize   Width and height of [illustration]. Defaults to `illustrationLarge`.
 * @param illustrationTint   Explicit tint for [illustration]. `null` = inherit from [UiImage.tint]
 *                           or [Kolt.colors.iconTint].
 * @param titleStyle         Text style for [title].
 * @param messageStyle       Text style for [message].
 * @param textColor          Colour applied to both [title] and [message].
 * @param actionStyle        [ButtonStyle] for the call-to-action button.
 * @param contentPadding     Padding applied inside the column, around all content.
 *                           Defaults to `paddingXLarge` on all sides.
 * @param spacing            Vertical space between the illustration, texts, and button.
 * @param titleMaxLines      Maximum number of lines for [title]. Defaults to unlimited — empty
 *                           state headings are often multi-line. Set to `1` for compact layouts.
 * @param messageMaxLines    Maximum number of lines for [message]. Defaults to unlimited.
 */
@Composable
fun AppsEmptyState(
    title: UiText,
    modifier: Modifier = Modifier,
    message: UiText? = null,
    illustration: UiImage? = null,
    action: UiText? = null,
    onAction: () -> Unit = {},
    illustrationSize: Dp = Kolt.sizes.illustrationLarge,
    illustrationTint: Color? = null,
    titleStyle: TextStyle = Kolt.typography.titleMedium.semiBold,
    messageStyle: TextStyle = Kolt.typography.bodyMedium,
    textColor: Color = Kolt.colors.subText,
    actionStyle: ButtonStyle = ButtonStyle.outlined(),
    contentPadding: PaddingValues = PaddingValues(Kolt.sizes.paddingXLarge),
    spacing: Dp = Kolt.sizes.paddingMedium,
    titleMaxLines: Int = Int.MAX_VALUE,
    messageMaxLines: Int = Int.MAX_VALUE,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        illustration?.let { icon ->
            AppsIcon(
                icon = icon,
                modifier = Modifier.size(illustrationSize),
                tint = illustrationTint,
                iconHeight = null,
            )
        }

        AppspirimentText(
            text = title,
            style = titleStyle,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = titleMaxLines,
            modifier = Modifier.fillMaxWidth(),
        )

        message?.let {
            AppspirimentText(
                text = it,
                style = messageStyle,
                color = textColor.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = messageMaxLines,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        action?.let {
            AppsButton(
                text = it,
                buttonStyle = actionStyle,
                modifier = Modifier.padding(top = Kolt.sizes.paddingSmall),
                onClick = onAction,
            )
        }
    }
}

