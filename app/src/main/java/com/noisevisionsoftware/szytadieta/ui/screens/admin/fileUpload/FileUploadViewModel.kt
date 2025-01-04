package com.noisevisionsoftware.szytadieta.ui.screens.admin.fileUpload

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.FileRepository
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
    private val authRepository: AuthRepository,
    networkManager: NetworkConnectivityManager,
    alertManager: AlertManager
) : BaseViewModel(networkManager, alertManager) {

    private val _uploadState = MutableStateFlow<FileUploadState>(FileUploadState.Initial)
    val uploadState = _uploadState.asStateFlow()

    private val uploadResults = mutableListOf<UploadResult>()

    fun uploadFile(uri: Uri, fileName: String) {
        viewModelScope.launch {
            try {
                uploadResults.clear()
                _uploadState.value = FileUploadState.Loading(
                    message = "Rozpoczynanie przesyłania",
                    progress = 0,
                    stage = UploadStage.UPLOADING,
                    previousStages = emptyList()
                )

                val currentUser = authRepository.getCurrentUser()
                    ?: throw Exception("Użytkownik nie jest zalogowany")

                fileRepository.uploadFile(uri, currentUser.uid, fileName)
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
}