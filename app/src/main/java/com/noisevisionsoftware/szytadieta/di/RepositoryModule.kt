package com.noisevisionsoftware.szytadieta.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noisevisionsoftware.szytadieta.data.FCMTokenRepository
import com.noisevisionsoftware.szytadieta.domain.repository.AdminRepository
import com.noisevisionsoftware.szytadieta.domain.repository.AppVersionRepository
import com.noisevisionsoftware.szytadieta.domain.repository.AuthRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.BodyMeasurementRepository
import com.noisevisionsoftware.szytadieta.domain.repository.FileRepository
import com.noisevisionsoftware.szytadieta.domain.repository.RecipeRepository
import com.noisevisionsoftware.szytadieta.domain.repository.StatisticsRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WeightRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.DietRepository
import com.noisevisionsoftware.szytadieta.domain.repository.dietRepository.ShoppingListRepository
import com.noisevisionsoftware.szytadieta.domain.repository.health.WaterRepository
import com.noisevisionsoftware.szytadieta.domain.service.dietService.DietService
import com.noisevisionsoftware.szytadieta.domain.service.dietService.FileMetadataService
import com.noisevisionsoftware.szytadieta.domain.service.dietService.StorageService
import com.noisevisionsoftware.szytadieta.domain.service.excelParser.ExcelParserService
import com.noisevisionsoftware.szytadieta.domain.service.excelParser.ExcelValidationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
        dietService: DietService,
        excelValidationService: ExcelValidationService,
        excelParserService: ExcelParserService,
        shoppingListRepository: ShoppingListRepository,
        @ApplicationContext appContext: Context
    ): FileRepository {
        return FileRepository(
            storageService,
            fileMetadataService,
            dietService,
            excelValidationService,
            excelParserService,
            shoppingListRepository,
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
    ): RecipeRepository = RecipeRepository(firestore, storage)

    @Provides
    @Singleton
    fun provideAppVersionRepository(
        firestore: FirebaseFirestore
    ): AppVersionRepository = AppVersionRepository(firestore)
}