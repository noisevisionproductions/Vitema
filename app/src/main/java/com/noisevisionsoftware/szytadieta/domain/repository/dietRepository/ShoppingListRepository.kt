package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }

    suspend fun getShoppingListForWeek(
        userId: String,
        weekStartDate: Long
    ): Result<ShoppingList> = runCatching {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("uploadedAt", weekStartDate)
            .whereLessThan("uploadedAt", weekStartDate + WEEK_IN_MILLIS)
            .limit(1)
            .get()
            .await()

        snapshot.documents.firstOrNull()
            ?.toObject(Diet::class.java)
            ?.shoppingList
            ?: throw Exception("Nie znaleziono listy zakupów dla wybranego tygodnia")
    }

    suspend fun getAvailableWeeks(userId: String): Result<List<Long>> = runCatching {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .orderBy("uploadedAt", Query.Direction.DESCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull {
            it.toObject(Diet::class.java)?.uploadedAt
        }.distinct()
    }
}