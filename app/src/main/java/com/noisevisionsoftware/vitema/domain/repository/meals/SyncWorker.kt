package com.noisevisionsoftware.vitema.domain.repository.meals

import android.content.Context
import android.icu.util.Calendar
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.noisevisionsoftware.vitema.domain.repository.AuthRepository
import com.noisevisionsoftware.vitema.utils.formatDate
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import javax.inject.Inject

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val eatenMealsRepository: EatenMealsRepository,
    private val authRepository: AuthRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val user = authRepository.getCurrentUser() ?: return Result.success()

        // Synchronizacja danych z ostatnich 7 dni
        val calendar = Calendar.getInstance()
        repeat(7) {
            val date = formatDate(calendar.timeInMillis)
            try {
                eatenMealsRepository.syncWithRemote(user.uid, date)
            } catch (e: Exception) {
                Log.e("SyncWorker", "Error syncing data for date: $date", e)
            }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return Result.success()
    }
}