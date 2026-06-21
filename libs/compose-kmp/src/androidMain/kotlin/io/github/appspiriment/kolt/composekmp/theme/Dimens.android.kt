package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.dimensionResource
import io.github.appspiriment.kolt.composekmp.R
import io.github.appspiriment.kolt.composekmp.wrappers.textSizeResource
import io.github.appspiriment.kolt.composekmp.wrappers.uiDimenResource

@Composable
actual fun createSizes(): Sizes {
    return Sizes(
        appBarSize = dimensionResource(id = R.dimen.app_bar_height),

        iconXSmall = dimensionResource(id = R.dimen.icon_xsmall),
        iconSmall = dimensionResource(id = R.dimen.icon_small),
        iconMedium = dimensionResource(id = R.dimen.icon_medium),
        iconStandard = dimensionResource(id = R.dimen.icon_standard),
        iconStandardLarge = dimensionResource(id = R.dimen.icon_standard_large),
        iconLarge = dimensionResource(id = R.dimen.icon_large),
        iconXLarge = dimensionResource(id = R.dimen.icon_xlarge),
        iconXXLarge = dimensionResource(id = R.dimen.icon_xxlarge),
        iconXXXLarge = dimensionResource(id = R.dimen.icon_xxxlarge),
        iconXXXXLarge = dimensionResource(id = R.dimen.icon_xxxxlarge),
        iconBig = dimensionResource(id = R.dimen.icon_big),
        iconGiant = dimensionResource(id = R.dimen.icon_giant),

        paddingGiant = dimensionResource(id = R.dimen.padding_giant),
        paddingXXXXLarge = dimensionResource(id = R.dimen.padding_xxxxlarge),
        paddingXXXLarge = dimensionResource(id = R.dimen.padding_xxxlarge),
        paddingXXLarge = dimensionResource(id = R.dimen.padding_xxlarge),
        paddingXLarge = dimensionResource(id = R.dimen.padding_xlarge),
        paddingLarge = dimensionResource(id = R.dimen.padding_large),
        paddingMedium = dimensionResource(id = R.dimen.padding_medium),
        paddingSmallMedium = dimensionResource(id = R.dimen.padding_smallmedium),
        paddingSmall = dimensionResource(id = R.dimen.padding_small),
        paddingXSmallPlus = dimensionResource(id = R.dimen.padding_xsmall_plus),
        paddingXSmall = dimensionResource(id = R.dimen.padding_xsmall),
        paddingXXSmall = dimensionResource(id = R.dimen.padding_xxsmall),
        paddingTiny = dimensionResource(id = R.dimen.padding_xxsmall),
        noPadding = dimensionResource(id = R.dimen.no_padding),

        cornerRadiusSmall = dimensionResource(id = R.dimen.corner_radius_small),
        cornerRadiusMedium = dimensionResource(id = R.dimen.corner_radius_medium),
        cornerRadiusNormal = dimensionResource(id = R.dimen.corner_radius_normal),
        cornerRadiusMediumLarge = dimensionResource(id = R.dimen.corner_radius_medium_large),
        cornerRadiusLarge = dimensionResource(id = R.dimen.corner_radius_large),
        cornerRadiusXLarge = dimensionResource(id = R.dimen.corner_radius_xlarge),
        cornerRadiusXXLarge = dimensionResource(id = R.dimen.corner_radius_xxlarge),
        cornerRadiusXXXLarge = dimensionResource(id = R.dimen.corner_radius_xxlarge),

        actionButtonSize = dimensionResource(id = R.dimen.action_button_size),
        floatingButtonSizeSmall = dimensionResource(id = R.dimen.fab_button_size_small),
        floatingButtonSize = dimensionResource(id = R.dimen.fab_button_size),
        floatingButtonSizeLarge = dimensionResource(id = R.dimen.fab_button_size_large),

        fontSizeMinimum = textSizeResource(id = R.dimen.font_size_minimum),
        fontSizeTiny = textSizeResource(id = R.dimen.font_size_tiny),
        fontSizeXXXSmall = textSizeResource(id = R.dimen.font_size_xxxsmall),
        fontSizeXXSmall = textSizeResource(id = R.dimen.font_size_xxsmall),
        fontSizeXSmall = textSizeResource(id = R.dimen.font_size_xsmall),
        fontSizeXSmallMedium = textSizeResource(id = R.dimen.font_size_xsmall_medium),
        fontSizeSmall = textSizeResource(id = R.dimen.font_size_small),
        fontSizeSmallMedium = textSizeResource(id = R.dimen.font_size_small_medium),
        fontSizeMedium = textSizeResource(id = R.dimen.font_size_medium),
        fontSizeMediumMid = textSizeResource(id = R.dimen.font_size_medium_mid),
        fontSizeMediumLarge = textSizeResource(id = R.dimen.font_size_medium_large),
        fontSizeLarge = textSizeResource(id = R.dimen.font_size_large),
        fontSizeXLarge = textSizeResource(id = R.dimen.font_size_xlarge),
        fontSizeXXLarge = textSizeResource(id = R.dimen.font_size_xxlarge),
        fontSizeXXXLarge = textSizeResource(id = R.dimen.font_size_xxxlarge),
        fontSizeBig = textSizeResource(id = R.dimen.font_size_big),
        fontSizeXBig = textSizeResource(id = R.dimen.font_size_xbig),
        fontSizeHuge = textSizeResource(id = R.dimen.font_size_huge),
        fontSizeGiant = textSizeResource(id = R.dimen.font_size_giant),
    )
}

