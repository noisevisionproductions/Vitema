package com.noisevisionsoftware.szytadieta.domain.service.dietService

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Diet
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
}