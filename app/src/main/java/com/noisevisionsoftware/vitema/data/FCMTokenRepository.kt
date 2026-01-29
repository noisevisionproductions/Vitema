package com.noisevisionsoftware.vitema.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FCMTokenRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    suspend fun updateToken(userId: String): Result<Unit> = runCatching {
        val token = messaging.token.await()

        firestore.collection("users")
            .document(userId)
            .update("fcmToken", token)
            .await()
    }

    suspend fun deleteToken(userId: String): Result<Unit> = runCatching {
        firestore.collection("users")
            .document(userId)
            .update("fcmToken", null)
            .await()
    }
}