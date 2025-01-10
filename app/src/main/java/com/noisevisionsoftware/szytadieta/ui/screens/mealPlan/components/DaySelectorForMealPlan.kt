package com.noisevisionsoftware.szytadieta.ui.screens.mealPlan.components

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.ui.common.CustomDatePickerDialog
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.getFormattedWeekDate
import java.util.Calendar

@Composable
fun DaySelectorForMealPlan(
    currentDate: Long,
    onDateSelected: (Long) -> Unit,
    availableWeeks: List<Long>? = null,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val currentCalendar = Calendar.getInstance().apply { timeInMillis = currentDate }
    val hasActiveDiet = availableWeeks?.any { startDate ->
        val endDate = DateUtils.addDaysToDate(startDate, 6)
        currentDate in startDate..endDate
    } ?: false

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
                    currentCalendar.add(Calendar.DAY_OF_YEAR, -1)
                    onDateSelected(currentCalendar.timeInMillis)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Poprzedni tydzień",
                    tint = if (hasActiveDiet)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
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
                    tint = if (hasActiveDiet)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getFormattedWeekDate(currentDate),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (hasActiveDiet)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = {
                    currentCalendar.add(Calendar.DAY_OF_YEAR, 1)
                    onDateSelected(currentCalendar.timeInMillis)
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Następny tydzień",
                    tint = if (hasActiveDiet)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showDatePicker) {
            CustomDatePickerDialog(
                highlightedDates = availableWeeks,
                currentDate = currentDate,
                onDateSelected = { date ->
                    onDateSelected(date)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}