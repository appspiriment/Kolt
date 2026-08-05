package io.github.appspiriment.kolt.composekmp.components.containers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person3
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.appspiriment.kolt.composekmp.components.containers.types.AppsTopBarButton
import io.github.appspiriment.kolt.composekmp.components.containers.types.ScaffoldColors
import io.github.appspiriment.kolt.composekmp.components.core.progress.FullscreenLoader
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.theme.Kolt.sizes
import io.github.appspiriment.kolt.composekmp.wrappers.UiImage
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.composekmp.wrappers.toUiImage
import io.github.appspiriment.kolt.composekmp.wrappers.toUiText

@Composable
fun AppsPageScaffold(
    modifier: Modifier = Modifier,
    navMode: NavigationMode = NavigationMode.EMPTY,
    navIconClick: (() -> Unit)? = null,
    appBarTitle: AppBarTitle? = null,
    actions: List<AppsTopBarButton>? = null,
    actionsContent: @Composable RowScope.(Color) -> Unit = {},
    bottomBar: (@Composable () -> Unit) = {},
    floatingActionButton: @Composable () -> Unit = {},
    isLoading: Boolean = false,
    colors: ScaffoldColors = ScaffoldColors.defaults(),
    contentPadding: PaddingValues = PaddingValues(all = sizes.noPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    PageScaffold(
        topBar = {
            AppsTopBar(
                navMode = navMode,
                navIconClick = navIconClick,
                actions = actions,
                actionsContent = actionsContent,
                appBarTitle = appBarTitle,
                background = colors.topBarBackground,
                onTopBarColor = colors.onTopBarColor
            )
        },
        bottomBar = bottomBar,
        modifier = modifier,
        isLoading = isLoading,
        floatingActionButton = floatingActionButton,
        backgroundColor = colors.backgroundColor
    ) { scaffoldPadding -> // The padding from the underlying Scaffold
        Column(
            modifier = Modifier
                .padding(scaffoldPadding) // Apply scaffold padding first to avoid content going under bars
                .fillMaxSize()
                .padding(contentPadding)  // Then apply any additional custom padding
        ) {
            content() // Your content is placed here, with ColumnScope
        }
    }
}


@Composable
fun PageScaffold(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    topBar: (@Composable () -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    bottomBar: (@Composable () -> Unit) = {},
    backgroundColor: Color = Kolt.colors.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            topBar?.invoke()
        },
        bottomBar = bottomBar,
        modifier = modifier.fillMaxSize(),
        floatingActionButton = floatingActionButton,
        containerColor = backgroundColor
    ) { innerPadding ->
        content.invoke(innerPadding)
        if (isLoading) {
            FullscreenLoader()
        }
    }
}

sealed class NavigationMode(val icon: UiImage, val contentDescription: String? = null) {
    data object EMPTY : NavigationMode(Icons.Default.Home.toUiImage(), null)
    data object BACK : NavigationMode(Icons.AutoMirrored.Filled.ArrowBack.toUiImage(), "Back")
    data object CLOSE : NavigationMode(Icons.Default.Close.toUiImage(), "Close")
    data object DRAWER : NavigationMode(Icons.Default.Menu.toUiImage(), "Menu")
    data object HOME : NavigationMode(Icons.Default.Home.toUiImage(), "Home")
}

sealed class AppBarTitle(open val modifier: Modifier) {
    data object None : AppBarTitle(Modifier)
    data class BrandLogo(val image: UiImage, override val modifier: Modifier = Modifier) :
        AppBarTitle(modifier)

    data class ScreenTitle(val title: UiText, override val modifier: Modifier = Modifier) :
        AppBarTitle(modifier)

    data class ScreenTitleWithIcon(
        val icon: UiImage,
        val iconHeight: Dp = 40.dp,
        val title: UiText,
        val subTitle: UiText? = null,
        val titleStyle: TextStyle? = null,
        val subTitleStyle: TextStyle? = null,
        val iconPadding: Dp = 12.dp,
        override val modifier: Modifier = Modifier
    ) : AppBarTitle(modifier)
}

@Composable
fun PreviewPageScaffold() {
    AppsPageScaffold(
        isLoading = false,
        appBarTitle = AppBarTitle.ScreenTitleWithIcon(
            icon = Icons.Filled.Person3.toUiImage(),
            title = "Test Application".toUiText(),
        ),
        actions = listOf(
            AppsTopBarButton(
                icon = Icons.Filled.Menu.toUiImage(),
                onClick = {}
            ),
        )
    ) {
        AppspirimentText("Appspirimnet Labs".toUiText())
    }
}
