package com.noisevisionsoftware.vitema.ui.screens.subscription

import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
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