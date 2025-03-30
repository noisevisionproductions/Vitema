package com.noisevisionsoftware.szytadieta.ui.screens.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.exceptions.PasswordField
import com.noisevisionsoftware.szytadieta.domain.exceptions.ValidationManager
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.screens.settings.SettingsViewModel

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var oldPasswordError by remember { mutableStateOf<String?>(null) }
    var newPasswordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val passwordUpdateState by viewModel.passwordUpdateState.collectAsState()

    LaunchedEffect(passwordUpdateState) {
        when (passwordUpdateState) {
            is ViewModelState.Error -> {
                val error = (passwordUpdateState as ViewModelState.Error).message
                when ((passwordUpdateState as ViewModelState.Error).field) {
                    PasswordField.OLD_PASSWORD -> oldPasswordError = error
                    PasswordField.NEW_PASSWORD -> newPasswordError = error
                    null -> newPasswordError = error
                }
            }

            is ViewModelState.Success -> {
                onDismiss()
            }

            ViewModelState.Initial, ViewModelState.Loading -> {
                oldPasswordError = null
                newPasswordError = null
                confirmPasswordError = null
            }
        }
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Zmiana hasła",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    "Wprowadź poniższe dane, aby zmienić swoje hasło do konta:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = {
                        oldPassword = it
                        oldPasswordError = null
                    },
                    label = { Text("Obecne hasło") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = oldPasswordError != null,
                    supportingText = oldPasswordError?.let { { Text(it) } },
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                            Icon(
                                imageVector = if (oldPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (oldPasswordVisible) "Ukryj hasło" else "Pokaż hasło"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                    supportingText = newPasswordError?.let { { Text(it) } },
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = if (newPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (newPasswordVisible) "Ukryj hasło" else "Pokaż hasło"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    label = { Text("Potwierdź nowe hasło") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = confirmPasswordError != null,
                    supportingText = confirmPasswordError?.let { { Text(it) } },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ukryj hasło" else "Pokaż hasło"
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
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
                enabled = passwordUpdateState !is ViewModelState.Loading
            ) {
                Text("Zmień hasło")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = passwordUpdateState !is ViewModelState.Loading
            ) {
                Text("Anuluj")
            }
        }
    )
}