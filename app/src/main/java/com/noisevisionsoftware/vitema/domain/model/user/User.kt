package com.noisevisionsoftware.vitema.domain.model.user

import android.icu.util.Calendar
import com.noisevisionsoftware.vitema.domain.model.user.auth.PrivacyConsent

data class User(
    val id: String = "",
    val createdAt: Long = 0,
    val email: String = "",
    val nickname: String = "",
    val firstAndLastName: String = "",
    val gender: Gender? = null,
    val birthDate: Long? = null,
    val storedAge: Int = 0,
    val privacyConsent: PrivacyConsent = PrivacyConsent(),
    val profileCompleted: Boolean = false,
    val surveyCompleted: Boolean = false,
    val role: UserRole = UserRole.USER,
    val note: String? = null,
    val fcmToken: String? = null
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