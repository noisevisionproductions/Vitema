package com.noisevisionsoftware.szytadieta.domain.model

enum class UserRole {
    USER,
    ADMIN;

    companion object {
        fun fromString(value: String?): UserRole {
            return try {
                value?.let { valueOf(it.uppercase()) } ?: USER
            } catch (e: IllegalArgumentException) {
                USER
            }
        }
    }
}