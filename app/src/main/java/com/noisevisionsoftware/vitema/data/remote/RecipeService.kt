package com.noisevisionsoftware.vitema.data.remote

import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.Recipe
import okhttp3.MultipartBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RecipeService {
    @GET("/api/recipes/{id}")
    suspend fun getRecipeById(@Path("id") recipeId: String): Recipe

    @GET("/api/recipes/batch")
    suspend fun getRecipesByIds(@Query("ids") recipeIds: List<String>): List<Recipe>

    @Multipart
    @POST("/api/recipes/{id}/image")
    suspend fun uploadRecipeImage(
        @Path("id") recipeId: String,
        @Part image: MultipartBody.Part
    ): RecipeImageResponse
}