package io.github.appspiriment.kolt.locationpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * MVI ViewModel base, shape per presentation-mvi.md — `androidx.lifecycle:lifecycle-viewmodel`
 * has shipped a real `commonMain` KMP artifact since 2.8, so this needs no `expect`/`actual`.
 */
internal abstract class MviViewModel<S : Any, I : Any, E : Any>(initialState: S) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    protected fun setState(reducer: S.() -> S) {
        _state.value = reducer(_state.value)
    }

    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    abstract fun onIntent(intent: I)
}
