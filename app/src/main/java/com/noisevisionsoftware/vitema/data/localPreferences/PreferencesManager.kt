package com.noisevisionsoftware.vitema.data.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noisevisionsoftware.vitema.domain.model.health.water.CustomWaterAmount
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
        private const val CUSTOM_WATER_AMOUNT = "custom_water_amount"
        private const val CUSTOM_WATER_LABEL = "custom_water_label"
        private const val VERSION_CHECK_ENABLED = "version_check_enabled"
        private const val SEPARATOR = "|"
        private const val INVITATION_PROMPT_SHOWN_PREFIX = "invitation_prompt_shown_"
    }

    // Version Check Preferences
    val isVersionCheckEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[booleanPreferencesKey(VERSION_CHECK_ENABLED)] ?: true
        }

    suspend fun setVersionCheckEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(VERSION_CHECK_ENABLED)] = enabled
        }
    }

    // Shopping List Preferences
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
            preferences.remove(getCheckedProductsKey(userId))
        }
    }

    // Water Amount Preferences
    suspend fun saveCustomWaterAmount(userId: String, amount: CustomWaterAmount) {
        dataStore.edit { preferences ->
            preferences[getWaterAmountKey(userId)] = amount.amount
            preferences[getWaterLabelKey(userId)] = amount.label
        }
    }

    fun getCustomWaterAmount(userId: String): Flow<CustomWaterAmount?> {
        return dataStore.data.map { preferences ->
            val amount = preferences[getWaterAmountKey(userId)]
            val label = preferences[getWaterLabelKey(userId)]
            if (amount != null && label != null) {
                CustomWaterAmount(amount, label)
            } else {
                null
            }
        }
    }

    fun getInvitationPromptShown(userId: String): Flow<Boolean> {
        val key = getInvitationPromptShownKey(userId)
        return dataStore.data.map { preferences ->
            preferences[key] ?: false
        }
    }

    suspend fun setInvitationPromptShown(userId: String, shown: Boolean) {
        val key = getInvitationPromptShownKey(userId)
        dataStore.edit { preferences ->
            preferences[key] = shown
        }
    }

    // Data Management
    suspend fun clearAllUserData(userId: String) {
        dataStore.edit { preferences ->
            preferences.remove(getCheckedProductsKey(userId))
            preferences.remove(getWaterAmountKey(userId))
            preferences.remove(getWaterLabelKey(userId))
            preferences.remove(getInvitationPromptShownKey(userId))
        }
    }

    // Private Key Generators
    private fun getCheckedProductsKey(userId: String): Preferences.Key<String> =
        stringPreferencesKey("$CHECKED_PRODUCTS_KEY_PREFIX$userId")

    private fun getWaterAmountKey(userId: String): Preferences.Key<Int> =
        intPreferencesKey("${userId}_$CUSTOM_WATER_AMOUNT")

    private fun getWaterLabelKey(userId: String): Preferences.Key<String> =
        stringPreferencesKey("${userId}_$CUSTOM_WATER_LABEL")

    private fun getInvitationPromptShownKey(userId: String): Preferences.Key<Boolean> =
        booleanPreferencesKey("$INVITATION_PROMPT_SHOWN_PREFIX$userId")
}