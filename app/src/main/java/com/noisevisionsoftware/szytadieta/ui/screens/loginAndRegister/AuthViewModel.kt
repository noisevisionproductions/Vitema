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
import com.noisevisionsoftware.szytadieta.domain.model.user.auth.EmailVerificationState
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

    private val _emailVerificationState = MutableStateFlow<EmailVerificationState>(
        EmailVerificationState.Initial
    )
    val emailVerificationState = _emailVerificationState.asStateFlow()

    private val _showVerificationDialog = MutableStateFlow(false)
    val showVerificationDialog = _showVerificationDialog.asStateFlow()

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
                        safeApiCall { authRepository.isEmailVerified() }
                            .onSuccess { isVerified ->
                                if (isVerified) {
                                    sessionManager.saveUserSession(it)
                                    checkProfileCompletion(it)
                                    _authState.value = AuthState.Success(it)
                                } else {
                                    logout(showMessage = false)
                                    _authState.value =
                                        AuthState.Error("Email nie został zweryfikowany. Sprawdź swoją skrzynkę pocztową i kliknij w link aktywacyjny.")
                                }
                            }
                            .onFailure { throwable ->
                                handleAuthError(throwable)
                            }
                    } ?: run {
                        _authState.value = AuthState.Error("Użytkownik nie jest zalogowany")
                    }
                }
                .onFailure { throwable ->
                    handleAuthError(throwable)
                }
        }
    }

    fun setShowVerificationDialog(show: Boolean) {
        _showVerificationDialog.value = show
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _emailVerificationState.value = EmailVerificationState.Loading
            safeApiCall { authRepository.isEmailVerified() }
                .onSuccess { isVerified ->
                    _emailVerificationState.value = if (isVerified) {
                        EmailVerificationState.Verified
                    } else {
                        EmailVerificationState.NotVerified
                    }
                }
                .onFailure { throwable ->
                    _emailVerificationState.value =
                        EmailVerificationState.Error(throwable.message ?: "Błąd weryfikacji")
                }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _emailVerificationState.value = EmailVerificationState.Loading
            safeApiCall { authRepository.resendVerificationEmail() }
                .onSuccess {
                    _emailVerificationState.value = EmailVerificationState.EmailSent
                    showSuccess("Link weryfikacyjny został wysłany ponownie na Twój adres email")
                }
                .onFailure { throwable ->
                    _emailVerificationState.value =
                        EmailVerificationState.Error(throwable.message ?: "Błąd wysyłania")
                    showError("Nie udało się wysłać emaila weryfikacyjnego: ${throwable.message}")
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
                    if (throwable is AppException.AuthException &&
                        throwable.message.contains("nie został zweryfikowany")
                    ) {
                        _showVerificationDialog.value = true
                    }
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
                    _emailVerificationState.value = EmailVerificationState.EmailSent
                    handleSuccessfulAuth(user)
                    showSuccess("Konto zostało utworzone. Sprawdź swoją skrzynkę email aby zweryfikować konto.")
                    logout(showMessage = false)
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

    fun logout(showMessage: Boolean = true) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                sessionManager.clearSession()
                authRepository.logout().getOrThrow()
                notificationManager.cancelAllNotifications()
                eventBus.emit(AppEvent.UserLoggedOut)
                _authState.value = AuthState.Logout
                if (showMessage) {
                    showSuccess("Wylogowano pomyślnie")
                }
            } catch (_: Exception) {
                showError("Błąd podczas wylogowywania")
            }
        }
    }

    suspend fun checkAdminAccess(): Boolean {
        return try {
            val user = getCurrentUser()
            user.role == UserRole.ADMIN
        } catch (_: Exception) {
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
                if (throwable.message.contains("nie został zweryfikowany") ||
                    throwable.message.contains("email") ||
                    throwable.message.contains("weryfikacji")
                ) {
                    _showVerificationDialog.value = true
                }
                showError(throwable.message)
                _authState.value = AuthState.Initial
                return
            }

            is AppException -> throwable
            is Exception -> {
                val mappedError = FirebaseErrorMapper.mapFirebaseAuthError(throwable)
                if (mappedError.message.contains("nie został zweryfikowany") ||
                    mappedError.message.contains("email") ||
                    mappedError.message.contains("weryfikacji")
                ) {
                    _showVerificationDialog.value = true
                }
                mappedError
            }

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