package com.noisevisionsoftware.vitema.domain.model.user.pending

import com.noisevisionsoftware.vitema.domain.model.user.Gender
import java.util.Date

data class PendingUser (
    val email: String = "",
    val gender: Gender = Gender.OTHER,
    val age: Int = 0,
    val firstAndLastName: String = "",
    val lastUpdated: Date = Date(),
    val measurements: List<PendingMeasurement>? = null
)