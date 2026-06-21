package io.github.appspiriment.kolt.composeutils.components.animation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

inline fun <reified T : Any> NavGraphBuilder.animatedComposable(
    enterAnimation: EnterTransition =  SlideInRightToLeft.enter,
    exitAnimation: ExitTransition = SlideInRightToLeft.exit,
    noinline content: @Composable (NavBackStackEntry) -> Unit,
) {
    composable<T>(


    ) { backStackEntry ->
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = backStackEntry) {
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = enterAnimation,
            exit = exitAnimation
        ) {
            content(backStackEntry)
        }
    }
}
