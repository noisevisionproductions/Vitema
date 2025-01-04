package com.noisevisionsoftware.szytadieta.domain.model.dietPlan

import java.util.UUID

data class Diet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val fileUrl: String = "",
    val uploadedAt: Long = System.currentTimeMillis(),
    val weeklyPlan: List<DayPlan> = emptyList(),
    val shoppingList: ShoppingList? = null
)