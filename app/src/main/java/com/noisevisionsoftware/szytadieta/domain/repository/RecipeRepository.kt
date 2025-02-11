package com.noisevisionsoftware.szytadieta.domain.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DayMeal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Recipe
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.RecipeReference
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RecipeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    private suspend fun getRecipesByDietId(dietId: String): Result<List<Recipe>> = runCatching {
        val referencesSnapshot = firestore.collection("recipe_references")
            .whereEqualTo("dietId", dietId)
            .get()
            .await()

        val recipeIds = referencesSnapshot.documents.mapNotNull {
            it.toObject(RecipeReference::class.java)?.recipeId
        }

        if (recipeIds.isEmpty()) return@runCatching emptyList()

        val recipesSnapshot = firestore.collection("recipes")
            .whereIn("id", recipeIds)
            .get()
            .await()

        recipesSnapshot.documents.mapNotNull { doc ->
            doc.toObject(Recipe::class.java)?.copy(id = doc.id)
        }
    }

    suspend fun getRecipeById(recipeId: String): Result<Recipe> = runCatching {
        val snapshot = firestore.collection("recipes")
            .document(recipeId)
            .get()
            .await()

        snapshot.toObject(Recipe::class.java)?.copy(id = snapshot.id)
            ?: throw Exception("Przepis nie został znaleziony")
    }

    suspend fun addPhotoToRecipe(recipeId: String, photoUri: Uri): Result<String> = runCatching {
        val photoRef = storage.reference
            .child("recipes")
            .child(recipeId)
            .child("${System.currentTimeMillis()}.jpg")

        photoRef.putFile(photoUri).await()

        val downloadUrl = photoRef.downloadUrl.await().toString()

        val recipeRef = firestore.collection("recipes").document(recipeId)
        firestore.runTransaction { transaction ->
            val recipe = transaction.get(recipeRef).toObject(Recipe::class.java)
            val updatedPhotos = recipe?.photos.orEmpty() + downloadUrl
            transaction.update(recipeRef, "photos", updatedPhotos)
        }.await()

        downloadUrl
    }


    suspend fun getRecipesForMeals(meals: List<DayMeal>): Result<Map<String, Recipe>> = runCatching {
        val recipeIds = meals.map { it.recipeId }.distinct()

        if (recipeIds.isEmpty()) return@runCatching emptyMap()

        val snapshot = firestore.collection("recipes")
            .whereIn(FieldPath.documentId(), recipeIds)
            .get()
            .await()

        snapshot.documents.mapNotNull { doc ->
            doc.toObject(Recipe::class.java)?.copy(id = doc.id)
        }.associateBy { it.id }
            .also { recipes ->
                Log.d("RecipeRepository", "Final recipes map: ${recipes.keys}")
            }
    }
}