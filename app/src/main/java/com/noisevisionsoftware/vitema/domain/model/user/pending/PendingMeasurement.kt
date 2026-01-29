package com.noisevisionsoftware.vitema.domain.model.user.pending

data class PendingMeasurement(
    val date: Long = 0,
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
)