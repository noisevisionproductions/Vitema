package com.noisevisionsoftware.szytadieta.domain.model.health.dietPlan

import com.noisevisionsoftware.szytadieta.utils.DateUtils
import java.util.UUID

data class Diet(
    val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val fileUrl: String = "",
    val uploadedAt: Long = DateUtils.getCurrentLocalDate(),
    val startDate: Long = DateUtils.getCurrentLocalDate(),
    val endDate: Long = DateUtils.getLocalDatePlusDays(7),
    val weeklyPlan: List<DayPlan> = emptyList(),
)