package com.noisevisionsoftware.szytadieta.domain.model.user

import com.noisevisionsoftware.szytadieta.utils.DateUtils

data class UserSettings(
    val userId: String = "",
    val waterDailyTarget: Int = 2000,
    val lastUpdated: Long = DateUtils.getCurrentPreciseTime()
)