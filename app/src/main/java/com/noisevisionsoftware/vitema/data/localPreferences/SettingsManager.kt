package com.noisevisionsoftware.vitema.data.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noisevisionsoftware.vitema.domain.model.dashboard.DashboardCardType
import com.noisevisionsoftware.vitema.domain.model.dashboard.DashboardConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    companion object {
        private val IS_DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        private val CARD_ORDER_KEY = stringPreferencesKey("dashboard_card_order")
        private val HIDDEN_CARDS_KEY = stringPreferencesKey("dashboard_hidden_cards")
        private val WATER_NOTIFICATIONS_ENABLED_KEY =
            booleanPreferencesKey("water_notifications_enabled")
    }

    val isDarkMode: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[IS_DARK_MODE_KEY] != false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_DARK_MODE_KEY] = enabled
        }
    }

    val dashboardConfig: Flow<DashboardConfig> = dataStore.data.map { preferences ->
        try {
            val defaultOrder = DashboardCardType.entries.map { it.name }
            val savedOrder = preferences[CARD_ORDER_KEY]?.split(",") ?: defaultOrder
            val savedHidden =
                preferences[HIDDEN_CARDS_KEY]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()

            val validOrder = savedOrder.mapNotNull { name ->
                try {
                    DashboardCardType.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }

            val validHidden = savedHidden.mapNotNull { name ->
                try {
                    DashboardCardType.valueOf(name)
                } catch (_: IllegalArgumentException) {
                    null
                }
            }.toSet()

            val missingCards = DashboardCardType.entries.filter { it !in validOrder }
            val finalOrder = validOrder + missingCards

            DashboardConfig(
                cardOrder = finalOrder,
                hiddenCards = validHidden
            )
        } catch (_: Exception) {
            DashboardConfig(
                cardOrder = DashboardCardType.entries,
                hiddenCards = emptySet()
            )
        }
    }

    suspend fun updateConfig(config: DashboardConfig) {
        dataStore.edit { preferences ->
            preferences[CARD_ORDER_KEY] = config.cardOrder.joinToString(",") { it.name }
            preferences[HIDDEN_CARDS_KEY] = config.hiddenCards.joinToString(",") { it.name }
        }
    }

    val waterNotificationsEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[WATER_NOTIFICATIONS_ENABLED_KEY] != false
    }

    suspend fun setWaterNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[WATER_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun clearSettings() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}