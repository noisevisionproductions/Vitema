package com.noisevisionsoftware.szytadieta

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.noisevisionsoftware.szytadieta.domain.alert.Alert
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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            alertManager.showAlert(Alert.Success("Powiadomienia zostały włączone"))
        } else {
            alertManager.showAlert(
                Alert.Error("Aby otrzymywać powiadomienia, włącz je w ustawieniach systemu")
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission()

        setContent {
            val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = isSystemInDarkTheme())

            FitApplicationTheme(
                darkTheme = isDarkMode
            ) {

                Box(modifier = Modifier.fillMaxSize()) {
                    PatternBackground()

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

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {

                }

                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    alertManager.showAlert(
                        Alert.Error(
                            "Powiadomienia są potrzebne, aby informować Cię o ważnych wydarzeniach"
                        )
                    )
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}