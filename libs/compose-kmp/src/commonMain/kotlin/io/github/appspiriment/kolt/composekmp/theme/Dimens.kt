package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import io.github.appspiriment.kolt.composekmp.wrappers.UiDimen


data class Sizes(
    val appBarSize: Dp = Dp.Unspecified,

    val iconXXSmall: Dp = Dp.Unspecified,
    val iconXSmall: Dp = Dp.Unspecified,
    val iconSmall: Dp = Dp.Unspecified,
    val iconMedium: Dp = Dp.Unspecified,
    val iconStandard: Dp = Dp.Unspecified,
    val iconStandardLarge: Dp = Dp.Unspecified,
    val iconLarge: Dp = Dp.Unspecified,
    val iconXLarge: Dp = Dp.Unspecified,
    val iconXXLarge: Dp = Dp.Unspecified,
    val iconXXXLarge: Dp = Dp.Unspecified,
    val iconXXXXLarge: Dp = Dp.Unspecified,
    val iconBig: Dp = Dp.Unspecified,
    val iconGiant: Dp = Dp.Unspecified,
    /** Large illustration size for empty-state / hero screens. */
    val illustrationLarge: Dp = Dp.Unspecified,

    val paddingGiant: Dp = Dp.Unspecified,
    val paddingXXXXLarge: Dp = Dp.Unspecified,
    val paddingXXXLarge: Dp = Dp.Unspecified,
    val paddingXXLarge: Dp = Dp.Unspecified,
    val paddingXLarge: Dp = Dp.Unspecified,
    val paddingLarge: Dp = Dp.Unspecified,
    val paddingMedium: Dp = Dp.Unspecified,
    val paddingSmallMedium: Dp = Dp.Unspecified,
    val paddingSmall: Dp = Dp.Unspecified,
    val paddingXSmallPlus: Dp = Dp.Unspecified,
    val paddingXSmall: Dp = Dp.Unspecified,
    val paddingXXSmall: Dp = Dp.Unspecified,
    val paddingTiny: Dp = Dp.Unspecified,
    val noPadding: Dp = Dp.Unspecified,

    val cornerRadiusSmall: Dp = Dp.Unspecified,
    val cornerRadiusMedium: Dp = Dp.Unspecified,
    val cornerRadiusNormal: Dp = Dp.Unspecified,
    val cornerRadiusMediumLarge: Dp = Dp.Unspecified,
    val cornerRadiusLarge: Dp = Dp.Unspecified,
    val cornerRadiusXLarge: Dp = Dp.Unspecified,
    val cornerRadiusXXLarge: Dp = Dp.Unspecified,
    val cornerRadiusXXXLarge: Dp = Dp.Unspecified,

    val actionButtonSize: Dp = Dp.Unspecified,
    val floatingButtonSizeSmall: Dp = Dp.Unspecified,
    val floatingButtonSize: Dp = Dp.Unspecified,
    val floatingButtonSizeLarge: Dp = Dp.Unspecified,

    val fontSizeMinimum: TextUnit = TextUnit.Unspecified,
    val fontSizeTiny: TextUnit = TextUnit.Unspecified,
    val fontSizeXXXSmall: TextUnit = TextUnit.Unspecified,
    val fontSizeXXSmall: TextUnit = TextUnit.Unspecified,
    val fontSizeXSmall: TextUnit = TextUnit.Unspecified,
    val fontSizeXSmallMedium: TextUnit = TextUnit.Unspecified,
    val fontSizeSmall: TextUnit = TextUnit.Unspecified,
    val fontSizeSmallMedium: TextUnit = TextUnit.Unspecified,
    val fontSizeMedium: TextUnit = TextUnit.Unspecified,
    val fontSizeMediumMid: TextUnit = TextUnit.Unspecified,
    val fontSizeMediumLarge: TextUnit = TextUnit.Unspecified,
    val fontSizeLarge: TextUnit = TextUnit.Unspecified,
    val fontSizeXLarge: TextUnit = TextUnit.Unspecified,
    val fontSizeXXLarge: TextUnit = TextUnit.Unspecified,
    val fontSizeXXXLarge: TextUnit = TextUnit.Unspecified,
    val fontSizeBig: TextUnit = TextUnit.Unspecified,
    val fontSizeXBig: TextUnit = TextUnit.Unspecified,
    val fontSizeHuge: TextUnit = TextUnit.Unspecified,
    val fontSizeGiant: TextUnit = TextUnit.Unspecified,
)


data class UiSizes(
    val appBarSize: UiDimen = UiDimen.DynamicDp.Unspecified,

    val iconXSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconMedium: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconStandard: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconStandardLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconXXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconXXXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconBig: UiDimen = UiDimen.DynamicDp.Unspecified,
    val iconGiant: UiDimen = UiDimen.DynamicDp.Unspecified,

    val paddingGiant: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXXXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingMedium: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingSmallMedium: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXSmallPlus: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingXXSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val paddingTiny: UiDimen = UiDimen.DynamicDp.Unspecified,
    val noPadding: UiDimen = UiDimen.DynamicDp.Unspecified,

    val cornerRadiusSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusMedium: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusNormal: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusMediumLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,
    val cornerRadiusXXXLarge: UiDimen = UiDimen.DynamicDp.Unspecified,

    val actionButtonSize: UiDimen = UiDimen.DynamicDp.Unspecified,
    val floatingButtonSizeSmall: UiDimen = UiDimen.DynamicDp.Unspecified,
    val floatingButtonSize: UiDimen = UiDimen.DynamicDp.Unspecified,
    val floatingButtonSizeLarge: UiDimen = UiDimen.DynamicDp.Unspecified,

    val fontSizeMinimum: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeTiny: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXXXSmall: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXXSmall: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXSmall: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXSmallMedium: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeSmall: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeSmallMedium: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeMedium: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeMediumMid: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeMediumLarge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeLarge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXLarge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXXLarge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeXXXLarge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeBig: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeHuge: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
    val fontSizeGiant: UiDimen = UiDimen.DynamicTextUnit.Unspecified,
)

@Composable
internal expect fun createSizes(): Sizes

@Composable
internal expect fun createUiSizes(): UiSizes

val LocalSizes by lazy { staticCompositionLocalOf { Sizes() } }
val LocalUiSizes by lazy { staticCompositionLocalOf { UiSizes() } }
