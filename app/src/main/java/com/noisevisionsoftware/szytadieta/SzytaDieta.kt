package com.noisevisionsoftware.szytadieta

import android.app.Application
import android.content.pm.ApplicationInfo
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.debug.internal.DebugAppCheckProvider
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltAndroidApp
class SzytaDieta : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        initializeServices()
    }

    private fun initializeServices() {
        try {
            FirebaseApp.initializeApp(this)

            val firebaseAppCheck = FirebaseAppCheck.getInstance()

            val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

            if (isDebuggable) {
                firebaseAppCheck.installAppCheckProviderFactory(
                    DebugAppCheckProviderFactory.getInstance()
                )
            } else {
                firebaseAppCheck.installAppCheckProviderFactory(
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Firebase", e)
        }

        try {
            WorkManager.initialize(this, workManagerConfiguration)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize WorkManager", e)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(getLogLevel()).setMaxSchedulerLimit(MAX_SCHEDULER_LIMIT)
            .setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE)).build()

    private fun getLogLevel(): Int =
        if (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) Log.DEBUG else Log.ERROR

    companion object {
        private const val TAG = "SzytaDieta"
        private const val MAX_SCHEDULER_LIMIT = 100
        private const val THREAD_POOL_SIZE = 4
    }
}