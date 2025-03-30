package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    suspend fun getCurrentUserData(): Result<User?> = try {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val documentSnapshot = firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            Result.success(documentSnapshot.toObject(User::class.java))
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserData(userData: User): Result<Unit> = try {
        firestore.collection("users")
            .document(userData.id)
            .set(userData)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateUserEmail(newEmail: String): Result<Unit> = try {
        val currentUser = auth.currentUser ?: throw Exception("Użytkownik nie jest zalogowany")
        val oldEmail = currentUser.email ?: throw Exception("Brak poprzedniego emaila")

        currentUser.verifyBeforeUpdateEmail(newEmail).await()

        firestore.collection("users")
            .document(currentUser.uid)
            .update("email", newEmail)
            .await()

        val oldFolderRef = storage.reference
            .child("users")
            .child("${oldEmail}_${currentUser.uid}")
            .child("diets")
        val newFolderRef = storage.reference
            .child("users")
            .child("${newEmail}_${currentUser.uid}")
            .child("diets")

        oldFolderRef.listAll().await().items.forEach { item ->
            val newItemRef = newFolderRef.child(item.name)
            item.downloadUrl.await().let { uri ->
                newItemRef.putFile(uri).await()
            }
        }

        oldFolderRef.listAll().await().items.forEach { it.delete().await() }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePassword(oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("Użytkownik nie jest zalogowany"))

            val email = currentUser.email ?: return Result.failure(Exception("Brak adresu email"))

            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            currentUser.reauthenticate(credential).await()

            currentUser.updatePassword(newPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val currentUser =
                auth.currentUser
                    ?: return Result.failure(Exception("Użytkownik nie jest zalogowany"))

            val email = currentUser.email ?: throw Exception("Brak emaila")
            val credential = EmailAuthProvider.getCredential(email, password)
            currentUser.reauthenticate(credential).await()

            val folderRef = storage.reference
                .child("users")
                .child("${email}_${currentUser.uid}")
                .child("diets")

            folderRef.listAll().await().items.forEach { it.delete().await() }

            firestore.collection("users")
                .document(currentUser.uid)
                .delete()
                .await()

            val batch = firestore.batch()

            batch.delete(firestore.collection("users").document(currentUser.uid))

            firestore.collection("bodyMeasurements")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    batch.delete(doc.reference)
                }

            firestore.collection("diets")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()
                .documents
                .forEach { doc ->
                    batch.delete(doc.reference)
                }

            batch.commit().await()

            currentUser.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser
}