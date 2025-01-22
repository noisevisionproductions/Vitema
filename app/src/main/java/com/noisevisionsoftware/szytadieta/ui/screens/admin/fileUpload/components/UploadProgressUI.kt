package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noisevisionsoftware.szytadieta.domain.state.file.FileUploadState
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadResult
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadResultStatus
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadStage

@Composable
fun UploadProgressUI(
    uploadState: FileUploadState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (uploadState) {
            is FileUploadState.Loading -> {
                CurrentUploadProgress(
                    progress = uploadState.progress,
                    message = uploadState.message,
                    stage = uploadState.stage
                )
            }

            is FileUploadState.Error -> {
                UploadStageResult(
                    stage = "Błąd",
                    status = UploadResultStatus.ERROR,
                    message = uploadState.message
                )
            }

            is FileUploadState.Success -> {
                UploadStageResult(
                    stage = "Sukces",
                    status = UploadResultStatus.SUCCESS,
                    message = "Plik został pomyślnie przesłany"
                )
            }

            FileUploadState.Initial -> Unit
            is FileUploadState.NeedsConfirmation -> Unit
        }

        when (uploadState) {
            is FileUploadState.Loading -> {
                PreviousStageHistory(stages = uploadState.previousStages)
            }

            is FileUploadState.Error -> {
                PreviousStageHistory(stages = uploadState.previousStages)
            }

            is FileUploadState.Success -> {
                PreviousStageHistory(stages = uploadState.previousStages)
            }

            FileUploadState.Initial -> Unit
            is FileUploadState.NeedsConfirmation -> Unit
        }
    }
}


@Composable
private fun CurrentUploadProgress(
    progress: Int,
    message: String,
    stage: UploadStage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )

        LinearProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stage.displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PreviousStageHistory(
    stages: List<UploadResult>,
    modifier: Modifier = Modifier
) {
    if (stages.isNotEmpty()) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Historia procesu:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            stages.forEach { result ->
                UploadStageResult(
                    stage = result.stage.displayMessage,
                    status = result.status,
                    message = result.message
                )
            }
        }
    }
}

@Composable
private fun UploadStageResult(
    stage: String,
    status: UploadResultStatus,
    message: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = when (status) {
                UploadResultStatus.SUCCESS -> Icons.Default.CheckCircle
                UploadResultStatus.ERROR -> Icons.Default.Error
                UploadResultStatus.IN_PROGRESS -> Icons.Default.Sync
            },
            tint = when (status) {
                UploadResultStatus.SUCCESS -> MaterialTheme.colorScheme.primary
                UploadResultStatus.ERROR -> MaterialTheme.colorScheme.error
                UploadResultStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            },
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )

        Column {
            Text(
                text = stage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
