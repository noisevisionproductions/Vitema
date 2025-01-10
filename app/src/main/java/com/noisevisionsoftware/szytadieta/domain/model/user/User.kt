package com.noisevisionsoftware.szytadieta.domain.model.user

import android.icu.util.Calendar

data class User(
    val id: String = "",
    val createdAt: Long = 0,
    val email: String = "",
    val nickname: String = "",
    val gender: Gender? = null,
    val birthDate: Long? = null,
    val storedAge: Int = 0,
    val profileCompleted: Boolean = false,
    val role: UserRole = UserRole.USER
) {
    fun calculateAge(): Int {
        return birthDate?.let {
            val today = Calendar.getInstance()
            val birthCalendar = Calendar.getInstance().apply {
                timeInMillis = birthDate
            }
            var age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

            if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            age
        } ?: 0
    }
}