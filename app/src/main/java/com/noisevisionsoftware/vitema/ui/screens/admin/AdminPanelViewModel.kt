package com.noisevisionsoftware.vitema.ui.screens.admin

import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.model.user.UserRole
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.UserRepository
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
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