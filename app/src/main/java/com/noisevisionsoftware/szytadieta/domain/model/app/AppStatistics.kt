package com.noisevisionsoftware.szytadieta.domain.model.app

import com.noisevisionsoftware.szytadieta.domain.model.user.Gender
import com.noisevisionsoftware.szytadieta.utils.DateUtils

data class AppStatistics(
    val totalUsers: Int = 0,
    val activeUsers: Int = 0,
    val newUsersThisMonth: Int = 0,
    val totalMeasurements: Int = 0,
    val averageUserMeasurements: Double = 0.0,
    val usersWithCompletedProfiles: Int = 0,
    val usersByGender: Map<Gender, Int> = emptyMap(),
    val measurementsByMont: Map<String, Int> = emptyMap(),
    val averageUserAge: Double? = null,
    val lastUpdated: Long = DateUtils.getCurrentLocalDate()
)
