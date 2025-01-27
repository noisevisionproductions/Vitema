package com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.waterIntake.WaterTrackingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWaterIntakeDialog(
    onDismiss: () -> Unit,
    onAmountSelected: (Int) -> Unit,
    viewModel: WaterTrackingViewModel = hiltViewModel()
) {
    val customAmount by viewModel.customAmount.collectAsState()
    var showCustomAmountDialog by remember { mutableStateOf(false) }

    val predefinedAmounts = buildList {
        add(250 to "Szklanka (250ml)")
        add(330 to "Mała butelka (330ml)")
        add(500 to "Butelka (500ml)")
        add(750 to "Duża butelka (750ml)")
        customAmount?.let {
            add(it.amount to it.label)
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Dodaj spożycie wody",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                predefinedAmounts.forEach { (amount, label) ->
                    Button(
                        onClick = { onAmountSelected(amount) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(label)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showCustomAmountDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Dodaj własną wartość")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Anuluj")
                }
            }
        }
    }

    if (showCustomAmountDialog) {
        CustomWaterAmountDialog(
            onDismiss = { showCustomAmountDialog = false },
            onConfirm = { amount, label ->
                viewModel.saveCustomAmount(amount, label)
                onAmountSelected(amount)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomWaterAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var labelError by remember { mutableStateOf<String?>(null) }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Własna wartość",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() }
                        amountError = null
                    },
                    label = { Text("Ilość (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = amountError != null,
                    supportingText = amountError?.let { { Text(it) } }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = {
                        label = it
                        labelError = null
                    },
                    label = { Text("Etykieta (np. Mój kubek)") },
                    isError = labelError != null,
                    supportingText = labelError?.let { { Text(it) } }
                )


                Spacer(modifier = Modifier.height(16.dp))

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

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val amountInt = amount.toIntOrNull() ?: 0
                            when {
                                amountInt <= 0 -> amountError = "Wartość musi być większa od 0"
                                amountInt > 5000 -> amountError = "Maksymalna wartość to 5000ml"
                                label.isBlank() -> labelError = "Podaj etykietę"
                                else -> {
                                    onConfirm(amountInt, label)
                                    onDismiss()
                                }
                            }
                        }
                    ) {
                        Text("Zapisz")
                    }
                }
            }
        }
    }
}