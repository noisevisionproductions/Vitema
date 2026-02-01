package com.noisevisionsoftware.vitema.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.ConfirmAlertDialog
import com.noisevisionsoftware.vitema.ui.common.CustomTopAppBar
import com.noisevisionsoftware.vitema.ui.common.LoadingOverlay
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import com.noisevisionsoftware.vitema.ui.screens.admin.ErrorMessage
import com.noisevisionsoftware.vitema.ui.screens.profile.invitation.InvitationDialog
import com.noisevisionsoftware.vitema.utils.formatDate

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit,
    onLogoutClick: () -> Unit = {},
) {
    val profileState by viewModel.profileState.collectAsState()
    var showInvitationDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Profil użytkownika",
                onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) })
        }) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = profileState) {
                is ViewModelState.Initial -> Unit
                is ViewModelState.Loading -> LoadingOverlay()
                is ViewModelState.Success -> ProfileScreenPage(
                    state = state,
                    onNavigate = onNavigate,
                    onLogoutClick = onLogoutClick,
                    onEnterCodeClick = { showInvitationDialog = true },
                    onDisconnectClick = { viewModel.disconnectTrainer() }
                )

                is ViewModelState.Error -> ErrorMessage(message = state.message)
            }

            if (showInvitationDialog) {
                InvitationDialog(
                    onDismiss = { showInvitationDialog = false })
            }
        }
    }
}

@Composable
private fun ProfileScreenPage(
    state: ViewModelState.Success<User>,
    onNavigate: (NavigationDestination) -> Unit,
    onLogoutClick: () -> Unit,
    onEnterCodeClick: () -> Unit,
    onDisconnectClick: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDisconnectDialog by remember { mutableStateOf(false) }
    val hasTrainer = !state.data.trainerId.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Column {
                        Text(
                            text = state.data.nickname,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = state.data.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        if (!hasTrainer) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Współpraca trenerska",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Otrzymałeś kod od trenera? Wpisz go tutaj, aby połączyć konta.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onEnterCodeClick, modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.VpnKey,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Wpisz kod zaproszenia")
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Twój Trener",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Twoje konto jest połączone z trenerem.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { showDisconnectDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Text("Zakończ współpracę")
                    }
                }
            }
        }

        ProfileSection(
            title = "Dane osobowe", content = {
                if (state.data.birthDate != null) {
                    ProfileItem(
                        icon = Icons.Default.DateRange,
                        label = "Data urodzenia",
                        value = formatDate(state.data.birthDate)
                    )
                }
                if (state.data.gender != null) {
                    ProfileItem(
                        icon = Icons.Default.Face,
                        label = "Płeć",
                        value = state.data.gender.displayName
                    )
                }
            })

        ProfileSection(
            title = "Informacje o koncie", content = {
                ProfileItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Data dołączenia",
                    value = formatDate(state.data.createdAt)
                )
            })

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Settings) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Default.Settings, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ustawienia")
                }

                FilledTonalButton(
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.EditProfile) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant)
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edytuj dane")
                }
            }
        }

        Button(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Wyloguj się", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }

    if (showDisconnectDialog) {
        ConfirmAlertDialog(
            onConfirm = {
                showDisconnectDialog = false
                onDisconnectClick()
            },
            onDismiss = { showDisconnectDialog = false },
            title = "Zakończyć współpracę?",
            message = "Czy na pewno chcesz odłączyć się od obecnego trenera? Stracisz dostęp do planów przypisanych przez niego.",
            confirmActionText = "Zakończ",
            dismissActionText = "Anuluj",
        )
    }

    if (showLogoutDialog) {
        ConfirmAlertDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            onDismiss = { showLogoutDialog = false },
            title = "Wylogowywanie",
            message = "Czy na pewno chcesz się wylogować?",
            confirmActionText = "Wyloguj",
            dismissActionText = "Anuluj"
        )
    }
}

@Composable
private fun ProfileSection(
    title: String, content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}


@Composable
private fun ProfileItem(
    icon: ImageVector, label: String, value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}