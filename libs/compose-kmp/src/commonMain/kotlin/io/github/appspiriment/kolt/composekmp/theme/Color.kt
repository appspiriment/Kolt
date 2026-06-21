package io.github.appspiriment.kolt.composekmp.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.appspiriment.kolt.composekmp.wrappers.UiColor
import io.github.appspiriment.kolt.composekmp.wrappers.toUiColor

data class BaseColors(
    val primary: Color = Color.Unspecified,
    val onPrimary : Color = Color.Unspecified,
    val secondary : Color = Color.Unspecified,
    val onSecondary : Color = Color.Unspecified,
    val tertiary : Color = Color.Unspecified,
    val onTertiary : Color = Color.Unspecified,
    val iconTint: Color = Color.Unspecified,
    val disabledIconTint: Color = Color.Unspecified,
    val drawerItem: Color = Color.Unspecified,
    val scrimColor : Color = Color.Unspecified,
    val navigationBarColor : Color = Color.Unspecified,
    val mainSurface : Color = Color.Unspecified,
    val onMainSurface : Color = Color.Unspecified,
    val secondarySurface : Color = Color.Unspecified,
    val onSecondarySurface : Color = Color.Unspecified,
    val background : Color = Color.Unspecified,
    val onBackground : Color = Color.Unspecified,
    val primaryCardContainer : Color = Color.Unspecified,
    val onPrimaryCardContainer : Color = Color.Unspecified,
    val onPrimaryCardContainerDimmed : Color = Color.Unspecified,
    val secondaryCardContainer : Color = Color.Unspecified,
    val onSecondaryCardContainer : Color = Color.Unspecified,
    val tertiaryCardContainer : Color = Color.Unspecified,
    val onTertiaryCardContainer : Color = Color.Unspecified,
    val greyCardContainer : Color = Color.Unspecified,
    val onGreyCardContainer : Color = Color.Unspecified,
    val error : Color = Color.Unspecified,
    val onError: Color = Color.Unspecified,
    val errorCardContainer : Color = Color.Unspecified,
    val onErrorCardContainer : Color = Color.Unspecified,
    val accentedBlueTitle : Color = Color.Unspecified,
    val accentedBlackTitle : Color = Color.Unspecified,
    val accentedRedTitle : Color = Color.Unspecified,
    val accentedBlueText : Color = Color.Unspecified,
    val accentedRedText : Color = Color.Unspecified,
    val accentedGoldText : Color = Color.Unspecified,
    val topAppBar : Color = Color.Unspecified,
    val onTopAppBar : Color = Color.Unspecified,
    val disabledText : Color = Color.Unspecified,
    val subText : Color = Color.Unspecified,
    val hintText : Color = Color.Unspecified,
    val dividerColor : Color = Color.Unspecified,
) {
    // ── Semantic status colors (derive from card-container palette when not overridden) ──

    /** Background of a "success" tag / badge. Defaults to [primaryCardContainer]. */
    val successContainer: Color get() = primaryCardContainer
    /** Content on top of [successContainer]. Defaults to [onPrimaryCardContainer]. */
    val onSuccessContainer: Color get() = onPrimaryCardContainer

    /** Background of a "warning" tag / badge. Defaults to [tertiaryCardContainer]. */
    val warningContainer: Color get() = tertiaryCardContainer
    /** Content on top of [warningContainer]. Defaults to [onTertiaryCardContainer]. */
    val onWarningContainer: Color get() = onTertiaryCardContainer

    /** Background of an "info" tag / badge. Defaults to [secondaryCardContainer]. */
    val infoContainer: Color get() = secondaryCardContainer
    /** Content on top of [infoContainer]. Defaults to [onSecondaryCardContainer]. */
    val onInfoContainer: Color get() = onSecondaryCardContainer

    /** Background of an "error" tag / badge. Defaults to [errorCardContainer]. */
    val errorContainer: Color get() = errorCardContainer
    /** Content on top of [errorContainer]. Defaults to [onErrorCardContainer]. */
    val onErrorContainer: Color get() = onErrorCardContainer

    /** Gold accent colour (e.g. star rating). Defaults to [accentedGoldText]. */
    val accentedGold: Color get() = accentedGoldText
    /** Lighter gold accent (e.g. empty star). Derives from [accentedGoldText] with reduced opacity. */
    val accentedGoldLight: Color get() = accentedGoldText.copy(alpha = 0.35f)

    /** Border / divider outline colour. Alias for [dividerColor]. */
    val outline: Color get() = dividerColor

    /** M3 alias for [primaryCardContainer]. */
    val primaryContainer: Color get() = primaryCardContainer

    /** Generic "success" colour. Defaults to [primary] (green-branded apps). */
    val success: Color get() = primary

    /** "Accented blue" link colour. Defaults to [accentedBlueText]. */
    val accentedBlue: Color get() = accentedBlueText
}
data class BaseUiColors(
    val primary: UiColor = UiColor.Unspecified,
    val onPrimary : UiColor = UiColor.Unspecified,
    val secondary : UiColor = UiColor.Unspecified,
    val onSecondary : UiColor = UiColor.Unspecified,
    val tertiary : UiColor = UiColor.Unspecified,
    val onTertiary : UiColor = UiColor.Unspecified,
    val iconTint: UiColor = UiColor.Unspecified,
    val disabledIconTint: UiColor = UiColor.Unspecified,
    val drawerItem: UiColor = UiColor.Unspecified,
    val scrimColor : UiColor = UiColor.Unspecified,
    val navigationBarColor : UiColor = UiColor.Unspecified,
    val mainSurface : UiColor = UiColor.Unspecified,
    val onMainSurface : UiColor = UiColor.Unspecified,
    val secondarySurface : UiColor = UiColor.Unspecified,
    val onSecondarySurface : UiColor = UiColor.Unspecified,
    val background : UiColor = UiColor.Unspecified,
    val onBackground : UiColor = UiColor.Unspecified,
    val primaryCardContainer : UiColor = UiColor.Unspecified,
    val onPrimaryCardContainer : UiColor = UiColor.Unspecified,
    val onPrimaryCardContainerDimmed : UiColor = UiColor.Unspecified,
    val secondaryCardContainer : UiColor = UiColor.Unspecified,
    val onSecondaryCardContainer : UiColor = UiColor.Unspecified,
    val tertiaryCardContainer : UiColor = UiColor.Unspecified,
    val onTertiaryCardContainer : UiColor = UiColor.Unspecified,
    val greyCardContainer : UiColor = UiColor.Unspecified,
    val onGreyCardContainer : UiColor = UiColor.Unspecified,
    val error : UiColor = UiColor.Unspecified,
    val onError: UiColor = UiColor.Unspecified,
    val errorCardContainer : UiColor = UiColor.Unspecified,
    val onErrorCardContainer : UiColor = UiColor.Unspecified,
    val accentedBlueTitle : UiColor = UiColor.Unspecified,
    val accentedBlackTitle : UiColor = UiColor.Unspecified,
    val accentedRedTitle : UiColor = UiColor.Unspecified,
    val accentedBlueText : UiColor = UiColor.Unspecified,
    val accentedRedText : UiColor = UiColor.Unspecified,
    val accentedGoldText : UiColor = UiColor.Unspecified,
    val topAppBar : UiColor = UiColor.Unspecified,
    val onTopAppBar : UiColor = UiColor.Unspecified,
    val disabledText : UiColor = UiColor.Unspecified,
    val subText : UiColor = UiColor.Unspecified,
    val hintText : UiColor = UiColor.Unspecified,
    val dividerColor : UiColor = UiColor.Unspecified,
)

