package com.noisevisionsoftware.szytadieta.domain.repository

import android.content.Context
import android.net.Uri
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import com.noisevisionsoftware.szytadieta.domain.model.file.FileStatus
import com.noisevisionsoftware.szytadieta.domain.service.excelParser.ExcelParserService
import com.noisevisionsoftware.szytadieta.domain.service.excelParser.ExcelValidationService
import com.noisevisionsoftware.szytadieta.domain.service.dietService.DietService
import com.noisevisionsoftware.szytadieta.domain.service.dietService.FileMetadataService
import com.noisevisionsoftware.szytadieta.domain.service.dietService.StorageService
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadProgress
import com.noisevisionsoftware.szytadieta.domain.state.file.UploadStage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.io.IOException
import javax.inject.Inject

class FileRepository @Inject constructor(
    private val storageService: StorageService,
    private val fileMetadataService: FileMetadataService,
    private val dietService: DietService,
    private val excelValidationService: ExcelValidationService,
    private val excelParserService: ExcelParserService,
    @ApplicationContext private val context: Context
) {
    fun uploadFile(
        uri: Uri,
        userId: String,
        fileName: String
    ): Flow<UploadProgress> = callbackFlow {
        try {
            val fileExtension = fileName.substringAfterLast(".", "")
            validateFile(uri, fileExtension)

            val downloadUrl = storageService.uploadFile(uri, userId, fileName) { progress ->
                trySend(UploadProgress.Progress(progress, UploadStage.UPLOADING))
            }

            trySend(UploadProgress.Progress(75, UploadStage.PARSING))

            val fileMetadata = fileMetadataService.createAndSaveMetadata(userId, fileName, downloadUrl)

            parseAndSaveDiet(uri, userId, downloadUrl, fileExtension) { stage, progress ->
                trySend(UploadProgress.Progress(progress, stage))
            }

            fileMetadataService.updateStatus(fileMetadata.id, FileStatus.PROCESSED)
            trySend(UploadProgress.Success(downloadUrl))

        } catch (e: Exception) {
            fileMetadataService.handleError(userId, fileName)
            trySend(UploadProgress.Error(e.message ?: "Błąd podczas przesyłania pliku"))
        } finally {
            close()
        }
    }

    private fun validateFile(uri: Uri, fileExtension: String) {
        context.contentResolver.openInputStream(uri)?.use { validationStream ->
            excelValidationService.validateExcelFile(validationStream, fileExtension)
                .getOrThrow()
        } ?: throw IOException("Nie można otworzyć pliku do walidacji")
    }

    private suspend fun parseAndSaveDiet(
        uri: Uri,
        userId: String,
        fileUrl: String,
        fileExtension: String,
        onProgress: (UploadStage, Int) -> Unit
    ): Diet {
        context.contentResolver.openInputStream(uri)?.use { parsingStream ->
            val diet = excelParserService.parseDietFile(
                inputStream = parsingStream,
                userId = userId,
                fileUrl = fileUrl,
                fileExtension = fileExtension
            ).getOrThrow()

            onProgress(UploadStage.PARSING, 85)
            onProgress(UploadStage.SAVING, 90)

            dietService.saveDiet(diet)
            return diet
        } ?: throw IOException("Nie można otworzyć pliku do parsowania")
    }


}