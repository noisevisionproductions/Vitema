package com.noisevisionsoftware.szytadieta.ui.screens.weight.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.ui.screens.weight.WeightStatItem
import com.noisevisionsoftware.szytadieta.utils.formatDate

@Composable
fun WeightChart(
    measurements: List<BodyMeasurements>,
    modifier: Modifier = Modifier
) {
    val sortedMeasurements = remember(measurements) {
        measurements.sortedBy { it.date }
    }

    val stats = remember(sortedMeasurements) {
        calculateWeightStats(sortedMeasurements)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WeightStatItem(
                    label = "Najniższa",
                    value = "${stats.minWeight} kg",
                    trend = null
                )
                WeightStatItem(
                    label = "Średnia",
                    value = "${stats.avgWeight} kg",
                    trend = stats.weeklyTrend
                )
                WeightStatItem(
                    label = "Najwyższa",
                    value = "${stats.maxWeight} kg",
                    trend = null
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                DetailedWeightChart(
                    measurements = sortedMeasurements,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun DetailedWeightChart(
    measurements: List<BodyMeasurements>,
    modifier: Modifier = Modifier
) {
    val lineColor = MaterialTheme.colorScheme.secondary.toArgb()
    val fillColorCustom = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f).toArgb()
    val xAxisTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()
    val axisLeftTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()
    val axisLeftGridColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f).toArgb()
    val lineDataSetTextColor = MaterialTheme.colorScheme.onSecondaryContainer.toArgb()

    val recentMeasurements = remember(measurements) {
        measurements.takeLast(7)
    }

    val entries = remember(recentMeasurements) {
        recentMeasurements.mapIndexed { index, measurement ->
            Entry(index.toFloat(), measurement.weight.toFloat())
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(false)

                extraBottomOffset = 15f

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val index = value.toInt()
                            return if (index >= 0 && index < recentMeasurements.size) {
                                formatDate(recentMeasurements[index].date)
                            } else ""
                        }
                    }
                    textColor = xAxisTextColor
                    setDrawGridLines(false)
                    labelRotationAngle = 45f
                    granularity = 1f
                    labelCount = 7
                }

                axisLeft.apply {
                    textColor = axisLeftTextColor
                    setDrawGridLines(true)
                    gridColor = axisLeftGridColor
                }

                axisRight.isEnabled = false

                val lineDataSet = LineDataSet(entries, "Waga").apply {
                    color = lineColor
                    setCircleColors(lineColor)
                    circleRadius = 4f
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillColor = fillColorCustom
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    valueTextColor = lineDataSetTextColor
                }

                data = LineData(lineDataSet)
                animateX(1000)
            }
        },
        update = { chart ->
            chart.data = LineData(
                LineDataSet(entries, "waga").apply {
                    color = lineColor
                    setCircleColor(lineColor)
                    circleRadius = 4f
                    lineWidth = 2f
                    setDrawFilled(true)
                    fillColor = fillColorCustom
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    valueTextColor = axisLeftTextColor
                }
            )
            chart.invalidate()
        }
    )
}

data class WeightStats(
    val minWeight: Int,
    val maxWeight: Int,
    val avgWeight: Int,
    val weeklyTrend: Double?
)

fun calculateWeightStats(measurements: List<BodyMeasurements>): WeightStats {
    if (measurements.isEmpty()) return WeightStats(0, 0, 0, null)

    val weights = measurements.map { it.weight }
    val minWeight = weights.minOrNull() ?: 0
    val maxWeight = weights.maxOrNull() ?: 0
    val avgWeight = weights.average().toInt()

    val weeklyTrend = if (measurements.size >= 2) {
        val firstWeight = measurements.first().weight
        val lastWeight = measurements.last().weight
        val weeksDiff =
            (measurements.last().date - measurements.first().date) / (7.0 * 24 * 60 * 60 * 1000)
        if (weeksDiff > 0) {
            (lastWeight - firstWeight) / weeksDiff
        } else null
    } else null

    return WeightStats(minWeight, maxWeight, avgWeight, weeklyTrend)
}
