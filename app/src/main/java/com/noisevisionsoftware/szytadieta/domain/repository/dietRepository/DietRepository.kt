package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.math.abs

class DietRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend fun getUserDietForDate(date: Long): Result<Diet?> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            getDietForDateAndUser(userId, date)
        }
    }

    suspend fun getDietForSpecificUserAndDate(userId: String, date: Long): Result<Diet?> = runCatching {
        getDietForDateAndUser(userId, date)
    }

    private suspend fun getDietForDateAndUser(userId: String, date: Long): Diet? {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .whereLessThanOrEqualTo("startDate", date)
            .whereGreaterThanOrEqualTo("endDate", date)
            .get()
            .await()

        return snapshot.documents.firstOrNull()?.toObject(Diet::class.java)
    }

    suspend fun getAvailableWeekDates(): Result<List<Long>> = runCatching {
        authRepository.withAuthenticatedUser { userId ->

            val snapshot = firestore.collection("diets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.getLong("startDate")
            }.distinct().sorted()
        }
    }

    suspend fun getClosestAvailableWeekDate(): Result<Long?> = runCatching {
        getAvailableWeekDates().getOrNull()?.let { dates ->
            val currentDate = DateUtils.getCurrentLocalDate()
            dates.minByOrNull { abs(it - currentDate) }
        }
    }

    suspend fun hasAnyDiets(): Result<Boolean> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val snapshot = firestore.collection("diets")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        }
    }
}