@Composable
actual fun createUiSizes(): UiSizes {
    return UiSizes(
        appBarSize = uiDimenResource(id = R.dimen.app_bar_height),

        iconXSmall = uiDimenResource(id = R.dimen.icon_xsmall),
        iconSmall = uiDimenResource(id = R.dimen.icon_small),
        iconMedium = uiDimenResource(id = R.dimen.icon_medium),
        iconStandard = uiDimenResource(id = R.dimen.icon_standard),
        iconStandardLarge = uiDimenResource(id = R.dimen.icon_standard_large),
        iconLarge = uiDimenResource(id = R.dimen.icon_large),
        iconXLarge = uiDimenResource(id = R.dimen.icon_xlarge),
        iconXXLarge = uiDimenResource(id = R.dimen.icon_xxlarge),
        iconXXXLarge = uiDimenResource(id = R.dimen.icon_xxxlarge),
        iconXXXXLarge = uiDimenResource(id = R.dimen.icon_xxxxlarge),
        iconBig = uiDimenResource(id = R.dimen.icon_big),
        iconGiant = uiDimenResource(id = R.dimen.icon_giant),

        paddingGiant = uiDimenResource(id = R.dimen.padding_giant),
        paddingXXXXLarge = uiDimenResource(id = R.dimen.padding_xxxxlarge),
        paddingXXXLarge = uiDimenResource(id = R.dimen.padding_xxxlarge),
        paddingXXLarge = uiDimenResource(id = R.dimen.padding_xxlarge),
        paddingXLarge = uiDimenResource(id = R.dimen.padding_xlarge),
        paddingLarge = uiDimenResource(id = R.dimen.padding_large),
        paddingMedium = uiDimenResource(id = R.dimen.padding_medium),
        paddingSmallMedium = uiDimenResource(id = R.dimen.padding_smallmedium),
        paddingSmall = uiDimenResource(id = R.dimen.padding_small),
        paddingXSmallPlus = uiDimenResource(id = R.dimen.padding_xsmall_plus),
        paddingXSmall = uiDimenResource(id = R.dimen.padding_xsmall),
        paddingXXSmall = uiDimenResource(id = R.dimen.padding_xxsmall),
        paddingTiny = uiDimenResource(id = R.dimen.padding_xxsmall),
        noPadding = uiDimenResource(id = R.dimen.no_padding),

        cornerRadiusSmall = uiDimenResource(id = R.dimen.corner_radius_small),
        cornerRadiusMedium = uiDimenResource(id = R.dimen.corner_radius_medium),
        cornerRadiusNormal = uiDimenResource(id = R.dimen.corner_radius_normal),
        cornerRadiusMediumLarge = uiDimenResource(id = R.dimen.corner_radius_medium_large),
        cornerRadiusLarge = uiDimenResource(id = R.dimen.corner_radius_large),
        cornerRadiusXLarge = uiDimenResource(id = R.dimen.corner_radius_xlarge),
        cornerRadiusXXLarge = uiDimenResource(id = R.dimen.corner_radius_xxlarge),
        cornerRadiusXXXLarge = uiDimenResource(id = R.dimen.corner_radius_xxlarge),

        actionButtonSize = uiDimenResource(id = R.dimen.action_button_size),
        floatingButtonSizeSmall = uiDimenResource(id = R.dimen.fab_button_size_small),
        floatingButtonSize = uiDimenResource(id = R.dimen.fab_button_size),
        floatingButtonSizeLarge = uiDimenResource(id = R.dimen.fab_button_size_large),

        fontSizeMinimum = uiDimenResource(id = R.dimen.font_size_minimum),
        fontSizeTiny = uiDimenResource(id = R.dimen.font_size_tiny),
        fontSizeXXXSmall = uiDimenResource(id = R.dimen.font_size_xxxsmall),
        fontSizeXXSmall = uiDimenResource(id = R.dimen.font_size_xxsmall),
        fontSizeXSmall = uiDimenResource(id = R.dimen.font_size_xsmall),
        fontSizeXSmallMedium = uiDimenResource(id = R.dimen.font_size_xsmall_medium),
        fontSizeSmall = uiDimenResource(id = R.dimen.font_size_small),
        fontSizeSmallMedium = uiDimenResource(id = R.dimen.font_size_small_medium),
        fontSizeMedium = uiDimenResource(id = R.dimen.font_size_medium),
        fontSizeMediumMid = uiDimenResource(id = R.dimen.font_size_medium_mid),
        fontSizeMediumLarge = uiDimenResource(id = R.dimen.font_size_medium_large),
        fontSizeLarge = uiDimenResource(id = R.dimen.font_size_large),
        fontSizeXLarge = uiDimenResource(id = R.dimen.font_size_xlarge),
        fontSizeXXLarge = uiDimenResource(id = R.dimen.font_size_xxlarge),
        fontSizeXXXLarge = uiDimenResource(id = R.dimen.font_size_xxxlarge),
        fontSizeBig = uiDimenResource(id = R.dimen.font_size_big),
        fontSizeHuge = uiDimenResource(id = R.dimen.font_size_huge),
        fontSizeGiant = uiDimenResource(id = R.dimen.font_size_giant),
    )
}
