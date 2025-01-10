package com.noisevisionsoftware.szytadieta.ui.screens.profile.profileEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.user.Gender
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.ui.common.CustomProgressIndicator
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.admin.ErrorMessage
import com.noisevisionsoftware.szytadieta.ui.screens.profile.components.GenderSelector
import com.noisevisionsoftware.szytadieta.ui.screens.profile.components.ProfileDatePicker
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import com.noisevisionsoftware.szytadieta.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    viewModel: ProfileEditViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()
    var formState by remember { mutableStateOf(EditProfileFormState()) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(profileState) {
        if (profileState is ViewModelState.Success) {
            val user = (profileState as ViewModelState.Success<User>).data
            formState = EditProfileFormState(
                nickname = user.nickname,
                birthDate = user.birthDate,
                gender = user.gender
            )
        }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Edytuj profil",
                onBackClick = { onNavigate(NavigationDestination.AuthenticatedDestination.Profile) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            when (profileState) {
                is ViewModelState.Initial -> Unit
                is ViewModelState.Loading -> CustomProgressIndicator()
                is ViewModelState.Error -> ErrorMessage(message = (profileState as ViewModelState.Error).message)
                is ViewModelState.Success -> {
                    EditProfileContent(
                        formState = formState,
                        onFormStateChange = { formState = it },
                        showDatePicker = showDatePicker,
                        onShowDatePickerChange = { showDatePicker = it },
                        onSave = { updatedForm ->
                            val user = (profileState as ViewModelState.Success<User>).data
                            viewModel.updateProfile(
                                user.copy(
                                    nickname = updatedForm.nickname,
                                    birthDate = updatedForm.birthDate,
                                    gender = updatedForm.gender,
                                    storedAge = updatedForm.birthDate?.let {
                                        DateUtils.calculateAge(
                                            it
                                        )
                                    }
                                        ?: 0,
                                    profileCompleted = updatedForm.isComplete()
                                )
                            )
                            onNavigate(NavigationDestination.AuthenticatedDestination.Profile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    formState: EditProfileFormState,
    onFormStateChange: (EditProfileFormState) -> Unit,
    showDatePicker: Boolean,
    onShowDatePickerChange: (Boolean) -> Unit,
    onSave: (EditProfileFormState) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderCard()

        NicknameField(
            nickname = formState.nickname,
            onNicknameChange = { onFormStateChange(formState.copy(nickname = it)) }
        )

        BirthDateField(
            birthDate = formState.birthDate,
            onShowDatePicker = { onShowDatePickerChange(true) }
        )

        GenderSelector(
            selectedGender = formState.gender,
            onGenderSelect = { onFormStateChange(formState.copy(gender = it)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        SaveButton(
            enabled = formState.isComplete(),
            onClick = { onSave(formState) }
        )

        if (showDatePicker) {
            ProfileDatePicker(
                initialDate = formState.birthDate,
                onDateSelected = {
                    onFormStateChange(formState.copy(birthDate = it))
                    onShowDatePickerChange(false)
                },
                onDismiss = { onShowDatePickerChange(false) }
            )
        }
    }
}

@Composable
private fun HeaderCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Edycja profilu",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Uzupełnij swoje dane, aby diety były odpowiednio dobrane pod Ciebie",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun NicknameField(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = nickname,
        onValueChange = onNicknameChange,
        label = { Text("Nazwa użytkownika") },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        supportingText = {
            Text("Minimum 3 znaki")
        },
        isError = nickname.length < 3 && nickname.isNotEmpty(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.outline
        )
    )
}

@Composable
private fun BirthDateField(
    birthDate: Long?,
    onShowDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Text(
            text = "Data urodzenia",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FilledTonalButton(
            onClick = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.DateRange,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = birthDate?.let { formatDate(it) } ?: "Wybierz datę"
            )
        }
        if (birthDate != null) {
            Text(
                text = "Wiek: ${DateUtils.calculateAge(birthDate)} lat",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun SaveButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text("Zapisz zmiany")
    }
}

data class EditProfileFormState(
    val nickname: String = "",
    val birthDate: Long? = null,
    val gender: Gender? = null
) {
    fun isComplete(): Boolean =
        nickname.length >= 3 && birthDate != null && gender != null
}