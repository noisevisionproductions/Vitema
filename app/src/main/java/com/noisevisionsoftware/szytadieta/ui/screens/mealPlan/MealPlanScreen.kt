package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DietDay
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.MealType
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.NutritionalValues
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Recipe
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomErrorMessage
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.DaySelectorForMealPlan
import com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components.NoMealPlanMessage

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val mealPlanState by viewModel.mealPlanState.collectAsState()
    val recipesState by viewModel.recipesState.collectAsState()
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
                    val dietDay = (mealPlanState as ViewModelState.Success<DietDay>).data
                    if (dietDay.meals.isEmpty()) {
                        NoMealPlanMessage(
                            hasAnyMealPlans = hasAnyMealPlans ?: false,
                            onNavigateToAvailableWeek = if (hasAnyMealPlans == true) {
                                { viewModel.navigateToClosestAvailableWeek() }
                            } else null,
                            onNavigate = onNavigate
                        )
                    } else {
                        DayMealList(
                            dietDay = dietDay,
                            recipes = recipesState
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayMealList(
    dietDay: DietDay,
    recipes: Map<String, Recipe>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = dietDay.meals.sortedBy { it.mealType.ordinal },
            key = { meal -> "${meal.recipeId}_${meal.time}" }
        ) { meal ->
            MealCard(
                meal = meal,
                recipe = recipes[meal.recipeId]
            )

        }
    }
}

@Composable
private fun MealCard(
    meal: Meal,
    recipe: Recipe?,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = modifier
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
                    imageVector = when (meal.mealType) {
                        MealType.BREAKFAST -> Icons.Default.WbSunny
                        MealType.SECOND_BREAKFAST -> Icons.Default.BrunchDining
                        MealType.LUNCH -> Icons.Default.LunchDining
                        MealType.SNACK -> Icons.Default.Restaurant
                        MealType.DINNER -> Icons.Default.DinnerDining
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text(
                        text = meal.mealType.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = meal.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recipe?.let { recipeData ->
                        Text(
                            text = recipeData.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )

                        NutritionalValues(values = recipeData.nutritionalValues)

                        Text(
                            text = recipeData.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } ?: Text(
                        text = "Ładowanie przepisu...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionalValues(
    values: NutritionalValues,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        NutritionalValue("Kalorie", "${values.calories} kcal")
        NutritionalValue("Białko", "${values.protein}g")
        NutritionalValue("Tłuszcze", "${values.fat}g")
        NutritionalValue("Węglowodany", "${values.carbs}g")
    }
}

@Composable
private fun NutritionalValue(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}