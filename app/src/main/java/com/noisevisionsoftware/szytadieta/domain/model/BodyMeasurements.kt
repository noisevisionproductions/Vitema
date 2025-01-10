package com.noisevisionsoftware.szytadieta.domain.model

import com.noisevisionsoftware.szytadieta.utils.DateUtils
import java.util.UUID

data class BodyMeasurements(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: Long = DateUtils.getCurrentLocalDate(),
    val height: Int = 0,
    val weight: Int = 0,
    val neck: Int = 0,
    val biceps: Int = 0,
    val chest: Int = 0,
    val waist: Int = 0,
    val belt: Int = 0,
    val hips: Int = 0,
    val thigh: Int = 0,
    val calf: Int = 0,
    val note: String = "",
    val weekNumber: Int = 0,
    val measurementType: MeasurementType = MeasurementType.WEIGHT_ONLY,
    val sourceType: MeasurementSourceType = MeasurementSourceType.APP
)

enum class MeasurementSourceType {
    APP,
    GOOGLE_SHEET
}

enum class MeasurementType {
    WEIGHT_ONLY,
    FULL_BODY
}