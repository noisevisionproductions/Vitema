package com.noisevisionsoftware.szytadieta.domain.model.subscription

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val features: List<String>,
    val durationInMonths: Int,
    val type: SubscriptionType
)