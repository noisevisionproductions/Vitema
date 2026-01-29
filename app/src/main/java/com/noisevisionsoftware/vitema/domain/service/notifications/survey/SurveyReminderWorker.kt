package com.noisevisionsoftware.vitema.domain.service.notifications.survey

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.noisevisionsoftware.vitema.domain.repository.UserRepository
import com.noisevisionsoftware.vitema.domain.service.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SurveyReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val userRepository: UserRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val userData =
                userRepository.getCurrentUserData().getOrNull() ?: return Result.success()

            if (!userData.surveyCompleted) {
                notificationHelper.showSurveyReminder()
            }

            return Result.success()
        } catch (_: Exception) {
            return Result.retry()
        }
    }
}


