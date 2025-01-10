package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.icu.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.MealType
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.WeekDay
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.DaySelectorForMealPlan
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.NoMealPlanMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val mealPlanState by viewModel.mealPlanState.collectAsState()
    val hasAnyMealPlans by viewModel.hasAnyMealPlans.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val availableWeeks by viewModel.availableWeeks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Plan posiłków",
            onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
            showRefreshIcon = true,
            onRefreshClick = { viewModel.refreshMealPlan() }
        )

        if (hasAnyMealPlans == true && mealPlanState !is ViewModelState.Loading) {
            DaySelectorForMealPlan(
                currentDate = currentDate,
                onDateSelected = { newDate ->
                    viewModel.setCurrentDate(newDate)
                },
                availableWeeks = availableWeeks
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (mealPlanState) {
                is ViewModelState.Initial -> Unit
                is ViewModelState.Loading -> LoadingOverlay()
                is ViewModelState.Error -> CustomErrorMessage(
                    message = (mealPlanState as ViewModelState.Error).message
                )

                is ViewModelState.Success -> {
                    val weeklyPlan = (mealPlanState as ViewModelState.Success<List<DayPlan>>).data
                    if (weeklyPlan.isEmpty()) {
                        NoMealPlanMessage(
                            hasAnyMealPlans = hasAnyMealPlans ?: false,
                            onNavigateToAvailableWeek = if (hasAnyMealPlans == true) {
                                { viewModel.navigateToClosestAvailableWeek() }
                            } else null,
                            onNavigate = onNavigate
                        )
                    } else {
                        WeeklyPlanContent(
                            weeklyPlan = weeklyPlan,
                            currentDate = currentDate
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyPlanContent(
    weeklyPlan: List<DayPlan>,
    currentDate: Long
) {
    val selectedDayPlan = weeklyPlan.find { dayPlan ->
        val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> WeekDay.MONDAY
            Calendar.TUESDAY -> WeekDay.TUESDAY
            Calendar.WEDNESDAY -> WeekDay.WEDNESDAY
            Calendar.THURSDAY -> WeekDay.THURSDAY
            Calendar.FRIDAY -> WeekDay.FRIDAY
            Calendar.SATURDAY -> WeekDay.SATURDAY
            Calendar.SUNDAY -> WeekDay.SUNDAY
            else -> WeekDay.MONDAY
        }
        dayPlan.dayOfWeek == dayOfWeek
    }

    selectedDayPlan?.let { dayPlan ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dayPlan.meals.sortedBy { it.name.ordinal }) { meal ->
                MealCard(meal)
            }
        }
    }
}

@Composable
private fun MealCard(
    meal: Meal
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (meal.name) {
                        MealType.BREAKFAST -> Icons.Default.WbSunny
                        MealType.SECOND_BREAKFAST -> Icons.Default.BrunchDining
                        MealType.LUNCH -> Icons.Default.LunchDining
                        MealType.SNACK -> Icons.Default.Restaurant
                        MealType.DINNER -> Icons.Default.DinnerDining
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = meal.name.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}