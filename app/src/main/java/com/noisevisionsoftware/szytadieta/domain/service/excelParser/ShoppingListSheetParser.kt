package com.noisevisionsoftware.szytadieta.domain.service.excelParser

import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingCategory
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingList
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.ShoppingProduct
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import javax.inject.Inject

class ShoppingListSheetParser @Inject constructor() {

    fun parseShoppingList(sheet: Sheet): ShoppingList {
        val categoryMap = mutableMapOf<String, MutableList<ShoppingProduct>>()

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            val category = getCellValue(row.getCell(0))
            val productName = getCellValue(row.getCell(1))
            val quantity = parseQuantity(row.getCell(2))
            val unit = getCellValue(row.getCell(3))

            if (category.isNotBlank() && productName.isNotBlank()) {
                val product = ShoppingProduct(
                    name = productName,
                    weeklyQuantity = quantity,
                    unit = unit
                )
                categoryMap.getOrPut(category) { mutableListOf() }.add(product)
            }
        }

        val categories = categoryMap.map { (name, products) ->
            ShoppingCategory(name, products)
        }

        return ShoppingList(categories)
    }

    private fun parseQuantity(cell: Cell?): Double {
        return when {
            cell == null -> 0.0
            cell.cellType == CellType.NUMERIC -> cell.numericCellValue
            cell.cellType == CellType.STRING -> cell.stringCellValue.toDoubleOrNull() ?: 0.0
            cell.cellType == CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.NUMERIC -> cell.numericCellValue
                CellType.STRING -> cell.stringCellValue.toDoubleOrNull() ?: 0.0
                else -> 0.0
            }
            else -> 0.0
        }
    }

    private fun getCellValue(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.STRING -> cell.stringCellValue
            CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.NUMERIC -> cell.numericCellValue.toString()
                CellType.STRING -> cell.stringCellValue
                else -> ""
            }
            else -> cell.toString()
        }.trim()
    }
}