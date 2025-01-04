package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.MealType
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.WeekDay
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.EmptyMealPlanMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val mealPlanState by viewModel.mealPlanState.collectAsState()

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

        when (mealPlanState) {
            is ViewModelState.Initial -> Unit
            is ViewModelState.Loading -> LoadingOverlay()
            is ViewModelState.Error -> ErrorMessage(
                message = (mealPlanState as ViewModelState.Error).message
            )

            is ViewModelState.Success -> {
                val weeklyPlan = (mealPlanState as ViewModelState.Success<List<DayPlan>>).data
                if (weeklyPlan.isEmpty()) {
                    EmptyMealPlanMessage()
                } else {
                    WeeklyPlanContent(weeklyPlan)
                }
            }
        }
    }
}

@Composable
private fun WeeklyPlanContent(weeklyPlan: List<DayPlan>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(weeklyPlan) { dayPlan ->
            DayPlayCard(dayPlan)
        }
    }

}

@Composable
private fun DayPlayCard(dayPlan: DayPlan) {
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (dayPlan.dayOfWeek) {
                        WeekDay.MONDAY -> "Poniedziałek"
                        WeekDay.TUESDAY -> "Wtorek"
                        WeekDay.WEDNESDAY -> "Środa"
                        WeekDay.THURSDAY -> "Czwartek"
                        WeekDay.FRIDAY -> "Piątek"
                        WeekDay.SATURDAY -> "Sobota"
                        WeekDay.SUNDAY -> "Niedziela"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Icon(
                    imageVector = if (expanded)
                        Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded)
                        "Zwiń" else "Rozwiń",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.rotate(
                        animateFloatAsState(
                            targetValue = if (expanded) 180f else 0f,
                            label = "Rotate Arrow"
                        ).value
                    )
                )
            }

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

@Composable
private fun DayMeals(meals: List<Meal>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        meals.forEach { meal ->
            MealItem(meal)
        }
    }
}

@Composable
private fun MealItem(meal: Meal) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded)
                        Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Zwiń" else "Rozwiń",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = meal.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}