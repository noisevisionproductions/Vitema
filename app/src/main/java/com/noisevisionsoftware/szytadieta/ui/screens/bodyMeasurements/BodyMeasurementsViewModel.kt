package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements

import android.icu.util.Calendar
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.MeasurementType
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.BodyMeasurementRepository
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BodyMeasurementsViewModel @Inject constructor(
    private val bodyMeasurementsRepository: BodyMeasurementRepository,
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _measurementsState = MutableStateFlow<MeasurementsState>(MeasurementsState.Initial)
    val measurementsState = _measurementsState.asStateFlow()

    sealed class MeasurementsState {
        data object Initial : MeasurementsState()
        data object Loading : MeasurementsState()
        data class Success(val measurements: List<BodyMeasurements>) : MeasurementsState()
        data class Error(val exception: AppException) : MeasurementsState()
    }

    init {
        getHistory()
    }

    fun addMeasurements(measurements: BodyMeasurements) {
        viewModelScope.launch {
            val currentUser = getCurrentUserOrThrow()

            val calendar = Calendar.getInstance()
            val updatedMeasurements = measurements.copy(
                userId = currentUser.uid,
                weekNumber = calendar.get(Calendar.WEEK_OF_YEAR),
                measurementType = MeasurementType.FULL_BODY
            )

            safeApiCall { bodyMeasurementsRepository.addMeasurements(updatedMeasurements) }
                .onSuccess {
                    showSuccess("Pomiary zostały zapisane")
                    getHistory()
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas dodawania pomiarów"
                    )
                }
        }
    }

    fun deleteMeasurement(measurementId: String) {
        handleOperation {
            safeApiCall {
                bodyMeasurementsRepository.deleteMeasurement(measurementId)
                    .onSuccess {
                        showSuccess("Pomyślnie usunięto pomiary")
                        getHistory()
                    }
                    .onFailure { throwable ->
                        throw AppException.UnknownException(
                            throwable.message ?: "Błąd podczas usuwania pomiarów"
                        )
                    }
            }
        }
    }

    private fun getHistory(monthsBack: Int = 3) {
        handleOperation {
            val currentUser = getCurrentUserOrThrow()

            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -monthsBack)
            val startDate = calendar.timeInMillis

            safeApiCall {
                bodyMeasurementsRepository.getMeasurementsHistory(
                    currentUser.uid,
                    startDate,
                    endDate
                ).map { measurements ->
                    measurements.filter { it.measurementType == MeasurementType.FULL_BODY }
                }
            }
                .onSuccess { measurements ->
                    _measurementsState.value = MeasurementsState.Success(measurements)
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas pobierania historii pomiarów"
                    )
                }
        }
    }

    private fun getCurrentUserOrThrow(): FirebaseUser {
        return authRepository.getCurrentUser()
            ?: throw AppException.AuthException("Użytkownik nie jest zalogowany")
    }

    private fun handleOperation(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _measurementsState.value = MeasurementsState.Loading
                block()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        val exception = when (throwable) {
            is AppException -> throwable
            else -> AppException.UnknownException(
                throwable.message ?: "Wystąpił nieoczekiwany błąd"
            )
        }
        _measurementsState.value = MeasurementsState.Error(exception)
        showError(exception.message)
    }
}