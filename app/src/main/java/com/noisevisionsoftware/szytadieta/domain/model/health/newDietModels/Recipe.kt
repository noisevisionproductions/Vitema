package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

data class Recipe (
    val id: String = "",
    val name: String = "",
    val instructions: String = "",
    val nutritionalValues: NutritionalValues = NutritionalValues(),
)