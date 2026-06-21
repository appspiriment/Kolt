package io.github.appspiriment.kolt.composeutils.components.utils.base

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Internal shared implementation of stateful ViewModel logic.
 * Eliminates duplication between [UiStateEventsViewModel] (extends [ViewModel])
 * and [UiStateEventsAndroidViewModel] (extends [AndroidViewModel]).
 */
internal class ViewModelDelegate<StateType : Any, UiEffect : Any>(
    initialState: StateType,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(initialState)
    val uiState = _uiState.asStateFlow()

    val currentState: StateType get() = _uiState.value

    private val initialStateSnapshot = initialState
    private val _uiEffectChannel = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffectFlow = _uiEffectChannel.receiveAsFlow()

    fun updateUiState(transform: (StateType) -> StateType) {
        _uiState.update { transform(it) }
    }

    fun resetState() {
        _uiState.value = initialStateSnapshot
    }

    fun sendUiEffect(effect: UiEffect) {
        scope.launch { _uiEffectChannel.send(effect) }
    }

    fun launchSafe(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(null, onError, block)

    /** Launches on [Dispatchers.IO] — for network calls, file I/O, database queries. */
    fun launchIO(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(Dispatchers.IO, onError, block)

    /** Launches on [Dispatchers.Default] — for CPU-intensive work (sorting, parsing, computation). */
    fun launchDefault(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(Dispatchers.Default, onError, block)

    private fun launchOn(
        dispatcher: CoroutineDispatcher?,
        onError: (Throwable) -> Unit,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val context: CoroutineContext = dispatcher ?: EmptyCoroutineContext
        scope.launch(context) {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                onError(e)
            }
        }
    }
}
