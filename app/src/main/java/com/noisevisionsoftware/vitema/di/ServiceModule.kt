package com.noisevisionsoftware.vitema.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.noisevisionsoftware.vitema.domain.service.excelParser.DietSheetParser
import com.noisevisionsoftware.vitema.domain.service.excelParser.ExcelParserService
import com.noisevisionsoftware.vitema.domain.service.excelParser.ExcelValidationService
import com.noisevisionsoftware.vitema.domain.service.excelParser.ShoppingListSheetParser
import com.noisevisionsoftware.vitema.domain.service.dietService.DietService
import com.noisevisionsoftware.vitema.domain.service.dietService.FileMetadataService
import com.noisevisionsoftware.vitema.domain.service.dietService.StorageService
import com.noisevisionsoftware.vitema.ui.base.EventBus
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideStorageService(storage: FirebaseStorage): StorageService {
        return StorageService(storage)
    }

    @Provides
    @Singleton
    fun provideFileMetadataService(firestore: FirebaseFirestore): FileMetadataService {
        return FileMetadataService(firestore)
    }

    @Provides
    @Singleton
    fun provideDietService(firestore: FirebaseFirestore): DietService {
        return DietService(firestore)
    }

    @Provides
    @Singleton
    fun provideExcelValidationService(): ExcelValidationService {
        return ExcelValidationService()
    }

    @Provides
    @Singleton
    fun provideExcelParserService(
        dietSheetParser: DietSheetParser,
        shoppingListSheetParser: ShoppingListSheetParser
    ): ExcelParserService {
        return ExcelParserService(dietSheetParser, shoppingListSheetParser)
    }

    @Provides
    @Singleton
    fun provideDietSheetParser(): DietSheetParser {
        return DietSheetParser()
    }

    @Provides
    @Singleton
    fun provideShoppingListSheetParser(): ShoppingListSheetParser {
        return ShoppingListSheetParser()
    }

    @Provides
    @Singleton
    fun provideEventBus(): EventBus = EventBus()
}