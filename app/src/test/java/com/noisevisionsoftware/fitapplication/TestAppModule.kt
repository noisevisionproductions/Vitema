package com.noisevisionsoftware.fitapplication

import com.noisevisionsoftware.fitapplication.domain.auth.AuthRepository
import com.noisevisionsoftware.fitapplication.domain.network.NetworkConnectivityManager
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
    fun provideAuthRepository(): AuthRepository = mockk(relaxed = true)

    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(): NetworkConnectivityManager = mockk(relaxed = true)
}