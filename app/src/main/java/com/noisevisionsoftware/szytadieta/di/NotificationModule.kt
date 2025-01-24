package com.noisevisionsoftware.szytadieta.di

import android.app.NotificationManager
import android.content.Context
import androidx.work.WorkManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.navigation.NavigationManager
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationHelper
import com.noisevisionsoftware.szytadieta.domain.service.notifications.NotificationScheduler
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)

    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context
    ): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideNotificationHelper(
        notificationManager: NotificationManager,
        @ApplicationContext context: Context
    ): NotificationHelper {
        return NotificationHelper(notificationManager, context)
    }

    @Provides
    @Singleton
    fun provideNotificationScheduler(
        @ApplicationContext context: Context,
        settingsManager: SettingsManager
    ): NotificationScheduler {
        return NotificationScheduler(context, settingsManager)
    }

    @Provides
    @Singleton
    fun provideNavigationManager(eventBus: EventBus): NavigationManager {
        return NavigationManager(eventBus)
    }
}