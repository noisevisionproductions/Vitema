package com.noisevisionsoftware.szytadieta.domain.service.dietService

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DietService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveDiet(diet: Diet) {
        firestore.collection("diets")
            .document(diet.id)
            .set(diet)
            .await()
    }

    suspend fun getUserDiets(userId: String): Result<List<Diet>> = runCatching {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        snapshot.documents.mapNotNull { it.toObject(Diet::class.java) }
    }

    suspend fun getUserDietsForPeriod(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Result<List<Diet>> = runCatching {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startDate", startDate)
            .whereLessThanOrEqualTo("endDate", endDate)
            .get()
            .await()

        snapshot.documents.mapNotNull { it.toObject(Diet::class.java) }
    }
}