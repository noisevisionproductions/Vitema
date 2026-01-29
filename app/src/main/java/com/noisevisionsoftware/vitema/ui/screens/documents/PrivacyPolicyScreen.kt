package com.noisevisionsoftware.vitema.ui.screens.documents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.ui.common.CustomTopAppBar
import com.noisevisionsoftware.vitema.ui.navigation.NavigationDestination
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(
    viewModel: DocumentsViewModel = hiltViewModel(),
    onNavigate: (NavigationDestination) -> Unit,
    isAuthenticated: Boolean = false
){
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CustomTopAppBar(
            title = "Polityka Prywatności",
            onBackClick = {
                if (isAuthenticated) {
                    onNavigate(NavigationDestination.AuthenticatedDestination.Settings)
                } else {
                    onNavigate(NavigationDestination.UnauthenticatedDestination.Register)
                }
            }
        )

        MarkdownText(
            markdown = viewModel.getPrivacyPolicy(),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}