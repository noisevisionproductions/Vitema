package com.noisevisionsoftware.szytadieta.ui.screens.settings.data

sealed class PasswordUpdateState {
    data object Initial : PasswordUpdateState()
    data object Loading : PasswordUpdateState()
    data object Success : PasswordUpdateState()
    data class Error(
        val message: String,
        val field: PasswordField? = null
    ) : PasswordUpdateState()
}

enum class PasswordField {
    OLD_PASSWORD,
    NEW_PASSWORD
}