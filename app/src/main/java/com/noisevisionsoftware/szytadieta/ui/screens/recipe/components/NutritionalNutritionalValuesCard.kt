package com.noisevisionsoftware.szytadieta.ui.screens.recipe.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.NutritionalValues

@Composable
fun NutritionalValuesCard(
    nutritionalValues: NutritionalValues,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Wartości odżywcze",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedNutritionalValue(
                    label = "Kalorie",
                    value = nutritionalValues.calories.toFloat(),
                    maxValue = 1000f,
                    unit = "kcal",
                    icon = Icons.Default.LocalFireDepartment,
                    isVisible = isVisible
                )

                AnimatedNutritionalValue(
                    label = "Białko",
                    value = nutritionalValues.protein.toFloat(),
                    maxValue = 100f,
                    unit = "g",
                    icon = Icons.Default.FitnessCenter,
                    isVisible = isVisible
                )

                AnimatedNutritionalValue(
                    label = "Tłuszcze",
                    value = nutritionalValues.fat.toFloat(),
                    maxValue = 100f,
                    unit = "g",
                    icon = Icons.Default.Water,
                    isVisible = isVisible
                )

                AnimatedNutritionalValue(
                    label = "Węglowodany",
                    value = nutritionalValues.carbs.toFloat(),
                    maxValue = 200f,
                    unit = "g",
                    icon = Icons.Default.Grain,
                    isVisible = isVisible
                )
            }
        }
    }
}

@Composable
private fun AnimatedNutritionalValue(
    label: String,
    value: Float,
    maxValue: Float,
    unit: String,
    icon: ImageVector,
    isVisible: Boolean
){
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) value / maxValue else 0f,
        animationSpec = tween(1000),
        label = "Progress Animation"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .padding(4.dp)
        ){
            CircularProgressIndicator(
                progress = {animatedProgress},
                modifier = Modifier.fillMaxSize(),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = value.toInt().toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}