package io.github.appspiriment.kolt.composekmp.components.containers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SmartPullToRefreshBox(
    modifier: Modifier = Modifier,
    showIndicator: Boolean = true,
    isExternallyRefreshing: Boolean = false,
    onRefreshTriggered: suspend () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    // This internal refreshing state is true ONLY while the onRefreshTriggered suspend fun is active
    var isInternallyRefreshing by remember { mutableStateOf(false) }

    // The PullRefreshIndicator should show refreshing if either the external flag is true
    // OR our internal operation is running.
    val displayRefreshing = showIndicator && (isExternallyRefreshing || isInternallyRefreshing)

    val pullRefreshState = rememberPullRefreshState(
        refreshing = displayRefreshing, // Indicator reflects combined state
        onRefresh = {
            // This lambda is called by pullRefreshState when user swipes.
            // We launch a coroutine to call the suspending onRefreshTriggered.
            if (!isInternallyRefreshing && !isExternallyRefreshing) { // Prevent re-entrancy
                coroutineScope.launch {
                    isInternallyRefreshing = true
                    try {
                        onRefreshTriggered() // Call the provided suspend function
                    } finally {
                        isInternallyRefreshing = false
                    }
                }
            }
        }
    )

    // If the external refreshing state becomes true (e.g. initial load, or refresh triggered elsewhere),
    // and we are not already internally refreshing, we might want to reflect that.
    // However, the primary trigger for onRefreshTriggered is the swipe.
    // This LaunchedEffect ensures that if isExternallyRefreshing is true,
    // the indicator shows, but it doesn't re-trigger onRefreshTriggered unless swiped.

    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(
                pullRefreshState,
                enabled = !isExternallyRefreshing && !isInternallyRefreshing
            )
        // Disable pull if an external refresh is already in progress or internal one is active
    ) {
        content()

        PullRefreshIndicator(
            refreshing = displayRefreshing, // Indicator uses the combined state
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}