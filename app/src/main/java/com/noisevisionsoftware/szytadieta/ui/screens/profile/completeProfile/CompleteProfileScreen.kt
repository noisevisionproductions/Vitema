package com.noisevisionsoftware.szytadieta.ui.screens.profile.completeProfile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.domain.model.user.Gender
import com.noisevisionsoftware.szytadieta.ui.navigation.NavigationDestination
import com.noisevisionsoftware.szytadieta.ui.screens.profile.components.ProfileCard
import com.noisevisionsoftware.szytadieta.ui.screens.profile.components.ProfileDatePicker
import com.noisevisionsoftware.szytadieta.ui.screens.profile.components.ProfileHeader
import com.noisevisionsoftware.szytadieta.utils.AppConfig
import com.noisevisionsoftware.szytadieta.utils.UrlHandler

@Composable
fun CompleteProfileScreen(
    completeProfileViewModel: CompleteProfileViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit,
    isLoading: Boolean = false
) {
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedGender by remember { mutableStateOf<Gender?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    val profileState by completeProfileViewModel.profileState.collectAsState()

    LaunchedEffect(profileState) {
        if (profileState is CompleteProfileViewModel.CompleteProfileState.Success) {
            onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ProfileContent(
            selectedDate = selectedDate,
            selectedGender = selectedGender,
            isLoading = isLoading,
            onDateClick = { showDatePicker = true },
            onGenderSelect = { selectedGender = it },
            onSkip = { onNavigate(NavigationDestination.AuthenticatedDestination.Dashboard) },
            onSave = {
                selectedDate?.let { date ->
                    selectedGender?.let { gender ->
                        completeProfileViewModel.setTempBirthDate(date)
                        completeProfileViewModel.setTempGender(gender)
                        completeProfileViewModel.saveProfile()
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        ProfileDatePicker(
            initialDate = selectedDate,
            onDateSelected = { selectedDate = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun ProfileContent(
    selectedDate: Long?,
    selectedGender: Gender?,
    isLoading: Boolean,
    onDateClick: () -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileHeader()
            ProfileCard(
                selectedDate = selectedDate,
                selectedGender = selectedGender,
                isLoading = isLoading,
                onDateClick = onDateClick,
                onGenderSelect = onGenderSelect,
                onSkip = onSkip,
                onSave = onSave
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "lub wypełnij naszą ankietę:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Wypełnienie ankiety jest wymagane, aby otrzymać ofertę indywidualnej diety",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Kliknij tutaj",
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.Underline
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable {
                        UrlHandler.openUrl(context, AppConfig.Urls.SURVEY_URL)
                    }
                    .padding(vertical = 4.dp)
            )
        }
    }
}