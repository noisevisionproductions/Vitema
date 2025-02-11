package com.noisevisionsoftware.szytadieta.domain.model.shopping

import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.MealType

sealed class ShoppingListGroup {
    data class ByRecipe(
        val recipeName: String,
        val dayIndex: Int,
        val mealType: MealType,
        val mealTime: String,
        val items: List<ShoppingListProductContext>
    ) : ShoppingListGroup()

    data class ByDay(
        val dayIndex: Int,
        val meals: List<MealGroup>,
        val items: List<ShoppingListProductContext>
    ) : ShoppingListGroup()

    data class SingleList(
        val items: List<ShoppingListProductContext>,
        val totalDays: Int
    ) : ShoppingListGroup()
}

data class MealGroup(
    val mealType: MealType,
    val mealTime: String,
    val recipes: List<Pair<String, List<ShoppingListProductContext>>>
)