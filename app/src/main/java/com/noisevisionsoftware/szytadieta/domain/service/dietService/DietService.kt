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

    suspend fun getDiet(dietId: String): Result<Diet> = runCatching {
        val snapshot = firestore.collection("diets")
            .document(dietId)
            .get()
            .await()

        snapshot.toObject(Diet::class.java)
            ?: throw Exception("Nie znaleziono diety")
    }

    suspend fun getUserDiets(userId: String): Result<List<Diet>> = runCatching {
        val snapshot = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        snapshot.documents.mapNotNull { it.toObject(Diet::class.java) }
    }
}