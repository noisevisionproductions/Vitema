package com.noisevisionsoftware.fitapplication.ui.screens.loginAndRegister

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.fitapplication.domain.auth.AuthRepository
import com.noisevisionsoftware.fitapplication.domain.exceptions.AppException
import com.noisevisionsoftware.fitapplication.domain.exceptions.ErrorMapper
import com.noisevisionsoftware.fitapplication.domain.model.User
import com.noisevisionsoftware.fitapplication.ui.common.UiEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class AuthState {
        data object Initial : AuthState()
        data object Loading : AuthState()
        data class Success(val user: User) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                authRepository.login(email, password)
                if (email.isBlank() || password.isBlank()) {
                    throw AppException.ValidationException("Wypełnij wszystkie pola")
                }

                authRepository.login(email, password)
                    .onSuccess { user ->
                        _authState.value = AuthState.Success(user)
                        _uiEvent.emit(UiEvent.ShowSuccess("Zalogowano pomyślnie"))
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        _uiEvent.emit(UiEvent.ShowError(appException.message))
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                _uiEvent.emit(UiEvent.ShowError(e.message))
            }
        }
    }

    fun register(nickname: String, email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                if (nickname.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    throw AppException.ValidationException("Wypełnij wszystkie pola")
                }

                if (password != confirmPassword) {
                    _authState.value = AuthState.Error("Hasła nie są identyczne")
                    return@launch
                }

                _authState.value = AuthState.Loading
                authRepository.register(nickname, email, password)
                    .onSuccess { user ->
                        _authState.value = AuthState.Success(user)
                        _uiEvent.emit(UiEvent.ShowSuccess("Konto zostało utworzone"))
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        _uiEvent.emit(UiEvent.ShowError(appException.message))
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                _uiEvent.emit(UiEvent.ShowError(e.message))
            }
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            try {
                _authState.value = AuthState.Loading
                authRepository.getCurrentUserData()
                    .onSuccess { user ->
                        user?.let {
                            _authState.value = AuthState.Success(it)
                        } ?: throw AppException.AuthException("Nie znaleziono danych użytkownika")
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _authState.value = AuthState.Error(appException.message)
                        _uiEvent.emit(UiEvent.ShowError(appException.message))
                    }
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                _uiEvent.emit(UiEvent.ShowError(e.message))
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
                authRepository.resetPassword(email)
                    .onSuccess {
                        _uiEvent.emit(UiEvent.ShowSuccess("Link do resetowania hasła został wysłany na podany adres email"))
                    }
                    .onFailure { throwable ->
                        val appException = when (throwable) {
                            is Exception -> ErrorMapper.mapFirebaseAuthError(throwable)
                            else -> AppException.UnknownException()
                        }
                        _uiEvent.emit(UiEvent.ShowError(appException.message))
                    }
                _authState.value = AuthState.Initial
            } catch (e: AppException) {
                _authState.value = AuthState.Error(e.message)
                _uiEvent.emit(UiEvent.ShowError(e.message))
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
                .onSuccess {
                    _authState.value = AuthState.Initial
                    _uiEvent.emit(UiEvent.ShowSuccess("Wylogowano pomyślnie"))
                }
                .onFailure {
                    _uiEvent.emit(UiEvent.ShowError("Błąd podczas wylogowywania"))
                }
        }
    }
}