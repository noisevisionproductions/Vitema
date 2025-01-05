package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload

import android.net.Uri
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
import com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components.UploadArea
import com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components.UploadControls
import com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components.UploadProgressUI
import com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload.components.UserSelectionSections

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
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var showUserSearch by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName =
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex =
                        cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    cursor.moveToFirst()
                    cursor.getString(nameIndex)
                }
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

        UploadArea(
            selectedFileUri = selectedFileUri,
            selectedFileName = selectedFileName,
            onFileSelect = { filePickerLauncher.launch(SUPPORTED_MIME_TYPES) }
        )

        UploadControls(
            uploadState = uploadState,
            onUploadClick = handleUpload,
            onRetryClick = handleUpload,
            onNewFileClick = {
                selectedFileUri = null
                selectedFileName = null
            },
            isUploadEnabled = selectedFileUri != null
        )

        UploadProgressUI(
            uploadState = uploadState,
            modifier = Modifier.fillMaxWidth()
        )
    }
}