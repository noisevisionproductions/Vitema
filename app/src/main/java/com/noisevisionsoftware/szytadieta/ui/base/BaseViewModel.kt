package com.noisevisionsoftware.szytadieta.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.Alert
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val networkManager: NetworkConnectivityManager,
    private val alertManager: AlertManager
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)

    init {
        observeNetworkConnection()
    }

    private fun observeNetworkConnection() {
        viewModelScope.launch {
            networkManager.isNetworkConnected.collect { isConnected ->
                _isNetworkAvailable.update { isConnected }
                if (!isConnected) {
                    showNetworkError()
                }
            }
        }
    }

    fun showError(message: String) {
        viewModelScope.launch {
            if (!_isNetworkAvailable.value) {
                showNetworkError()
                return@launch
            }
            alertManager.showAlert(Alert.Error(message))
        }
    }

    fun showSuccess(message: String) {
        viewModelScope.launch {
            if (_isNetworkAvailable.value) {
                alertManager.showAlert(Alert.Success(message))
            } else {
                showNetworkError()
            }
        }
    }

    suspend fun <T> safeApiCall(
        apiCall: suspend () -> Result<T>
    ): Result<T> {
        return if (!_isNetworkAvailable.value) {
            Result.failure(AppException.NetworkException("Brak połączenia z internetem"))
        } else {
            try {
                apiCall()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun showNetworkError() {
        alertManager.showAlert(
            Alert.Error("Brak połączenia z internetem")
        )
    }
}