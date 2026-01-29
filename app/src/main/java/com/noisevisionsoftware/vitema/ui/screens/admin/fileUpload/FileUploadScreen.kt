package com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.noisevisionsoftware.vitema.domain.state.file.FileUploadState
import com.noisevisionsoftware.vitema.ui.common.ConfirmAlertDialog
import com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload.components.UploadArea
import com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload.components.UploadControls
import com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload.components.UploadProgressUI
import com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload.components.UserSelectionSections
import com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload.components.WeekSelectorForDietUpload

private val SUPPORTED_MIME_TYPES = arrayOf(
    "application/vnd.ms-excel",                     // .xls
    "application/x-excel",                          // alternatywny typ dla .xls
    "application/excel",                            // alternatywny typ dla .xls
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" // .xlsx
)

@Composable
fun FileUploadScreen(
    viewModel: FileUploadViewModel = hiltViewModel()
) {
    val uploadState by viewModel.uploadState.collectAsState()
    val userState by viewModel.userState.collectAsState()
    val selectedUsers by viewModel.selectedUsers.collectAsState()
    val selectedStartDate by viewModel.selectedStartDate.collectAsState()
    val selectedFileUri by viewModel.selectedFileUri.collectAsState()
    val selectedFileName by  viewModel.selectedFileName.collectAsState()
    var showUserSearch by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            val fileName = context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
            viewModel.setSelectedFile(it, fileName)
        }
    }

    val handleUpload = {
        if (selectedFileUri != null && selectedFileName != null) {
            viewModel.uploadFile(selectedFileUri!!, selectedFileName!!)
        } else {
            filePickerLauncher.launch(SUPPORTED_MIME_TYPES)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        UserSelectionSections(
            userState = userState,
            showUserSearch = showUserSearch,
            selectedUsers = selectedUsers,
            onSearchVisibilityChange = { showUserSearch = it },
            onSearch = viewModel::searchUser,
            onUserSelect = viewModel::toggleUserSelection
        )

        WeekSelectorForDietUpload(
            selectedDate = selectedStartDate,
            onDateSelected = viewModel::setSelectedStartDate
        )

        UploadArea(
            selectedFileUri = selectedFileUri,
            selectedFileName = selectedFileName,
            onFileSelect = { filePickerLauncher.launch(SUPPORTED_MIME_TYPES) }
        )

        UploadControls(
            uploadState = uploadState,
            onUploadClick = { showConfirmationDialog = true },
            onRetryClick = handleUpload,
            onNewFileClick = {
               viewModel.setSelectedFile(null, null)
            },
            isUploadEnabled = selectedFileUri != null && selectedUsers.isNotEmpty() && selectedStartDate != null
        )

        UploadProgressUI(
            uploadState = uploadState,
            modifier = Modifier.fillMaxWidth()
        )
    }

    when (val state = uploadState) {
        is FileUploadState.NeedsConfirmation -> {
            ConfirmAlertDialog(
                onConfirm = {
                    state.onConfirm()
                },
                onDismiss = {
                    viewModel.loadUploadScreen()
                },
                title = "Nadpisanie diet",
                message = state.message,
                confirmActionText = "Nadpisz",
                dismissActionText = "Anuluj"
            )
        }
        is FileUploadState.Initial,
        is FileUploadState.Loading,
        is FileUploadState.Success,
        is FileUploadState.Error -> {
            if (showConfirmationDialog) {
                ConfirmAlertDialog(
                    onConfirm = {
                        showConfirmationDialog = false
                        handleUpload()
                    },
                    onDismiss = { showConfirmationDialog = false },
                    title = "Potwierdź przydzielanie diety",
                    message = "Czy na pewno chcesz udostępnić tą dietę dla tych użytkowników:",
                    confirmActionText = "Udostępnij",
                    dismissActionText = "Anuluj"
                )
            }
        }
    }
}