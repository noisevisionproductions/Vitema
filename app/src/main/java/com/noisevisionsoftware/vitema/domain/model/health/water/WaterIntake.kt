package com.noisevisionsoftware.vitema.domain.model.health.water

import com.noisevisionsoftware.vitema.utils.DateUtils
import java.util.UUID

data class WaterIntake(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val date: Long = DateUtils.getCurrentLocalDate(),
    val amount: Int = 0,
    val timestamp: Long = DateUtils.getCurrentPreciseTime()
)
