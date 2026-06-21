package io.github.appspiriment.kolt.composeutils.components.utils.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * [AndroidViewModel]-based equivalent of [UiEventsViewModel].
 * Use this when you need access to [Application] context (e.g., for system services,
 * ContentResolver, or resources outside a Compose context) but do not need persistent
 * UI state — only events and one-shot effects.
 *
 * @param EventType  Sealed class representing user-driven actions (button taps, swipes, etc.)
 * @param UiEffect   Sealed class representing one-shot effects consumed by the UI
 *                   (navigation, snackbar, toast, etc.).
 */
abstract class UiEventsAndroidViewModel<EventType : Any, UiEffect : Any>(
    application: Application,
) : AndroidViewModel(application) {

    private val _uiEffectChannel = Channel<UiEffect>(Channel.BUFFERED)

    /** Collect this in the UI to react to one-shot effects. */
    val uiEffectFlow = _uiEffectChannel.receiveAsFlow()

    /** Emit a one-shot effect to the UI. Call from within the ViewModel only. */
    protected fun sendUiEffect(effect: UiEffect) {
        viewModelScope.launch { _uiEffectChannel.send(effect) }
    }

    /**
     * Handle a user-driven event. Implement all UI interactions here;
     * never call ViewModel methods directly from the UI.
     */
    abstract fun onEvent(event: EventType)

    /**
     * Launches on [viewModelScope]'s default dispatcher.
     * Use for lightweight work or when you need to update state on the Main thread.
     */
    protected fun launchSafe(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(null, onError, block)

    /** Launches on [Dispatchers.IO] — for network, file I/O, and database work. */
    protected fun launchIO(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(Dispatchers.IO, onError, block)

    /** Launches on [Dispatchers.Default] — for CPU-intensive computation. */
    protected fun launchDefault(
        onError: (Throwable) -> Unit = {},
        block: suspend CoroutineScope.() -> Unit,
    ) = launchOn(Dispatchers.Default, onError, block)

    private fun launchOn(
        dispatcher: CoroutineDispatcher?,
        onError: (Throwable) -> Unit,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        val context: CoroutineContext = dispatcher ?: EmptyCoroutineContext
        viewModelScope.launch(context) {
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
