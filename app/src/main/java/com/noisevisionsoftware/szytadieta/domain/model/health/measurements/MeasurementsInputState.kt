package com.noisevisionsoftware.szytadieta.domain.model.health.measurements

data class MeasurementsInputState(
    val neck: String = "",
    val biceps: String = "",
    val chest: String = "",
    val waist: String = "",
    val hips: String = "",
    val belt: String = "",
    val thigh: String = "",
    val calf: String = "",
    val weight: String = "",
    val height: String = "",
    val note: String = "",
    val validationState: MeasurementsValidationState = MeasurementsValidationState()
)
