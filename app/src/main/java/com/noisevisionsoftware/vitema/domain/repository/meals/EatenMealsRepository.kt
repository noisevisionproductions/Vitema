package com.noisevisionsoftware.vitema.domain.repository.meals

import kotlinx.coroutines.flow.Flow

interface EatenMealsRepository {
    suspend fun saveEatenMeal(userId: String, date: String, mealId: String)
    suspend fun removeEatenMeal(userId: String, date: String, mealId: String)
    suspend fun getEatenMeals(userId: String, date: String): Set<String>
    fun observeEatenMeals(userId: String, date: String): Flow<Set<String>>
    suspend fun syncWithRemote(userId: String, date: String)
}