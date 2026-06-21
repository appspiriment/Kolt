package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.unit.Dp
import io.github.appspiriment.kolt.composekmp.theme.Kolt

@Composable
fun VerticalSpacer(height: Dp = Kolt.sizes.paddingMedium){
    Spacer(modifier = Modifier.height(height))
}

@Composable
fun HorizontalSpacer(width: Dp  = Kolt.sizes.paddingMedium){
    Spacer(modifier = Modifier.width(width))
}

@Composable
fun ColumnScope.FillerSpacer(minHeight: Dp = Kolt.sizes.noPadding){
    Spacer(modifier = Modifier.heightIn(min = minHeight).weight(1f))
}

@Composable
fun RowScope.FillerSpacer(minWidth: Dp = Kolt.sizes.noPadding){
    Spacer(modifier = Modifier.widthIn(min = minWidth).weight(1f))
}