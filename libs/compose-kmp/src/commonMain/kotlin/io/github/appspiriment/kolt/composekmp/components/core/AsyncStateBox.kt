package io.github.appspiriment.kolt.composekmp.components.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.appspiriment.kolt.composekmp.components.core.progress.AppsCircularProgress
import io.github.appspiriment.kolt.composekmp.components.core.text.AppspirimentText
import io.github.appspiriment.kolt.composekmp.theme.Kolt
import io.github.appspiriment.kolt.composekmp.wrappers.UiText
import io.github.appspiriment.kolt.utils.state.AsyncState

/**
 * A Compose wrapper that drives UI rendering from an [AsyncState] value.
 *
 * Each state has a dedicated slot composable so callers only write the content
 * relevant to each state, and the container handles switching between them.
 *
 * ```kotlin
 * AsyncStateBox(
 *     state = uiState.articles,
 *     onLoading = { ShimmerListItem() },
 *     onError   = { _, message -> ErrorScreen(message) },
 *     onSuccess = { articles  -> ArticleList(articles) },
 * )
 * ```
 *
 * ### Slot defaults
 * - [onIdle]    — renders nothing (empty [Box]).
 * - [onLoading] — centered [AppsCircularProgress].
 * - [onError]   — centered error message text using [Kolt.colors.error].
 * - [onSuccess] — **required**, no default.
 *
 * ### Module boundary
 * `AsyncState` lives in `:utils` (pure Kotlin, no Compose). `compose-utils` depends on
 * `:utils` via `implementation` — not `api` — so the dependency is internal and is not
 * leaked transitively to consumers of `compose-utils`.
 *
 * @param state       The [AsyncState] driving the UI.
 * @param modifier    Applied to the root [Box].
 * @param onIdle      Content shown in [AsyncState.Idle]. Default: empty.
 * @param onLoading   Content shown in [AsyncState.Loading]. Default: centered spinner.
 * @param onError     Content shown in [AsyncState.Error]. Receives the optional [Throwable]
 *                    and display message. Default: centered error text.
 * @param onSuccess   Content shown in [AsyncState.Success]. Receives the typed [T] data.
 */
@Composable
fun <T> AsyncStateBox(
    state: AsyncState<T>,
    modifier: Modifier = Modifier,
    onIdle: @Composable () -> Unit = {},
    onLoading: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Kolt.sizes.paddingXXLarge),
            contentAlignment = Alignment.Center,
        ) {
            AppsCircularProgress()
        }
    },
    onError: @Composable (throwable: Throwable?, message: String?) -> Unit = { _, message ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Kolt.sizes.paddingMedium),
            contentAlignment = Alignment.Center,
        ) {
            AppspirimentText(
                text = message?.let { UiText.DynamicString(it) }
                    ?: UiText.DynamicString("Something went wrong"),
                style = Kolt.typography.bodyMedium,
                color = Kolt.colors.error,
            )
        }
    },
    onSuccess: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        when (state) {
            is AsyncState.Idle    -> onIdle()
            is AsyncState.Loading -> onLoading()
            is AsyncState.Error   -> onError(state.throwable, state.message)
            is AsyncState.Success -> onSuccess(state.data)
        }
    }
}

