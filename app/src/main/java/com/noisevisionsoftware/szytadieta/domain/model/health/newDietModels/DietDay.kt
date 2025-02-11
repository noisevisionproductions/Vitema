package com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DietDay(
    @PropertyName("date")
    @field:JvmField
    val timestamp: Timestamp = Timestamp.now(),
    val meals: List<DayMeal> = emptyList()
) {
    val date: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(timestamp.seconds * 1000))
}
