package com.noisevisionsoftware.szytadieta.data.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalEatenMealsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.eatenMealsDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "eaten_meals"
    )

    private val dataStore = context.eatenMealsDataStore

    suspend fun saveEatenMeal(userId: String, date: String, mealId: String) {
        dataStore.edit { preferences ->
            val key = buildKey(userId, date)
            val prefsKey = stringPreferencesKey(key)
            val currentMeals = preferences[prefsKey]
                ?.split(",")
                ?.toMutableSet() ?: mutableSetOf()

            currentMeals.add(mealId)
            preferences[prefsKey] = currentMeals.joinToString(",")
        }
    }

    suspend fun removeEatenMeal(userId: String, date: String, mealId: String) {
        dataStore.edit { preferences ->
            val key = buildKey(userId, date)
            val prefsKey = stringPreferencesKey(key)
            val currentMeals = preferences[prefsKey]
                ?.split(",")
                ?.toMutableSet() ?: mutableSetOf()

            currentMeals.remove(mealId)
            preferences[prefsKey] = currentMeals.joinToString(",")
        }
    }

    fun observeEatenMeals(userId: String, date: String): Flow<Set<String>> {
        return dataStore.data.map { preferences ->
            val key = buildKey(userId, date)
            val prefsKey = stringPreferencesKey(key)
            preferences[prefsKey]
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toSet() ?: emptySet()
        }
    }

    private fun buildKey(userId: String, date: String) = "${userId}_${date}_eaten_meals"
}