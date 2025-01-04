package com.noisevisionsoftware.szytadieta.domain.service.excelParser

import android.util.Log
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.Diet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import javax.inject.Inject

class ExcelParserService @Inject constructor(
    private val dietSheetParser: DietSheetParser,
    private val shoppingListSheetParser: ShoppingListSheetParser
) {
    companion object {
        private const val DIET_SHEET_INDEX = 0
        private const val SHOPPING_LIST_SHEET_INDEX = 1
        private const val TAG = "ExcelParserService"
    }

    fun parseDietFile(
        inputStream: InputStream,
        userId: String,
        fileUrl: String,
        fileExtension: String
    ): Result<Diet> = runCatching {
        if (fileExtension.lowercase() !in listOf("xlsx", "xls")) {
            throw IllegalArgumentException("Nieobsługiwany format pliku. Wspierane formaty to: .xlsx, .xls")
        }

        val workbook = WorkbookFactory.create(inputStream)

        Diet(
            userId = userId,
            fileUrl = fileUrl,
            weeklyPlan = dietSheetParser.parseWeeklyPlan(workbook.getSheetAt(DIET_SHEET_INDEX)),
            shoppingList = shoppingListSheetParser.parseShoppingList(workbook.getSheetAt(
                SHOPPING_LIST_SHEET_INDEX
            ))
        ).also {
            workbook.close()
        }
    }.onFailure {
        Log.e(TAG, "Błąd podczas parsowania pliku: ${it.message}", it)
    }
}