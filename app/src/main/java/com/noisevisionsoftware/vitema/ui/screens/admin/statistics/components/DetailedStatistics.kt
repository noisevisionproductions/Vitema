package com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.app.AppStatistics
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DetailedStatistics(
    statistics: AppStatistics,
    modifier: Modifier = Modifier
) {
    StatisticsCard(
        title = "Szczegółowe statystyki",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailRow(
                label = "Nowi użytkownicy w tym miesiącu",
                value = statistics.newUsersThisMonth.toString()
            )
            DetailRow(
                label = "Użytkownicy z wypełnionym profilem",
                value = "${statistics.usersWithCompletedProfiles} (${
                    calculatePercentage(
                        statistics.usersWithCompletedProfiles,
                        statistics.totalUsers
                    )
                }%)"
            )
            statistics.averageUserAge?.let {
                DetailRow(
                    label = "Średni wiek użytkowników",
                    value = "%.1f lat".format(it)
                )
            }
            DetailRow(
                label = "Ostatnia aktualizacja",
                value = SimpleDateFormat(
                    "dd.MM.yyyy HH:mm",
                    Locale("pl")
                ).format(Date(statistics.lastUpdated))
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun calculatePercentage(part: Int, total: Int): Int {
    return if (total > 0) ((part.toDouble() / total) * 100).toInt() else 0
}
