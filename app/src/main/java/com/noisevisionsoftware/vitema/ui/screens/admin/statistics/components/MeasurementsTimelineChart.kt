package com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.noisevisionsoftware.vitema.domain.model.app.AppStatistics

@Composable
fun MeasurementsTimelineChart(
    statistics: AppStatistics,
    modifier: Modifier = Modifier
) {
    val chartData = remember(statistics) {
        statistics.measurementsByMont
            .toList()
            .sortedBy { it.first }
    }

    val entries = remember(chartData) {
        chartData.mapIndexed { index, (_, count) ->
            Entry(index.toFloat(), count.toFloat())
        }
    }

    val labels = remember(chartData) {
        chartData.map { it.first }
    }

    StatisticsCard(
        title = "Pomiary w czasie",
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    setTouchEnabled(true)
                    isDragEnabled = true
                    setScaleEnabled(true)
                    setPinchZoom(true)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(labels)
                        granularity = 1f
                        labelRotationAngle = -45f
                    }

                    axisLeft.apply {
                        axisMinimum = 0f
                        granularity = 1f
                    }

                    axisRight.isEnabled = false
                    legend.isEnabled = true
                }
            },
            update = { chart ->
                val dataSet = LineDataSet(entries, "Liczba pomiarów").apply {
                    color = Color.rgb(64, 89, 128)
                    setCircleColors(Color.rgb(64, 89, 128))
                    lineWidth = 2f
                    circleRadius = 4f
                    setDrawCircleHole(false)
                    valueTextSize = 10f
                    valueTextColor = Color.BLACK
                }

                val lineData = LineData(dataSet)
                chart.data = lineData
                chart.invalidate()
            }
        )
    }
}