package com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.ValidationManager
import com.noisevisionsoftware.szytadieta.domain.model.user.Gender
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.UserRepository
import com.noisevisionsoftware.szytadieta.ui.base.AppEvent
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CompleteProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _completeProfileState =
        MutableStateFlow<CompleteProfileState>(CompleteProfileState.Initial)
    val profileState = _completeProfileState.asStateFlow()

    sealed class CompleteProfileState {
        data object Initial : CompleteProfileState()
        data object Loading : CompleteProfileState()
        data class Success(val user: User) : CompleteProfileState()
        data class Error(val message: String) : CompleteProfileState()
    }

    private var tempBirthDate: Long? = null
    private var tempGender: Gender? = null

    private fun checkProfileCompletion(): Flow<Boolean> = flow {
        userRepository.getCurrentUserData()
            .onSuccess { user ->
                val isCompleted = user?.let {
                    it.birthDate != null &&
                            it.gender != null &&
                            it.storedAge > 0
                } ?: false
                emit(isCompleted)
            }
            .onFailure {
                emit(false)
            }
    }

    fun setTempBirthDate(birthDate: Long) {
        tempBirthDate = birthDate
    }

    fun setTempGender(gender: Gender) {
        tempGender = gender
    }

    fun saveProfile() {
        viewModelScope.launch {
            try {
                tempBirthDate?.let { birthDate ->
                    ValidationManager.validateBirthDate(birthDate).getOrThrow()
                    _completeProfileState.value = CompleteProfileState.Loading

                    updateUserField { currentUser ->
                        val updatedUser = currentUser.copy(
                            birthDate = birthDate,
                            gender = tempGender,
                            profileCompleted = true
                        )

                        updatedUser.copy(
                            storedAge = updatedUser.calculateAge()
                        )
                    }
                }
            } catch (e: AppException) {
                handleError(e)
            }
        }
    }

    private suspend fun updateUserField(updateUser: (User) -> User) {
        userRepository.getCurrentUser()?.let {
            safeApiCall { userRepository.getCurrentUserData() }
                .onSuccess { currentUser ->
                    currentUser?.let { user ->
                        val updatedUser = updateUser(user)
                        safeApiCall { userRepository.updateUserData(updatedUser) }
                            .onSuccess {
                                _completeProfileState.value =
                                    CompleteProfileState.Success(updatedUser)
                                eventBus.emit(AppEvent.RefreshData)
                                showSuccess("Profil został zaktualizowany")
                                tempBirthDate = null
                                tempGender = null
                            }
                            .onFailure { throwable ->
                                handleError(throwable)
                            }
                    }
                }
        }
    }

    private fun handleError(throwable: Throwable) {
        val appException = when (throwable) {
            is AppException -> throwable
            else -> AppException.UnknownException()
        }
        _completeProfileState.value = CompleteProfileState.Error(appException.message)
        showError(appException.message)
    }

    override fun onUserLoggedOut() {
        _completeProfileState.value = CompleteProfileState.Initial
    }

    override fun onRefreshData() {
        checkProfileCompletion()
    }
}