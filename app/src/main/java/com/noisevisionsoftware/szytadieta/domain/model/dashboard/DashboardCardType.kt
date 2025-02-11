package com.noisevisionsoftware.szytadieta.domain.model.dashboard

enum class DashboardCardType {
    MEAL_PLAN,
    SHOPPING_LIST,
    WATER_TRACKING,
    MEASUREMENTS,
    WEIGHT,
    DIET_GUIDE;

    val displayName: String
        get() = when (this) {
            MEAL_PLAN -> "Plan posiłków"
            SHOPPING_LIST -> "Lista zakupów"
            WATER_TRACKING -> "Śledzenie wody"
            MEASUREMENTS -> "Pomiary ciała"
            WEIGHT -> "Waga"
            DIET_GUIDE -> "Poradnik dietetyczny"
        }
}