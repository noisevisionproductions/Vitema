package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.MeasurementType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeightRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val weightsCollection = "bodyMeasurements"

    suspend fun addWeight(bodyMeasurements: BodyMeasurements): Result<Unit> = try {
        firestore.collection(weightsCollection)
            .document(bodyMeasurements.id)
            .set(bodyMeasurements)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserWeights(userId: String): Result<List<BodyMeasurements>> = try {
        val snapshot = firestore.collection(weightsCollection)
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull { it.toObject(BodyMeasurements::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteWeight(weightId: String): Result<Unit> = try {
        firestore.collection(weightsCollection)
            .document(weightId)
            .delete()
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getLatestWeight(userId: String): Result<BodyMeasurements?> = try {
        val snapshot = firestore.collection(weightsCollection)
            .whereEqualTo("userId", userId)
            .whereEqualTo("measurementType", MeasurementType.WEIGHT_ONLY)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        Result.success(snapshot.documents.firstOrNull()?.toObject(BodyMeasurements::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
}