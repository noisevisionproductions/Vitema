package com.noisevisionsoftware.szytadieta.ui.screens.subscription

import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    alertManager: AlertManager,
    networkManager: NetworkConnectivityManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    fun selectPlan() {
        showSuccess("Funkcjonalność w przygotowaniu")
    }
}