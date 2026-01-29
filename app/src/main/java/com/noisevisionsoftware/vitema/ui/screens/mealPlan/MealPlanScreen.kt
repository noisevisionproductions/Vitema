package com.noisevisionsoftware.vitema.ui.screens.mealPlan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.DietDay
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.Recipe
import com.noisevisionsoftware.vitema.domain.model.health.dietPlan.toMeal
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.CustomErrorMessage
import com.noisevisionsoftware.vitema.ui.common.CustomTopAppBar
import com.noisevisionsoftware.vitema.ui.common.LoadingOverlay
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import com.noisevisionsoftware.vitema.ui.navigation.NavigationViewModel
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.components.DailyCaloriesSummary
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.components.DaySelectorForMealPlan
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.components.MealCard
import com.noisevisionsoftware.vitema.ui.screens.mealPlan.components.NoMealPlanMessage

@Composable
fun MealPlanScreen(
    viewModel: MealPlanViewModel = hiltViewModel(),
    navigationViewModel: NavigationViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val mealPlanState by viewModel.mealPlanState.collectAsState()
    val recipesState by viewModel.recipesState.collectAsState()
    val hasAnyMealPlans by viewModel.hasAnyMealPlans.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val availableWeeks by viewModel.availableWeeks.collectAsState()
    val eatenMeals by viewModel.eatenMeals.collectAsState()

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
                            hasAnyMealPlans = hasAnyMealPlans == true,
                            onNavigateToAvailableWeek = if (hasAnyMealPlans == true) {
                                { viewModel.navigateToClosestAvailableWeek() }
                            } else null
                        )
                    } else {
                        DayMealList(
                            dietDay = dietDay,
                            recipes = recipesState,
                            eatenMeals = eatenMeals,
                            onMealToggle = { recipeId ->
                                viewModel.toggleMealEaten(recipeId)
                            },
                            onRecipeClick = { recipeId ->
                                navigationViewModel.setRecipeId(recipeId)
                                onNavigate(NavigationDestination.AuthenticatedDestination.RecipeScreen)
                            }
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
    eatenMeals: Set<String>,
    onMealToggle: (String) -> Unit,
    onRecipeClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            DailyCaloriesSummary(
                dietDay = dietDay,
                recipes = recipes,
                eatenMeals = eatenMeals
            )
        }

        items(
            items = dietDay.meals.sortedBy { it.mealType.ordinal },
            key = { meal -> "${meal.recipeId}_${meal.time}" }
        ) { meal ->
            MealCard(
                meal = meal.toMeal(),
                recipe = recipes[meal.recipeId],
                isEaten = meal.recipeId in eatenMeals,
                onMealToggle = { onMealToggle(meal.recipeId) },
                onRecipeClick = onRecipeClick
            )

        }
    }
}
