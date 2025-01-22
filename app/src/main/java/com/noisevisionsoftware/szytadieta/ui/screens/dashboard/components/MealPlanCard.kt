package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.RestaurantMenu
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
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.MealType
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import java.util.Calendar

@Composable
fun MealPlanCard(
    onClick: () -> Unit,
    todayMeals: ViewModelState<List<Meal>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        .background(
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
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

            TodayMealsPreview(
                todayMeals = todayMeals
            )
        }
    }
}

@Composable
private fun TodayMealsPreview(
    todayMeals: ViewModelState<List<Meal>>
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
                        currentMeal = nextMeal
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Następny posiłek:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = nextMeal.name.displayName,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Text(
                            text = nextMeal.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
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
private fun MealsProgress(
    meals: List<Meal>,
    currentMeal: Meal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        meals.forEach { meal ->
            MealProgressItem(
                meal = meal,
                isActive = meal == currentMeal,
                isPast = isMealInPast(meal),
                modifier = Modifier.weight(1f)
            )
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
                .size(32.dp)
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
                text = meal.name.displayName.first().toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = when {
                    isActive -> MaterialTheme.colorScheme.onTertiary
                    else -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}

private fun isMealInPast(meal: Meal): Boolean {
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

    return when (meal.name) {
        MealType.BREAKFAST -> hourOfDay > 10
        MealType.SECOND_BREAKFAST -> hourOfDay > 12
        MealType.LUNCH -> hourOfDay > 16
        MealType.SNACK -> hourOfDay > 18
        MealType.DINNER -> hourOfDay >= 23
    }
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

private fun findNextMeal(meals: List<Meal>): Meal {
    val calendar = Calendar.getInstance()
    val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)

    return meals.firstOrNull { meal ->
        when (meal.name) {
            MealType.BREAKFAST -> hourOfDay <= 10
            MealType.SECOND_BREAKFAST -> hourOfDay in 10..12
            MealType.LUNCH -> hourOfDay in 12..16
            MealType.SNACK -> hourOfDay in 16..18
            MealType.DINNER -> hourOfDay >= 18
        }
    } ?: meals.first()
}