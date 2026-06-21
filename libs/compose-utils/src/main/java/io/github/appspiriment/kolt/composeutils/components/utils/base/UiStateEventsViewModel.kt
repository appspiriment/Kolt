package io.github.appspiriment.kolt.composeutils.components.utils.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class UiStateEventsViewModel <StateType: Any, EventType:Any, UiEventType:Any>(private val initialState: StateType): ViewModel(){
    private val _uiState = MutableStateFlow<StateType>(initialState)
    val uiState = _uiState.asStateFlow()

    private val _uiEventChannel = Channel<UiEventType>(Channel.BUFFERED)

    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    fun sendUiEvent(uiEvent: UiEventType){
        viewModelScope.launch {
            _uiEventChannel.send(uiEvent)
        }
    }

    fun updateUiState(transform: (StateType)->StateType){
        _uiState.update {
            transform(it)
        }
    }

    abstract fun onEvent(event: EventType)
}