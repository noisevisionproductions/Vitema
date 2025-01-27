package com.noisevisionsoftware.szytadieta.ui.screens.waterIntake

import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.domain.model.health.water.CustomWaterAmount
import com.noisevisionsoftware.szytadieta.domain.model.health.water.WaterIntake
import com.noisevisionsoftware.szytadieta.domain.model.user.UserSettings
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WaterRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WaterTrackingViewModel @Inject constructor(
    private val waterRepository: WaterRepository,
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _waterIntakeState =
        MutableStateFlow<ViewModelState<List<WaterIntake>>>(ViewModelState.Initial)
    val waterIntakeState = _waterIntakeState.asStateFlow()

    private val _customAmount = MutableStateFlow<CustomWaterAmount?>(null)
    val customAmount = _customAmount.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.getCurrentLocalDate())
    val selectedDate = _selectedDate.asStateFlow()

    private val _userSettings =
        MutableStateFlow<ViewModelState<UserSettings>>(ViewModelState.Initial)
    val userSettings = _userSettings.asStateFlow()

    init {
        loadUserSettings()
        loadWaterIntakes()
        loadCustomAmount()
    }

    private fun loadUserSettings() {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    val settings = waterRepository.getUserSettings(userId).getOrThrow()
                    _userSettings.value = ViewModelState.Success(settings)
                }
            } catch (e: Exception) {
                _userSettings.value =
                    ViewModelState.Error(e.message ?: "Błąd podczas ładowania ustawień")
            }
        }
    }

    private fun loadWaterIntakes() {
        handleOperation(_waterIntakeState) {
            authRepository.withAuthenticatedUser { userId ->
                loadWaterIntakesData(userId, _selectedDate.value)
            }
        }
    }

    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            try {
                _waterIntakeState.value = when (val currentState = _waterIntakeState.value) {
                    is ViewModelState.Success -> ViewModelState.Loading
                    else -> currentState
                }

                authRepository.withAuthenticatedUser { userId ->
                    val waterIntake = WaterIntake(
                        userId = userId,
                        amount = amount,
                        date = _selectedDate.value
                    )
                    waterRepository.addWaterIntake(waterIntake).getOrThrow()
                    val newData = loadWaterIntakesData(userId, _selectedDate.value)
                    _waterIntakeState.value = ViewModelState.Success(newData)
                }
            } catch (e: Exception) {
                _waterIntakeState.value = ViewModelState.Error(e.message ?: "Wystąpił błąd")
                showError(e.message ?: "Wystąpił błąd podczas dodawania wody")
            }
        }
    }

    fun updateDailyTarget(target: Int) {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    waterRepository.updateUserSettings(userId, target).getOrThrow()
                    loadUserSettings()
                    showSuccess("Zaktualizowano dzienny cel")
                }
            } catch (e: Exception) {
                showError(e.message ?: "Wystąpił błąd podczas aktualizacji celu")
            }
        }
    }

    fun updateSelectedDate(date: Long) {
        _selectedDate.value = date
        loadWaterIntakes()
        loadCustomAmount()
    }


    private fun loadCustomAmount() {
        viewModelScope.launch {
            authRepository.withAuthenticatedUser { userId ->
                preferencesManager.getCustomWaterAmount(userId).collect { amount ->
                    _customAmount.value = amount
                }
            }
        }
    }

    fun saveCustomAmount(amount: Int, label: String) {
        viewModelScope.launch {
            try {
                authRepository.withAuthenticatedUser { userId ->
                    val customAmount = CustomWaterAmount(amount, label)
                    preferencesManager.saveCustomWaterAmount(userId, customAmount)
                    _customAmount.value = customAmount
                    showSuccess("Zapisano własną wartość")
                }
            } catch (e: Exception) {
                showError("Błąd podczas zapisywania własnej wartości")
            }
        }
    }

    private suspend fun loadWaterIntakesData(userId: String, date: Long): List<WaterIntake> {
        return waterRepository.getDailyWaterIntakes(userId, date).getOrThrow()
    }

    public override fun onRefreshData() {
        loadUserSettings()
        loadWaterIntakes()
        loadCustomAmount()
    }

    override fun onNavigationEvent(destination: NavigationDestination) {
        when (destination) {
            NavigationDestination.AuthenticatedDestination.WaterIntake -> {
                loadWaterIntakes()
                loadCustomAmount()
            }

            else -> super.onNavigationEvent(destination)
        }
    }
}