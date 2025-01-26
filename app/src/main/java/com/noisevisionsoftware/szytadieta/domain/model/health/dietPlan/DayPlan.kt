package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.Meal

data class DayPlan(
    val dayOfWeek: WeekDay = WeekDay.MONDAY,
    val meals: List<Meal> = emptyList()
)
