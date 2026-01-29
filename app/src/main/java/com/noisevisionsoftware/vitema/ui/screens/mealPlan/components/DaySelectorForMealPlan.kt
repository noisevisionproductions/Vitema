package com.noisevisionsoftware.vitema.ui.screens.mealPlan.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.ui.common.CustomDatePickerDialog
import com.noisevisionsoftware.vitema.utils.DateUtils
import com.noisevisionsoftware.vitema.utils.formatDate

@Composable
fun DaySelectorForMealPlan(
    currentDate: Long,
    onDateSelected: (Long) -> Unit,
    availableWeeks: List<Long>,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val isDateAvailable = remember(currentDate, availableWeeks) {
        availableWeeks.any { availableDate ->
            val currentDay = DateUtils.getStartOfDay(currentDate)
            val availableDay = DateUtils.getStartOfDay(availableDate)
            currentDay == availableDay
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val prevDate = DateUtils.addDaysToDate(currentDate, -1)
                    onDateSelected(prevDate)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Poprzedni dzień",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { showDatePicker = true }
                    .padding(vertical = 12.dp)
                    .fillMaxWidth(0.7f)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Kalendarz",
                    modifier = Modifier.size(24.dp),
                    tint = if (isDateAvailable)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatDate(currentDate),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isDateAvailable)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = {
                    val nextDate = DateUtils.addDaysToDate(currentDate, 1)
                    onDateSelected(nextDate)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Następny dzień",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showDatePicker) {
            CustomDatePickerDialog(
                highlightedDates = availableWeeks,
                onDateSelected = { date ->
                    onDateSelected(date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}