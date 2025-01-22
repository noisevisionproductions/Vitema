package com.noisevisionsoftware.szytadieta.domain.repository

import android.icu.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.model.AppStatistics
import com.noisevisionsoftware.szytadieta.domain.model.user.Gender
import com.noisevisionsoftware.szytadieta.domain.model.user.User
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private suspend fun generateStatistics(): Result<AppStatistics> = try {
        val userSnapshot = firestore.collection("users")
            .get()
            .await()

        val users = userSnapshot.documents.mapNotNull { it.toObject(User::class.java) }

        val measurementsSnapshot = firestore.collection("bodyMeasurements")
            .get()
            .await()

        val totalMeasurements = measurementsSnapshot.size()

        val calendar = Calendar.getInstance()
        val thirtyDaysAgo = calendar.apply {
            add(Calendar.DAY_OF_MONTH, -30)
        }.timeInMillis

        val activeUsers = users.count { user ->
            user.createdAt > thirtyDaysAgo
        }

        val thisMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val newUsersThisMonth = users.count { user ->
            user.createdAt >= thisMonth
        }

        val completedProfiles = users.count { it.profileCompleted }

        val genderStats = users.groupBy { it.gender ?: Gender.OTHER }
            .mapValues { it.value.size }

        val averageAge = users.map { it.calculateAge() }
            .takeIf { it.isNotEmpty() }
            ?.average()

        val measurementsByMonth = measurementsSnapshot.documents
            .mapNotNull { doc -> doc.getLong("date") }
            .groupBy { date ->
                val cal = Calendar.getInstance().apply { timeInMillis = date }
                String.format(
                    Locale.US,
                    "%d-%02d",
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1
                )
            }
            .mapValues { it.value.size }

        Result.success(
            AppStatistics(
                totalUsers = users.size,
                activeUsers = activeUsers,
                newUsersThisMonth = newUsersThisMonth,
                totalMeasurements = totalMeasurements,
                averageUserMeasurements = if (users.isNotEmpty())
                    totalMeasurements.toDouble() / users.size else 0.0,
                usersWithCompletedProfiles = completedProfiles,
                usersByGender = genderStats,
                measurementsByMont = measurementsByMonth,
                averageUserAge = averageAge
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun cacheStatistics(statistics: AppStatistics): Result<Unit> = try {
        firestore.collection("statistics")
            .document("app_statistics")
            .set(statistics)
            .await()

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getStatistics(): Result<AppStatistics> = try {
        val snapshot = firestore.collection("statistics")
            .document("app_statistics")
            .get()
            .await()

        val statistics = snapshot.toObject(AppStatistics::class.java)
            ?: throw Exception("Nie znaleziono statystyk")

        val oneHourInMillis = 60L * 60 * 1000

        if (DateUtils.getCurrentLocalDate() - statistics.lastUpdated > oneHourInMillis) {
            generateStatistics()
                .onSuccess { cacheStatistics(it) }
        } else {
            Result.success(statistics)
        }
    } catch (e: Exception) {
        generateStatistics()
            .onSuccess { cacheStatistics(it) }
    }
}