@Composable
internal expect fun baseColors(isDarkTheme: Boolean): BaseColors

@Composable
internal expect fun baseUiColors(isDarkTheme: Boolean): BaseUiColors

fun getLightColors() = BaseColors(
    primary = Color(0xFFC20F2F),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF121212),
    tertiary = Color(0xFF3700B3),
    onTertiary = Color(0xFFFFFFFF),
    scrimColor = Color(0xFF020202),
    iconTint = Color(0xFF888E92),
    disabledIconTint = Color(0xFF5D1D3B),
    drawerItem = Color(0xFF5F6368),
    navigationBarColor = Color(0xFF282828),
    mainSurface = Color(0xFFFFFFFF),
    onMainSurface = Color(0xFF121212),
    secondarySurface = Color(0xFFEBEBEB),
    onSecondarySurface = Color(0xFF121212),
    background = Color(0xFFF3F3F3),
    onBackground = Color(0xFF1C1B1F),
    primaryCardContainer = Color(0xFFFFFFFF),
    onPrimaryCardContainer = Color(0xFF121212),
    onPrimaryCardContainerDimmed = Color(0xFF5D6364),
    secondaryCardContainer = Color(0xFFEBEBEB),
    onSecondaryCardContainer = Color(0xFF121212),
    tertiaryCardContainer = Color(0xFFEBEBEB),
    onTertiaryCardContainer = Color(0xFF121212),
    greyCardContainer = Color(0xFFF1F1F1),
    onGreyCardContainer = Color(0xFF121212),
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF),
    errorCardContainer = Color(0xFFFFB5B5),
    onErrorCardContainer = Color(0xFF7E0000),
    accentedBlueTitle = Color(0xFF027DDF),
    accentedBlackTitle = Color(0xFF121212),
    accentedRedTitle = Color(0xFFAC081D),
    accentedBlueText = Color(0xFF027DDF),
    accentedRedText = Color(0xFFAC081D),
    accentedGoldText = Color(0xFFC46F08),
    topAppBar = Color(0xFFC20F2F),
    onTopAppBar = Color(0xFFFFFFFF),
    disabledText = Color(0xFFBDBDBD),
    subText = Color(0xFF757575),
    hintText = Color(0x99121212),
    dividerColor = Color(0xFFBDBDBD)
)

