package com.noisevisionsoftware.szytadieta.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dashboard.DashboardCardType
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.TopBar
import com.noisevisionsoftware.szytadieta.ui.screens.admin.ErrorMessage
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.AnimatedSpotlightOverlay
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.DietGuideCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.DraggableCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MealPlanCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.MeasurementsCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.TutorialTooltip
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.WaterTrackingCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.WeightCard
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.DashboardShoppingListViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components.shoppingListCard.ShoppingListCard
import com.noisevisionsoftware.szytadieta.ui.screens.settings.components.dashboard.DashboardSettingsViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.WaterTrackingViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    shoppingListViewModel: DashboardShoppingListViewModel = hiltViewModel(),
    waterTrackingViewModel: WaterTrackingViewModel = hiltViewModel(),
    dashboardSettingsViewModel: DashboardSettingsViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit = {}
) {
    val userRole by viewModel.userRole.collectAsState()
    val latestWeight by viewModel.latestWeight.collectAsState()
    val weightHistory by viewModel.weightHistory.collectAsState()
    val latestMeasurements by viewModel.latestMeasurements.collectAsState()
    val measurementsHistory by viewModel.measurementsHistory.collectAsState()
    val todayMeals by viewModel.todayMeals.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    var editButtonBounds by remember { mutableStateOf(Rect.Zero) }
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
        else -> 2000
    }

    val dashboardConfig by dashboardSettingsViewModel.dashboardConfig.collectAsState()
    var isEditMode by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshDashboardData()
        shoppingListViewModel.refreshShoppingList()
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
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(
                16.dp
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
                    userState = userRole,
                    onEditButtonPositionChanged = { rect ->
                        editButtonBounds = rect
                    },
                    actions = {
                        IconButton(onClick = { isEditMode = !isEditMode }) {
                            Icon(
                                imageVector = if (isEditMode)
                                    Icons.Default.Done else Icons.Default.DragIndicator,
                                contentDescription = if (isEditMode)
                                    "Zakończ edycję" else "Edytuj układ"
                            )
                        }
                    }
                )
            }

            when (dashboardConfig) {
                is ViewModelState.Success -> {
                    val config = (dashboardConfig as ViewModelState.Success).data
                    config.cardOrder.forEachIndexed { index, cardType ->
                        if (cardType !in config.hiddenCards) {
                            item {
                                DraggableCard(
                                    index = index,
                                    isEditMode = isEditMode,
                                    onMove = { from, to ->
                                        dashboardSettingsViewModel.reorderCards(from, to)
                                    }
                                ) {
                                    when (cardType) {
                                        DashboardCardType.MEAL_PLAN -> MealPlanCard(
                                            todayMeals = todayMeals,
                                            onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.MealPlan) },
                                            recipes = recipes
                                        )

                                        DashboardCardType.SHOPPING_LIST -> ShoppingListCard(
                                            onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.ShoppingList) }
                                        )

                                        DashboardCardType.WATER_TRACKING -> WaterTrackingCard(
                                            currentAmount = totalWaterIntake,
                                            targetAmount = dailyTarget,
                                            isLoading = waterIntakeIsLoading,
                                            customAmount = (waterTrackingViewModel.customAmount.collectAsState().value),
                                            onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.WaterIntake) },
                                            onAddWater = { waterTrackingViewModel.addWaterIntake(250) }
                                        )

                                        DashboardCardType.MEASUREMENTS -> MeasurementsCard(
                                            latestMeasurements = latestMeasurements,
                                            measurementsHistory = measurementsHistory,
                                            onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.BodyMeasurements) }
                                        )

                                        DashboardCardType.WEIGHT -> WeightCard(
                                            latestWeight = latestWeight,
                                            weightHistory = weightHistory,
                                            onClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Weight) }
                                        )

                                        DashboardCardType.DIET_GUIDE -> DietGuideCard(
                                            onClick = {onNavigate(NavigationDestination.AuthenticatedDestination.DietGuide)}
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is ViewModelState.Loading -> {
                    item {
                        LoadingOverlay()
                    }
                }

                is ViewModelState.Error -> {
                    item {
                        ErrorMessage(
                            message = (dashboardConfig as ViewModelState.Error).message
                        )
                    }
                }

                else -> Unit
            }
        }

        AnimatedSpotlightOverlay(
            visible = showTutorial,
            targetBounds = editButtonBounds,
            onDismiss = { viewModel.dismissTutorial() }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                TutorialTooltip(
                    visible = showTutorial,
                    onDismiss = { viewModel.dismissTutorial() },
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .padding(
                            top = with(LocalDensity.current) {
                                editButtonBounds.bottom.toDp() + 16.dp
                            }
                        )
                        .align(Alignment.TopCenter)
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