package com.noisevisionsoftware.szytadieta.data.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.ui.base.AppEvent
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val eventBus: EventBus
) {
    private val dataStore = context.dataStore
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        observeEvents()
    }

    private fun observeEvents() {
        scope.launch {
            eventBus.events.collect { event ->
                when (event) {
                    is AppEvent.UserLoggedOut -> clearSession()
                    else -> Unit
                }
            }
        }
    }

    companion object {
        private val USER_ID_KEY = stringPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val DASHBOARD_SCROLL_INDEX = intPreferencesKey("dashboard_scroll_index")
        private val DASHBOARD_SCROLL_OFFSET = intPreferencesKey("dashboard_scroll_offset")
        private val DASHBOARD_TUTORIAL_SHOWN = booleanPreferencesKey("dashboard_tutorial_shown")
    }

    data class ScrollPosition(
        val index: Int = 0,
        val offset: Int = 0
    )

    suspend fun saveUserSession(user: User) {
        dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
            preferences[USER_EMAIL_KEY] = user.email
        }
    }

    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
            preferences[DASHBOARD_SCROLL_INDEX] = 0
            preferences[DASHBOARD_SCROLL_OFFSET] = 0
        }
    }

    suspend fun saveDashboardScrollPosition(index: Int, offset: Int) {
        dataStore.edit { preferences ->
            preferences[DASHBOARD_SCROLL_INDEX] = index
            preferences[DASHBOARD_SCROLL_OFFSET] = offset
        }
    }

    fun getDashboardScrollPosition(): Flow<ScrollPosition> {
        return dataStore.data.map { preferences ->
            ScrollPosition(
                index = preferences[DASHBOARD_SCROLL_INDEX] ?: 0,
                offset = preferences[DASHBOARD_SCROLL_OFFSET] ?: 0
            )
        }
    }

    val isDashboardTutorialShown: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DASHBOARD_TUTORIAL_SHOWN] ?: false
    }

    suspend fun markDashboardTutorialAsShown() {
        dataStore.edit { preferences ->
            preferences[DASHBOARD_TUTORIAL_SHOWN] = true
        }
    }

    val userSessionFlow: Flow<User?> = dataStore.data.map { preferences ->
        if (preferences[USER_ID_KEY] != null) {
            User(
                id = preferences[USER_ID_KEY] ?: "",
                email = preferences[USER_EMAIL_KEY] ?: ""
            )
        } else {
            null
        }
    }
}