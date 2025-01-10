package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.ui.common.CustomDatePickerDialog
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate

@Composable
fun WeekSelectorForDietUpload(
    selectedDate: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "Aktualnie wybranie daty początkowej automatycznie ustawi okres diety na 7 dni." +
                        "Dlatego w fazie testów przyjmowane jak na razie są diety rozpisane tylko na tydzień" +
                        " (i automatycznie lista zakupów tylko na ten tydzień)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }

        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        ) {
            Text(
                text = selectedDate?.let {
                    val startDate = formatDate(it)
                    val endDate = formatDate(DateUtils.addDaysToDate(it, 7))
                    "Od $startDate do $endDate"
                } ?: "Wybierz okres diety",
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (showDatePicker) {
            CustomDatePickerDialog(
                onDateSelected = { timestamp ->
                    onDateSelected(timestamp)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false },
                currentDate = DateUtils.getCurrentLocalDate(),
                allowAllDates = true
            )
        }
    }
}