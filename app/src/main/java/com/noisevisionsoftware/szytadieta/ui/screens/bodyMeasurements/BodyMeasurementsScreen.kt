package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Score
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.ui.common.UiEventHandler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BodyMeasurementsScreen(
    viewModel: BodyMeasurementsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val measurementsState by viewModel.measurementsState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { MeasurementsTopBar(onBackClick = onBackClick) },
        floatingActionButton = {
            AddMeasurementsFAB(onClick = { showAddDialog = true })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
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

            UiEventHandler(
                uiEvent = viewModel.uiEvent,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementsTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Pomiary ciała") },
        navigationIcon = {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Wróć"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun AddMeasurementsFAB(
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Dodaj pomiary"
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
            fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
        }, label = ""
    ) { state ->
        when (state) {
            is BodyMeasurementsViewModel.MeasurementsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is BodyMeasurementsViewModel.MeasurementsState.Success -> MeasurementsList(
                measurements = state.measurements,
                onDeleteClick = onDeleteClick
            )

            is BodyMeasurementsViewModel.MeasurementsState.Initial -> EmptyMeasurementsList()
            else -> EmptyMeasurementsList()
        }
    }
}

@Composable
private fun MeasurementsList(
    measurements: List<BodyMeasurements>,
    onDeleteClick: (String) -> Unit
) {
    if (measurements.isEmpty()) {
        EmptyMeasurementsList()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                MeasurementsStats(measurements = measurements)
            }

            items(
                items = measurements,
                key = { it.id }
            ) { measurement ->
                MeasurementItem(
                    measurement = measurement,
                    onDeleteClick = { onDeleteClick(measurement.id) }
                )
            }
        }
    }
}

@Composable
private fun EmptyMeasurementsList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Score,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak pomiarów",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dodaj swój pierwszy pomiar używając przycisku poniżej",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MeasurementsStats(
    measurements: List<BodyMeasurements>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(
            contentColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Ostatnie pomiary",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            measurements.firstOrNull()?.let { latest ->
                MeasurementStatRow("Waga", "${latest.weight} kg")
                MeasurementStatRow("Szyja", "${latest.neck} cm")
                MeasurementStatRow("Biceps", "${latest.biceps} cm")
                MeasurementStatRow("Klatka", "${latest.chest} cm")
                MeasurementStatRow("Talia", "${latest.waist} cm")
                MeasurementStatRow("Biodra", "${latest.hips} cm")
                MeasurementStatRow("Uda", "${latest.thigh} cm")
                MeasurementStatRow("Łydki", "${latest.calf} cm")
            }
        }
    }
}

@Composable
private fun MeasurementStatRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementItem(
    measurement: BodyMeasurements,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        .format(Date(measurement.date)),
                    style = MaterialTheme.typography.titleMedium
                )
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            MeasurementStatRow("Waga", "${measurement.weight} kg")
            MeasurementStatRow("Szyja", "${measurement.neck} cm")
            MeasurementStatRow("Biceps", "${measurement.biceps} cm")
            MeasurementStatRow("Klatka", "${measurement.chest} cm")
            MeasurementStatRow("Talia", "${measurement.waist} cm")
            MeasurementStatRow("Biodra", "${measurement.hips} cm")
            MeasurementStatRow("Uda", "${measurement.thigh} cm")
            MeasurementStatRow("Łydki", "${measurement.calf} cm")

            if (measurement.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = measurement.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDeleteDialog) {
        BasicAlertDialog(
            onDismissRequest = { showDeleteDialog = false }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = MaterialTheme.shapes.large,
                tonalElevation = AlertDialogDefaults.TonalElevation
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Potwierdź usunięcie",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "Czy na pewno chcesz usunąć te pomiary?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("Anuluj")
                        }
                        TextButton(
                            onClick = {
                                onDeleteClick()
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Usuń")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddMeasurementsDialog(
    viewModel: BodyMeasurementsViewModel = hiltViewModel(),
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurements) -> Unit
) {
    var neck by remember { mutableStateOf("") }
    var biceps by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var thigh by remember { mutableStateOf("") }
    var calf by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var hasError by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj nowe pomiary",
                    style = MaterialTheme.typography.headlineSmall
                )

                MeasurementTextField("Szyja (cm)", neck) { neck = it }
                MeasurementTextField("Biceps (cm)", biceps) { biceps = it }
                MeasurementTextField("Klatka piersiowa (cm)", chest) { chest = it }
                MeasurementTextField("Talia (cm)", waist) { waist = it }
                MeasurementTextField("Biodra (cm)", hips) { hips = it }
                MeasurementTextField("Uda (cm)", thigh) { thigh = it }
                MeasurementTextField("Łydki (cm)", calf) { calf = it }
                MeasurementTextField("Waga (kg)", weight) { weight = it }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notatka (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Anuluj")
                    }

                    TextButton(
                        onClick = {
                            try {
                                val measurements = BodyMeasurements(
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
                                if (validateMeasurements(measurements)) {
                                    onConfirm(measurements)
                                } else {
                                    hasError = true
                                }
                            } catch (e: Exception) {
                                hasError = true
                            }
                        }
                    ) {
                        Text("Dodaj")
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace(",", ".")) },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private fun validateMeasurements(
    measurements: BodyMeasurements
): Boolean {
    return measurements.neck > 0 && measurements.biceps > 0 && measurements.chest > 0 &&
            measurements.waist > 0 && measurements.hips > 0 && measurements.thigh > 0 &&
            measurements.calf > 0 && measurements.weight > 0
}
