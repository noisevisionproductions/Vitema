package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.JsonAdapter
import com.noisevisionsoftware.szytadieta.utils.TimestampAdapter

data class ShoppingList(
    val id: String = "",
    val userId: String = "",
    val dietId: String = "",
    val items: List<String> = emptyList(),

    @JsonAdapter(TimestampAdapter::class)
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    val startDate: String = "",
    val endDate: String = ""
)

data class DatePeriod(
    val startDate: String,
    val endDate: String
) {
    fun format(): String = "$startDate - $endDate"
}