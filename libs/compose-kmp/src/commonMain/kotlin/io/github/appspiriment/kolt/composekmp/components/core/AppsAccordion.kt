package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.SmoothCornerShape
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

/**
 * A premium animated collapsible panel (accordion) component.
 * Uses G2 smooth corners [SmoothCornerShape] for premium aesthetics.
 *
 * @param expanded Whether the accordion is currently open.
 * @param onExpandedChange Callback triggered when the expansion state changes.
 * @param header The composable content for the header action slot.
 * @param modifier The modifier to be applied to the outer Card container.
 * @param headerPadding Inner padding for the header slot click target.
 * @param contentPadding Inner padding for the body content slot.
 * @param cardColors The colors of the card background.
 * @param shape The shape of the accordion container. Defaults to [SmoothCornerShape].
 * @param content Composable child content rendered within [AnimatedVisibilityScope].
 */
@Composable
fun AppsAccordion(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    header: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    headerPadding: PaddingValues = PaddingValues(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    cardColors: CardColors = CardDefaults.cardColors(),
    shape: Shape = SmoothCornerShape(),
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = cardColors,
        shape = shape
    ) {
        Column {
            Box(
                modifier = Modifier
                    .clickable { onExpandedChange(!expanded) }
                    .padding(headerPadding)
                    .fillMaxWidth()
            ) {
                header()
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Box(modifier = Modifier.padding(contentPadding)) {
                    content()
                }
            }
        }
    }
}

/**
 * Overloaded convenience [AppsAccordion] that takes a standard title and optional subtitle directly.
 */
@Composable
fun AppsAccordion(
    title: UiText,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: UiText? = null,
    headerPadding: PaddingValues = PaddingValues(16.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    cardColors: CardColors = CardDefaults.cardColors(),
    shape: Shape = SmoothCornerShape(),
    titleStyle: TextStyle = Kolt.typography.textMedium.semiBold,
    subtitleStyle: TextStyle = Kolt.typography.textSmall,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AppsAccordion(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = modifier,
        headerPadding = headerPadding,
        contentPadding = contentPadding,
        cardColors = cardColors,
        shape = shape,
        header = {
            AppsAccordionHeader(
                title = title,
                subtitle = subtitle,
                expanded = expanded,
                titleStyle = titleStyle,
                subtitleStyle = subtitleStyle
            )
        },
        content = content
    )
}

/**
 * Default header layout wrapper for accordions, with a title, subtitle, and rotating chevron.
 */
@Composable
fun AppsAccordionHeader(
    title: UiText,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    subtitle: UiText? = null,
    titleStyle: TextStyle = Kolt.typography.textMedium.semiBold,
    subtitleStyle: TextStyle = Kolt.typography.textSmall,
) {
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            AppspirimentText(
                text = title,
                style = titleStyle,
                color = Kolt.colors.onMainSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                AppspirimentText(
                    text = subtitle,
                    style = subtitleStyle,
                    color = Kolt.colors.onMainSurface.copy(alpha = 0.7f)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = if (expanded) "Collapse" else "Expand",
            modifier = Modifier.rotate(rotation),
            tint = Kolt.colors.onMainSurface
        )
    }
}
