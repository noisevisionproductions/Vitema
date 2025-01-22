package com.noisevisionsoftware.szytadieta.domain.service.excelParser

import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.DayPlan
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.Meal
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.MealType
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.WeekDay
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import javax.inject.Inject

class DietSheetParser @Inject constructor() {

    companion object {
        private const val DAY_COLUMN = 0
        private const val BREAKFAST_COLUMN = 1
        private const val SECOND_BREAKFAST_COLUMN = 2
        private const val LUNCH_COLUMN = 3
        private const val SNACK_COLUMN = 4
        private const val DINNER_COLUMN = 5
    }

    fun parseWeeklyPlan(sheet: Sheet): List<DayPlan> {
        val weeklyPlan = mutableListOf<DayPlan>()

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            val dayName = row.getCell(DAY_COLUMN)?.stringCellValue?.trim() ?: continue
            val weekDay = WeekDay.fromPolishName(dayName) ?: continue

            val meals = mutableListOf<Meal>()

            addMealIfExists(meals, row, BREAKFAST_COLUMN, MealType.BREAKFAST)
            addMealIfExists(meals, row, SECOND_BREAKFAST_COLUMN, MealType.SECOND_BREAKFAST)
            addMealIfExists(meals, row, LUNCH_COLUMN, MealType.LUNCH)
            addMealIfExists(meals, row, SNACK_COLUMN, MealType.SNACK)
            addMealIfExists(meals, row, DINNER_COLUMN, MealType.DINNER)

            if (meals.isNotEmpty()) {
                weeklyPlan.add(DayPlan(weekDay, meals))
            }
        }

        return weeklyPlan
    }

    private fun addMealIfExists(
        meals: MutableList<Meal>,
        row: Row,
        columnIndex: Int,
        mealType: MealType
    ) {
        val cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
        val description = cell?.stringCellValue?.trim()

        if (!description.isNullOrBlank()) {
            meals.add(
                Meal(
                    name = mealType,
                    description = description
                )
            )
        }
    }
}