package com.noisevisionsoftware.vitema.ui.screens.admin.fileUpload

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.vitema.domain.alert.AlertManager
import com.noisevisionsoftware.vitema.domain.model.SearchableData
import com.noisevisionsoftware.vitema.domain.model.user.User
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AdminRepository
import com.noisevisionsoftware.vitema.domain.repository.FileRepository
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.vitema.domain.state.ViewModelState
import com.noisevisionsoftware.vitema.domain.state.file.FileUploadState
import com.noisevisionsoftware.vitema.domain.state.file.UploadProgress
import com.noisevisionsoftware.vitema.domain.state.file.UploadResult
import com.noisevisionsoftware.vitema.domain.state.file.UploadResultStatus
import com.noisevisionsoftware.vitema.domain.state.file.UploadStage
import com.noisevisionsoftware.vitema.ui.base.BaseViewModel
import com.noisevisionsoftware.vitema.ui.base.EventBus
import com.noisevisionsoftware.vitema.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FileUploadViewModel @Inject constructor(
    private val fileRepository: FileRepository,
    private val adminRepository: AdminRepository,
    private val dietRepository: DietRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager,
    eventBus: EventBus
) : BaseViewModel(networkManager, alertManager, eventBus) {

    private val _uploadState = MutableStateFlow<FileUploadState>(FileUploadState.Initial)
    val uploadState = _uploadState.asStateFlow()

    private val _userState =
        MutableStateFlow<ViewModelState<SearchableData<User>>>(ViewModelState.Initial)
    val userState = _userState.asStateFlow()

    private val _selectedUsers = MutableStateFlow<Set<String>>(emptySet())
    val selectedUsers = _selectedUsers.asStateFlow()

    private val _selectedStartDate = MutableStateFlow<Long?>(null)
    val selectedStartDate = _selectedStartDate.asStateFlow()

    private val _selectedFileUri = MutableStateFlow<Uri?>(null)
    val selectedFileUri = _selectedFileUri.asStateFlow()

    private val _selectedFileName = MutableStateFlow<String?>(null)
    val selectedFileName = _selectedFileName.asStateFlow()

    private val uploadResults = mutableListOf<UploadResult>()

    init {
        loadUsers()
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
        _userState.value = ViewModelState.Initial
        _selectedStartDate.value = null
        _selectedFileUri.value = null
        _selectedFileName.value = null
        uploadResults.clear()
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

    fun setSelectedStartDate(date: Long) {
        _selectedStartDate.value = date
    }

    fun setSelectedFile(uri: Uri?, fileName: String?) {
        _selectedFileUri.value = uri
        _selectedFileName.value = fileName
    }

    fun uploadFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            when {
                selectedUsers.value.isEmpty() -> {
                    showError("Wybierz przynajmniej jednego użytkownika")
                    return@launch
                }

                selectedStartDate.value == null -> {
                    showError("Wybierz tydzień dla diety")
                    return@launch
                }

                else -> {
                    val existingDiets = checkExistingDiets()
                    if (existingDiets.isNotEmpty()) {
                        showConfirmationDialogForOverwrite(existingDiets) {
                            startUpload(uri, fileName)
                        }
                    } else {
                        startUpload(uri, fileName)
                    }
                }
            }
        }
    }

    private suspend fun checkExistingDiets(): List<String> {
        selectedStartDate.value!!

        return selectedUsers.value.mapNotNull { userId ->
            val exists = dietRepository.hasAnyDiets()
                .getOrNull() != null
            if (exists) userId else null
        }
    }

    private fun showConfirmationDialogForOverwrite(
        userIds: List<String>,
        onConfirm: () -> Unit
    ) {
        val userEmails = (_userState.value as? ViewModelState.Success)?.data?.items
            ?.filter { it.id in userIds }
            ?.joinToString("\n") { it.email }

        _uploadState.value = FileUploadState.NeedsConfirmation(
            message = "Następujący użytkownicy mają już przypisaną dietę w tym terminie:\n\n" +
                    "$userEmails\n\n" +
                    "Czy chcesz nadpisać istniejące diety?",
            onConfirm = onConfirm
        )
    }

    private fun startUpload(uri: Uri, fileName: String) {
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
                    val startDate = selectedStartDate.value!!
                    DateUtils.addDaysToDate(startDate, 6)

                    fileRepository.uploadFile(
                        uri = uri,
                        userId = userId,
                        fileName = fileName
                    ).collect { progress ->
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
                handleUploadError(e.message ?: "Wystąpił błąd podczas przesyłania pliku")
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

    override fun onUserLoggedOut() {
        _userState.value = ViewModelState.Initial
        _uploadState.value = FileUploadState.Initial
    }
}