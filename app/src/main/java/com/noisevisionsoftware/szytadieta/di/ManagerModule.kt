package com.noisevisionsoftware.szytadieta.di

import android.content.Context
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.PreferencesManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.SessionManager
import com.noisevisionsoftware.szytadieta.data.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.ui.base.EventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagerModule {
    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(
        @ApplicationContext context: Context
    ): NetworkConnectivityManager = NetworkConnectivityManager(context)

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context,
        eventBus: EventBus
    ): SessionManager = SessionManager(context, eventBus)

    @Provides
    @Singleton
    fun provideSettingsManager(
        @ApplicationContext context: Context
    ): SettingsManager = SettingsManager(context)

    @Provides
    @Singleton
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ): PreferencesManager = PreferencesManager(context)

    @Provides
    @Singleton
    fun provideAlertManager(): AlertManager = AlertManager()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context{
        return context
    }
}