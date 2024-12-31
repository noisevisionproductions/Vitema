package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.User
import com.noisevisionsoftware.szytadieta.domain.model.UserRole
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    suspend fun getAllUsers(): Result<List<User>> = try {
        val snapshot = firestore.collection("users")
            .get()
            .await()

        Result.success(snapshot.documents.mapNotNull {
            it.toObject(User::class.java)
        })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserRole(userId: String, newRole: UserRole): Result<Unit> = try {
        firestore.collection("users")
            .document(userId)
            .update("role", newRole.name)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}