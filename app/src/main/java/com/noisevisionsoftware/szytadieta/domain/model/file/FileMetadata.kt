package com.noisevisionsoftware.szytadieta.domain.model.file

import java.util.UUID

data class FileMetadata (
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val fileName: String,
    val fileUrl: String,
    val fileType: FileType,
    val uploadedAt: Long = System.currentTimeMillis(),
    val status: FileStatus = FileStatus.PENDING
)