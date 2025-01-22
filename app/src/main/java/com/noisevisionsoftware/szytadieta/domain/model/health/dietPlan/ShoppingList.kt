package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

import java.util.UUID

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val dietId: String = "",
    val userId: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0,
    val categories: List<ShoppingCategory> = emptyList()
)
