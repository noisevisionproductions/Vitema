package com.noisevisionsoftware.szytadieta.ui.screens.waterIntake

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.common.animation.CelebrationEffect
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.AddWaterIntakeDialog
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.DailyProgressCard
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.DaySelectorWater
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.SetWaterTargetDialog
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.WaterIntakeActions
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components.WaterIntakeHistory

@Composable
fun WaterTrackingScreen(
    viewModel: WaterTrackingViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val waterIntakeState by viewModel.waterIntakeState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showCelebration by remember { mutableStateOf(false) }

    val dailyTarget = when (userSettings) {
        is ViewModelState.Success -> (userSettings as ViewModelState.Success).data.waterDailyTarget
        else -> 2000
    }
    var prevTotalAmount by remember { mutableIntStateOf(0) }

    LaunchedEffect(waterIntakeState) {
        if (waterIntakeState is ViewModelState.Success) {
            val currentTotal = (waterIntakeState as ViewModelState.Success).data.sumOf { it.amount }
            if (dailyTarget in (prevTotalAmount + 1)..currentTotal) {
                showCelebration = true
            }
            prevTotalAmount = currentTotal
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CustomTopAppBar(
                title = "Śledzenie wody",
                onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
                showRefreshIcon = true,
                onRefreshClick = { viewModel.loadWaterIntakes() }
            )

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DaySelectorWater(
                            currentDate = selectedDate,
                            onDateSelected = { viewModel.updateSelectedDate(it) }
                        )
                    }

                    when (waterIntakeState) {
                        is ViewModelState.Loading -> {
                            item { LoadingOverlay() }
                        }

                        is ViewModelState.Success -> {
                            val intakes = (waterIntakeState as ViewModelState.Success).data
                            val totalAmount = intakes.sumOf { it.amount }

                            item {
                                DailyProgressCard(
                                    currentAmount = totalAmount,
                                    targetAmount = dailyTarget,
                                    onTargetClick = { showTargetDialog = true }
                                )
                            }

                            item {
                                WaterIntakeActions(
                                    onAddClick = { showAddDialog = true }
                                )
                            }

                            item {
                                WaterIntakeHistory(
                                    intakes = intakes
                                )
                            }
                        }

                        is ViewModelState.Error -> {
                            item {
                                Text(
                                    text = (waterIntakeState as ViewModelState.Error).message,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }
        }

        CelebrationEffect(
            show = showCelebration,
            onAnimationEnd = { showCelebration = false }
        )

        if (showAddDialog) {
            AddWaterIntakeDialog(
                onDismiss = { showAddDialog = false },
                onAmountSelected = { amount ->
                    viewModel.addWaterIntake(amount)
                    showAddDialog = false
                }
            )
        }

        if (showTargetDialog) {
            SetWaterTargetDialog(
                currentTarget = dailyTarget,
                onDismiss = { showTargetDialog = false },
                onConfirm = { newTarget ->
                    viewModel.updateDailyTarget(newTarget)
                    showTargetDialog = false
                }
            )
        }
    }
}
