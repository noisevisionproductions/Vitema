package com.noisevisionsoftware.vitema.domain.service.notifications.water

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noisevisionsoftware.vitema.domain.service.notifications.NotificationHelper
import com.noisevisionsoftware.vitema.domain.service.notifications.NotificationScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WaterReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val notificationScheduler: NotificationScheduler
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "WaterReminderWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val messages = listOf(
                "Czas na łyka wody! 💧",
                "Nawodnij się! Twoje ciało będzie wdzięczne. 💦",
                "Pamiętaj o wodzie! 🚰",
            )

            notificationHelper.showWaterReminder(messages.random())
            notificationScheduler.scheduleWaterReminder()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Błąd podczas wyświetlania powiadomienia", e)
            Result.failure()
        }
    }
}