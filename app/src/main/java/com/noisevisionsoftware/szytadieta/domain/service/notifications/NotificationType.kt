package com.noisevisionsoftware.szytadieta.domain.service.notifications

enum class NotificationType(val channelId: String) {
    WATER_REMINDER("water_reminders"),
    DIET_UPDATE("diet_updates"),
    SURVEY_REMINDER("survey_reminders"),
    MEASUREMENT_REMINDER("measurement_reminders"),
    GENERAL("general");
}