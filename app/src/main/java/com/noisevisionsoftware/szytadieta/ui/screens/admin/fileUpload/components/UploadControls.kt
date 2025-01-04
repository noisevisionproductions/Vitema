package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noisevisionsoftware.szytadieta.domain.state.file.FileUploadState

@Composable
fun UploadControls(
    uploadState: FileUploadState,
    onUploadClick: () -> Unit,
    onRetryClick: () -> Unit,
    onNewFileClick: () -> Unit,
    isUploadEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    when (uploadState) {
        is FileUploadState.Initial -> {
            UploadState(
                onClick = onUploadClick,
                enabled = isUploadEnabled,
                message = "Prześlij plik",
                modifier = modifier
            )
        }

        is FileUploadState.Success -> {
            UploadState(
                onClick = onNewFileClick,
                message = "Prześlij kolejny plik",
                modifier = modifier
            )
        }

        is FileUploadState.Error -> {
            UploadState(
                message = "Spróbuj ponownie",
                onClick = onRetryClick,
                modifier = modifier
            )
        }

        else -> Unit
    }
}

@Composable
private fun UploadState(
    onClick: () -> Unit,
    enabled: Boolean = true,
    message: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(message)
    }
}