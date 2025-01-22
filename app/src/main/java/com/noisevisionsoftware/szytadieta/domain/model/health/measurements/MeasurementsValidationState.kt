package com.noisevisionsoftware.szytadieta.domain.model.health.measurements

data class MeasurementsValidationState(
    val weight: String? = null,
    val neck: String? = null,
    val biceps: String? = null,
    val chest: String? = null,
    val waist: String? = null,
    val belt: String? = null,
    val hips: String? = null,
    val thigh: String? = null,
    val height: String? = null,
    val calf: String? = null
)
