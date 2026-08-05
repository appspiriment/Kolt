package io.github.appspiriment.kolt.composekmp.components.containers.bottomnavigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import io.github.appspiriment.kolt.composekmp.components.containers.types.AppsBottomBarButton
import io.github.appspiriment.kolt.composekmp.components.core.HorizontalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.semiBold
import io.github.appspiriment.kolt.composekmp.wrappers.toUiColor
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

// ponytail: the NavController-based `AppsBottomNavigation` wrapper from compose-utils was not
// ported — it depended on androidx.navigation-compose (NavController/currentBackStackEntryAsState),
// which isn't a compose-kmp dependency and is superseded by this project's Navigation 3
// (caller-owned back stack) convention. `AppsBottomNavBar` below is the presentation-only piece
// (no navigation dependency) and is fully ported; wire it to a Navigation 3 back stack at the
// call site by mapping `currentRoute` from your own NavKey list.

/**
 * A presentation-layer composable for a bottom navigation bar. It is independent of any navigation
 * logic and relies on the provided [currentRoute] to determine the selection state.
 *
 * This structure makes the component easily previewable and testable.
 *
 * @param routes A list of [AppsBottomBarButton] items to display in the bar.
 * @param currentRoute The currently selected route.
 * @param onRouteClick A callback invoked when a navigation item is clicked.
 * @param visibleItemsCount The maximum number of items to display. If the size of [routes] exceeds
 * this, a "More" button will be displayed with the rest in an overflow menu.
 * @param modifier A [Modifier] to be applied to the [BottomNavigation] container.
 * @param backgroundColor The background color of the navigation bar.
 * @param selectedColor The color for the icon and text of the currently selected item.
 * @param unselectedColor The color for the icon and text of unselected items.
 * @param selectedBubbleColor The background color for the bubble displayed behind the selected item.
 * @param itemContent A composable lambda that defines the content of each navigation item.
 */
@Composable
fun AppsBottomNavBar(
    routes: List<AppsBottomBarButton>,
    currentRoute: Any?,
    onRouteClick: (Any) -> Unit,
    modifier: Modifier = Modifier,
    visibleItemsCount: Int = 5,
    backgroundColor: Color = Kolt.colors.mainSurface,
    selectedColor: Color = Kolt.colors.primary,
    unselectedColor: Color = Kolt.colors.onPrimaryCardContainer,
    selectedBubbleColor: Color = Color.Transparent,
    itemContent: @Composable RowScope.(route: AppsBottomBarButton, isSelected: Boolean, selectedColor: Color, unselectedColor: Color) -> Unit = { route, isSelected, selColor, unselColor ->
        DefaultBottomNavigationItem(
            route = route,
            isSelected = isSelected,
            selectedColor = selColor,
            unselectedColor = unselColor,
            selectedBubbleColor = selectedBubbleColor,
            onClick = { onRouteClick(route.route) }
        )
    }
) {
    val (visibleRoutes, hiddenRoutes) = remember(routes, visibleItemsCount) {
        if (routes.size > visibleItemsCount && visibleItemsCount > 0) {
            val visibleCount = visibleItemsCount - 1
            Pair(routes.take(visibleCount), routes.drop(visibleCount))
        } else {
            Pair(routes, emptyList())
        }
    }

    BottomNavigation(
        backgroundColor = backgroundColor,
        modifier = modifier
    ) {
        HorizontalSpacer(Kolt.sizes.paddingSmall)
        visibleRoutes.forEach { route ->
            val isSelected = route.route == currentRoute
            itemContent(route, isSelected, selectedColor, unselectedColor)
        }

        if (hiddenRoutes.isNotEmpty()) {
            var showMoreMenu by remember { mutableStateOf(false) }
            val isMoreSelected = remember(currentRoute, hiddenRoutes) {
                hiddenRoutes.any { it.route == currentRoute }
            }
            DefaultBottomNavigationItem(
                route = AppsBottomBarButton(
                    name = "More",
                    route = hiddenRoutes.first().route, // Dummy route, click is overridden
                    icon = Icons.Default.MoreHoriz.toUiImage()
                ),
                isSelected = isMoreSelected,
                selectedColor = selectedColor,
                unselectedColor = unselectedColor,
                selectedBubbleColor = selectedBubbleColor,
                onClick = { showMoreMenu = true }
            )
            Box {
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    hiddenRoutes.forEach { hiddenRoute ->
                        DropdownMenuItem(onClick = {
                            onRouteClick(hiddenRoute.route)
                            showMoreMenu = false
                        }) {
                            Text(text = hiddenRoute.name)
                        }
                    }
                }
            }
        }
        HorizontalSpacer(Kolt.sizes.paddingSmall)
    }
}

/**
 * The default visual implementation for an item in the [AppsBottomNavBar].
 */
@Composable
fun RowScope.DefaultBottomNavigationItem(
    route: AppsBottomBarButton,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    selectedBubbleColor: Color,
    onClick: () -> Unit
) {
    BottomNavigationItem(
        icon = {
            AppsIcon(
                icon = route.icon.setTint(if (isSelected) selectedColor.toUiColor() else unselectedColor.toUiColor()),
                iconHeight = Kolt.sizes.iconMedium
            )
        },
        label = {
            AppspirimentText(
                text = route.name.toUiText(),
                color = if (isSelected) selectedColor else unselectedColor,
                style = if (isSelected) Kolt.typography.textSmall.semiBold else Kolt.typography.textSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Kolt.sizes.paddingXXSmall)
            )
        },
        selected = isSelected,
        onClick = onClick,
        modifier = Modifier
            .padding(vertical = Kolt.sizes.paddingSmall)
            .background(
                color = if (isSelected) selectedBubbleColor else Color.Transparent,
                shape = RoundedCornerShape(Kolt.sizes.cornerRadiusXLarge)
            )
    )
}
