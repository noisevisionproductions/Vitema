package com.noisevisionsoftware.szytadieta.domain.model.dietPlan

data class DayPlan(
    val dayOfWeek: WeekDay = WeekDay.MONDAY,
    val meals: List<Meal> = emptyList()
)
