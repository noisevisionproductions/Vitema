package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

data class Meal(
    val time: String = "",
    val name: String = "",
    val mealType: MealType = MealType.BREAKFAST,
    val recipeId: String = "",
    val recipe: Recipe? = null
)

fun DayMeal.toMeal(): Meal {
    return Meal(
        time = time,
        mealType = mealType,
        recipeId = recipeId
    )
}