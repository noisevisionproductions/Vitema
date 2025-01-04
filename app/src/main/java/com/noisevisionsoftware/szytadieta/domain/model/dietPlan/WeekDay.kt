package com.noisevisionsoftware.szytadieta.domain.model.dietPlan

enum class WeekDay {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    companion object {
        fun fromPolishName(name: String): WeekDay? = when (name.lowercase()) {
            "poniedziałek" -> MONDAY
            "wtorek" -> TUESDAY
            "środa" -> WEDNESDAY
            "czwartek" -> THURSDAY
            "piątek" -> FRIDAY
            "sobota" -> SATURDAY
            "niedziela" -> SUNDAY
            else -> null
        }
    }
}