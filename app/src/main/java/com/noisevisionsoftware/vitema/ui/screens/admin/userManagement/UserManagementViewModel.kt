package com.noisevisionsoftware.vitema.ui.screens.admin.userManagement

import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.model.SearchableData
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.model.user.UserRole
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AdminRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val adminRepository: AdminRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _userState =
        MutableStateFlow<ViewModelState<SearchableData<User>>>(ViewModelState.Initial)
    val userState = _userState.asStateFlow()

    init {
        loadUsers()
    }

    fun searchUsers(query: String) {
        val currentState = _userState.value
        if (currentState is ViewModelState.Success) {
            _userState.value = ViewModelState.Success(
                currentState.data.updateSearch(query)
            )
        }
    }

    fun updateUserRole(userId: String, newRole: UserRole) {
        handleOperation(_userState) {
            adminRepository.updateUserRole(userId, newRole).getOrThrow()
            showSuccess("Rola użytkownika została zaktualizowana")

            val users = adminRepository.getAllUsers().getOrThrow()

            val currentQuery =
                (_userState.value as? ViewModelState.Success)?.data?.searchQuery ?: ""

            SearchableData.create(
                items = users,
                searchPredicate = { user, query ->
                    user.email.contains(query, ignoreCase = true) ||
                            user.nickname.contains(query, ignoreCase = true)
                }
            ).updateSearch(currentQuery)
        }
    }

    fun loadUsers() {
        handleOperation(_userState) {
            val users = adminRepository.getAllUsers().getOrThrow()
            SearchableData.create(
                items = users,
                searchPredicate = { user, query ->
                    user.email.contains(query, ignoreCase = true) ||
                            user.nickname.contains(query, ignoreCase = true)
                }
            )
        }
    }

    override fun onUserLoggedOut() {
        _userState.value = ViewModelState.Initial
    }
}