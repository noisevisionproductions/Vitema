package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.WeekDay

@Composable
fun DayHeader(
    dayPlan: DayPlan,
    date: String,
    expanded: Boolean,
    onExpandClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpandClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
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
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = if (expanded) "Zwiń" else "Rozwiń",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.rotate(
                animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "Rotate Arrow"
                ).value
            )
        )
    }
}
