package com.noisevisionsoftware.szytadieta.domain.repository

import android.icu.util.Calendar
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.noisevisionsoftware.szytadieta.data.FCMTokenRepository
import com.noisevisionsoftware.szytadieta.domain.exceptions.AppException
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.BodyMeasurements
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.MeasurementSourceType
import com.noisevisionsoftware.szytadieta.domain.model.health.measurements.MeasurementType
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.domain.model.user.pending.PendingUser
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val fcmTokenRepository: FCMTokenRepository
) {
    suspend fun register(nickname: String, email: String, password: String): Result<User> = try {
        val pendingUserSnapshot = firestore.collection("pendingUsers")
            .document(email)
            .get()
            .await()

        val authResult = auth.createUserWithEmailAndPassword(email, password).await()

        authResult.user?.let { firebaseUser ->
            var user = User(
                id = firebaseUser.uid,
                email = email,
                nickname = nickname,
                createdAt = DateUtils.getCurrentLocalDate()
            )

            if (pendingUserSnapshot.exists()) {
                val pendingUser = pendingUserSnapshot.toObject<PendingUser>()
                pendingUser?.let { pending ->
                    user = user.copy(
                        gender = pending.gender,
                        storedAge = pending.age,
                        profileCompleted = true
                    )

                    pending.measurements?.forEach { measurement ->
                        val bodyMeasurement = BodyMeasurements(
                            userId = user.id,
                            date = measurement.date,
                            height = measurement.height,
                            weight = measurement.weight,
                            neck = measurement.neck,
                            biceps = measurement.biceps,
                            chest = measurement.chest,
                            waist = measurement.waist,
                            belt = measurement.belt,
                            hips = measurement.hips,
                            thigh = measurement.thigh,
                            calf = measurement.calf,
                            measurementType = MeasurementType.FULL_BODY,
                            sourceType = MeasurementSourceType.GOOGLE_SHEET,
                            weekNumber = getWeekNumber(measurement.date)
                        )

                        firestore.collection("bodyMeasurements")
                            .document(bodyMeasurement.id)
                            .set(bodyMeasurement)
                            .await()
                    }

                    firestore.collection("pendingUsers")
                        .document(email)
                        .delete()
                        .await()
                }
            }

            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()

            fcmTokenRepository.updateToken(user.id)
                .onFailure { throwable ->
                    Log.e("AuthRepository", "Błąd podczas aktualizacji tokenu FCM", throwable)
                }

            Result.success(user)
        } ?: Result.failure(Exception("Błąd podczas tworzenia konta"))
    } catch (e: Exception) {
        Log.e("Register", "User register error", e)
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
                fcmTokenRepository.updateToken(it.id)
                    .onFailure { throwable ->
                        Log.e("AuthRepository", "Błąd podczas aktualizacji tokenu FCM", throwable)
                    }

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

    suspend fun logout(): Result<Unit> = try {
        auth.currentUser?.uid?.let { userId ->
            fcmTokenRepository.deleteToken(userId)
                .onFailure { throwable ->
                    Log.e("AuthRepository", "Błąd podczas usuwania tokenu FCM", throwable)
                }
        }

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

    private fun getWeekNumber(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }
}