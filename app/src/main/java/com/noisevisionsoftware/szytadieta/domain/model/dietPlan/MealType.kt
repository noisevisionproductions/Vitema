package com.noisevisionsoftware.szytadieta.domain.model.dietPlan

import com.google.gson.annotations.JsonAdapter
import com.noisevisionsoftware.szytadieta.domain.model.dietPlan.adapter.MealTypeAdapter

@JsonAdapter(MealTypeAdapter::class)
enum class MealType {
    @field:JvmField
    BREAKFAST,

    @field:JvmField
    SECOND_BREAKFAST,

    @field:JvmField
    LUNCH,

    @field:JvmField
    SNACK,

    @field:JvmField
    DINNER;

    val displayName: String
        get() = when (this) {
            BREAKFAST -> "Śniadanie"
            SECOND_BREAKFAST -> "Drugie śniadanie"
            LUNCH -> "Obiad"
            SNACK -> "Podwieczorek"
            DINNER -> "Kolacja"
        }

    companion object {
        fun fromPolishName(name: String): MealType? = when (name.lowercase().trim()) {
            "śniadanie" -> BREAKFAST
            "drugie śniadanie" -> SECOND_BREAKFAST
            "obiad" -> LUNCH
            "podwieczorek" -> SNACK
            "kolacja" -> DINNER
            else -> null
        }
    }
}