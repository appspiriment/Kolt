package io.github.appspiriment.kolt.composekmp.components.containers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.containers.types.AppsTopBarButton
import io.github.appspiriment.kolt.composekmp.components.containers.types.DrawerItem
import io.github.appspiriment.kolt.composekmp.components.containers.types.ScaffoldColors
import io.github.appspiriment.kolt.composekmp.components.core.VerticalSpacer
import io.github.appspiriment.kolt.composekmp.components.core.image.AppsIcon
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.theme.Kolt.typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A marker interface to provide a common type for all drawer item identifiers.
 * This allows for type-safe handling of click events in the `onDrawerItemClicked` callback.
 *
 * Example:
 * ```
 * enum class MyDrawerItems : DrawerIdentifier {
 *     HOME, SETTINGS, PROFILE
 * }
 * ```
 */
interface DrawerIdentifier

/**
 * A high-level, opinionated scaffold that provides a navigation drawer with a pre-configured
 * [AppsTopBar]. It is ideal for standard screens that require a drawer, a title, and actions.
 *
 * This composable simplifies the setup by composing [AppsDrawerScaffold] with a standard top bar
 * and a list-based drawer content.
 *
 * @param appBarTitle The title configuration for the top app bar. See [AppBarTitle].
 * @param colors The color scheme for the scaffold components (top bar, drawer, etc.).
 * @param gestureEnabled Whether the drawer can be opened and closed with a swipe gesture.
 * @param drawerShape The shape of the drawer's container.
 * @param scrimColor The color of the scrim that overlays the main content when the drawer is open.
 * @param actions A list of buttons to display on the right side of the top app bar.
 * @param drawerOptions The list of items to display in the navigation drawer. See [DrawerItem].
 * @param drawerHeader An optional composable to be displayed at the top of the drawer.
 * @param drawerFooter An optional composable to be displayed at the bottom of the drawer.
 * @param onDrawerItemClicked A callback invoked when a drawer item is clicked. It receives the
 * [DrawerIdentifier] associated with the clicked item.
 * @param isLoading A flag to indicate a loading state, which is passed to the underlying [PageScaffold].
 * @param content The main content of the screen to be displayed behind the drawer.
 */
