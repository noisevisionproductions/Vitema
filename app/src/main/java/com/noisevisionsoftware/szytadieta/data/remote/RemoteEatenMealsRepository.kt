package com.noisevisionsoftware.szytadieta.data.remote

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteEatenMealsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveEatenMeal(userId: String, date: String, mealId: String) {
        val documentRef = firestore.collection("eatenMeals")
            .document("${userId}_${date}")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            if (snapshot.exists()) {
                transaction.update(
                    documentRef,
                    "meals",
                    FieldValue.arrayUnion(mealId)
                )
            } else {
                val newDocument = mapOf(
                    "userId" to userId,
                    "date" to date,
                    "meals" to listOf(mealId)
                )
                transaction.set(documentRef, newDocument)
            }
        }.await()
    }

    suspend fun removeEatenMeal(userId: String, date: String, mealId: String) {
        val documentRef = firestore.collection("eatenMeals")
            .document("${userId}_${date}")

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(documentRef)

            if (snapshot.exists()) {
                val currentMeals = when (val meals = snapshot.get("meals")) {
                    is List<*> -> meals
                        .filterIsInstance<String>()
                        .toList()

                    null -> emptyList()
                    else -> {
                        Log.w("EatenMealsRepository", "Unexpected meals type: ${meals::class.java}")
                        emptyList()
                    }
                }

                val updatedMeals = currentMeals.filter { it != mealId }

                if (updatedMeals.isEmpty()) {
                    transaction.delete(documentRef)
                } else {
                    transaction.update(documentRef, "meals", updatedMeals)
                }
            }
        }.await()
    }

    suspend fun getEatenMeals(userId: String, date: String): Set<String> {
        return try {
            val snapshot = firestore.collection("eatenMeals")
                .document("${userId}_${date}")
                .get()
                .await()

            when (val meals = snapshot.get("meals")) {
                is List<*> -> meals
                    .filterIsInstance<String>()
                    .toSet()

                null -> emptySet()
                else -> {
                    Log.w("EatenMealsRepository", "Unexpected meals type: ${meals::class.java}")
                    emptySet()
                }
            }
        } catch (e: Exception) {
            Log.e(
                "EatenMealsRepository",
                "Error getting eaten meals for userId: $userId, date: $date",
                e
            )
            emptySet()
        }
    }
}