package com.noisevisionsoftware.szytadieta.domain.model.health.measurements

import com.noisevisionsoftware.szytadieta.utils.DateUtils
import java.util.Calendar
import java.util.UUID

data class BodyMeasurements(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: Long = DateUtils.getCurrentPreciseTime(),
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
    val weekNumber: Int = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR),
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