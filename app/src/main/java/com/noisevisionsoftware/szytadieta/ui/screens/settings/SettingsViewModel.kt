package com.noisevisionsoftware.szytadieta.ui.screens.settings

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.FirebaseErrorMapper
import com.noisevisionsoftware.szytadieta.domain.exceptions.ValidationManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SessionManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.PasswordField
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.PasswordUpdateState
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.SettingsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsManager: SettingsManager,
    private val sessionManager: SessionManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _settingsState = MutableStateFlow<SettingsState>(SettingsState.Initial)
    val settingsState = _settingsState.asStateFlow()

    private val _passwordUpdateState =
        MutableStateFlow<PasswordUpdateState>(PasswordUpdateState.Initial)
    val passwordUpdateState = _passwordUpdateState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsManager.isDarkMode.collect { isDarkMode ->
                _settingsState.value = SettingsState.Success(isDarkMode = isDarkMode)
            }
        }
    }

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsManager.setDarkMode(enabled)
                showSuccess("Motyw został zmieniony")
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _settingsState.value = SettingsState.Loading

                safeApiCall { authRepository.deleteAccount() }
                    .onSuccess {
                        sessionManager.clearSession()
                        settingsManager.clearSettings()
                        showSuccess("Konto zostało usunięte")
                        _settingsState.value = SettingsState.Success(isAccountDeleted = true)
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            try {
                _passwordUpdateState.value = PasswordUpdateState.Loading

                ValidationManager.validatePassword(newPassword).getOrThrow()
                ValidationManager.validatePassword(oldPassword).getOrThrow()

                safeApiCall { authRepository.updatePassword(oldPassword, newPassword) }
                    .onSuccess {
                        showSuccess("Hasło zostało zmienione")
                        _passwordUpdateState.value = PasswordUpdateState.Success
                    }
                    .onFailure { throwable ->
                        val error = FirebaseErrorMapper.mapFirebaseAuthError(throwable as Exception)
                        val field = when {
                            error.message.contains("obecne hasło") ||
                                    error.message.contains("Nieprawidłowe hasło") -> PasswordField.OLD_PASSWORD

                            else -> PasswordField.NEW_PASSWORD
                        }
                        _passwordUpdateState.value = PasswordUpdateState.Error(error.message, field)
                    }
            } catch (e: Exception) {
                val error = when (e) {
                    is AppException -> e
                    else -> FirebaseErrorMapper.mapFirebaseAuthError(e)
                }
                _passwordUpdateState.value = PasswordUpdateState.Error(error.message)
            }
        }
    }

    fun resetPasswordUpdateState() {
        _passwordUpdateState.value = PasswordUpdateState.Initial
    }

    private fun handleError(throwable: Throwable) {
        val appException = when (throwable) {
            is AppException -> throwable
            is Exception -> FirebaseErrorMapper.mapFirebaseAuthError(throwable)
            else -> AppException.UnknownException()
        }
        _settingsState.value = SettingsState.Error(appException.message)
        showError(appException.message)
    }
}