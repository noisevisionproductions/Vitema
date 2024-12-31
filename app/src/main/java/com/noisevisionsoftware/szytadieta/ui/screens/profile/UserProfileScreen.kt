package com.noisevisionsoftware.szytadieta.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.ui.common.CustomProgressIndicator
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val profileState by viewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = "Profil użytkownika",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = profileState) {
                is UserProfileViewModel.ProfileState.Loading -> {
                    CustomProgressIndicator()
                }

                is UserProfileViewModel.ProfileState.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProfileSection(
                            title = "Podstawowe informacje",
                            content = {
                                ProfileItem(
                                    icon = Icons.Default.Person,
                                    label = "Nick",
                                    value = state.user.nickname
                                )
                                ProfileItem(
                                    icon = Icons.Default.Email,
                                    label = "Email",
                                    value = state.user.email
                                )
                                if (state.user.birthDate != null) {
                                    ProfileItem(
                                        icon = Icons.Default.DateRange,
                                        label = "Data urodzenia",
                                        value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                            .format(Date(state.user.birthDate))
                                    )
                                }
                                if (state.user.gender != null) {
                                    ProfileItem(
                                        icon = Icons.Default.Face,
                                        label = "Płeć",
                                        value = state.user.gender.displayName
                                    )
                                }
                            }
                        )

                        ProfileSection(
                            title = "Informacje o koncie",
                            content = {
                                ProfileItem(
                                    icon = Icons.Default.CalendarToday,
                                    label = "Data dołączenia",
                                    value = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                                        .format(Date(state.user.createdAt))
                                )
                            }
                        )

                        Button(
                            onClick = {},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edytuj profil")
                        }
                    }
                }

                is UserProfileViewModel.ProfileState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                else -> Unit
            }
        }
    }
}


@Composable
private fun ProfileSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            content()
        }
    }
}


@Composable
private fun ProfileItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}