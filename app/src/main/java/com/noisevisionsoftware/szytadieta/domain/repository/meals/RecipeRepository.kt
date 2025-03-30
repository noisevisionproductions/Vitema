package com.noisevisionsoftware.szytadieta.domain.repository.meals

import android.net.Uri
import android.util.Log
import com.noisevisionsoftware.szytadieta.data.remote.RecipeService
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.DayMeal
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Recipe
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val recipeService: RecipeService
) {

    suspend fun getRecipeById(recipeId: String): Result<Recipe> = runCatching {
        recipeService.getRecipeById(recipeId)
    }

    suspend fun addPhotoToRecipe(recipeId: String, photoUri: Uri): Result<String> = runCatching {
        val file = File(photoUri.path ?: throw IllegalArgumentException("Invalid file path"))
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        recipeService.uploadRecipeImage(recipeId, body).imageUrl
    }

    suspend fun getRecipesForMeals(meals: List<DayMeal>): Result<Map<String, Recipe>> =
        runCatching {
            val recipeIds = meals.map { it.recipeId }.distinct().filter { it.isNotBlank() }

            if (recipeIds.isEmpty()) {
                return@runCatching emptyMap()
            }

            try {
                val recipesMap = mutableMapOf<String, Recipe>()

                try {
                    val recipesList = recipeService.getRecipesByIds(recipeIds)

                    recipesMap.putAll(recipesList.associateBy { it.id })

                    val missingIds = recipeIds.filter { id -> !recipesMap.containsKey(id) }
                    if (missingIds.isNotEmpty()) {

                        for (id in missingIds) {
                            try {
                                val recipe = recipeService.getRecipeById(id)
                                recipesMap[id] = recipe
                            } catch (e: Exception) {
                                Log.e(
                                    "RecipeRepository",
                                    "Nie udało się pobrać przepisu $id: ${e.message}",
                                    e
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    for (id in recipeIds) {
                        try {
                            val recipe = recipeService.getRecipeById(id)
                            recipesMap[id] = recipe
                        } catch (e: Exception) {
                            Log.e(
                                "RecipeRepository",
                                "Nie udało się pobrać przepisu $id: ${e.message}",
                                e
                            )
                        }
                    }
                }

                recipesMap
            } catch (e: Exception) {
                Log.e(
                    "RecipeRepository",
                    "Krytyczny błąd podczas pobierania przepisów: ${e.message}",
                    e
                )
                throw AppException.NetworkException("Błąd podczas pobierania przepisów: ${e.localizedMessage}")
            }
        }
}