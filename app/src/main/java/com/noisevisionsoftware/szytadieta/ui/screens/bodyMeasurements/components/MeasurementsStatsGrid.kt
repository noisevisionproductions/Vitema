package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements


@Composable
fun MeasurementsStatsGrid(measurements: BodyMeasurements) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasurementStatItem(
                label = "Waga",
                value = "${measurements.weight}",
                unit = "kg",
                modifier = Modifier.weight(1f)
            )
            MeasurementStatItem(
                label = "Szyja",
                value = "${measurements.neck}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasurementStatItem(
                label = "Biceps",
                value = "${measurements.biceps}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
            MeasurementStatItem(
                label = "Klatka",
                value = "${measurements.chest}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasurementStatItem(
                label = "Talia",
                value = "${measurements.waist}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
            MeasurementStatItem(
                label = "Biodra",
                value = "${measurements.hips}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            MeasurementStatItem(
                label = "Uda",
                value = "${measurements.thigh}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
            MeasurementStatItem(
                label = "Łydki",
                value = "${measurements.calf}",
                unit = "cm",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MeasurementStatItem(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
