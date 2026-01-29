package com.noisevisionsoftware.vitema.di

import android.content.Context
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noisevisionsoftware.vitema.data.FCMTokenRepository
import com.noisevisionsoftware.vitema.data.localPreferences.LocalEatenMealsRepository
import com.noisevisionsoftware.vitema.data.remote.RemoteEatenMealsRepository
import com.noisevisionsoftware.vitema.domain.network.NetworkConnectivityManager
import com.noisevisionsoftware.vitema.domain.repository.AdminRepository
import com.noisevisionsoftware.vitema.domain.repository.AppVersionRepository
import com.noisevisionsoftware.vitema.domain.repository.AuthRepository
import com.noisevisionsoftware.vitema.domain.repository.health.BodyMeasurementRepository
import com.noisevisionsoftware.vitema.domain.repository.FileRepository
import com.noisevisionsoftware.vitema.domain.repository.meals.RecipeRepositoryOld
import com.noisevisionsoftware.vitema.domain.repository.StatisticsRepository
import com.noisevisionsoftware.vitema.domain.repository.health.WeightRepository
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.vitema.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.vitema.domain.repository.health.WaterRepository
import com.noisevisionsoftware.vitema.domain.repository.meals.EatenMealsRepository
import com.noisevisionsoftware.vitema.domain.repository.meals.EatenMealsRepositoryImpl
import com.noisevisionsoftware.vitema.domain.service.dietService.FileMetadataService
import com.noisevisionsoftware.vitema.domain.service.dietService.StorageService
import com.noisevisionsoftware.vitema.domain.service.excelParser.ExcelValidationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        fcmTokenRepository: FCMTokenRepository
    ): AuthRepository = AuthRepository(
        auth, firestore,
        fcmTokenRepository = fcmTokenRepository
    )

    @Provides
    @Singleton
    fun provideWeightRepository(
        bodyMeasurementRepository: BodyMeasurementRepository
    ): WeightRepository = WeightRepository(bodyMeasurementRepository)

    @Provides
    @Singleton
    fun provideBodyMeasurementsRepository(
        firestore: FirebaseFirestore
    ): BodyMeasurementRepository = BodyMeasurementRepository(firestore)

    @Provides
    @Singleton
    fun provideAdminRepository(
        firestore: FirebaseFirestore
    ): AdminRepository = AdminRepository(firestore)

    @Provides
    @Singleton
    fun provideStatisticsRepository(
        firestore: FirebaseFirestore
    ): StatisticsRepository {
        return StatisticsRepository(firestore)
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        storageService: StorageService,
        fileMetadataService: FileMetadataService,
        excelValidationService: ExcelValidationService,
        @ApplicationContext appContext: Context
    ): FileRepository {
        return FileRepository(
            storageService,
            fileMetadataService,
            excelValidationService,
            appContext
        )
    }

    @Provides
    @Singleton
    fun provideDietRepository(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository
    ): DietRepository {
        return DietRepository(firestore, authRepository)
    }

    @Provides
    @Singleton
    fun provideShoppingListRepository(
        firebaseFirestore: FirebaseFirestore
    ): ShoppingListRepository {
        return ShoppingListRepository(
            firestore = firebaseFirestore
        )
    }

    @Provides
    @Singleton
    fun provideWaterRepository(
        firestore: FirebaseFirestore
    ): WaterRepository = WaterRepository(firestore)


    @Provides
    @Singleton
    fun provideRecipeRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): RecipeRepositoryOld = RecipeRepositoryOld(firestore, storage)

    @Provides
    @Singleton
    fun provideAppVersionRepository(
        firestore: FirebaseFirestore
    ): AppVersionRepository = AppVersionRepository(firestore)

    @Provides
    @Singleton
    fun provideEatenMealsRepository(
        localRepository: LocalEatenMealsRepository,
        remoteRepository: RemoteEatenMealsRepository,
        networkManager: NetworkConnectivityManager,
        workManager: WorkManager,
        @ApplicationScope scope: CoroutineScope
    ): EatenMealsRepository {
        return EatenMealsRepositoryImpl(
            localRepository = localRepository,
            remoteRepository = remoteRepository,
            networkManager = networkManager,
            workManager = workManager,
            scope = scope
        )
    }
}