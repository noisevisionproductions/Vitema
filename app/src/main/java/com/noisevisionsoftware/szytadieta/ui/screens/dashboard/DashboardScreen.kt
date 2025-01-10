package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.TopBar
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MealPlanCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MeasurementsCard
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
    onNavigate: (NavigationDestination) -> Unit = {}
) {
    val userRole by viewModel.userRole.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val latestMeasurements by viewModel.latestMeasurements.collectAsState()
    val measurementsHistory by viewModel.measurementsHistory.collectAsState()
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
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                TopBar(
                    onAdminPanelClick = {
                        viewModel.checkAdminAccess { hasAccess ->
                            if (hasAccess) {
                                onNavigate(NavigationDestination.AuthenticatedDestination.AdminPanel)
                            } else {
                                viewModel.showError("Brak uprawnień administratora")
                            }
                        }
                    },
                    userState = userRole
                )
            }

            item {
                MealPlanCard(
                    todayMeals = todayMeals,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.MealPlan) }
                )
            }

            item {
                ShoppingListCard(
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.ShoppingList) }
                )
            }

            item {
                WeightCard(
                    latestWeight = latestWeight,
                    weightHistory = weightHistory,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Weight) }
                )
            }

            item {
                MeasurementsCard(
                    latestMeasurements = latestMeasurements,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.BodyMeasurements) },
                    measurementsHistory = measurementsHistory
                )
            }
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