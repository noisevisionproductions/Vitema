package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.utils.getWeekStartDate
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShoppingListRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getShoppingListForWeek(
        userId: String,
        weekDate: Long
    ): Result<ShoppingList> = runCatching {
        val weekStart = getWeekStartDate(weekDate)
        val weekEnd = weekStart + 7 * 24 * 60 * 60 * 1000L

        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("startDate", weekStart)
            .whereLessThan("startDate", weekEnd)
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
            .orderBy("startDate", Query.Direction.ASCENDING)
            .get()
            .await()

        snapshot.documents.mapNotNull {
            it.toObject(Diet::class.java)?.startDate
        }.distinct()
    }

    fun observerDietChanges(userId: String): Flow<Boolean> = callbackFlow {
        val subscription = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(snapshot?.documents?.isNotEmpty() ?: false)
            }

        awaitClose { subscription.remove() }
    }
}