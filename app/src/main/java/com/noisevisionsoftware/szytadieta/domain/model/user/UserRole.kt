package com.noisevisionsoftware.szytadieta.domain.model.user

enum class UserRole {
    USER,
    ADMIN,
    OWNER;

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