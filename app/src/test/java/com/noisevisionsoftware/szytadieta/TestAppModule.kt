package com.noisevisionsoftware.szytadieta

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.noisevisionsoftware.szytadieta.domain.alert.AlertManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SessionManager
import com.noisevisionsoftware.szytadieta.domain.localPreferences.SettingsManager
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.szytadieta.domain.repository.AdminRepository
import com.noisevisionsoftware.szytadieta.domain.repository.BodyMeasurementRepository
import com.noisevisionsoftware.szytadieta.domain.repository.WeightRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
class TestAppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideWeightRepository(): WeightRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideBodyMeasurementsRepository(): BodyMeasurementRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideAdminRepository(): AdminRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(): NetworkConnectivityManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideSettingsManager(): SettingsManager = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideAlertManager(): AlertManager = mockk(relaxed = true)
}