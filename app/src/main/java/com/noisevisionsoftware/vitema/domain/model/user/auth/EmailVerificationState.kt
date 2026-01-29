package com.noisevisionsoftware.vitema.domain.model.user.auth

sealed class EmailVerificationState {
    data object Initial : EmailVerificationState()
    data object Loading : EmailVerificationState()
    data object NotVerified : EmailVerificationState()
    data object Verified : EmailVerificationState()
    data object EmailSent : EmailVerificationState()
    data class Error(val message: String) : EmailVerificationState()
}