package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

data class RecipeReference(
    val recipeId: String = "",
    val dietId: String = "",
    val userId: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val addedAt: Long = 0
)
