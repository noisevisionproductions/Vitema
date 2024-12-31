package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.ui.common.CustomProgressIndicator
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components.AddMeasurementsDialog
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components.EmptyMeasurementsList
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components.MeasurementsList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: BodyMeasurementsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val measurementsState by viewModel.measurementsState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Pomiary ciała",
                onBackClick = onBackClick,
                actions = {
                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtruj",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Statystyki",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            AddMeasurementsFAB(onClick = { showAddDialog = true })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            MeasurementsContent(
                measurementsState = measurementsState,
                onDeleteClick = viewModel::deleteMeasurement
            )

            if (showAddDialog) {
                AddMeasurementsDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { measurements ->
                        viewModel.addMeasurements(measurements)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddMeasurementsFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Dodaj pomiary",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun MeasurementsContent(
    measurementsState: BodyMeasurementsViewModel.MeasurementsState,
    onDeleteClick: (String) -> Unit
) {
    AnimatedContent(
        targetState = measurementsState,
        transitionSpec = {
            (fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { fullHeight -> fullHeight }
            )).togetherWith(
                fadeOut(animationSpec = tween(300)) + slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { fullHeight -> -fullHeight }
                )
            )
        },
        label = "MeasurementsContentTransition"
    ) { state ->
        when (state) {
            is BodyMeasurementsViewModel.MeasurementsState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CustomProgressIndicator()
                }
            }

            is BodyMeasurementsViewModel.MeasurementsState.Success ->
                MeasurementsList(
                    measurements = state.measurements,
                    onDeleteClick = onDeleteClick
                )

            else -> EmptyMeasurementsList()
        }
    }
}

data class MeasurementsInputState(
    val neck: String = "",
    val biceps: String = "",
    val chest: String = "",
    val waist: String = "",
    val hips: String = "",
    val thigh: String = "",
    val calf: String = "",
    val weight: String = "",
    val note: String = ""
) {
    fun isValid(): Boolean {
        val measurements = listOf(neck, biceps, chest, waist, hips, thigh, calf, weight)
        return measurements.all {
            it.isNotBlank() && it.toDoubleOrNull()?.let { value -> value > 0 } == true
        }
    }

    fun toBodyMeasurements() = BodyMeasurements(
        neck = neck.toDoubleOrNull() ?: 0.0,
        biceps = biceps.toDoubleOrNull() ?: 0.0,
        chest = chest.toDoubleOrNull() ?: 0.0,
        waist = waist.toDoubleOrNull() ?: 0.0,
        hips = hips.toDoubleOrNull() ?: 0.0,
        thigh = thigh.toDoubleOrNull() ?: 0.0,
        calf = calf.toDoubleOrNull() ?: 0.0,
        weight = weight.toDoubleOrNull() ?: 0.0,
        note = note
    )
}