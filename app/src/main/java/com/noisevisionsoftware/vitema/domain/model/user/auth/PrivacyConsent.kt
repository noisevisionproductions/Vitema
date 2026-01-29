package com.noisevisionsoftware.vitema.domain.model.user.auth

data class PrivacyConsent(
    val privacyPolicyAccepted: Boolean = false,
    val termsAccepted: Boolean = false,
    val timestamp: Long = 0L
)