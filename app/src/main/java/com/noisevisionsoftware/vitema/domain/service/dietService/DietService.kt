package com.noisevisionsoftware.vitema.domain.service.dietService

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.Diet
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