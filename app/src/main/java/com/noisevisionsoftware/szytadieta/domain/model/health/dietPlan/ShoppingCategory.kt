package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

data class ShoppingCategory(
    val name: String = "",
    val products: List<ShoppingProduct> = emptyList()
)