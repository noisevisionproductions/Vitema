package com.noisevisionsoftware.vitema.domain.model.user

enum class UserRole {
    USER, ADMIN, OWNER,
    TRAINER;

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