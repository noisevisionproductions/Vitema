package com.noisevisionsoftware.vitema.ui.screens.profile

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.exceptions.AppException
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.InvitationRepository
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
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val invitationRepository: InvitationRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _profileState = MutableStateFlow<ViewModelState<User>>(ViewModelState.Initial)
    val profileState = _profileState.asStateFlow()

    init {
        loadUserProfile()
        observeEvents()
    }

    private fun loadUserProfile() {
        handleOperation(_profileState) {
            userRepository.getCurrentUserData()
                .getOrThrow()
                ?: throw AppException.AuthException("Nie można załadować profilu")
        }
    }

    fun disconnectTrainer() {
        viewModelScope.launch {
            handleOperation(_profileState) {
                invitationRepository.disconnectFromTrainer().getOrThrow()

                userRepository.getCurrentUserData().getOrThrow()
                    ?: throw AppException.AuthException("Błąd odświeżania profilu")
            }

            showSuccess("Współpraca z trenerem została zakończona.")
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.RefreshData -> loadUserProfile()
                    else -> Unit
                }
            }
        }
    }

    override fun onUserLoggedOut() {
        _profileState.value = ViewModelState.Initial
    }

    override fun onRefreshData() {
        loadUserProfile()
    }
}