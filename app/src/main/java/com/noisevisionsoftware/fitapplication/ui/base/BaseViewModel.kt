package com.noisevisionsoftware.fitapplication.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.fitapplication.domain.exceptions.AppException
import com.noisevisionsoftware.fitapplication.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.fitapplication.ui.common.UiEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class BaseViewModel(
    private val networkManager: NetworkConnectivityManager
) : ViewModel() {

    private val _uiEvent = MutableStateFlow<UiEvent?>(null)
    val uiEvent: StateFlow<UiEvent?> = _uiEvent.asStateFlow()

    private var isNetworkAvailable = true
    private var currentMessageJob: Job? = null

    init {
        observeNetworkConnection()
    }

    private fun observeNetworkConnection() {
        networkManager.isNetworkConnected
            .onEach { isConnected ->
                isNetworkAvailable = isConnected
                if (!isConnected) {
                    showError("Brak połączenia z internetem")
                }
            }
            .launchIn(viewModelScope)
    }

    protected fun showError(message: String) {
        currentMessageJob?.cancel()
        currentMessageJob = viewModelScope.launch {
            _uiEvent.value = UiEvent.ShowError(message)
            delay(3000)
            _uiEvent.value = null
        }
    }

    protected fun showSuccess(message: String) {
        currentMessageJob?.cancel()
        viewModelScope.launch {
            _uiEvent.value = UiEvent.ShowSuccess(message)
            delay(1500)
            _uiEvent.value = null
        }
    }

    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> Result<T>
    ): Result<T> {
        return if (!isNetworkAvailable) {
            Result.failure(AppException.NetworkException("Brak połączenia z internetem"))
        } else {
            try {
                apiCall()
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentMessageJob?.cancel()
    }
}