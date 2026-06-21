package io.github.appspiriment.kolt.composeutils.components.utils.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class UiEventsViewModel <EventType:Any, UiEventType:Any>: ViewModel() {


    private val _uiEventChannel = Channel<UiEventType>(Channel.BUFFERED)

    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    protected fun sendUiEvent(uiEvent: UiEventType){
        viewModelScope.launch {
            _uiEventChannel.send(uiEvent)
        }
    }

    internal abstract fun onEvent(event: EventType)
}