fun getDarkColors() = BaseColors(
    primary = Color(0xFFBB86FC),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF018786),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF3700B3),
    onTertiary = Color(0xFFFFFFFF),
    scrimColor = Color(0xFF020202),
    iconTint = Color(0xFF697075),
    disabledIconTint = Color(0xFF6A4F90),
    drawerItem = Color(0xFFBDBDBD),
    navigationBarColor = Color(0xFF020202),
    mainSurface = Color(0xFF121212),
    onMainSurface = Color(0xFFDADADA),
    secondarySurface = Color(0xFF313131),
    onSecondarySurface = Color(0xFFDADADA),
    background = Color(0xFF000000),
    onBackground = Color(0xFFDADADA),
    primaryCardContainer = Color(0xFF0C1117),
    onPrimaryCardContainer = Color(0xFFEAEBEE),
    onPrimaryCardContainerDimmed = Color(0xFF83888E),
    secondaryCardContainer = Color(0xFF313131),
    onSecondaryCardContainer = Color(0xFFDADADA),
    tertiaryCardContainer = Color(0xFF313131),
    onTertiaryCardContainer = Color(0xFFDADADA),
    greyCardContainer = Color(0xFF313131),
    onGreyCardContainer = Color(0xFFDADADA),
    error = Color(0xFFCF6679),
    onError = Color(0xFF31060D),
    errorCardContainer = Color(0xABCF6679),
    onErrorCardContainer = Color(0xABFFEDDB),
    accentedBlueTitle = Color(0xFFC694FF),
    accentedBlackTitle = Color(0xFFC694FF),
    accentedRedTitle = Color(0xFFBB86FC),
    accentedBlueText = Color(0xAB03DAC6),
    accentedRedText = Color(0xFFBB86FC),
    accentedGoldText = Color(0xFFEB8202),
    topAppBar = Color(0xFF1E1E1E),
    onTopAppBar = Color(0xFFF1F1F2),
    disabledText = Color(0xFF616161),
    subText = Color(0xFFBDBDBD),
    hintText = Color(0xFF8B9096),
    dividerColor = Color(0x99636363)
)

fun BaseColors.toUiColors() = BaseUiColors(
    primary = primary.toUiColor(),
    onPrimary = onPrimary.toUiColor(),
    secondary = secondary.toUiColor(),
    onSecondary = onSecondary.toUiColor(),
    tertiary = tertiary.toUiColor(),
    onTertiary = onTertiary.toUiColor(),
    scrimColor = scrimColor.toUiColor(),
    iconTint = iconTint.toUiColor(),
    disabledIconTint = disabledIconTint.toUiColor(),
    drawerItem = drawerItem.toUiColor(),
    navigationBarColor = navigationBarColor.toUiColor(),
    mainSurface = mainSurface.toUiColor(),
    onMainSurface = onMainSurface.toUiColor(),
    secondarySurface = secondarySurface.toUiColor(),
    onSecondarySurface = onSecondarySurface.toUiColor(),
    background = background.toUiColor(),
    onBackground = onBackground.toUiColor(),
    primaryCardContainer = primaryCardContainer.toUiColor(),
    onPrimaryCardContainer = onPrimaryCardContainer.toUiColor(),
    onPrimaryCardContainerDimmed = onPrimaryCardContainerDimmed.toUiColor(),
    secondaryCardContainer = secondaryCardContainer.toUiColor(),
    onSecondaryCardContainer = onSecondaryCardContainer.toUiColor(),
    tertiaryCardContainer = tertiaryCardContainer.toUiColor(),
    onTertiaryCardContainer = onTertiaryCardContainer.toUiColor(),
    greyCardContainer = greyCardContainer.toUiColor(),
    onGreyCardContainer = onGreyCardContainer.toUiColor(),
    error = error.toUiColor(),
    onError = onError.toUiColor(),
    errorCardContainer = errorCardContainer.toUiColor(),
    onErrorCardContainer = onErrorCardContainer.toUiColor(),
    accentedBlueTitle = accentedBlueTitle.toUiColor(),
    accentedBlackTitle = accentedBlackTitle.toUiColor(),
    accentedRedTitle = accentedRedTitle.toUiColor(),
    accentedBlueText = accentedBlueText.toUiColor(),
    accentedRedText = accentedRedText.toUiColor(),
    accentedGoldText = accentedGoldText.toUiColor(),
    topAppBar = topAppBar.toUiColor(),
    onTopAppBar = onTopAppBar.toUiColor(),
    disabledText = disabledText.toUiColor(),
    subText = subText.toUiColor(),
    hintText = hintText.toUiColor(),
    dividerColor = dividerColor.toUiColor()
)


val LocalColors = staticCompositionLocalOf { BaseColors() }
val LocalUiColors = staticCompositionLocalOf { BaseUiColors() }
