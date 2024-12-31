package com.noisevisionsoftware.szytadieta.ui.screens.settings.data

sealed class SettingsState {
    data object Initial : SettingsState()
    data object Loading : SettingsState()
    data class Success(
        val isDarkMode: Boolean = false,
        val isAccountDeleted: Boolean = false
    ) : SettingsState()
    data class Error(val message: String) : SettingsState()
}