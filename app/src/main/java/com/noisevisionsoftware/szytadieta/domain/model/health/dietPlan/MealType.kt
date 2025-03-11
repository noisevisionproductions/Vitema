package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

import com.google.gson.annotations.JsonAdapter
import com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan.adapter.MealTypeAdapter

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
}