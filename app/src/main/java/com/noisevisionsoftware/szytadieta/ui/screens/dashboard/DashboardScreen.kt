package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.user.UserRole
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MealPlanCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MeasurementsCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.UserProfileCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.WeightCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.DashboardShoppingListViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.ShoppingListCard
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    shoppingListViewModel: DashboardShoppingListViewModel = hiltViewModel(),
    onAdminPanelClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onBodyMeasurementsClick: () -> Unit = {},
    onProgressClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onMealPlanClick: () -> Unit = {},
    onShoppingListClick: () -> Unit = {}
) {
    val userRole by viewModel.userRole.collectAsState()
    val userData by viewModel.userData.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val latestMeasurements by viewModel.latestMeasurements.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                coroutineScope {
                    launch { viewModel.refreshDashboardData() }
                    launch { shoppingListViewModel.refreshShoppingList() }
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TopBar(
                    onLogoutClick = onLogoutClick,
                    onAdminPanelClick = onAdminPanelClick,
                    onProfileClick = onProfileClick,
                    userState = userRole
                )
            }

            item {
                UserProfileCard(
                    userData = userData,
                    onClick = onProfileClick
                )
            }

            item {
                WeightCard(
                    latestWeight = latestWeight,
                    onClick = onProgressClick
                )
            }
            item {
                MeasurementsCard(
                    latestMeasurements = latestMeasurements,
                    onClick = onBodyMeasurementsClick
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MealPlanCard(
                        modifier = Modifier.weight(1f),
                        todayMeals = todayMeals,
                        onClick = onMealPlanClick
                    )
                    ShoppingListCard(
                        modifier = Modifier.weight(1f),
                        onClick = onShoppingListClick
                    )
                }
            }

            /*  item {
            SettingsCard(onClick = onSettingsClick)
        }*/
        }

        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TopBar(
    onLogoutClick: () -> Unit,
    onAdminPanelClick: () -> Unit,
    onProfileClick: () -> Unit,
    userState: ViewModelState<UserRole>,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Szyta Dieta",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "Twój osobisty plan żywieniowy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(
                    visible = userState is ViewModelState.Success && userState.data == UserRole.ADMIN,
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
                ) {
                    TopBarIconButton(
                        icon = Icons.Default.AdminPanelSettings,
                        contentDescription = "Panel admina",
                        onClick = onAdminPanelClick
                    )
                }

                TopBarIconButton(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Wyloguj",
                    onClick = { showLogoutDialog = true }
                )

                TopBarIconButton(
                    icon = Icons.Default.AccountCircle,
                    contentDescription = "Profil",
                    onClick = onProfileClick
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
            onDismiss = { showLogoutDialog = false }
        )
    }
}

@Composable
private fun TopBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isHovered by remember { mutableStateOf(false) }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .pointerHoverIcon(PointerIcon.Hand)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(40.dp)
                .scale(if (isHovered) 1.1f else 1f),
            tint = MaterialTheme.colorScheme.primary
        )
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