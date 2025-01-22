package com.noisevisionsoftware.szytadieta.ui.screens.weight

import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WeightRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class WeightViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val weightRepository: WeightRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _weightState =
        MutableStateFlow<ViewModelState<List<BodyMeasurements>>>(ViewModelState.Initial)
    val weightState = _weightState.asStateFlow()

    init {
        loadWeights()
    }

    fun addWeight(weight: Int, note: String = "") {
        when {
            weight < 40 -> {
                _weightState.value = ViewModelState.Error("Waga musi być nie mniejsza niż 40 kg")
                return
            }

            weight > 250 -> {
                _weightState.value = ViewModelState.Error("Waga musi być nie większa niż 250 kg")
                return
            }
        }

        handleOperation(_weightState) {
            authRepository.withAuthenticatedUser { userId ->
                val bodyMeasurementsEntry = BodyMeasurements(
                    userId = userId,
                    weight = weight,
                    note = note,
                    measurementType = MeasurementType.WEIGHT_ONLY
                )

                safeApiCall { weightRepository.addWeight(bodyMeasurementsEntry) }
                    .onSuccess {
                        showSuccess("Pomyślnie dodano wagę")
                        loadWeights()
                    }
                    .onFailure { throwable ->
                        throw AppException.UnknownException(
                            throwable.message ?: "Błąd podczas dodawania wagi"
                        )
                    }

                loadWeightsData()
            }
        }
    }

    fun deleteWeight(weightId: String) {
        handleOperation(_weightState) {
            safeApiCall { weightRepository.deleteWeight(weightId) }
                .onSuccess {
                    showSuccess("Pomyślnie usunięto wpis")
                    loadWeights()
                }
                .onFailure { throwable ->
                    throw AppException.UnknownException(
                        throwable.message ?: "Błąd podczas usuwania wpisu"
                    )
                }

            loadWeightsData()
        }
    }

    fun loadWeights() {
        handleOperation(_weightState) {
            loadWeightsData()
        }
    }

    private suspend fun loadWeightsData(): List<BodyMeasurements> {
        return safeApiCall {
            authRepository.withAuthenticatedUser { userId ->
                weightRepository.getUserWeights(userId)
                    .map { measurements ->
                        measurements.filter {
                            it.measurementType == MeasurementType.FULL_BODY ||
                                    it.measurementType == MeasurementType.WEIGHT_ONLY
                        }
                    }
            }
        }.getOrThrow()
    }

    override fun onUserLoggedOut() {
        super.onUserLoggedOut()
        _weightState.value = ViewModelState.Initial
    }
}