package com.noisevisionsoftware.szytadieta.domain.service.notifications

import android.content.Context
import android.icu.util.Calendar
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.repository.UserRepository
import com.noisevisionsoftware.szytadieta.domain.service.notifications.survey.SurveyReminderWorker
import com.noisevisionsoftware.szytadieta.domain.service.notifications.water.WaterReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager,
    private val userRepository: UserRepository
) {
    companion object {
        private const val WATER_REMINDER_WORK = "water_reminder_work"
        private const val SURVEY_REMINDER_WORK = "survey_reminder_work"
    }

    suspend fun scheduleWaterReminder() {
        if (!settingsManager.waterNotificationsEnabled.first()) {
            return
        }

        val currentTime = Calendar.getInstance()
        val scheduledTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, Random.nextInt(12, 18))
            set(Calendar.MINUTE, Random.nextInt(0, 59))
            set(Calendar.SECOND, 0)

            if (before(currentTime)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val waterReminderRequest = OneTimeWorkRequestBuilder<WaterReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WATER_REMINDER_WORK,
            ExistingWorkPolicy.REPLACE,
            waterReminderRequest
        )
    }

    suspend fun scheduleSurveyReminder() {
        val userData = userRepository.getCurrentUserData().getOrNull() ?: return

        if (userData.surveyCompleted) {
            return
        }

        val currentTime = Calendar.getInstance()

        // Wtorek
        val tuesdayTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (before(currentTime)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        // Piątek
        val fridayTime = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 18)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)

            if (before(currentTime)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val scheduledTime = if (tuesdayTime.before(fridayTime)) tuesdayTime else fridayTime
        val delay = scheduledTime.timeInMillis - currentTime.timeInMillis

        val surveyReminderRequest = OneTimeWorkRequestBuilder<SurveyReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SURVEY_REMINDER_WORK,
            ExistingWorkPolicy.REPLACE,
            surveyReminderRequest
        )
    }

    fun cancelSurveyReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(SURVEY_REMINDER_WORK)
    }

    fun cancelWaterReminder() {
        WorkManager.getInstance(context).cancelUniqueWork(WATER_REMINDER_WORK)
    }
}