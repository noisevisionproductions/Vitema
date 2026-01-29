package com.noisevisionsoftware.vitema.domain.service.excelParser

import android.util.Log
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import javax.inject.Inject

class ShoppingListSheetParser @Inject constructor() {

    companion object {
        private const val CATEGORY_COLUMN = 0
        private const val PRODUCT_COLUMN = 1
        private const val QUANTITY_COLUMN = 2
    }
/*
    fun parseShoppingList(sheet: Sheet): List<ShoppingCategory> {
        val categoryMap = mutableMapOf<String, MutableList<ShoppingProduct>>()
        var currentCategory = ""

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            val categoryCell = getCellValue(row.getCell(CATEGORY_COLUMN))
            val productName = getCellValue(row.getCell(PRODUCT_COLUMN))
            val quantityString = getCellValue(row.getCell(QUANTITY_COLUMN))

            if (categoryCell.isNotBlank()) {
                currentCategory = categoryCell
            }

            if (productName.isNotBlank()) {
                val (quantity, unit) = if (quantityString.isBlank()) {
                    Log.w(
                        "ShoppingListParser",
                        "Brak ilości dla produktu: $productName w kategorii: $currentCategory"
                    )
                    Pair(0.0, "")
                } else {
                    parseQuantityAndUnit(quantityString)
                }

                val product = ShoppingProduct(
                    name = productName,
                    weeklyQuantity = quantity,
                    unit = unit
                )
                categoryMap.getOrPut(currentCategory) { mutableListOf() }.add(product)
            }
        }

        return categoryMap.map { (name, products) ->
            ShoppingCategory(name, products.sortedBy { it.name })
        }.sortedBy { it.name }
    }*/

    private fun parseQuantityAndUnit(value: String): Pair<Double, String> {
        val pattern = """([\d,.]+)\s*([a-zA-Złśżźćńąę]+|sztuki?|opakowania?)?""".toRegex()

        val match = pattern.find(value.trim())
        if (match != null) {
            val quantityStr = match.groupValues[1].replace(",", ".")
            val quantity = formatQuantity(quantityStr.toDoubleOrNull() ?: 0.0)
            val unit = match.groupValues[2].trim()

            val normalizedUnit = when {
                unit.startsWith("sztuk") || unit.startsWith("szt") -> "sztuk"
                unit.startsWith("opak") -> "opakowania"
                unit.startsWith("kilo") || unit == "kg" -> "kg"
                unit.startsWith("gram") || unit == "g" -> "g"
                unit.startsWith("litr") || unit == "l" -> "l"
                unit.startsWith("mili") || unit == "ml" -> "ml"
                else -> {
                    Log.w("ShoppingListParser", "Nieznana jednostka miary: $unit")
                    unit
                }
            }

            return Pair(quantity, normalizedUnit)
        }

        return Pair(0.0, "")
    }

    private fun formatQuantity(value: Double): Double {
        if (value % 1 == 0.0) {
            return value.toInt().toDouble()
        }
        return value
    }

    private fun getCellValue(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.NUMERIC -> {
                val value = cell.numericCellValue
                if (value % 1 == 0.0) {
                    value.toInt().toString()
                } else {
                    value.toString()
                }
            }

            CellType.STRING -> cell.stringCellValue
            CellType.FORMULA -> when (cell.cachedFormulaResultType) {
                CellType.NUMERIC -> {
                    val value = cell.numericCellValue
                    if (value % 1 == 0.0) {
                        value.toInt().toString()
                    } else {
                        value.toString()
                    }
                }

                CellType.STRING -> cell.stringCellValue
                else -> ""
            }

            else -> cell.toString()
        }.trim()
    }
}