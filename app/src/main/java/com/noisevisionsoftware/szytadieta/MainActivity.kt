package com.noisevisionsoftware.szytadieta

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.noisevisionsoftware.szytadieta.domain.alert.Alert
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.navigation.NavigationManager
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationHelper
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationScheduler
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationType
import com.noisevisionsoftware.szytadieta.ui.common.AlertHandler
import com.noisevisionsoftware.szytadieta.ui.common.appVersion.AppVersionViewModel
import com.noisevisionsoftware.szytadieta.ui.common.appVersion.UpdateDialog
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.MainScreen
import com.noisevisionsoftware.szytadieta.ui.theme.FitApplicationTheme
import com.noisevisionsoftware.szytadieta.ui.theme.PatternBackground
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var alertManager: AlertManager

    @Inject
    lateinit var settingsManager: SettingsManager

    @Inject
    lateinit var navigationManager: NavigationManager

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val appVersionViewModel: AppVersionViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        ::handlePermissionResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            appVersionViewModel.checkAppVersion()
            checkNotificationPermission()
        }

        setContent {
            MainScreenContent()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationNavigation(intent)
    }

    @Composable
    private fun MainScreenContent() {
        val isDarkMode by settingsManager.isDarkMode.collectAsState(initial = isSystemInDarkTheme())
        val destination = intent.getStringExtra("destination")
        val dietId = intent.getStringExtra("diet_id")
        val updateDialogState by appVersionViewModel.updateDialogState.collectAsState()

        LaunchedEffect(destination) {
            if (destination == NotificationHelper.EXTRA_DESTINATION) {
                navigationManager.navigateToScreen(NavigationDestination.AuthenticatedDestination.WaterIntake)
            }
            if (destination == "diet_plan" && dietId != null) {
                navigationManager.navigateToScreen(
                    NavigationDestination.AuthenticatedDestination.MealPlan
                )
            }
        }

        when (val state = updateDialogState) {
            is AppVersionViewModel.UpdateDialogState.Visible -> {
                UpdateDialog(
                    currentVersion = state.currentVersion,
                    requiredVersion = state.requiredVersion,
                    updateMessage = state.updateMessage,
                    isForceUpdate = state.isForceUpdate,
                    onUpdate = { appVersionViewModel.openPlayStore() },
                    onDismiss = {
                        if (!state.isForceUpdate) {
                            appVersionViewModel.dismissUpdateDialog()
                        }
                    }
                )
            }
            else -> Unit
        }

        FitApplicationTheme(darkTheme = isDarkMode) {
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

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            alertManager.showAlert(Alert.Success("Powiadomienia zostały włączone"))
            scheduleNotifications()
        } else {
            alertManager.showAlert(
                Alert.Error("Aby otrzymywać powiadomienia, włącz je w ustawieniach systemu")
            )
        }
    }

    private fun handleNotificationNavigation(intent: Intent?) {
        when (intent?.getStringExtra("notification_type")) {
            NotificationType.WATER_REMINDER.name -> {
                lifecycleScope.launch {
                    navigationManager.navigateToScreen(
                        NavigationDestination.AuthenticatedDestination.WaterIntake
                    )
                }
            }

            NotificationType.DIET_UPDATE.name -> {
                val dietId = intent.getStringExtra("diet_id")
                if (dietId != null) {
                    lifecycleScope.launch {
                        navigationManager.navigateToScreen(
                            NavigationDestination.AuthenticatedDestination.MealPlan
                        )
                    }
                }
            }
        }
    }

    private fun scheduleNotifications() {
        lifecycleScope.launch {
            try {
                notificationScheduler.scheduleWaterReminder()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error scheduling notifications", e)
                alertManager.showAlert(
                    Alert.Error("Wystąpił błąd podczas planowania powiadomień")
                )
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
                    scheduleNotifications()
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
        } else {
            scheduleNotifications()
        }
    }
}