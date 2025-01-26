package com.noisevisionsoftware.szytadieta.ui.screens.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.Utils
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.utils.formatDate

@Composable
fun WeightCard(
    latestWeight: ViewModelState<BodyMeasurements?>,
    weightHistory: ViewModelState<List<BodyMeasurements>>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktualna waga",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            when (latestWeight) {
                is ViewModelState.Success -> {
                    latestWeight.data?.let { measurement ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${measurement.weight} kg",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )

                                    if (weightHistory is ViewModelState.Success) {
                                        val previousWeight = weightHistory.data
                                            .getOrNull(1)?.weight

                                        previousWeight?.let { prevWeight ->
                                            val weightDiff = measurement.weight - prevWeight
                                            val trend = if (weightDiff >= 0) "+" else ""
                                            Text(
                                                text = "$trend$weightDiff kg",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (weightDiff > 0)
                                                    MaterialTheme.colorScheme.error
                                                else
                                                    MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                if (weightHistory is ViewModelState.Success && weightHistory.data.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .padding(vertical = 8.dp)
                                            .padding(start = 20.dp)
                                    ) {
                                        WeightTrendMiniChart(
                                            weights = weightHistory.data.take(7),
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Ostatni pomiar: ${formatDate(measurement.date)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    } ?: EmptyStateView()
                }

                is ViewModelState.Loading -> {
                    LoadingOverlay()
                }

                else -> EmptyStateView()
            }
        }
    }
}

@Composable
private fun WeightTrendMiniChart(
    weights: List<BodyMeasurements>,
    modifier: Modifier = Modifier
) {
    val colorLine = MaterialTheme.colorScheme.secondary.toArgb()
    val fillColorLine = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f).toArgb()
    val circleFillColor = MaterialTheme.colorScheme.primary.toArgb()
    val entries = remember(weights) {
        weights
            .sortedBy { it.date }
            .mapIndexed { index, measurement ->
                Entry(index.toFloat(), measurement.weight.toFloat())
            }
    }

    if (entries.size < 2) return

    val lineDataSet = remember(entries) {
        LineDataSet(entries, "Waga").apply {
            color = colorLine
            setDrawCircles(true)
            circleRadius = 3f
            circleHoleRadius = 2f
            setCircleColors(colorLine)
            circleHoleColor = circleFillColor
            setDrawValues(false)
            lineWidth = 3f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER
            setDrawFilled(true)
            fillColor = fillColorLine
            fillAlpha = 50
        }
    }

    val lineData = remember(lineDataSet) {
        LineData(lineDataSet)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            Utils.init(context)

            LineChart(context).apply {
                data = lineData
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
                isDragEnabled = false
                setScaleEnabled(false)
                setPinchZoom(false)

                xAxis.apply {
                    isEnabled = false
                }

                axisLeft.apply {
                    isEnabled = false
                }

                axisRight.apply {
                    isEnabled = false
                }

                setViewPortOffsets(0f, 0f, 0f, 0f)
                animateX(1000)
            }
        },
        update = { chart ->
            chart.data = lineData
            chart.invalidate()
        }
    )
}

@Composable
private fun EmptyStateView() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Brak wagi ciała",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kliknij, aby dodać pierwszą wagę ciała",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}
