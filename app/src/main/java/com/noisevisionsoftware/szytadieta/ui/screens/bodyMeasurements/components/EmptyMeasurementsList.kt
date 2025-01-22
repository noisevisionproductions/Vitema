package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Score
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements

@Composable
fun MeasurementsList(
    measurements: List<BodyMeasurements>,
    onDeleteClick: (String) -> Unit
) {
    if (measurements.isEmpty()) {
        EmptyMeasurementsList()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                vertical = 16.dp,
                horizontal = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Historia pomiarów",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Twoje wcześniejsze pomiary",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(
                items = measurements,
                key = { it.id }
            ) { measurement ->
                ExpandableMeasurementCard(
                    measurement = measurement,
                    onDeleteClick = { onDeleteClick(measurement.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
fun EmptyMeasurementsList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(horizontal = 32.dp)
                .animateContentSize()
        ) {
            Icon(
                imageVector = Icons.Default.Score,
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
            )

            Text(
                text = "Brak pomiarów ciała",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Dodaj swój pierwszy pomiar używając przycisku poniżej",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}