package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import android.icu.util.Calendar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.WeekDay
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.DayHeader
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.DayMeals
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.NoMealPlanMessage
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.WeekSelector
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val mealPlanState by viewModel.mealPlanState.collectAsState()
    val hasAnyMealPlans by viewModel.hasAnyMealPlans.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Plan posiłków",
            onBackClick = onBackClick,
            showRefreshIcon = true,
            onRefreshClick = { viewModel.refreshMealPlan() }
        )

        WeekSelector(
            currentDate = currentDate,
            onDateSelected = { newDate ->
                viewModel.setCurrentDate(newDate)
                viewModel.loadMealPlan(newDate)
            }
        )

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
                        } else null
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

@Composable
private fun WeeklyPlanContent(
    weeklyPlan: List<DayPlan>,
    currentDate: Long
) {
    val calendar = Calendar.getInstance().apply { timeInMillis = currentDate }
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(weeklyPlan) { dayPlan ->
            calendar.set(Calendar.DAY_OF_WEEK, dayPlan.dayOfWeek.toCalendarDay())
            val dayDate = dateFormatter.format(calendar.time)

            DayPlayCard(
                dayPlan = dayPlan,
                date = dayDate
            )
        }
    }
}

@Composable
private fun DayPlayCard(
    dayPlan: DayPlan,
    date: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
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
            DayHeader(
                dayPlan = dayPlan,
                date = date,
                expanded = expanded,
                onExpandClick = { expanded = !expanded }
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(
                    expandFrom = Alignment.Top,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(
                    initialAlpha = 0.3f,
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Top,
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 300)
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    DayMeals(dayPlan.meals)
                }
            }
        }
    }
}


fun WeekDay.toCalendarDay(): Int = when (this) {
    WeekDay.MONDAY -> Calendar.MONDAY
    WeekDay.TUESDAY -> Calendar.TUESDAY
    WeekDay.WEDNESDAY -> Calendar.WEDNESDAY
    WeekDay.THURSDAY -> Calendar.THURSDAY
    WeekDay.FRIDAY -> Calendar.FRIDAY
    WeekDay.SATURDAY -> Calendar.SATURDAY
    WeekDay.SUNDAY -> Calendar.SUNDAY
}