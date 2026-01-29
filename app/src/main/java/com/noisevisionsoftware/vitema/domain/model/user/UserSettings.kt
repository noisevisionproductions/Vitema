package com.noisevisionsoftware.vitema.domain.model.user

import com.noisevisionsoftware.vitema.utils.DateUtils

data class UserSettings(
    val userId: String = "",
    val waterDailyTarget: Int = 2000,
    val lastUpdated: Long = DateUtils.getCurrentPreciseTime()
)