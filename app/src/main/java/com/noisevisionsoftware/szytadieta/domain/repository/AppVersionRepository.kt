package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.app.AppVersion
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AppVersionRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getAppVersion(): Result<AppVersion> = runCatching {
        val snapshot = firestore.collection("config")
            .document("app_version")
            .get()
            .await()

        snapshot.toObject(AppVersion::class.java)
            ?: throw Exception("Nie można pobrać informacji o wersji aplikacji")
    }
}