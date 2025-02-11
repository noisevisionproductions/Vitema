package com.noisevisionsoftware.szytadieta.domain.model.shopping

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.noisevisionsoftware.szytadieta.domain.model.health.newDietModels.MealType
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

data class ShoppingList(
    val id: String = "",
    val userId: String = "",
    val dietId: String = "",
    @get:PropertyName("items")
    @set:PropertyName("items")
    var items: Any = emptyList<Any>(),
    val version: Int? = null,

    @field:JvmField
    @PropertyName("createdAt")
    val createdAt: Timestamp = Timestamp.now(),

    @field:JvmField
    @PropertyName("startDate")
    val startTimestamp: Timestamp = Timestamp.now(),

    @field:JvmField
    @PropertyName("endDate")
    val endTimestamp: Timestamp = Timestamp.now()
) {
    val startDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(startTimestamp.seconds * 1000))

    val endDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(endTimestamp.seconds * 1000))

    @get:Exclude
    val productsList: List<String>
        get() = when {
            version == 2 && items is List<*> -> {
                (items as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            }
            items is List<*> -> {
                (items as? List<*>)?.mapNotNull { item ->
                    when (item) {
                        is Map<*, *> -> item["name"] as? String
                        else -> null
                    }
                }?.filter { it.isNotBlank() }?.map { it.trim() } ?: emptyList()
            }
            else -> emptyList()
        }.distinct()
}

data class DatePeriod(
    @field:JvmField
    val startTimestamp: Timestamp,

    @field:JvmField
    val endTimestamp: Timestamp
) {
    val startDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(startTimestamp.seconds * 1000))

    val endDate: String
        get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            .format(Date(endTimestamp.seconds * 1000))

    val localStartDate: LocalDate
        get() = Instant.ofEpochSecond(startTimestamp.seconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    val localEndDate: LocalDate
        get() = Instant.ofEpochSecond(endTimestamp.seconds)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    fun format(): String = "$startDate - $endDate"
}

data class ShoppingListProductContext (
    val productId: String = "",
    val name: String = "",
    val recipeId: String = "",
    val dayIndex: Int = 0,
    val mealType: MealType = MealType.BREAKFAST,
    val quantity: Int = 1,
    val occurrences: List<String>? = null
)