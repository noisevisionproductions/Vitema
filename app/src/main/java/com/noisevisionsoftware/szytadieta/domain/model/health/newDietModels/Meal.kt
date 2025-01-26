package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

data class Meal(
    val time: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val recipeId: String = ""
)
