package com.noisevisionsoftware.szytadieta.ui.screens.admin

import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.UserRepository
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminPanelViewModel @Inject constructor(
    private val userRepository: UserRepository,
    eventBus: EventBus,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager, eventBus) {

    suspend fun checkAdminState(): Result<Boolean> =
        userRepository.getCurrentUserData().map { user ->
            user?.role == UserRole.ADMIN
        }
}