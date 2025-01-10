package com.noisevisionsoftware.szytadieta.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingIndicator
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.admin.ErrorMessage
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.ChangePasswordDialog
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.DeleteAccountDialog
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.SettingsClickableItem
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.SettingsSwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit,
    onLogout: () -> Unit
) {
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val settingsState by viewModel.settingsState.collectAsState()
    val passwordUpdateState by viewModel.passwordUpdateState.collectAsState()

    LaunchedEffect(passwordUpdateState) {
        when (passwordUpdateState) {
            is ViewModelState.Success -> {
                showChangePasswordDialog = false
                viewModel.resetPasswordUpdateState()
            }

            else -> Unit
        }
    }

    LaunchedEffect(settingsState) {
        if (settingsState is ViewModelState.Success &&
            (settingsState as ViewModelState.Success<SettingsViewModel.SettingsData>).data.isAccountDeleted
        ) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Ustawienia",
                onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Profile) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            when (val state = settingsState) {
                ViewModelState.Initial -> Unit
                ViewModelState.Loading -> LoadingIndicator()
                is ViewModelState.Success -> {
                    SettingsContent(
                        settingsData = state.data,
                        onDarkModeChange = viewModel::updateSettings,
                        onChangePasswordClick = { showChangePasswordDialog = true },
                        onDeleteAccountClick = { showDeleteAccountDialog = true },
                        onPrivacyPolicyClick = { onNavigate(NavigationDestination.AuthenticatedDestination.PrivacyPolicy) },
                        onRegulationsClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Regulations) },
                    )
                }

                is ViewModelState.Error -> ErrorMessage(message = state.message)
            }
        }
        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = {
                    showChangePasswordDialog = false
                    viewModel.resetPasswordUpdateState()
                },
                onConfirm = viewModel::updatePassword
            )
        }

        if (showDeleteAccountDialog) {
            DeleteAccountDialog(
                onDismiss = { showDeleteAccountDialog = false },
                onConfirm = {
                    viewModel.deleteAccount()
                    showDeleteAccountDialog = false
                }
            )
        }
    }
}

@Composable
private fun SettingsContent(
    settingsData: SettingsViewModel.SettingsData,
    onDarkModeChange: (Boolean) -> Unit,
    onChangePasswordClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onRegulationsClick: () -> Unit
) {
    SettingsSection(title = "Wygląd") {
        SettingsSwitchItem(
            title = "Tryb ciemny",
            description = "Włącz ciemny motyw aplikacji",
            icon = Icons.Default.DarkMode,
            checked = settingsData.isDarkMode,
            onCheckedChange = onDarkModeChange
        )
    }

    SettingsSection(title = "Bezpieczeństwo") {
        SettingsClickableItem(
            title = "Zmień hasło",
            description = "Zaktualizuj swoje hasło",
            icon = Icons.Default.Lock,
            onClick = onChangePasswordClick
        )

        SettingsClickableItem(
            title = "Usuń konto",
            description = "Trwale usuń swoje konto i wszystkie dane",
            icon = Icons.Default.Delete,
            onClick = onDeleteAccountClick,
            textColor = MaterialTheme.colorScheme.error
        )
    }

    SettingsSection(title = "O aplikacji") {
        SettingsClickableItem(
            title = "Regulamin",
            description = "Zapoznaj się z regulaminem aplikacji",
            icon = Icons.Default.Description,
            onClick = onRegulationsClick
        )

        SettingsClickableItem(
            title = "Polityka prywatności",
            description = "Informacje o przetwarzaniu danych",
            icon = Icons.Default.Security,
            onClick = onPrivacyPolicyClick
        )

        SettingsClickableItem(
            title = "Wersja aplikacji",
            description = settingsData.appVersion,
            icon = Icons.Default.Info,
            onClick = {}
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
