package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.TopBar
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MealPlanCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MeasurementsCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.WaterTrackingCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.WeightCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.DashboardShoppingListViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.ShoppingListCard
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.WaterTrackingViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class, FlowPreview::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    shoppingListViewModel: DashboardShoppingListViewModel = hiltViewModel(),
    waterTrackingViewModel: WaterTrackingViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit = {}
) {
    val userRole by viewModel.userRole.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val latestMeasurements by viewModel.latestMeasurements.collectAsState()
    val measurementsHistory by viewModel.measurementsHistory.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val scrollPosition by viewModel.scrollPosition.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val waterIntakeState by waterTrackingViewModel.waterIntakeState.collectAsState()
    val userSettings by waterTrackingViewModel.userSettings.collectAsState()
    val waterIntakeIsLoading = waterIntakeState is ViewModelState.Loading

    val totalWaterIntake = when (waterIntakeState) {
        is ViewModelState.Success -> (waterIntakeState as ViewModelState.Success).data.sumOf { it.amount }
        else -> 0
    }

    val dailyTarget = when (userSettings) {
        is ViewModelState.Success -> (userSettings as ViewModelState.Success).data.waterDailyTarget
        else -> 1500
    }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboardData()
        shoppingListViewModel.refreshShoppingList()
    }

    LaunchedEffect(Unit) {
        if (scrollPosition.index > 0 || scrollPosition.offset > 0) {
            listState.scrollToItem(
                index = scrollPosition.index,
                scrollOffset = scrollPosition.offset
            )
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }
            .debounce(300)
            .collect { (index, offset) ->
                if (index > 0 || offset > 0) {
                    viewModel.saveScrollPosition(index, offset)
                }
            }
    }

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
            state = listState,
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 24.dp,
                bottom = 24.dp
            )
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
                WaterTrackingCard(
                    currentAmount = totalWaterIntake,
                    targetAmount = dailyTarget,
                    isLoading = waterIntakeIsLoading,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.WaterIntake) },
                    onAddWater = { waterTrackingViewModel.addWaterIntake(250) }
                )
            }

            item {
                MeasurementsCard(
                    latestMeasurements = latestMeasurements,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.BodyMeasurements) },
                    measurementsHistory = measurementsHistory
                )
            }

            item {
                WeightCard(
                    latestWeight = latestWeight,
                    weightHistory = weightHistory,
                    onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Weight) }
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