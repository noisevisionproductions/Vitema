package com.noisevisionsoftware.vitema.ui.screens.profile.profileEdit

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.exceptions.AppException
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.UserRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.AppEvent
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileEditViewModel @Inject constructor(
    private val userRepository: UserRepository,
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
            userRepository.getCurrentUserData()
                .getOrThrow()
                ?: throw AppException.AuthException("Nie można załadować profilu")
        }
    }

    fun updateProfile(updatedUser: User) {
        handleOperation(_profileState) {
            userRepository.updateUserData(updatedUser)
                .getOrThrow()
            showSuccess("Profil został zaktualizowany")
            viewModelScope.launch {
                eventBus.emit(AppEvent.RefreshData)
            }
            updatedUser
        }
    }
}