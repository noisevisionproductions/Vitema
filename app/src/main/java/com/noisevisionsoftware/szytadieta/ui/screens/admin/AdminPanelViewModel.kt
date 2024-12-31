package com.noisevisionsoftware.szytadieta.ui.screens.admin

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.model.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AdminRepository
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _adminState = MutableStateFlow<AdminState>(AdminState.Initial)
    val adminState = _adminState.asStateFlow()

    sealed class AdminState {
        data object Initial : AdminState()
        data object Loading : AdminState()
        data class Success(val users: List<User>) : AdminState()
        data class Error(val message: String) : AdminState()
    }

    init {
        loadUsers()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            try {
                _adminState.value = AdminState.Loading

                val currentUser = authRepository.getCurrentUserData().getOrNull()
                if (currentUser?.role != UserRole.ADMIN) {
                    throw AppException.AuthException("Brak uprawnień administratora")
                }

                adminRepository.getAllUsers()
                    .onSuccess { users ->
                        _adminState.value = AdminState.Success(users)
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        viewModelScope.launch {
            try {
                adminRepository.updateUserRole(userId, newRole)
                    .onSuccess {
                        showSuccess("Rola użytkownika została zaktualizowana")
                        loadUsers()
                    }
                    .onFailure { throwable ->
                        handleError(throwable)
                    }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is AppException -> throwable.message
            else -> "Wystapił nieoczekiwany błąd"
        }
        _adminState.value = AdminState.Error(message)
        showError(message)
    }
}