package com.noisevisionsoftware.vitema.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.vitema.domain.model.user.Gender

@Composable
fun ProfileCard(
    selectedDate: Long?,
    selectedGender: Gender?,
    isLoading: Boolean,
    onDateClick: () -> Unit,
    onGenderSelect: (Gender) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            DateSelector(
                selectedDate = selectedDate,
                onDateClick = onDateClick
            )

            GenderSelector(
                selectedGender = selectedGender,
                onGenderSelect = onGenderSelect
            )

            ActionButtons(
                isEnabled = selectedDate != null && selectedGender != null,
                isLoading = isLoading,
                onSkip = onSkip,
                onSave = onSave
            )
        }
    }
}