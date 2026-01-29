package com.noisevisionsoftware.vitema.domain.alert

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlertManager @Inject constructor() {
    private val _currentAlert = MutableStateFlow<Alert?>(null)
    val currentAlert: StateFlow<Alert?> = _currentAlert.asStateFlow()

    fun showAlert(alert: Alert) {
        _currentAlert.value = alert
    }

    fun clearAlert() {
        _currentAlert.value = null
    }
}

sealed class Alert {
    abstract val message: String
    abstract val duration: Long

    data class Error(
        override val message: String, override val duration: Long = 2000L
    ) : Alert()

    data class Success(
        override val message: String, override val duration: Long = 2000L
    ) : Alert()
}