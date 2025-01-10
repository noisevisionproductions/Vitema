package com.noisevisionsoftware.szytadieta.domain.model.file

import com.noisevisionsoftware.szytadieta.utils.DateUtils
import java.util.UUID

data class FileMetadata (
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val fileName: String,
    val fileUrl: String,
    val fileType: FileType,
    val uploadedAt: Long = DateUtils.getCurrentLocalDate(),
    val status: FileStatus = FileStatus.PENDING
)