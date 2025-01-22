package com.noisevisionsoftware.szytadieta.domain.repository.health

import android.icu.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.MeasurementType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class BodyMeasurementRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val measurementsCollection = "bodyMeasurements"

    suspend fun addMeasurements(measurements: BodyMeasurements): Result<Unit> = try {
        firestore.collection(measurementsCollection)
            .document(measurements.id)
            .set(measurements)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getMeasurementsHistory(
        userId: String,
        startDate: Long? = null,
        endDate: Long? = null,
        limit: Long = 50
    ): Result<List<BodyMeasurements>> = try {
        var query = firestore.collection(measurementsCollection)
            .whereEqualTo("userId", userId)

        if (startDate != null && endDate != null) {
            query = query
                .whereGreaterThanOrEqualTo("date", startDate)
                .whereLessThanOrEqualTo("date", endDate)
        }

        val snapshot = query
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull {
            it.toObject(BodyMeasurements::class.java)
        })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getCurrentWeekMeasurements(userId: String): Result<BodyMeasurements?> = try {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)

        val snapshot = firestore.collection(measurementsCollection)
            .whereEqualTo("userId", userId)
            .whereEqualTo("weekNumber", currentWeek)
            .get()
            .await()

        Result.success(snapshot.documents.firstOrNull()?.toObject(BodyMeasurements::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteMeasurement(measurementId: String): Result<Unit> = try {
        firestore.collection(measurementsCollection)
            .document(measurementId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getLatestMeasurements(
        userId: String,
        limit: Int = 7
    ): Result<List<BodyMeasurements>> = try {
        val snapshot = firestore.collection(measurementsCollection)
            .whereEqualTo("userId", userId)
            .whereEqualTo("measurementType", MeasurementType.FULL_BODY)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull { it.toObject(BodyMeasurements::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }
}