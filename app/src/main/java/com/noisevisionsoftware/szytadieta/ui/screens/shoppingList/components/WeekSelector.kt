package com.noisevisionsoftware.szytadieta.ui.screens.shoppingList.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WeekSelector(
    availableWeeks: List<Long>,
    selectedWeek: Long?,
    onWeekSelected: (Long) -> Unit,
    getFormattedWeekDate: (Long) -> String,
    modifier: Modifier = Modifier
) {
    if (availableWeeks.isEmpty()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isPreviousEnabled = selectedWeek != availableWeeks.last()
            val isNextEnabled = selectedWeek != availableWeeks.first()

            IconButton(
                onClick = {
                    val currentIndex = availableWeeks.indexOf(selectedWeek)
                    if (currentIndex < availableWeeks.size - 1) {
                        onWeekSelected(availableWeeks[currentIndex + 1])
                    }
                },
                enabled = isPreviousEnabled
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Poprzedni tydzień",
                    tint = if (isPreviousEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }

            Text(
                text = selectedWeek?.let { getFormattedWeekDate(it) } ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = {
                    val currentIndex = availableWeeks.indexOf(selectedWeek)
                    if (currentIndex > 0) {
                        onWeekSelected(availableWeeks[currentIndex - 1])
                    }
                },
                enabled = isNextEnabled
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Następny tydzień",
                    tint = if (isNextEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
            }
        }
    }
}