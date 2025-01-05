package com.noisevisionsoftware.szytadieta.ui.screens

import androidx.lifecycle.ViewModel
import com.noisevisionsoftware.szytadieta.ui.navigation.DashboardScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class MainViewModel @Inject constructor() : ViewModel() {
    private val _currentScreen = MutableStateFlow<DashboardScreen>(DashboardScreen.Login)
    val currentScreen = _currentScreen.asStateFlow()

    fun updateScreen(screen: DashboardScreen) {
        _currentScreen.value = screen
    }
}