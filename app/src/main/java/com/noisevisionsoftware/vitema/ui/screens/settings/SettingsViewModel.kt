package com.noisevisionsoftware.vitema.ui.screens.settings

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.exceptions.AppException
import com.noisevisionsoftware.vitema.domain.exceptions.FirebaseErrorMapper
import com.noisevisionsoftware.vitema.domain.exceptions.PasswordField
import com.noisevisionsoftware.vitema.domain.exceptions.ValidationManager
import com.noisevisionsoftware.vitema.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.vitema.data.localPreferences.SessionManager
import com.noisevisionsoftware.vitema.data.localPreferences.SettingsManager
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AuthRepository
import com.noisevisionsoftware.vitema.domain.repository.UserRepository
import com.noisevisionsoftware.vitema.domain.service.notifications.NotificationManager
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.utils.AppVersionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val settingsManager: SettingsManager,
    private val sessionManager: SessionManager,
    private val appVersionUtils: AppVersionUtils,
    private val preferencesManager: PreferencesManager,
    private val notificationManager: NotificationManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _settingsState =
        MutableStateFlow<ViewModelState<SettingsData>>(ViewModelState.Initial)
    val settingsState = _settingsState.asStateFlow()

    private val _passwordUpdateState =
        MutableStateFlow<ViewModelState<PasswordUpdateData>>(ViewModelState.Initial)
    val passwordUpdateState = _passwordUpdateState.asStateFlow()

    val isVersionCheckEnabled = preferencesManager.isVersionCheckEnabled

    val waterNotificationsEnabled = notificationManager.waterNotificationsEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    data class SettingsData(
        val isDarkMode: Boolean = false,
        val isAccountDeleted: Boolean = false,
        val appVersion: String = "",
        val areNotificationsEnabled: Boolean = false
    )

    data class PasswordUpdateData(
        val field: PasswordField? = null
    )

    init {
        observeAppSettings()
    }

    private fun observeAppSettings() {
        viewModelScope.launch {
            settingsManager.isDarkMode.collect { isDarkMode ->
                _settingsState.value = ViewModelState.Success(
                    SettingsData(
                        isDarkMode = isDarkMode,
                        appVersion = appVersionUtils.getAppVersion(),
                        areNotificationsEnabled = notificationManager.areNotificationsEnabled()
                    )
                )
            }
        }
    }

    fun updateSettings(enabled: Boolean) {
        handleOperation(_settingsState) {
            settingsManager.setDarkMode(enabled)
            showSuccess("Motyw został zmieniony")
            SettingsData(
                isDarkMode = enabled,
                appVersion = appVersionUtils.getAppVersion()
            )
        }
    }

    fun deleteAccount(password: String) {
        handleOperation(_settingsState) {
            authRepository.withAuthenticatedUser { userId ->

                ValidationManager.validatePassword(password).getOrThrow()

                safeApiCall { userRepository.deleteAccount(password) }
                    .fold(
                        onSuccess = {
                            sessionManager.clearSession()
                            settingsManager.clearSettings()
                            preferencesManager.clearAllUserData(userId = userId)
                            showSuccess("Konto zostało usunięte")

                            SettingsData(
                                isAccountDeleted = true,
                                appVersion = appVersionUtils.getAppVersion()
                            )
                        },
                        onFailure = { throwable ->
                            val error = FirebaseErrorMapper.mapFirebaseAuthError(throwable as Exception)
                            throw AppException.AuthException(error.message)
                        }
                    )
            }
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String) {
        handleOperation(_passwordUpdateState) {
            ValidationManager.validatePassword(oldPassword).getOrThrow()
            ValidationManager.validatePassword(newPassword).getOrThrow()

            safeApiCall { userRepository.updatePassword(oldPassword, newPassword) }
                .fold(
                    onSuccess = {
                        showSuccess("Hasło zostało zmienione")
                        PasswordUpdateData()
                    },
                    onFailure = { throwable ->
                        val error = FirebaseErrorMapper.mapFirebaseAuthError(throwable as Exception)
                        val field = when {
                            error.message.contains("obecne hasło") ||
                                    error.message.contains("Nieprawidłowe hasło") ->
                                PasswordField.OLD_PASSWORD

                            else -> PasswordField.NEW_PASSWORD
                        }
                        throw AppException.ValidationException(error.message, field)
                    }
                )
        }
    }

    fun setWaterNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            notificationManager.setWaterNotificationsEnabled(enabled)
        }
    }

    fun setVersionCheckEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setVersionCheckEnabled(enabled)
        }
    }

    fun resetPasswordUpdateState() {
        _passwordUpdateState.value = ViewModelState.Initial
    }

    override fun onUserLoggedOut() {
        _settingsState.value = ViewModelState.Initial
        _passwordUpdateState.value = ViewModelState.Initial
    }
}