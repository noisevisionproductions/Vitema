package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Recipe(
    val id: String = "",
    val name: String = "",
    val instructions: String = "",
    val createdAt: Timestamp?,
    val photos: List<String> = emptyList(),
    val nutritionalValues: NutritionalValues? = null,
    val ingredients: List<String> = emptyList(),
    val parentRecipeId: String? = null
)