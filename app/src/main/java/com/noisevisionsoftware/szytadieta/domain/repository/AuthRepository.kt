package com.noisevisionsoftware.szytadieta.domain.repository

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    suspend fun register(nickname: String, email: String, password: String): Result<User> = try {
        val authResult = auth.createUserWithEmailAndPassword(email, password).await()

        authResult.user?.let { firebaseUser ->
            val user = User(
                id = firebaseUser.uid,
                email = email,
                nickname = nickname,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()

            Result.success(user)
        } ?: Result.failure(Exception("Błąd podczas tworzenia konta"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, password: String): Result<User> = try {
        val authResult = auth.signInWithEmailAndPassword(email, password).await()

        authResult.user?.let { firebaseUser ->
            val documentSnapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = documentSnapshot.toObject(User::class.java)
            user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Nie znaleziono danych użytkownika"))
        } ?: Result.failure(Exception("Błąd podczas logowania"))
    } catch (e: Exception) {
        Result.failure(e)
    }

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

    suspend fun resetPassword(email: String): Result<Unit> = try {
        auth.sendPasswordResetEmail(email).await()
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

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun logout(): Result<Unit> = try {
        auth.signOut()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val currentUser =
                auth.currentUser
                    ?: return Result.failure(Exception("Użytkownik nie jest zalogowany"))

            firestore.collection("users")
                .document(currentUser.uid)
                .delete()
                .await()

            val batch = firestore.batch()

            firestore.collection("bodyMeasurements")
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

    suspend fun <T> withAuthenticatedUser(action: suspend (String) -> T): T {
        val currentUser = getCurrentUser()
            ?: throw AppException.AuthException("Użytkownik nie jest zalogowany")

        return action(currentUser.uid)
    }
}