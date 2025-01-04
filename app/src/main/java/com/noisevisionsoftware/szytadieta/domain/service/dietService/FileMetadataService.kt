package com.noisevisionsoftware.szytadieta.domain.service.dietService

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.file.FileMetadata
import com.noisevisionsoftware.szytadieta.domain.model.file.FileStatus
import com.noisevisionsoftware.szytadieta.domain.model.file.FileType
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FileMetadataService @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun createAndSaveMetadata(
        userId: String,
        fileName: String,
        fileUrl: String
    ): FileMetadata {
        val metadata = FileMetadata(
            userId = userId,
            fileName = fileName,
            fileUrl = fileUrl,
            fileType = FileType.fromFileName(fileName),
            status = FileStatus.PENDING
        )
        saveFileMetadata(metadata)
        return metadata
    }

    suspend fun updateStatus(fileId: String, status: FileStatus) {
        firestore.collection("files")
            .document(fileId)
            .update("status", status)
            .await()
    }

    suspend fun handleError(userId: String, fileName: String) {
        try {
            val metadata = firestore.collection("files")
                .whereEqualTo("userId", userId)
                .whereEqualTo("fileName", fileName)
                .get()
                .await()
                .documents
                .firstOrNull()

            metadata?.reference?.update("status", FileStatus.ERROR)?.await()
        } catch (e: Exception) {
            Log.e("FileMetadataService", "Error handling upload error: ${e.message}")
        }
    }

    private suspend fun saveFileMetadata(metadata: FileMetadata) {
        firestore.collection("files")
            .document(metadata.id)
            .set(metadata)
            .await()
    }
}