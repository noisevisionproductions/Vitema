package com.noisevisionsoftware.vitema.domain.service.dietService

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class StorageService @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadFile(
        uri: Uri,
        userId: String,
        fileName: String,
        onProgress: (Int) -> Unit
    ): String {
        val fileRef = storage.reference
            .child("diets")
            .child(userId)
            .child(fileName)

        val uploadTask = fileRef.putFile(uri)
        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            onProgress(progress.toInt())
        }

        uploadTask.await()
        return fileRef.downloadUrl.await().toString()
    }
}