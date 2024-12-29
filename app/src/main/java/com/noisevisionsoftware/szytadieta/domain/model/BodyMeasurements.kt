package com.noisevisionsoftware.szytadieta.domain.model

import java.util.UUID

data class BodyMeasurements(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: Long = System.currentTimeMillis(),
    val neck: Double = 0.0,
    val biceps: Double = 0.0,
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hips: Double = 0.0,
    val thigh: Double = 0.0,
    val calf: Double = 0.0,
    val weight: Double = 0.0,
    val note: String = "",
    val weekNumber: Int = 0,
    val measurementType: MeasurementType = MeasurementType.WEIGHT_ONLY
)

enum class MeasurementType {
    WEIGHT_ONLY,
    FULL_BODY
}