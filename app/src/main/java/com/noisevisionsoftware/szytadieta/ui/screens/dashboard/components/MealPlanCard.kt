package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.DayMeal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Recipe
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.toMeal
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import java.util.Calendar

@Composable
fun MealPlanCard(
    onClick: () -> Unit,
    todayMeals: ViewModelState<List<DayMeal>>,
    recipes: Map<String, Recipe>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan posiłków",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TodayMealsPreview(
                todayMeals = todayMeals,
                recipes = recipes
            )
        }
    }
}

@Composable
private fun TodayMealsPreview(
    todayMeals: ViewModelState<List<DayMeal>>,
    recipes: Map<String, Recipe>
) {
    when (todayMeals) {
        is ViewModelState.Success -> {
            if (todayMeals.data.isNotEmpty()) {
                val nextMeal = findNextMeal(todayMeals.data)

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MealsProgress(
                        meals = todayMeals.data,
                        currentMeal = nextMeal.toMeal()
                    )

                    NextMealInfo(
                        meal = nextMeal.toMeal(),
                        recipes = recipes
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Zobacz pełny plan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            } else {
                EmptyMealsContent()
            }
        }

        is ViewModelState.Loading -> LoadingContent()
        else -> EmptyMealsContent()
    }
}

@Composable
private fun NextMealInfo(
    meal: Meal,
    recipes: Map<String, Recipe>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SetMeal,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = "Następny posiłek:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }

        Text(
            text = recipes[meal.recipeId]?.name ?: "Ładowanie...",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        recipes[meal.recipeId]?.nutritionalValues?.let { nutritionalValues ->
            if (nutritionalValues.calories > 0) {
                val kcalFormatted = nutritionalValues.calories.toInt().toString()
                Text(
                    text = "$kcalFormatted kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun MealsProgress(
    meals: List<DayMeal>,
    currentMeal: Meal
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            meals.forEach { meal ->
                MealProgressItem(
                    meal = meal.toMeal(),
                    isActive = meal.toMeal() == currentMeal,
                    isPast = isMealInPast(meal.toMeal()),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MealProgressItem(
    meal: Meal,
    isActive: Boolean,
    isPast: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    when {
                        isActive -> MaterialTheme.colorScheme.tertiary
                        isPast -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = meal.mealType.displayName.first().toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = when {
                    isActive -> MaterialTheme.colorScheme.onTertiary
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }

        Text(
            text = meal.time,
            style = MaterialTheme.typography.bodySmall,
            color = when {
                isActive -> MaterialTheme.colorScheme.tertiary
                isPast -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            }
        )
    }
}

private fun isMealInPast(meal: Meal): Boolean {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentTimeInMinutes = currentHour * 60 + currentMinute

    val (mealHour, mealMinute) = meal.time.split(":").map { it.toInt() }
    val mealTimeInMinutes = mealHour * 60 + mealMinute

    return currentTimeInMinutes > mealTimeInMinutes
}

@Composable
private fun EmptyMealsContent() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Brak posiłków na dzisiaj",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kliknij, aby dowiedzieć się więcej",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
        }

    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

private fun findNextMeal(meals: List<DayMeal>): DayMeal {
    val calendar = Calendar.getInstance()
    val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
    val currentMinute = calendar.get(Calendar.MINUTE)
    val currentTimeInMinutes = currentHour * 60 + currentMinute

    return meals
        .sortedBy { it.time }
        .firstOrNull { meal ->
            val (mealHour, mealMinute) = meal.time.split(":").map { it.toInt() }
            val mealTimeInMinutes = mealHour * 60 + mealMinute
            mealTimeInMinutes > currentTimeInMinutes
        } ?: meals.first()
}