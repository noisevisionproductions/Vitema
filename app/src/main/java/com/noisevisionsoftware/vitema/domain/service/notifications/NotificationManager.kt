package com.noisevisionsoftware.vitema.domain.service.notifications

import android.app.NotificationManager
import com.noisevisionsoftware.vitema.data.localPreferences.SettingsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val notificationManager: NotificationManager,
    private val notificationScheduler: NotificationScheduler,
    private val settingsManager: SettingsManager
) {
    fun areNotificationsEnabled(): Boolean {
        return notificationManager.areNotificationsEnabled()
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }

    suspend fun setWaterNotificationsEnabled(enabled: Boolean) {
        settingsManager.setWaterNotificationsEnabled(enabled)
        if (enabled) {
            notificationScheduler.scheduleWaterReminder()
        } else {
            notificationScheduler.cancelWaterReminder()
        }
    }

    val waterNotificationsEnabled = settingsManager.waterNotificationsEnabled
}