package com.noisevisionsoftware.szytadieta.ui.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.Alert
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val networkManager: NetworkConnectivityManager,
    private val alertManager: AlertManager,
    protected val eventBus: EventBus
) : ViewModel() {

    private val _isNetworkAvailable = MutableStateFlow(true)

    init {
        observeNetworkConnection()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.UserLoggedOut -> onUserLoggedOut()
                    is AppEvent.RefreshData -> onRefreshData()
                }
            }
        }
    }

    protected open fun onUserLoggedOut() {

    }

    protected open fun onRefreshData() {
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

    private fun <T> handleError(
        throwable: Throwable,
        stateFlow: MutableStateFlow<ViewModelState<T>>,
        customErrorMessage: String? = null
    ) {
        val message = when (throwable) {
            is AppException -> throwable.message
            else -> customErrorMessage ?: "Wystąpił nieoczekiwany błąd"
        }
        stateFlow.value = ViewModelState.Error(message)
        showError(message)
    }

    protected fun <T> handleOperation(
        stateFlow: MutableStateFlow<ViewModelState<T>>,
        operation: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                stateFlow.value = ViewModelState.Loading
                val result = operation()
                stateFlow.value = ViewModelState.Success(result)
            } catch (e: Exception) {
                Log.e("Operation error", "Operation error details:", e)
                handleError(e, stateFlow)
            }
        }
    }

    private fun showNetworkError() {
        alertManager.showAlert(
            Alert.Error("Brak połączenia z internetem")
        )
    }
}