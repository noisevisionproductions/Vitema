package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.lerp
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.DietDay
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Recipe

@Composable
fun DailyCaloriesSummary(
    dietDay: DietDay,
    recipes: Map<String, Recipe>,
    eatenMeals: Set<String>,
    modifier: Modifier = Modifier
) {
    val totalCalories = recipes.values
        .sumOf { it.nutritionalValues.calories }
        .toFloat()

    val consumedCalories = dietDay.meals
        .filter { it.recipeId in  eatenMeals }
        .mapNotNull { meal -> recipes[meal.recipeId]?.nutritionalValues?.calories }
        .sum()
        .toFloat()

    val progress = (consumedCalories / totalCalories).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000),
        label = "Calorie Intake Progress Animation"
    )

    val startColor = MaterialTheme.colorScheme.primary
    val stopColor = MaterialTheme.colorScheme.tertiary

    val progressColor by remember(animatedProgress) {
        derivedStateOf {
            lerp(
                start = startColor,
                stop = stopColor,
                fraction = animatedProgress
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Dzienne spożycie kalorii",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${consumedCalories.toInt()}/${totalCalories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}