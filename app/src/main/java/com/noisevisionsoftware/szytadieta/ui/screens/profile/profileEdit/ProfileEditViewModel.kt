package com.noisevisionsoftware.szytadieta.ui.screens.profile.profileEdit

import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.AppEvent
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _profileState = MutableStateFlow<ViewModelState<User>>(ViewModelState.Initial)
    val profileState = _profileState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        handleOperation(_profileState) {
            authRepository.getCurrentUserData()
                .getOrThrow()
                ?: throw AppException.AuthException("Nie można załadować profilu")
        }
    }

    fun updateProfile(updatedUser: User) {
        handleOperation(_profileState) {
            authRepository.updateUserData(updatedUser)
                .getOrThrow()
            showSuccess("Profil został zaktualizowany")

            eventBus.emit(AppEvent.RefreshData)

            updatedUser
        }
    }
}