package com.noisevisionsoftware.szytadieta.domain.state.file

sealed class FileUploadState {
    data object Initial : FileUploadState()
    data class Loading(
        val message: String,
        val progress: Int,
        val stage: UploadStage,
        val previousStages: List<UploadResult> = emptyList()
    ) : FileUploadState()
    data class Error(
        val message: String,
        val previousStages: List<UploadResult> = emptyList()
    ) : FileUploadState()
    data class Success(
        val previousStages: List<UploadResult> = emptyList()
    ) : FileUploadState()
}