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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.ChangePasswordDialog
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.DeleteAccountDialog
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.SettingsClickableItem
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.SettingsSwitchItem
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.PasswordUpdateState
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.SettingsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    val settingsState by viewModel.settingsState.collectAsState()
    val passwordUpdateState by viewModel.passwordUpdateState.collectAsState()

    LaunchedEffect(passwordUpdateState) {
        if (passwordUpdateState is PasswordUpdateState.Success) {
            showChangePasswordDialog = false
            viewModel.resetPasswordUpdateState()
        }
    }

    LaunchedEffect(settingsState) {
        if (settingsState is SettingsState.Success &&
            (settingsState as SettingsState.Success).isAccountDeleted
        ) {
            onLogout()
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Ustawienia",
                onBackClick = onBackClick
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
                is SettingsState.Success -> {
                    SettingsSection(title = "Wygląd") {
                        SettingsSwitchItem(
                            title = "Tryb ciemny",
                            description = "Włącz ciemny motyw aplikacji",
                            icon = Icons.Default.DarkMode,
                            checked = state.isDarkMode,
                            onCheckedChange = { viewModel.updateDarkMode(it) }
                        )
                    }

                    SettingsSection(
                        title = "Bezpieczeństwo"
                    ) {
                        SettingsClickableItem(
                            title = "Zmień hasło",
                            description = "Zaktualizuj swoje hasło",
                            icon = Icons.Default.Lock,
                            onClick = { showChangePasswordDialog = true }
                        )

                        SettingsClickableItem(
                            title = "Usuń konto",
                            description = "Trwale ussuń swoje konto i wszystkie dane",
                            icon = Icons.Default.Delete,
                            onClick = { showDeleteAccountDialog = true },
                            textColor = MaterialTheme.colorScheme.error
                        )
                    }

                    SettingsSection(title = "O aplikacji") {
                        SettingsClickableItem(
                            title = "Regulamin",
                            description = "Zapoznaj się z regulaminem aplikacji",
                            icon = Icons.Default.Description,
                            onClick = {}
                        )

                        SettingsClickableItem(
                            title = "Polityka prywatności",
                            description = "Informacje o przetwarzaniu danych",
                            icon = Icons.Default.Security,
                            onClick = {}
                        )

                        Text(
                            text = "Wersja 1.0.0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                is SettingsState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                }

                is SettingsState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                else -> Unit
            }
        }

        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                onDismiss = {
                    showChangePasswordDialog = false
                    viewModel.resetPasswordUpdateState()
                },
                onConfirm = { oldPassword, newPassword ->
                    viewModel.updatePassword(oldPassword, newPassword)
                }
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
            )
        ) {
            content()
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
