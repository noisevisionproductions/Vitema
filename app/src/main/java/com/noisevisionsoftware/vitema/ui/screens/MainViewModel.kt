package com.noisevisionsoftware.vitema.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.ui.base.AppEvent
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val eventBus: EventBus
) : ViewModel() {

    private val _currentScreen =
        MutableStateFlow<NavigationDestination>(NavigationDestination.UnauthenticatedDestination.Login)
    val currentScreen = _currentScreen.asStateFlow()

    fun refreshAllScreens() {
        viewModelScope.launch {
            eventBus.emit(AppEvent.RefreshData)
        }
    }

    fun clearAllScreens() {
        viewModelScope.launch {
            eventBus.emit(AppEvent.UserLoggedOut)
        }
    }

    fun updateScreen(screen: NavigationDestination) {
        _currentScreen.value = screen
    }
}