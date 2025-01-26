package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Recipe
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private suspend fun getRecipe(recipeId: String): Result<Recipe> = runCatching {
        val snapshot = firestore.collection("recipes")
            .document(recipeId)
            .get()
            .await()

        snapshot.toObject(Recipe::class.java)?.copy(id = snapshot.id)
            ?: throw Exception("Nie znaleziono przepisu")
    }

    suspend fun getRecipesForMeals(meals: List<Meal>): Result<Map<String, Recipe>> = runCatching {
        val recipeIds = meals.map { it.recipeId }.distinct()

        val recipes = recipeIds.mapNotNull { recipeId ->
            val snapshot = firestore.collection("recipes")
                .document(recipeId)
                .get()
                .await()

            snapshot.toObject(Recipe::class.java)?.copy(id = snapshot.id)
        }

        recipes.associateBy { it.id }
    }
}