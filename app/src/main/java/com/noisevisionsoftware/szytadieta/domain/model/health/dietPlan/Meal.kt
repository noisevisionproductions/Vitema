package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

data class Meal(
    val name: MealType = MealType.BREAKFAST,
    val description: String = ""
)
