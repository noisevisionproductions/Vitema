package com.noisevisionsoftware.vitema.ui.screens.bodyMeasurements.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements

@Composable
fun MeasurementsStatsGrid(measurements: BodyMeasurements) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        MeasurementSection(
            title = "Waga",
            icon = Icons.Default.MonitorWeight
        ) {
            MeasurementCard(
                label = "Waga",
                value = "${measurements.weight}",
                unit = "kg",
                modifier = Modifier.fillMaxWidth()
            )
        }

        MeasurementSection(
            title = "Górne partie",
            icon = Icons.Default.FitnessCenter
        ) {
            MeasurementGrid(
                items = listOf(
                    MeasurementData("Szyja", measurements.neck),
                    MeasurementData("Biceps", measurements.biceps),
                    MeasurementData("Klatka", measurements.chest)
                )
            )
        }

        MeasurementSection(
            title = "Środkowe partie",
            icon = Icons.Default.Straighten
        ) {
            MeasurementGrid(
                items = listOf(
                    MeasurementData("Talia", measurements.waist),
                    MeasurementData("Pas", measurements.belt),
                    MeasurementData("Biodra", measurements.hips)
                )
            )
        }

        MeasurementSection(
            title = "Dolne partie",
            icon = Icons.Default.SportsMartialArts
        ) {
            MeasurementGrid(
                items = listOf(
                    MeasurementData("Uda", measurements.thigh),
                    MeasurementData("Łydki", measurements.calf)
                )
            )
        }
    }
}

@Composable
private fun MeasurementSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
        }
        content()
    }
}

@Composable
private fun MeasurementGrid(
    items: List<MeasurementData>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.chunked(3).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { item ->
                    MeasurementCard(
                        label = item.label,
                        value = "${item.value}",
                        unit = "cm",
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.small
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

private data class MeasurementData(
    val label: String,
    val value: Int
)