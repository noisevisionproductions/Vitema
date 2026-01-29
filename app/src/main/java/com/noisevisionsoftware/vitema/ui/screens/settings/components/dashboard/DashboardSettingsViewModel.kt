package com.noisevisionsoftware.vitema.ui.screens.settings.components.dashboard

import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.data.localPreferences.SettingsManager
import com.noisevisionsoftware.vitema.domain.model.dashboard.DashboardCardType
import com.noisevisionsoftware.vitema.domain.model.dashboard.DashboardConfig
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardSettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _dashboardConfig =
        MutableStateFlow<ViewModelState<DashboardConfig>>(ViewModelState.Initial)
    val dashboardConfig = _dashboardConfig.asStateFlow()

    init {
        loadDashboardConfig()
    }

    private fun loadDashboardConfig() {
        viewModelScope.launch {
            _dashboardConfig.value = ViewModelState.Loading
            try {
                settingsManager.dashboardConfig.collect { config ->
                    _dashboardConfig.value = ViewModelState.Success(config)
                }
            } catch (e: Exception) {
                _dashboardConfig.value =
                    ViewModelState.Error(e.message ?: "Błąd podczas ładowania konfiguracji")
            }
        }
    }

    fun toggleCardVisibility(cardType: DashboardCardType, isVisible: Boolean) {
        viewModelScope.launch {
            try {
                val currentConfig =
                    (_dashboardConfig.value as? ViewModelState.Success)?.data ?: return@launch

                val newConfig = if (isVisible) {
                    currentConfig.copy(hiddenCards = currentConfig.hiddenCards - cardType)
                } else {
                    currentConfig.copy(hiddenCards = currentConfig.hiddenCards + cardType)
                }

                settingsManager.updateConfig(newConfig)
                showSuccess("Zaktualizowano ustawienia dashboardu")

            } catch (e: Exception) {
                showError(e.message ?: "Błąd podczas aktualizacji ustawień")
            }
        }
    }

    fun reorderCards(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            try {
                val currentConfig = (_dashboardConfig.value as? ViewModelState.Success)?.data ?: return@launch
                val currentOrder = currentConfig.cardOrder.toMutableList()

                if (fromIndex in currentOrder.indices && toIndex in currentOrder.indices) {
                    val item = currentOrder.removeAt(fromIndex)
                    currentOrder.add(toIndex, item)

                    val newConfig = currentConfig.copy(cardOrder = currentOrder)
                    settingsManager.updateConfig(newConfig)
                }
            } catch (e: Exception) {
                showError("Błąd podczas zmiany kolejności: ${e.message}")
            }
        }
    }

    override fun onUserLoggedOut() {
        viewModelScope.launch {
            settingsManager.clearSettings()
        }
    }
}