package com.noisevisionsoftware.szytadieta.ui.screens.admin.statistics.components

import android.graphics.Color
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.noisevisionsoftware.szytadieta.domain.model.app.AppStatistics

@Composable
fun GenderDistributionChart(
    statistics: AppStatistics,
    modifier: Modifier = Modifier
) {
    val entries = remember(statistics) {
        statistics.usersByGender.map { (gender, count) ->
            PieEntry(count.toFloat(), gender.displayName)
        }
    }

    StatisticsCard(
        title = "Rozkład płci użytkowników",
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                PieChart(context).apply {
                    description.isEnabled = false
                    isDrawHoleEnabled = true
                    setHoleColor(Color.TRANSPARENT)
                    legend.isEnabled = true
                    setEntryLabelColor(Color.BLACK)
                    setEntryLabelTextSize(12f)
                }
            },
            update = { chart ->
                val dataSet = PieDataSet(entries, "").apply {
                    colors = listOf(
                        Color.rgb(64, 89, 128),
                        Color.rgb(149, 165, 124),
                        Color.rgb(217, 184, 162)
                    )
                    valueTextSize = 14f
                    valueTextColor = Color.BLACK
                }

                val pieData = PieData(dataSet)
                chart.data = pieData
                chart.invalidate()
            }
        )
    }
}