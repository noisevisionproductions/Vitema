package com.noisevisionsoftware.szytadieta.domain.state.file

data class UploadResult (
    val stage: UploadStage,
    val status: UploadResultStatus,
    val message: String?= null
)

enum class UploadResultStatus {
    SUCCESS, ERROR, IN_PROGRESS
}