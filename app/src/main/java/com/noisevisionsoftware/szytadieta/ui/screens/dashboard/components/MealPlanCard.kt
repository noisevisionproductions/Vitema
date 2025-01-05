package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.MealType
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
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Plan posiłków",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Icon(
                    imageVector = Icons.Default.RestaurantMenu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
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
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (todayMeals) {
            is ViewModelState.Success -> {
                if (todayMeals.data.isNotEmpty()) {
                    val nextMeal = findNextMeal(todayMeals.data)

                    Text(
                        text = "Następny posiłek:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )

                    Text(
                        text = nextMeal.name.displayName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Text(
                        text = nextMeal.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                } else {
                    Text(
                        text = "Brak zaplanowanych posiłków na dziś",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            is ViewModelState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            else -> {
                Text(
                    text = "Sprwadź swój plan posiłków",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
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