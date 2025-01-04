package com.noisevisionsoftware.szytadieta.domain.state.file

sealed class UploadProgress {
    data class Progress(val percent: Int, val stage: UploadStage) : UploadProgress()
    data class Success(val downloadUrl: String) : UploadProgress()
    data class Error(val message: String) : UploadProgress()
}

enum class UploadStage {
    UPLOADING,
    PARSING,
    SAVING;

    val displayMessage: String
        get() = when (this) {
            UPLOADING -> "Przesyłanie pliku"
            PARSING -> "Przetwarzanie danych"
            SAVING -> "Zapisywanie diety"
        }
}