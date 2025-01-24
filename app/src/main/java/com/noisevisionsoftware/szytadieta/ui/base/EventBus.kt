package com.noisevisionsoftware.szytadieta.ui.base

import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventBus @Inject constructor(){
    private val _events = MutableSharedFlow<AppEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: AppEvent) {
        _events.emit(event)
    }
}

sealed class AppEvent {
    data class Navigation(val destination: NavigationDestination) : AppEvent()
    data object UserLoggedOut : AppEvent()
    data object RefreshData : AppEvent()
}