package com.noisevisionsoftware.vitema.ui.screens.weight

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.noisevisionsoftware.vitema.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.vitema.domain.model.health.measurements.MeasurementSourceType
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.ui.common.ConfirmAlertDialog
import com.noisevisionsoftware.vitema.ui.common.CustomTopAppBar
import com.noisevisionsoftware.vitema.ui.common.LoadingOverlay
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import com.noisevisionsoftware.vitema.ui.screens.weight.components.WeightChart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WeightScreen(
    viewModel: WeightViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val weightState by viewModel.weightState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Historia wagi",
                onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
                showRefreshIcon = true,
                onRefreshClick = { viewModel.loadWeights() }
            )
        },
        floatingActionButton = {
            AddWeightFAB(onClick = { showAddDialog = true })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            WeightContent(
                weightState = weightState,
                onDeleteClick = viewModel::deleteWeight
            )

            if (showAddDialog) {
                AddWeightDialog(
                    onDismiss = { showAddDialog = false },
                    onConfirm = { weight, note ->
                        viewModel.addWeight(weight = weight, note = note)
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AddWeightFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Default.Add, "Dodaj wagę")
    }
}

@Composable
private fun WeightContent(
    weightState: ViewModelState<List<BodyMeasurements>>,
    onDeleteClick: (String) -> Unit
) {
    AnimatedContent(
        targetState = weightState,
        transitionSpec = {
            (fadeIn() + slideInVertically()).togetherWith(fadeOut() + slideOutVertically())
        }, label = ""
    ) { state ->
        when (state) {
            is ViewModelState.Loading,
            ViewModelState.Initial -> LoadingOverlay()
            is ViewModelState.Success -> WeightList(
                bodyMeasurements = state.data,
                onDeleteClick = onDeleteClick
            )
            is ViewModelState.Error -> WeightError(state.message)
        }
    }
}

@Composable
private fun WeightError(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun WeightList(
    bodyMeasurements: List<BodyMeasurements>,
    onDeleteClick: (String) -> Unit
) {
    if (bodyMeasurements.isEmpty()) {
        EmptyWeightList()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Historia pomiarów wagi",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Twoje wcześniejsze pomiary oraz ich statystyki",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                WeightChart(measurements = bodyMeasurements)
            }

            items(
                items = bodyMeasurements,
                key = { weight -> weight.id }
            ) { weight ->
                WeightItem(
                    bodyMeasurements = weight,
                    onDeleteClick = { onDeleteClick(weight.id) }
                )
            }
        }
    }
}

@Composable
fun WeightStatItem(
    label: String,
    value: String,
    trend: Double?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        trend?.let {
            val trendText = if (it >= 0) "+%.1f" else "%.1f"
            Text(
                text = String.format(trendText, it) + " kg/tydz",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun EmptyWeightList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Scale,
                contentDescription = "Waga",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak pomiarów wagi",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
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
private fun WeightItem(
    bodyMeasurements: BodyMeasurements,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                WeightHeader(
                    weight = bodyMeasurements.weight,
                    sourceType = bodyMeasurements.sourceType
                )
                WeightDateTime(
                    date = bodyMeasurements.date,
                    sourceType = bodyMeasurements.sourceType
                )
                if (bodyMeasurements.note.isNotBlank()) {
                    Text(
                        text = bodyMeasurements.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Usuń wpis",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        ConfirmAlertDialog(
            onConfirm = { onDeleteClick() },
            onDismiss = { showDeleteDialog = false },
            title = "Potwierdź usunięcie wagi",
            message = "Czy na pewno chcesz usunąć ten wpis?",
            confirmActionText = "Usuń",
            dismissActionText = "Anuluj"
        )
    }
}

@Composable
private fun WeightHeader(
    weight: Int,
    sourceType: MeasurementSourceType,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "$weight kg",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (sourceType == MeasurementSourceType.GOOGLE_SHEET) {
            Icon(
                imageVector = Icons.Default.CloudDone,
                contentDescription = "Dane z Google Sheets",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun WeightDateTime(
    date: Long,
    sourceType: MeasurementSourceType,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(Date(date)),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (sourceType == MeasurementSourceType.GOOGLE_SHEET) {
            Text(
                text = "Google Sheets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddWeightDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var weightText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj nowy pomiar",
                    style = MaterialTheme.typography.headlineSmall
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = weightText,
                        onValueChange = {
                            weightText = it
                            errorMessage = null
                        },
                        label = { Text("Waga (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorMessage != null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = errorMessage?.let { { Text(it) } }
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notatka (opcjonalnie)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

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
                                val weight = weightText.toInt()
                                when {
                                    weight < 40 -> errorMessage =
                                        "Waga musi być nie mniejsza niż 40 kg"

                                    weight > 250 -> errorMessage =
                                        "Waga musi być nie większa niż 250 kg"

                                    else -> onConfirm(weight, note)
                                }
                            } catch (e: NumberFormatException) {
                                errorMessage = "Wprowadź prawidłową wagę"
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