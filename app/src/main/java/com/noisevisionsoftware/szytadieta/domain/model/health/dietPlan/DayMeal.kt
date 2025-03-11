package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

data class DayMeal(
    val recipeId: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val time: String = ""
)