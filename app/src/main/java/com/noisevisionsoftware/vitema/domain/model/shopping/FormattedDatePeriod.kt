package com.noisevisionsoftware.vitema.domain.model.shopping

data class FormattedDatePeriod(
    val period: DatePeriod,
    val displayText: String,
    val subtitle: String,
    val isCurrentWeek: Boolean
)