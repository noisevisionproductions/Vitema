package com.noisevisionsoftware.szytadieta.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState = _profileState.asStateFlow()

    sealed class ProfileState {
        data object Initial : ProfileState()
        data object Loading : ProfileState()
        data class Success(val user: User) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { user ->
                    user?.let {
                        _profileState.value = ProfileState.Success(it)
                    } ?: run {
                        _profileState.value = ProfileState.Error("Nie można załadować profilu")
                    }
                }
                .onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    fun updateUserProfile(updateUser: User) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            safeApiCall { authRepository.updateUserData(updateUser) }
                .onSuccess {
                    _profileState.value = ProfileState.Success(updateUser)
                    showSuccess("Profil został zaktualizowany")
                }
                .onFailure { throwable ->
                    handleError(throwable)
                }
        }
    }

    private fun handleError(throwable: Throwable) {
        val message = when (throwable) {
            is AppException -> throwable.message
            else -> "Wystąpił nieoczekiwany błąd"
        }
        _profileState.value = ProfileState.Error(message)
        showError(message)
    }
}