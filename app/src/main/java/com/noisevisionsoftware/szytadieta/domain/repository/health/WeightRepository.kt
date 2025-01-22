package com.noisevisionsoftware.szytadieta.domain.repository.health

import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val bodyMeasurementRepository: BodyMeasurementRepository
) {

    suspend fun addWeight(bodyMeasurements: BodyMeasurements): Result<Unit> {
        return bodyMeasurementRepository.addMeasurements(bodyMeasurements)
    }

    suspend fun getUserWeights(userId: String): Result<List<BodyMeasurements>> {
        return bodyMeasurementRepository.getMeasurementsHistory(userId)
    }

    suspend fun deleteWeight(weightId: String): Result<Unit> {
        return bodyMeasurementRepository.deleteMeasurement(weightId)
    }

    suspend fun getLatestWeights(userId: String, limit: Int = 7): Result<List<BodyMeasurements>> {
        return bodyMeasurementRepository.getMeasurementsHistory(userId, limit = limit.toLong())
    }
}