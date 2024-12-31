package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.model.UserRole
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _userRole = MutableStateFlow<UserRole?>(null)
    val userRole = _userRole.asStateFlow()

    init {
        loadUserRole()
    }

    private fun loadUserRole() {
        viewModelScope.launch {
            safeApiCall { authRepository.getCurrentUserData() }
                .onSuccess { user ->
                    _userRole.value = user?.role
                }
        }
    }
}