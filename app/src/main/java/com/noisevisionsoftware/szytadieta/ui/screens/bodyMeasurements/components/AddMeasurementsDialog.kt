package com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.model.BodyMeasurements
import com.noisevisionsoftware.szytadieta.ui.screens.bodyMeasurements.MeasurementsInputState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeasurementsDialog(
    onDismiss: () -> Unit,
    onConfirm: (BodyMeasurements) -> Unit
) {
    var measurements by remember { mutableStateOf(MeasurementsInputState()) }
    var hasError by remember { mutableStateOf(false) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .verticalScroll(rememberScrollState()),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Dodaj nowe pomiary",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MeasurementInputField(
                            label = "Waga",
                            value = measurements.weight,
                            onValueChange = { measurements = measurements.copy(weight = it) },
                            unit = "kg",
                            isError = hasError && measurements.weight.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Szyja",
                            value = measurements.neck,
                            onValueChange = { measurements = measurements.copy(neck = it) },
                            unit = "cm",
                            isError = hasError && measurements.neck.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Biceps",
                            value = measurements.biceps,
                            onValueChange = { measurements = measurements.copy(biceps = it) },
                            unit = "cm",
                            isError = hasError && measurements.biceps.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Klatka piersiowa",
                            value = measurements.chest,
                            onValueChange = { measurements = measurements.copy(chest = it) },
                            unit = "cm",
                            isError = hasError && measurements.chest.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Talia",
                            value = measurements.waist,
                            onValueChange = { measurements = measurements.copy(waist = it) },
                            unit = "cm",
                            isError = hasError && measurements.waist.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Biodra",
                            value = measurements.hips,
                            onValueChange = { measurements = measurements.copy(hips = it) },
                            unit = "cm",
                            isError = hasError && measurements.hips.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Uda",
                            value = measurements.thigh,
                            onValueChange = { measurements = measurements.copy(thigh = it) },
                            unit = "cm",
                            isError = hasError && measurements.thigh.isBlank(),
                            imeAction = ImeAction.Next
                        )

                        MeasurementInputField(
                            label = "Łydki",
                            value = measurements.calf,
                            onValueChange = { measurements = measurements.copy(calf = it) },
                            unit = "cm",
                            isError = hasError && measurements.calf.isBlank(),
                            imeAction = ImeAction.Done
                        )
                    }
                }

                OutlinedTextField(
                    value = measurements.note,
                    onValueChange = { measurements = measurements.copy(note = it) },
                    label = { Text("Notatka (opcjonalnie)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text("Anuluj")
                    }

                    TextButton(
                        onClick = {
                            if (measurements.isValid()) {
                                onConfirm(measurements.toBodyMeasurements())
                                hasError = false
                            } else {
                                hasError = true
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Dodaj")
                    }
                }
            }
        }
    }
}

@Composable
private fun MeasurementInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.replace(",", ".")) },
        label = { Text(label) },
        trailingIcon = {
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = imeAction
        ),
        isError = isError,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            errorBorderColor = MaterialTheme.colorScheme.error,
        )
    )
}