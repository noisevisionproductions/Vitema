package com.noisevisionsoftware.szytadieta.ui.screens.loginAndRegister

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.FirebaseErrorMapper
import com.noisevisionsoftware.szytadieta.domain.exceptions.ValidationManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.SessionManager
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationManager
import com.noisevisionsoftware.szytadieta.domain.state.AuthState
import com.noisevisionsoftware.szytadieta.ui.base.AppEvent
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    private val notificationManager: NotificationManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _authState = MutableStateFlow<AuthState<User>>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _profileCompleted = MutableStateFlow<Boolean?>(null)
    val profileCompleted = _profileCompleted.asStateFlow()

    val userSession = sessionManager.userSessionFlow

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { user ->
                    user?.let {
                        sessionManager.saveUserSession(it)
                        checkProfileCompletion(it)
                        _authState.value = AuthState.Success(it)
                    } ?: run {
                        _authState.value = AuthState.Error("Użytkownik nie jest zalogowany")
                    }
                }
                .onFailure { throwable ->
                    handleAuthError(throwable)
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                ValidationManager.validateEmail(email).getOrThrow()
                ValidationManager.validatePassword(password).getOrThrow()
                _authState.value = AuthState.Loading

                val result = authRepository.login(email, password)
                result.onSuccess { user ->
                    handleSuccessfulAuth(user)
                }.onFailure { throwable ->
                    Log.e("LoginError", "Login failed", throwable)
                    handleAuthError(throwable)
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun register(nickname: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                ValidationManager.validateNickname(nickname).getOrThrow()
                ValidationManager.validateEmail(email).getOrThrow()
                ValidationManager.validatePassword(password).getOrThrow()
                ValidationManager.validatePasswordConfirmation(password, confirmPassword)
                    .getOrThrow()

                _authState.value = AuthState.Loading

                val result = authRepository.register(nickname, email, password)

                result.onSuccess { user ->
                    handleSuccessfulAuth(user)
                    showSuccess("Konto zostało utworzone")
                }.onFailure { throwable ->
                    Log.e("RegistrationError", "Registration failed", throwable)
                    handleAuthError(throwable)
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                ValidationManager.validateEmail(email).getOrThrow()

                val result = authRepository.resetPassword(email)
                result.onSuccess {
                    showSuccess("Link do resetowania hasła został wysłany na podany adres email")
                    _authState.value = AuthState.Initial
                }.onFailure { throwable ->
                    handleAuthError(throwable)
                }
            } catch (e: Exception) {
                handleAuthError(e)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                sessionManager.clearSession()
                authRepository.logout().getOrThrow()
                notificationManager.cancelAllNotifications()
                eventBus.emit(AppEvent.UserLoggedOut)
                _authState.value = AuthState.Logout
            } catch (e: Exception) {
                showError("Błąd podczas wylogowywania")
            }
        }
    }

    suspend fun checkAdminAccess(): Boolean {
        return try {
            val user = getCurrentUser()
            user.role == UserRole.ADMIN
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentUser(): User {
        return authRepository.getCurrentUserData()
            .getOrThrow()
            ?: throw AppException.AuthException("Nie znaleziono danych użytkownika")
    }

    private fun handleAuthError(throwable: Throwable) {
        val appException = when (throwable) {
            is AppException.ValidationException -> {
                showError(throwable.message)
                return
            }

            is AppException.AuthException -> {
                showError(throwable.message)
                _authState.value = AuthState.Initial
                return
            }

            is AppException -> throwable
            is Exception -> FirebaseErrorMapper.mapFirebaseAuthError(throwable)
            else -> AppException.UnknownException()
        }
        if (appException is AppException.NetworkException || appException is AppException.UnknownException) {
            _authState.value = AuthState.Error(appException.message)
        } else {
            _authState.value = AuthState.Initial
        }
        showError(appException.message)
    }

    private fun checkProfileCompletion(user: User) {
        viewModelScope.launch {
            _profileCompleted.value = user.let {
                it.birthDate != null &&
                        it.gender != null &&
                        it.storedAge > 0
            }
        }
    }

    private suspend fun handleSuccessfulAuth(user: User) {
        sessionManager.saveUserSession(user)
        checkProfileCompletion(user)
        _authState.value = AuthState.Success(user)
    }

    override fun onRefreshData() {
        viewModelScope.launch {
            authRepository.getCurrentUserData()
                .onSuccess { user ->
                    user?.let {
                        checkProfileCompletion(it)
                    }
                }
        }
    }
}