package com.noisevisionsoftware.vitema.ui.screens.profile.invitation

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.InvitationRepository
import com.noisevisionsoftware.vitema.ui.base.AppEvent
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvitationViewModel @Inject constructor(
    private val invitationRepository: InvitationRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState = _uiState.asStateFlow()

    fun onCodeChanged(newCode: String) {
        if (newCode.length <= 20) {
            _uiState.update {
                it.copy(code = newCode.uppercase().trim())
            }
        }
    }

    fun submitCode() {
        val currentCode = _uiState.value.code

        if (currentCode.length < 6) {
            showError("Kod jest za krótki")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            invitationRepository.acceptInvitation(currentCode)
                .onSuccess { message ->
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    showSuccess(message)
                    eventBus.emit(AppEvent.RefreshData)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false) }
                    showError(error.message ?: "Wystąpił błąd")
                }
        }
    }

    fun resetState() {
        _uiState.update { InvitationUiState() }
    }
}

data class InvitationUiState(
    val code: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false
)