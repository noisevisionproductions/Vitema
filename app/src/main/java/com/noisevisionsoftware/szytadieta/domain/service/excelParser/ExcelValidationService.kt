package com.noisevisionsoftware.szytadieta.domain.service.excelParser

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import javax.inject.Inject

class ExcelValidationService @Inject constructor() {

    companion object {
        private const val REQUIRED_SHEET_COUNT = 2
        private const val DIET_SHEET_INDEX = 0
        private const val SHOPPING_LIST_SHEET_INDEX = 1
    }

    fun validateExcelFile(
        inputStream: InputStream,
        fileExtension: String
    ): Result<Unit> = runCatching {
        when (fileExtension.lowercase()) {
            "xlsx", "xls" -> validateExcelFile(inputStream)
            else -> throw IllegalArgumentException("Nieobsługiwany format pliku: $fileExtension")
        }
    }

    private fun validateExcelFile(inputStream: InputStream) {
        val workbook = WorkbookFactory.create(inputStream)

        require(workbook.numberOfSheets >= REQUIRED_SHEET_COUNT) {
            "Plik musi zawierać dokładnie $REQUIRED_SHEET_COUNT arkusze (dieta i lista zakupów)"
        }

        val dietSheet = workbook.getSheetAt(DIET_SHEET_INDEX)
        require(dietSheet.physicalNumberOfRows > 0) { "Arkusz z dietą jest pusty" }

        val shoppingSheet = workbook.getSheetAt(SHOPPING_LIST_SHEET_INDEX)
        require(shoppingSheet.physicalNumberOfRows > 0) { "Arkusz z listą zakupów jest pusty" }

        workbook.close()
    }
}