package com.noisevisionsoftware.szytadieta.ui.screens.settings.components.dashboard

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.dashboard.DashboardCardType
import com.noisevisionsoftware.szytadieta.domain.model.dashboard.DashboardConfig
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.LoadingOverlay
import com.noisevisionsoftware.szytadieta.ui.screens.admin.ErrorMessage

@Composable
fun DashboardSettingsScreen(
    viewModel: DashboardSettingsViewModel = hiltViewModel()
) {
    val dashboardConfig by viewModel.dashboardConfig.collectAsState()

    when (dashboardConfig) {
        is ViewModelState.Loading -> {
            LoadingOverlay()
        }

        is ViewModelState.Error -> {
            ErrorMessage(message = (dashboardConfig as ViewModelState.Error).message)
        }

        is ViewModelState.Success -> {
            val config = (dashboardConfig as ViewModelState.Success<DashboardConfig>).data
            LazyColumn {
                item {
                    Text(
                        text = "Widoczność kart",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                items(DashboardCardType.entries.toTypedArray()) { cardType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cardType.displayName,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = cardType !in config.hiddenCards,
                            onCheckedChange = { isVisible ->
                                viewModel.toggleCardVisibility(cardType, isVisible)
                            }
                        )
                    }
                }
            }
        }

        else -> Unit
    }
}