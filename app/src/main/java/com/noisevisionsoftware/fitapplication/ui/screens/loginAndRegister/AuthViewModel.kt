package com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.fitapplication.domain.auth.AuthRepository
import com.noisevisionsoftware.fitapplication.domain.auth.SessionManager
import com.noisevisionsoftware.fitapplication.domain.exceptions.AppException
import com.noisevisionsoftware.fitapplication.domain.exceptions.ErrorMapper
import com.noisevisionsoftware.fitapplication.domain.model.User
import com.noisevisionsoftware.fitapplication.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.fitapplication.domain.auth.ValidationManager
import com.noisevisionsoftware.fitapplication.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager,
    networkManager: NetworkConnectivityManager
) : BaseViewModel(networkManager) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    val userSession = sessionManager.userSessionFlow

    sealed class AuthState {
        data object Initial : AuthState()
        data object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
        data object LoggedOut : AuthState()
    }

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.getCurrentUserData()
                .onSuccess { user ->
                    user?.let {
                        sessionManager.saveUserSession(it)
                        _authState.value = AuthState.Success(it)
                    }
                }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                ValidationManager.validateEmail(email).getOrThrow()
                ValidationManager.validatePassword(password).getOrThrow()

                _authState.value = AuthState.Loading
                safeApiCall { authRepository.login(email, password) }
                    .onSuccess { user ->
                        sessionManager.saveUserSession(user)
                        _authState.value = AuthState.Success(user)
                        showSuccess("Zalogowano pomyślnie")
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        showError(appException.message)
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                showError(e.message)
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
                safeApiCall { authRepository.register(nickname, email, password) }
                    .onSuccess { user ->
                        sessionManager.saveUserSession(user)
                        _authState.value = AuthState.Success(user)
                        showSuccess("Konto zostało utworzone")
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        showError(appException.message)
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                showError(e.message)
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                safeApiCall { authRepository.getCurrentUserData() }
                    .onSuccess { user ->
                        user?.let {
                            _authState.value = AuthState.Success(it)
                        } ?: run {
                            val exception =
                                AppException.AuthException("Nie znaleziono danych użytkownika")
                            _authState.value = AuthState.Error(exception.message)
                            showError(exception.message)
                        }
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        showError(appException.message)
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                showError(e.message)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            try {
                if (email.isBlank()) {
                    throw AppException.ValidationException("Wprowadź adres email")
                }

                _authState.value = AuthState.Loading
                safeApiCall { authRepository.resetPassword(email) }
                    .onSuccess {
                        showSuccess("Link do resetowania hasła został wysłany na podany adres email")
                        _authState.value = AuthState.Initial
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        showError(appException.message)
                        _authState.value = AuthState.Error(appException.message)
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                showError(e.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            safeApiCall {
                sessionManager.clearSession()
                authRepository.logout()
            }
                .onSuccess {
                    _authState.value = AuthState.LoggedOut
                    showSuccess("Wylogowano pomyślnie")
                }
                .onFailure {
                    showError("Błąd podczas wylogowywania")
                }
        }
    }
}