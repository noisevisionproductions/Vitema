package com.noisevisionsoftware.szytadieta.domain.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noisevisionsoftware.szytadieta.domain.model.health.water.CustomWaterAmount
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
        private const val CUSTOM_WATER_AMOUNT = "custom_water_amount"
        private const val CUSTOM_WATER_LABEL = "custom_water_label"
    }

    suspend fun saveCheckedProducts(userId: String, checkedProducts: Set<String>) {
        val key = getCheckedProductsKey(userId)
        dataStore.edit { preferences ->
            preferences[key] = checkedProducts.joinToString(SEPARATOR)
        }
    }

    fun getCheckedProducts(userId: String): Flow<Set<String>> {
        val key = getCheckedProductsKey(userId)
        return dataStore.data.map { preferences ->
            preferences[key]?.split(SEPARATOR)?.filter { it.isNotEmpty() }?.toSet() ?: emptySet()
        }
    }

    suspend fun clearCheckedProducts(userId: String) {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey("${CHECKED_PRODUCTS_KEY_PREFIX}$userId"))
        }
    }

    suspend fun saveCustomWaterAmount(userId: String, amount:  CustomWaterAmount) {
        context.dataStore.edit { preferences ->
            preferences[intPreferencesKey("${userId}_${CUSTOM_WATER_AMOUNT}")] = amount.amount
            preferences[stringPreferencesKey("${userId}_${CUSTOM_WATER_LABEL}")] = amount.label
        }
    }

    fun getCustomWaterAmount(userId: String): Flow<CustomWaterAmount?> {
        return context.dataStore.data.map { preferences ->
            val amount = preferences[intPreferencesKey("${userId}_custom_water_amount")]
            val label = preferences[stringPreferencesKey("${userId}_custom_water_label")]
            if (amount != null && label != null) {
                CustomWaterAmount(amount, label)
            } else {
                null
            }
        }
    }

    suspend fun clearAllUserData(userId: String) {
        val key = getCheckedProductsKey(userId)
        dataStore.edit { preferences ->
            preferences.remove(key)
        }
    }

    private fun getCheckedProductsKey(userId: String): Preferences.Key<String> {
        return stringPreferencesKey("${CHECKED_PRODUCTS_KEY_PREFIX}$userId")
    }
}