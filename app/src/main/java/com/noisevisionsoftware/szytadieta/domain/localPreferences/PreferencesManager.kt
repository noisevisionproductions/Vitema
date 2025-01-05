package com.noisevisionsoftware.szytadieta.domain.localPreferences

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
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "shopping_list_preferences")
    private val dataStore = context.dataStore

    companion object {
        private const val CHECKED_PRODUCTS_KEY_PREFIX = "checked_products_"
        private const val SEPARATOR = "|"
    }

    suspend fun saveCheckedProducts(userId: String, checkedProducts: Set<String>) {
        val key = stringPreferencesKey("${CHECKED_PRODUCTS_KEY_PREFIX}$userId")
        dataStore.edit { preferences ->
            preferences[key] = checkedProducts.joinToString(SEPARATOR)
        }
    }

    fun getCheckedProducts(userId: String): Flow<Set<String>> {
        val key = stringPreferencesKey("${CHECKED_PRODUCTS_KEY_PREFIX}$userId")
        return dataStore.data.map { preferences ->
            preferences[key]?.split(SEPARATOR)?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        }
    }
}