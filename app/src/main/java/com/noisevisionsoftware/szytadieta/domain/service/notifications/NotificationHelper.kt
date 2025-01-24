package com.noisevisionsoftware.szytadieta.domain.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.noisevisionsoftware.szytadieta.MainActivity
import com.noisevisionsoftware.szytadieta.R
import com.noisevisionsoftware.szytadieta.domain.service.notifications.water.WaterReminderReceiver
import com.noisevisionsoftware.szytadieta.utils.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    private val notificationManager: NotificationManager,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val CHANNEL_ID = "szyta_dieta_channel"
        private const val CHANNEL_NAME = "Szyta Dieta"
        private const val CHANNEL_DESCRIPTION = "Powiadomienia z aplikacji Szyta Dieta"
        private const val WATER_REMINDER_NOTIFICATION_ID = 1001
        const val EXTRA_DESTINATION = "destination"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun showBasicNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(DateUtils.getCurrentLocalDate().toInt(), notification)
    }

    fun showWaterReminder(message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_DESTINATION, "water_tracking")
        }

        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val addWaterIntent = Intent(context, WaterReminderReceiver::class.java).apply {
            action = "ADD_WATER"
        }

        val addWaterPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            addWaterIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water_drop)
            .setContentTitle("Śledzenie wody")
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nUstaw dzienny cel spożycia wody i śledź go w aplikacji!")
            )
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_add_water,
                "Dodaj szklankę",
                addWaterPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(WATER_REMINDER_NOTIFICATION_ID, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}