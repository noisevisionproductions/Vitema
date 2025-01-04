package com.noisevisionsoftware.szytadieta.ui.screens.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.szytadieta.ui.common.CustomTopAppBar
import com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.FileUploadScreen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.userManagement.UserManagementScreen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.model.AdminMenuItem
import com.noisevisionsoftware.szytadieta.ui.screens.admin.navigation.AdminScreen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.statistics.StatisticsScreen
import com.noisevisionsoftware.szytadieta.ui.screens.admin.statistics.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    adminViewModel: AdminPanelViewModel = hiltViewModel(),
    statisticsViewModel: StatisticsViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    var currentScreen by remember { mutableStateOf<AdminScreen>(AdminScreen.Dashboard) }
    var showSearchBar by remember { mutableStateOf(false) }

    BackHandler {
        if (currentScreen != AdminScreen.Dashboard) {
            currentScreen = AdminScreen.Dashboard
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        adminViewModel.checkAdminState()
            .onFailure {
                adminViewModel.showError("Brak uprawnień administratora")
                onBackClick()
            }
    }

    Scaffold(
        topBar = {
            CustomTopAppBar(
                title = when (currentScreen) {
                    AdminScreen.Dashboard -> "Panel administratora"
                    AdminScreen.UserManagement -> "Zarządzanie użytkownikami"
                    AdminScreen.Statistics -> "Statystyki"
                    AdminScreen.FileUpload -> "Dodaj diety"
                },
                onBackClick = {
                    if (currentScreen == AdminScreen.Dashboard) {
                        onBackClick()
                    } else {
                        currentScreen = AdminScreen.Dashboard
                    }
                },
                showSearchIcon = currentScreen == AdminScreen.UserManagement,
                showFilterIcon = currentScreen == AdminScreen.UserManagement,
                showRefreshIcon = currentScreen == AdminScreen.Statistics,
                onSearchClick = { showSearchBar = true },
                onRefreshClick = { statisticsViewModel.loadStatistics() }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentScreen) {
                AdminScreen.Dashboard -> AdminDashboard(
                    onMenuItemClick = { screen -> currentScreen = screen }
                )

                AdminScreen.UserManagement -> UserManagementScreen(
                    showSearchBar = showSearchBar,
                    onSearchBarVisibilityChange = { showSearchBar = it }
                )

                AdminScreen.Statistics -> StatisticsScreen()

                AdminScreen.FileUpload -> FileUploadScreen()
            }
        }
    }
}

@Composable
private fun AdminDashboard(
    onMenuItemClick: (AdminScreen) -> Unit
) {
    val menuItems = remember {
        listOf(
            AdminMenuItem(
                title = "Użytkownicy",
                description = "Zarządzaj kontami użytkowników",
                icon = Icons.Default.People,
                screen = AdminScreen.UserManagement
            ),
            AdminMenuItem(
                title = "Statystyki",
                description = "Zobacz statystyki aplikacji",
                icon = Icons.Default.BarChart,
                screen = AdminScreen.Statistics
            ),
            AdminMenuItem(
                title = "Dodaj dietę",
                description = "Wrzuć plik z dietą",
                icon = Icons.Default.CloudUpload,
                screen = AdminScreen.FileUpload
            )
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menuItems) { item ->
            AdminMenuCard(
                menuItem = item,
                onClick = { onMenuItemClick(item.screen) }
            )
        }
    }
}

@Composable
private fun AdminMenuCard(
    menuItem: AdminMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = menuItem.icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = menuItem.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = menuItem.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}