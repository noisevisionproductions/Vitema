package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

class DietRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend fun getUserDietForDate(date: Long): Result<Diet?> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val formattedDate = formatFirestoreDate(date)

            val snapshot = firestore.collection("diets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Diet::class.java)?.copy(
                    id = doc.id
                )
            }.firstOrNull { diet ->
                diet.days.any { it.date == formattedDate }
            }
        }
    }

    suspend fun getAvailableDates(): Result<List<Long>> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val snapshot = firestore.collection("diets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            snapshot.documents
                .mapNotNull { it.toObject(Diet::class.java) }
                .flatMap { diet ->
                    diet.days.map { day ->
                        day.timestamp.seconds * 1000
                    }
                }
                .distinct()
                .sorted()
        }
    }

    suspend fun getClosestAvailableDate(): Result<Long?> = runCatching {
        getAvailableDates().getOrNull()?.let { dates ->
            val today = DateUtils.getCurrentLocalDate()
            val startOfToday = Calendar.getInstance().apply {
                timeInMillis = today
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            dates.minByOrNull { date ->
                abs(date - startOfToday)
            }
        }
    }

    fun observeDietChanges(userId: String): Flow<List<Diet>> = callbackFlow {
        val subscription = firestore.collection("diets")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val diets = snapshot?.documents?.mapNotNull {
                    it.toObject(Diet::class.java)?.copy(id = it.id)
                } ?: emptyList()

                trySend(diets)
            }

        awaitClose { subscription.remove() }
    }

    suspend fun hasAnyDiets(): Result<Boolean> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val snapshot = firestore.collection("diets")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            !snapshot.isEmpty
        }
    }

    private fun formatFirestoreDate(date: Long): String {
        return SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(date))
    }
}