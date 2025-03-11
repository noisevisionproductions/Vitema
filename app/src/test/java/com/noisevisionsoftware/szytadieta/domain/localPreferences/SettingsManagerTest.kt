package com.noisevisionsoftware.szytadieta.domain.localPreferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.noisevisionsoftware.szytadieta.MainDispatcherRule
import com.noisevisionsoftware.szytadieta.data.localPreferences.SettingsManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SettingsManagerTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testCoroutineScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var settingsManager: SettingsManager

    @Before
    fun setUp() {
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testCoroutineScope,
            produceFile = {testContext.preferencesDataStoreFile("test_settings")}
        )
        settingsManager = SettingsManager(testContext)
    }

    @After
    fun cleanUp(){
        testCoroutineScope.cancel()
        File(testContext.filesDir, "datastore/test_settings.preferences_pb").delete()
    }

    @Test
    fun isDarkMode_shouldReturnFalseByDefault() = runTest {
        val isDarkMode = settingsManager.isDarkMode.first()
        assertThat(isDarkMode).isFalse()
    }

    @Test
    fun setDarkMode_shouldUpdateDarkModeValue() = runTest {
        settingsManager.setDarkMode(true)
        val isDarkMode = settingsManager.isDarkMode.first()
        assertThat(isDarkMode).isTrue()
    }

    @Test
    fun setDarkMode_shouldOverwritePreviousValue() = runTest {
        settingsManager.setDarkMode(true)
        assertThat(settingsManager.isDarkMode.first()).isTrue()

        settingsManager.setDarkMode(false)
        assertThat(settingsManager.isDarkMode.first()).isFalse()
    }

    @Test
    fun clearSettings_shouldResetToDefaultValues() = runTest {
        settingsManager.setDarkMode(true)
        assertThat(settingsManager.isDarkMode.first()).isTrue()

        settingsManager.clearSettings()

        assertThat(settingsManager.isDarkMode.first()).isFalse()
    }

    @Test
    fun isDarkMode_shouldPersistValueAfterReCreatingManager() = runTest {
        settingsManager.setDarkMode(true)

        val newSettingsManager = SettingsManager(testContext)

        assertThat(newSettingsManager.isDarkMode.first()).isTrue()
    }
}