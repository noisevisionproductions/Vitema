package com.noisevisionsoftware.szytadieta.domain.repository.health

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.health.water.WaterIntake
import com.noisevisionsoftware.szytadieta.domain.model.user.UserSettings
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WaterRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    init {
        firestore.collection("waterIntakes")
            .document("--indexes--")
            .set(
                mapOf(
                    "fields" to listOf(
                        mapOf(
                            "fieldPath" to "userId",
                            "mode" to "ASCENDING"
                        ),
                        mapOf(
                            "fieldPath" to "date",
                            "mode" to "ASCENDING"
                        )
                    )
                )
            )
    }

    suspend fun addWaterIntake(waterIntake: WaterIntake): Result<Unit> = try {
        firestore.runTransaction { transaction ->
            val docRef = firestore.collection("waterIntakes")
                .document(waterIntake.id)

            val existingDoc = transaction.get(docRef)
            if (!existingDoc.exists()) {
                transaction.set(docRef, waterIntake)
            }
        }.await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getDailyWaterIntakes(userId: String, date: Long): Result<List<WaterIntake>> = try {
        val startOfDay = DateUtils.getStartOfDay(date)
        val endOfDay = DateUtils.getEndOfDay(date)

        val snapshot = firestore.collection("waterIntakes")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("date", startOfDay)
            .whereLessThanOrEqualTo("date", endOfDay)
            .orderBy("date", Query.Direction.ASCENDING)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull { it.toObject(WaterIntake::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserSettings(userId: String, waterDailyTarget: Int): Result<Unit> = try {
        val userSettings = UserSettings(
            userId = userId,
            waterDailyTarget = waterDailyTarget,
            lastUpdated = DateUtils.getCurrentPreciseTime()
        )
        firestore.collection("userSettings")
            .document(userId)
            .set(userSettings)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserSettings(userId: String): Result<UserSettings> = try {
        val snapshot = firestore.collection("userSettings")
            .document(userId)
            .get()
            .await()

        val userSettings = snapshot.toObject(UserSettings::class.java)
            ?: UserSettings(userId = userId)
        Result.success(userSettings)
    } catch (e: Exception) {
        Result.failure(e)
    }
}