package io.github.appspiriment.kolt.composeutils.components.containers.bottomnavigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import io.github.appspiriment.kolt.composeutils.components.containers.PageScaffold
import io.github.appspiriment.kolt.composeutils.components.containers.types.AppsBottomBarButton
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.colors


/**
 * A scaffold that automatically integrates a [NavHost] with an [AppsBottomNavigation] bar.
 *
 * This component simplifies the common pattern of having a bottom navigation bar that is only
 * visible for a specific set of top-level destinations. It automatically handles the visibility
 * of the bar based on the current route.
 *
 * @param navController The [NavHostController] for the navigation graph.
 * @param startDestination The starting route of the graph.
 * @param bottomBarItems The list of routes that should be displayed in the bottom navigation bar.
 *        The bar will only be visible when the current destination is one of these routes.
 * @param modifier A modifier to be applied to the [PageScaffold].
 * @param bottomNavbarModifier A modifier to be applied to the [AppsBottomNavigation] component.
 * @param visibleItemsCount The maximum number of items to display in the bottom bar before showing a "More" menu.
 * @param topDivider An optional composable for the divider line shown above the bottom navigation bar.
 *                   Set to `null` to hide it.
 * @param backgroundColor The background color of the navigation bar.
 * @param selectedColor The color for the selected item's icon and text.
 * @param unselectedColor The color for unselected items' icons and text.
 * @param selectedBubbleColor The background color for the bubble behind the selected item.
 * @param navGraphBuilderScope A lambda to define the navigation graph using the [NavGraphBuilder].
 */
@Composable
fun AppsBottomNavigationNavHost(
    navController: NavHostController,
    startDestination: Any,
    bottomBarItems: List<AppsBottomBarButton>,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    bottomNavbarModifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    topDivider: @Composable (() -> Unit)? = { HorizontalDivider(thickness = 0.5.dp, color = colors.primaryCardContainer) },
    backgroundColor: Color = Kolt.colors.mainSurface,
    selectedColor: Color = Kolt.colors.primary,
    unselectedColor: Color = Kolt.colors.onPrimaryCardContainer,
    selectedBubbleColor: Color = Color.Transparent,
    navGraphBuilderScope: NavGraphBuilder.(PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination


    PageScaffold(
        topBar = null,
        bottomBar = {
            if (showBottomBar) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    topDivider?.invoke()
                    AppsBottomNavigation(
                        navController = navController,
                        bottomBarRoutes = bottomBarItems,
                        modifier = bottomNavbarModifier,
                        visibleItemsCount = visibleItemsCount,
                        backgroundColor = backgroundColor,
                        selectedColor = selectedColor,
                        unselectedColor = unselectedColor,
                        selectedBubbleColor = selectedBubbleColor,
                    )
                }
            }
        },
        modifier = modifier.navigationBarsPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.padding(bottom = it.calculateBottomPadding())
        ) {
            navGraphBuilderScope(this, it)
        }
    }
}
