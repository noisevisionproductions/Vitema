package com.noisevisionsoftware.szytadieta.domain.service.notifications

enum class NotificationType(val channelId: String) {
    WATER_REMINDER("water_reminders"),
    DIET_UPDATE("diet_updates"),
    MEASUREMENT_REMINDER("measurement_reminders"),
    GENERAL("general");

    companion object {
        fun fromString(type: String?): NotificationType {
            return try {
                valueOf(type?.uppercase() ?: "")
            } catch (e: Exception) {
                GENERAL
            }
        }
    }
}