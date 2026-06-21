package io.github.appspiriment.kolt.composekmp.components.core.text

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.UiText

@Composable
fun KeyValuePairText(
    key: UiText,
    value: UiText,
    modifier: Modifier = Modifier,
    prefix: UiText? = null,
    keyColor: Color = Kolt.colors.onMainSurface,
    prefixColor: Color = Kolt.colors.onMainSurface,
    valueColor: Color = Kolt.colors.onMainSurface,
    keyStyle: TextStyle = Kolt.typography.textMedium.semiBold,
    valueStyle: TextStyle = Kolt.typography.textMedium,
    prefixStyle: TextStyle = Kolt.typography.textMedium,
    alignBothSides: Boolean = true,
    keyModifier: Modifier = Modifier,
    valueModifier: Modifier = Modifier,
    spaceBetween: Dp = 8.dp,
) {
    val finalModifier = if (alignBothSides) modifier.fillMaxWidth() else modifier.wrapContentWidth()
    Row(
        modifier = finalModifier,
        horizontalArrangement = if (alignBothSides) Arrangement.SpaceBetween else Arrangement.spacedBy(
            spaceBetween,
            Alignment.Start
        )
    ) {
        PrefixedText(
            text = key,
            prefix = prefix,
            color = keyColor,
            prefixColor = prefixColor,
            textStyle = keyStyle,
            prefixStyle = prefixStyle,
            modifier = keyModifier
        )
        AppspirimentText(
            text = value,
            color = valueColor,
            style = valueStyle,
            modifier = valueModifier
        )
    }
}
