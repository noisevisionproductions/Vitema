package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onAdminPanelClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBodyMeasurementsClick: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMealPlanClick: () -> Unit = {}
) {
    val userState by viewModel.userRole.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TopBar(
                onLogoutClick = onLogoutClick,
                onAdminPanelClick = onAdminPanelClick,
                onProfileClick = onProfileClick,
                userState = userState
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                item {
                    DashboardCard(
                        title = "Pomiary",
                        icon = Icons.Default.Straighten,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = onBodyMeasurementsClick
                    )
                }
                item {
                    DashboardCard(
                        title = "Waga",
                        icon = Icons.Default.MonitorWeight,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = onProgressClick
                    )
                }
                item {
                    DashboardCard(
                        title = "Plan posiłków",
                        icon = Icons.Default.Restaurant,
                        backgroundColor = MaterialTheme.colorScheme.primary,
                        onClick = onMealPlanClick
                    )
                }
                item {
                    DashboardCard(
                        title = "Lista zakupów",
                        icon = Icons.AutoMirrored.Filled.Note,
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        onClick = {}
                    )
                }
                item {
                    DashboardCard(
                        title = "Ustawienia",
                        icon = Icons.Default.Settings,
                        backgroundColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
fun TopBar(
    onLogoutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onProfileClick: () -> Unit,
    userState: ViewModelState<UserRole>,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "Witaj!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Co dzisiaj zjemy?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            if (userState is ViewModelState.Success && userState.data == UserRole.ADMIN) {
                IconButton(onClick = { onAdminPanelClick() }) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Panel admina",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            IconButton(onClick = { showLogoutDialog = true }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Wyloguj",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { onProfileClick() }) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                onLogoutClick()
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
fun DashboardCard(
    title: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(48.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Wylogowywanie",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Czy na pewno chcesz się wylogować?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Anuluj")
                    }

                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Wyloguj")
                    }
                }
            }
        }
    }
}