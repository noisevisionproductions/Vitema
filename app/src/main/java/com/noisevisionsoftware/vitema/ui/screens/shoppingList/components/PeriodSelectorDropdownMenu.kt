package com.noisevisionsoftware.vitema.ui.screens.shoppingList.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.shopping.DatePeriod
import com.noisevisionsoftware.vitema.domain.model.shopping.FormattedDatePeriod
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun PeriodSelectorDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    availablePeriods: List<DatePeriod>,
    selectedPeriod: DatePeriod?,
    onPeriodSelected: (DatePeriod) -> Unit
) {
    val displayFormatter = remember { DateTimeFormatter.ofPattern("d MMMM", Locale("pl")) }
    val displayFormatterWithYear = remember { DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("pl")) }

    val formattedPeriods = remember(availablePeriods) {
        availablePeriods.map { period ->
            val startDate = period.localStartDate
            val endDate = period.localEndDate
            val now = LocalDate.now()

            val isCurrentWeek = now in startDate..endDate

            val displayText = buildString {
                append(startDate.format(displayFormatter))
                append(" - ")
                append(endDate.format(displayFormatterWithYear))
            }

            val subtitle = when {
                isCurrentWeek -> "Bieżący tydzień"
                startDate > now -> {
                    val days = ChronoUnit.DAYS.between(now, startDate)
                    when {
                        days == 1L -> "Jutro"
                        days < 7 -> "Za $days dni"
                        days < 14 -> "Za tydzień"
                        else -> "Za ${days / 7} tygodni"
                    }
                }
                else -> {
                    val days = ChronoUnit.DAYS.between(endDate, now)
                    when {
                        days == 1L -> "Wczoraj"
                        days < 7 -> "$days dni temu"
                        days < 14 -> "Tydzień temu"
                        else -> "${days / 7} tygodni temu"
                    }
                }
            }

            FormattedDatePeriod(period, displayText, subtitle, isCurrentWeek)
        }.sortedByDescending { it.period.startTimestamp.seconds }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .width(280.dp)
            .heightIn(max = 400.dp)
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    text = "Wybierz okres",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            enabled = false,
            onClick = { }
        )

        HorizontalDivider()

        formattedPeriods.forEach { formattedPeriod ->
            DropdownMenuItem(
                text = {
                    Column {
                        Text(
                            text = formattedPeriod.displayText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = formattedPeriod.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (formattedPeriod.isCurrentWeek)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                leadingIcon = if (formattedPeriod.isCurrentWeek) {
                    {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null,
                trailingIcon = if (formattedPeriod.period == selectedPeriod) {
                    {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else null,
                onClick = { onPeriodSelected(formattedPeriod.period) }
            )
        }
    }
}