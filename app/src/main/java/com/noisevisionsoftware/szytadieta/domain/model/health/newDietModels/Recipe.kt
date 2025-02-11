package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Recipe(
    val id: String = "",
    val name: String = "",
    val instructions: String = "",

    @PropertyName("createdAt")
    @get:PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    val photos: List<String> = emptyList(),
    val nutritionalValues: NutritionalValues = NutritionalValues(),
    val ingredients: List<String> = emptyList(),
    val parentRecipeId: String? = null
)