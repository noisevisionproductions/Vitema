package com.noisevisionsoftware.szytadieta.domain.service.excelParser

import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Diet
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.ShoppingList
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
    }

    data class ParseResult(
        val diet: Diet,
        val shoppingList: ShoppingList
    )
/*
    fun parseFile(
        inputStream: InputStream,
        userId: String,
        fileUrl: String,
        startDate: Long,
        endDate: Long
    ): Result<ParseResult> = runCatching {
        val workbook = WorkbookFactory.create(inputStream)

        val diet = Diet(
            userId = userId,
            fileUrl = fileUrl,
            startDate = startDate,
            endDate = endDate,
            weeklyPlan = dietSheetParser.parseWeeklyPlan(workbook.getSheetAt(DIET_SHEET_INDEX))
        )

        val shoppingList = ShoppingList(
            userId = userId,
            dietId = diet.id,
            startDate = startDate,
            endDate = endDate,
            categories = shoppingListSheetParser.parseShoppingList(
                workbook.getSheetAt(
                    SHOPPING_LIST_SHEET_INDEX
                )
            )
        )

        ParseResult(diet, shoppingList)
    }*/
}