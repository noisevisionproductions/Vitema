package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

data class DietDay(
    val date: String = "",
    val meals: List<Meal> = emptyList()
)
