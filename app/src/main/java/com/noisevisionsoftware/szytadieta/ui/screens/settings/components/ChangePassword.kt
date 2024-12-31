package com.noisevisionsoftware.szytadieta.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.ValidationManager
import com.noisevisionsoftware.szytadieta.ui.screens.settings.SettingsViewModel
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.PasswordField
import com.noisevisionsoftware.szytadieta.ui.screens.settings.data.PasswordUpdateState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val passwordUpdateState by viewModel.passwordUpdateState.collectAsState()

    LaunchedEffect(passwordUpdateState) {
        when (val state = passwordUpdateState) {
            is PasswordUpdateState.Error -> {
                when (state.field) {
                    PasswordField.OLD_PASSWORD -> {
                        oldPasswordError = state.message
                    }

                    PasswordField.NEW_PASSWORD -> {
                        newPasswordError = state.message
                    }

                    null -> {
                        newPasswordError = state.message
                    }
                }
            }

            is PasswordUpdateState.Success -> {
                onDismiss()
            }

            else -> {}
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Zmiana hasła",
                    style = MaterialTheme.typography.headlineSmall
                )

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = {
                        oldPassword = it
                        oldPasswordError = null
                    },
                    label = { Text("Obecne hasło") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = oldPasswordError != null,
                    supportingText = oldPasswordError?.let {
                        { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        newPasswordError = null
                        confirmPasswordError = null
                    },
                    label = { Text("Nowe hasło") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = newPasswordError != null,
                    supportingText = newPasswordError?.let {
                        { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    label = { Text("Potwierdź nowe hasło") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPasswordError != null,
                    supportingText = confirmPasswordError?.let {
                        { Text(text = it, color = MaterialTheme.colorScheme.error) }
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = passwordUpdateState !is PasswordUpdateState.Loading
                    ) {
                        Text("Anuluj")
                    }
                    TextButton(
                        onClick = {
                            oldPasswordError = null
                            newPasswordError = null
                            confirmPasswordError = null

                            var isValid = true

                            ValidationManager.validatePassword(oldPassword)
                                .onFailure {
                                    oldPasswordError =
                                        (it as? AppException.ValidationException)?.message
                                    isValid = false
                                }

                            ValidationManager.validatePassword(newPassword)
                                .onFailure {
                                    newPasswordError =
                                        (it as? AppException.ValidationException)?.message
                                    isValid = false
                                }

                            ValidationManager.validatePasswordConfirmation(
                                newPassword,
                                confirmPassword
                            )
                                .onFailure {
                                    confirmPasswordError =
                                        (it as? AppException.ValidationException)?.message
                                    isValid = false
                                }

                            if (isValid) {
                                onConfirm(oldPassword, newPassword)
                            }
                        },
                        enabled = passwordUpdateState !is PasswordUpdateState.Loading
                    ) {
                        if (passwordUpdateState is PasswordUpdateState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Zmień")
                        }
                    }
                }
            }
        }
    }
}