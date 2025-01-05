package com.noisevisionsoftware.szytadieta.domain.repository.dietRepository

import android.icu.util.Calendar
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.service.dietService.DietService
import javax.inject.Inject

class DietRepository @Inject constructor(
    private val dietService: DietService,
    private val authRepository: AuthRepository
) {
    suspend fun getUserDietForDate(date: Long): Result<Diet?> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = date

                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val weekStart = calendar.timeInMillis

            calendar.add(Calendar.DAY_OF_WEEK, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)

            val weekEnd = calendar.timeInMillis

            dietService.getUserDietsForPeriod(userId, weekStart, weekEnd)
                .getOrThrow()
                .maxByOrNull { it.uploadedAt }
        }
    }

    suspend fun hasAnyDiets(): Result<Boolean> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            dietService.getUserDiets(userId)
                .getOrThrow()
                .isNotEmpty()
        }
    }

    suspend fun getClosestAvailableWeekDate(): Result<Long> = runCatching {
        authRepository.withAuthenticatedUser { userId ->
            val diets = dietService.getUserDiets(userId).getOrThrow()
            if (diets.isEmpty()) throw Exception("Brak dostępnych planów")

            val now = System.currentTimeMillis()
            diets.filter { it.startDate >= now }
                .minByOrNull { it.startDate }?.startDate
                ?: diets.maxByOrNull { it.endDate }?.startDate
                ?: throw Exception("Nie znaleziono odpowiedniej daty")
        }
    }
}