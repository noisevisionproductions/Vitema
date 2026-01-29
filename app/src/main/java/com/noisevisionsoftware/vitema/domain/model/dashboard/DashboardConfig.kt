package com.noisevisionsoftware.vitema.domain.model.dashboard

data class DashboardConfig(
    val cardOrder: List<DashboardCardType> = DashboardCardType.entries,
    val hiddenCards: Set<DashboardCardType> = emptySet()
)