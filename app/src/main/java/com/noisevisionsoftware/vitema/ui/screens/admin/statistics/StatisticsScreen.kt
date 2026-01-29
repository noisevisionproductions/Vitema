package com.noisevisionsoftware.vitema.ui.screens.admin.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.LoadingIndicator
import com.noisevisionsoftware.vitema.ui.screens.admin.ErrorMessage
import com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components.DetailedStatistics
import com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components.GenderDistributionChart
import com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components.MeasurementsTimelineChart
import com.noisevisionsoftware.vitema.ui.screens.admin.statistics.components.StatisticsOverview

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val statisticsState by viewModel.statisticsState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (statisticsState) {
            is ViewModelState.Initial -> {
                LaunchedEffect(Unit) {
                    viewModel.loadStatistics()
                }
            }

            is ViewModelState.Loading -> LoadingIndicator()
            is ViewModelState.Success -> {
                val statistics = (statisticsState as ViewModelState.Success).data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatisticsOverview(statistics)

                    GenderDistributionChart(statistics)

                    MeasurementsTimelineChart(statistics)

                    DetailedStatistics(statistics)
                }
            }

            is ViewModelState.Error -> {
                ErrorMessage(message = (statisticsState as ViewModelState.Error).message)
            }
        }
    }
}