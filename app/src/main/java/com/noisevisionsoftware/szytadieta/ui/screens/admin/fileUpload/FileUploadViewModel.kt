package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.model.SearchableData
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AdminRepository
import com.noisevisionsoftware.szytadieta.domain.repository.FileRepository
import com.noisevisionsoftware.szytadieta.domain.state.ViewModelState
import com.noisevisionsoftware.szytadieta.domain.state.file.FileUploadState
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadProgress
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadResult
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadResultStatus
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadStage
import com.noisevisionsoftware.szytadieta.ui.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val adminRepository: AdminRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _uploadState = MutableStateFlow<FileUploadState>(FileUploadState.Initial)
    val uploadState = _uploadState.asStateFlow()

    private val _userState =
        MutableStateFlow<ViewModelState<SearchableData<User>>>(ViewModelState.Initial)
    val userState = _userState.asStateFlow()

    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers = _selectedUsers.asStateFlow()

    private val uploadResults = mutableListOf<UploadResult>()

    init {
        loadUsers()
    }

    fun searchUser(query: String) {
        val currentState = _userState.value
        if (currentState is ViewModelState.Success) {
            _userState.value = ViewModelState.Success(
                currentState.data.updateSearch(query)
            )
        }
    }

    fun toggleUserSelection(userId: String) {
        val currentSelection = _selectedUsers.value
        _selectedUsers.value = if (currentSelection.contains(userId)) {
            currentSelection - userId
        } else {
            currentSelection + userId
        }
    }


    fun uploadFile(uri: Uri, fileName: String) {
        if (selectedUsers.value.isEmpty()) {
            showError("Wybierz przynajmniej jednego użytkownika")
            return
        }

        viewModelScope.launch {
            try {
                uploadResults.clear()
                _uploadState.value = FileUploadState.Loading(
                    message = "Rozpoczynanie przesyłania",
                    progress = 0,
                    stage = UploadStage.UPLOADING,
                    previousStages = emptyList()
                )

                selectedUsers.value.forEach { userId ->
                    fileRepository.uploadFile(uri, userId, fileName)
                        .collect { progress ->
                            when (progress) {
                                is UploadProgress.Progress -> {
                                    handleProgressUpdate(progress)
                                }

                                is UploadProgress.Success -> {
                                    handleUploadSuccess()
                                }

                                is UploadProgress.Error -> {
                                    handleUploadError(progress.message)
                                }
                            }
                        }
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Wystąpił błąd podczas przesyłania pliku"
                handleUploadError(errorMessage)
            }
        }
    }

    private fun handleProgressUpdate(progress: UploadProgress.Progress) {
        when (progress.stage) {
            UploadStage.UPLOADING -> {
                if (progress.percent >= 74) {
                    addUploadResult(
                        UploadStage.UPLOADING,
                        UploadResultStatus.SUCCESS,
                        "Przesyłanie pliku zakończone"
                    )
                }
            }

            UploadStage.PARSING -> {
                if (!uploadResults.any { it.stage == UploadStage.UPLOADING }) {
                    addUploadResult(
                        UploadStage.UPLOADING,
                        UploadResultStatus.SUCCESS,
                        "Przesyłanie pliku zakończone"
                    )
                }
            }

            UploadStage.SAVING -> {
                if (!uploadResults.any { it.stage == UploadStage.PARSING }) {
                    addUploadResult(
                        UploadStage.PARSING,
                        UploadResultStatus.SUCCESS,
                        "Parsowanie pliku zakończone"
                    )
                }
            }
        }

        _uploadState.value = FileUploadState.Loading(
            message = progress.stage.displayMessage,
            progress = progress.percent,
            stage = progress.stage,
            previousStages = uploadResults.toList()
        )
    }

    private fun handleUploadSuccess() {
        addUploadResult(
            UploadStage.SAVING,
            UploadResultStatus.SUCCESS,
            "Zapisywanie pliku zakończone"
        )
        _uploadState.value = FileUploadState.Success(previousStages = uploadResults)
        showSuccess("Plik został pomyślnie przesłany")
    }

    private fun handleUploadError(errorMessage: String) {
        val currentState = _uploadState.value
        if (currentState is FileUploadState.Loading) {
            addUploadResult(
                currentState.stage,
                UploadResultStatus.ERROR,
                errorMessage
            )
        }

        _uploadState.value = FileUploadState.Error(
            message = errorMessage,
            previousStages = uploadResults
        )
        showError(errorMessage)
    }

    private fun addUploadResult(
        stage: UploadStage,
        status: UploadResultStatus,
        message: String? = null
    ) {
        uploadResults.add(
            UploadResult(
                stage = stage,
                status = status,
                message = message
            )
        )
    }

    private fun loadUsers() {
        handleOperation(_userState) {
            val users = adminRepository.getAllUsers().getOrThrow()
            SearchableData.create(
                items = users,
                searchPredicate = { user, query ->
                    user.email.contains(query, ignoreCase = true) ||
                            user.nickname.contains(query, ignoreCase = true)
                }
            )
        }
    }

    fun loadUploadScreen() {
        _uploadState.value = FileUploadState.Initial
        _selectedUsers.value = emptySet()
        uploadResults.clear()
        loadUsers()
    }
}