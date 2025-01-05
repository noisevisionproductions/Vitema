package com.noisevisionsoftware.szytadieta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.ui.common.AlertHandler
import com.noisevisionsoftware.szytadieta.ui.screens.MainScreen
import com.noisevisionsoftware.szytadieta.ui.theme.FitApplicationTheme
import com.noisevisionsoftware.szytadieta.ui.theme.PatternBackground
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var alertManager: AlertManager

    @Inject
    lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = isSystemInDarkTheme())

            FitApplicationTheme(
                darkTheme = isDarkMode
            ) {

                Box(modifier = Modifier.fillMaxSize()) {
                    PatternBackground(
                        patternColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        MainScreen()
                    }

                    AlertHandler(
                        alertManager = alertManager,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}