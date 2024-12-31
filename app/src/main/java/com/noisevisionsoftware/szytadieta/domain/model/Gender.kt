package com.noisevisionsoftware.szytadieta.domain.model

enum class Gender {
    MALE, FEMALE, OTHER;

    val displayName: String
        get() = when (this) {
            MALE -> "Mężczyzna"
            FEMALE -> "Kobieta"
            else -> "Inna"
        }

    companion object {
        fun fromString(value: String): Gender {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                OTHER
            }
        }
    }
}