package com.noisevisionsoftware.szytadieta.domain.service.notifications

import android.app.NotificationManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val notificationManager: NotificationManager
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
}