@Composable
fun AppsTitleDrawerScaffold(
    appBarTitle: AppBarTitle? = null,
    colors: ScaffoldColors = ScaffoldColors.defaults(),
    gestureEnabled: Boolean = true,
    drawerShape: Shape = RectangleShape,
    scrimColor: Color = DrawerDefaults.scrimColor,
    actions: List<AppsTopBarButton>? = null,
    drawerOptions: List<DrawerItem>? = null,
    drawerHeader: (@Composable () -> Unit)? = null,
    drawerFooter: (@Composable () -> Unit)? = null,
    onDrawerItemClicked: (DrawerIdentifier) -> Unit,
    isLoading: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {

    AppsDrawerScaffold(
        colors = colors,
        gestureEnabled = gestureEnabled,
        drawerShape = drawerShape,
        scrimColor = scrimColor,
        topBar = { _: DrawerState, _: CoroutineScope, onNavClick: () -> Unit ->
            AppsTopBar(
                navMode = NavigationMode.DRAWER,
                navIconClick = onNavClick,
                actions = actions,
                appBarTitle = appBarTitle,
                background = colors.topBarBackground,
                onTopBarColor = colors.onTopBarColor
            )
        },
        drawerContent = { drawerState: DrawerState, coroutineScope: CoroutineScope ->
            drawerHeader?.invoke()
            VerticalSpacer()
            drawerState.run {
                drawerOptions?.forEach {
                    DrawerLayoutMenuItem(
                        scope = coroutineScope,
                        item = it,
                        textColor = colors.drawerItemColor,
                        onItemClick = onDrawerItemClicked
                    )
                }
            }
            VerticalSpacer()
            drawerFooter?.invoke()
        },
        isLoading = isLoading,
        content = content
    )
}

/**
 * A flexible, lower-level scaffold that provides the core behavior for a [ModalNavigationDrawer].
 * This composable gives you full control over the `topBar` and `drawerContent` by accepting
 * composable lambdas for them.
 *
 * Use this when you need a custom top bar or a drawer layout that doesn't fit the standard
 * list-based structure provided by [AppsTitleDrawerScaffold].
 *
 * @param colors The color scheme for the scaffold components.
 * @param gestureEnabled Whether the drawer can be opened and closed with a swipe gesture.
 * @param isLoading A flag to indicate a loading state.
 * @param drawerShape The shape of the drawer's container.
 * @param scrimColor The color of the scrim that overlays the main content when the drawer is open.
 * @param topBar A composable lambda to define a custom top bar. It receives the [DrawerState],
 * a [CoroutineScope], and a callback to handle the navigation icon click to open/close the drawer.
 * @param drawerContent A composable lambda that defines the entire content of the drawer. It receives
 * the [DrawerState] and a [CoroutineScope] to allow for custom interactions with the drawer.
 * @param content The main content of the screen.
 */
@Composable
fun AppsDrawerScaffold(
    colors: ScaffoldColors = ScaffoldColors.defaults(),
    gestureEnabled: Boolean = true,
    isLoading: Boolean = false,
    drawerShape: Shape = RectangleShape,
    scrimColor: Color = DrawerDefaults.scrimColor,
    topBar: (@Composable (state: DrawerState, scope: CoroutineScope, drawerIconAction: () -> Unit) -> Unit)? = null,
    drawerContent: (@Composable ColumnScope.(state: DrawerState, scope: CoroutineScope) -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = drawerState,
        scrimColor = scrimColor,
        gesturesEnabled = gestureEnabled,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = drawerShape,
                modifier = Modifier.wrapContentWidth(),
                drawerContainerColor = colors.drawerBackgroundColor
            ) {
                drawerContent?.invoke(this, drawerState, scope)
            }
        },
    ) {
        PageScaffold(
            backgroundColor = colors.backgroundColor,
            topBar = {
                topBar?.invoke(drawerState, scope) {
                    scope.launch {
                        drawerState.apply {
                            if (isClosed) open() else close()
                        }
                    }
                }
            },
            isLoading = isLoading
        ) {
            content.invoke(it)
        }
    }
}

/**
 * An internal helper composable that renders a single item within the navigation drawer.
 * It handles the item's click event, displays an icon and title, and shows optional dividers.
 *
 * @param scope The [CoroutineScope] used to control the drawer (e.g., to close it on item click).
 * @param textColor The color to be used for the menu item's text.
 * @param item The [DrawerItem] data object containing the information to be displayed.
 * @param onItemClick The callback invoked when the item is clicked.
 */
@Composable
fun DrawerState.DrawerLayoutMenuItem(
    scope: CoroutineScope = rememberCoroutineScope(),
    textColor: Color,
    item: DrawerItem,
    onItemClick: (DrawerIdentifier)->Unit
) {

    if (item.showTopDivider) {
        HorizontalDivider(
            color = Kolt.colors.dividerColor,
            thickness = 0.5.dp,
            modifier = Modifier.padding(vertical = sizes.paddingSmall, horizontal = sizes.paddingMedium)
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (item.closeDrawer) {
                    scope.launch {
                        if (isOpen) close()
                    }
                }
                onItemClick(item.drawerIdentifier())
            }
            .padding(start = sizes.paddingXXLarge, top = item.verticalPadding ?: sizes.paddingSmallMedium, bottom = item.verticalPadding ?: sizes.paddingSmallMedium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            item.icon?.let {
                AppsIcon(
                    icon = it,
                    modifier = Modifier
                        .padding(end = sizes.paddingMedium)
                        .size(sizes.iconMedium)
                )
            }
            AppspirimentText(
                text = item.menuTitle,
                style = item.textStyle ?: typography.textMedium,
                color = textColor ,
                modifier = Modifier.padding()
            )
        }
        item.trailingIcon?.let {
            AppsIcon(
                icon = it,
                modifier = Modifier.padding(end = sizes.paddingMedium)
            )
        }
    }

    if (item.showBottomDivider) {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = sizes.paddingSmall),
            thickness = 0.5.dp,
            color = Kolt.colors.dividerColor
        )
    }
}
