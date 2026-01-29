package com.noisevisionsoftware.vitema.ui.screens.bodyMeasurements

import android.icu.util.Calendar
import android.util.Log
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementSourceType
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AuthRepository
import com.noisevisionsoftware.vitema.domain.repository.health.BodyMeasurementRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class BodyMeasurementsViewModel @Inject constructor(
    private val bodyMeasurementsRepository: BodyMeasurementRepository,
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _measurementsState =
        MutableStateFlow<ViewModelState<List<BodyMeasurements>>>(ViewModelState.Initial)
    val measurementsState = _measurementsState.asStateFlow()

    init {
        getMeasurementsHistory()
    }

    suspend fun addMeasurements(measurements: BodyMeasurements): Result<Unit> {
        return try {

            authRepository.withAuthenticatedUser { userId ->
                val calendar = Calendar.getInstance()
                val updatedMeasurements = measurements.copy(
                    userId = userId,
                    date = DateUtils.getCurrentPreciseTime(),
                    weekNumber = calendar.get(Calendar.WEEK_OF_YEAR),
                    measurementType = MeasurementType.FULL_BODY,
                    sourceType = MeasurementSourceType.APP
                )

                Log.d("BodyMeasurementsVM", "Saving measurements: $updatedMeasurements")

                bodyMeasurementsRepository.addMeasurements(updatedMeasurements)
                    .onSuccess {
                        showSuccess("Pomiary zostały zapisane")
                        loadMeasurementsHistory()
                        Result.success(Unit)
                    }
                    .onFailure { error ->
                        Log.e("BodyMeasurementsVM", "Error saving measurements", error)
                        showError(error.message ?: "Wystąpił błąd podczas dodawania pomiarów")
                    }
            }
        } catch (e: Exception) {
            Log.e("BodyMeasurementsVM", "Error adding measurements", e)
            showError(e.message ?: "Wystąpił błąd podczas dodawania pomiarów")
            Result.failure(e)
        }
    }

    fun deleteMeasurement(measurementId: String) {
        handleOperation(_measurementsState) {
            bodyMeasurementsRepository.deleteMeasurement(measurementId)
                .getOrThrow()

            showSuccess("Pomyślnie usunięto pomiary")

            loadMeasurementsHistory()
        }
    }

    fun getMeasurementsHistory(monthsBack: Int = 3) {
        handleOperation(_measurementsState) {
            loadMeasurementsHistory(monthsBack)
        }
    }

    private suspend fun loadMeasurementsHistory(monthsBack: Int = 3): List<BodyMeasurements> {
        return authRepository.withAuthenticatedUser { userId ->
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, -monthsBack)
            val startDate = calendar.timeInMillis

            return@withAuthenticatedUser bodyMeasurementsRepository.getMeasurementsHistory(
                userId,
                startDate,
                endDate
            )
                .getOrThrow()
                .filter { it.measurementType == MeasurementType.FULL_BODY }
        }
    }

    suspend fun getLastMeasurements(): BodyMeasurements? {
        return try {
            authRepository.withAuthenticatedUser { userId ->
                bodyMeasurementsRepository.getMeasurementsHistory(
                    userId = userId,
                    limit = 10
                ).getOrNull()?.firstOrNull { it.measurementType == MeasurementType.FULL_BODY }
            }
        } catch (e: Exception) {
            Log.e("BodyMeasurementsVM", "Error getting last measurements", e)
            null
        }
    }

    override fun onUserLoggedOut() {
        _measurementsState.value = ViewModelState.Initial
    }
}