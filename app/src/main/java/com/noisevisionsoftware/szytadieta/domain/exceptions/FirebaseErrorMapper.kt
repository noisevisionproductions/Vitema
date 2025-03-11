package com.noisevisionsoftware.szytadieta.domain.exceptions

import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.crashlytics.crashlytics

object FirebaseErrorMapper {

    fun mapFirebaseAuthError(e: Exception): AppException {
        Firebase.crashlytics.apply {
            setCustomKey("error_type", "auth_error")
            setCustomKey("error_message", e.message ?: "Unknown error")
            setCustomKey("error_class", e.javaClass.simpleName)
            when (e) {
                is FirebaseAuthInvalidCredentialsException -> {
                    setCustomKey("error_code", e.errorCode)
                    setCustomKey("error_details", "invalid_credentials")
                }

                is FirebaseAuthInvalidUserException -> {
                    setCustomKey("error_code", e.errorCode)
                    setCustomKey("error_details", "invalid_user")
                }

                is FirebaseAuthEmailException -> {
                    setCustomKey("error_details", "email_error")
                }
            }
            recordException(e)
        }

        return when (e) {
            is FirebaseAuthInvalidCredentialsException -> {
                AppException.AuthException(mapErrorCode(e.errorCode))
            }

            is FirebaseAuthInvalidUserException -> {
                AppException.AuthException(mapErrorCode(e.errorCode))
            }

            is FirebaseAuthEmailException -> {
                when {
                    e.message?.contains("email-already-in-use") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_EMAIL_ALREADY_IN_USE"))

                    else -> AppException.AuthException(
                        e.localizedMessage ?: "Błąd związany z adresem email"
                    )
                }
            }

            is FirebaseException -> {
                when {
                    e.message?.contains("network") == true ->
                        AppException.NetworkException("Problem z połączeniem internetowym")

                    e.message?.contains("too-many-requests") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_OPERATION_NOT_ALLOWED"))

                    e.message?.contains("operation-not-allowed") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_OPERATION_NOT_ALLOWED"))

                    e.message?.contains("expired") == true ->
                        AppException.AuthException(mapErrorCode("ERROR_USER_TOKEN_EXPIRED"))

                    e.message?.contains("CONFIGURATION_NOT_FOUND") == true ->
                        AppException.AuthException("Błąd konfiguracji. Spróbuj ponownie później")

                    e.message?.contains("blocked all requests") == true ||
                            e.message?.contains("RecaptchaAction") == true ->
                        AppException.AuthException("Zbyt wiele prób logowania. Spróbuj ponownie za kilka minut")

                    e.message?.contains("email address is already in use") == true ||
                            (e.message?.contains("RecaptchaAction") == true &&
                                    e.message?.contains("already in use") == true) ->
                        AppException.AuthException(mapErrorCode("ERROR_EMAIL_ALREADY_IN_USE"))

                    else -> AppException.UnknownException()
                }
            }

            is IllegalArgumentException ->
                AppException.ValidationException("Nieprawidłowe dane: ${e.message}")

            else -> AppException.UnknownException()
        }
    }

    private fun mapErrorCode(errorCode: String): String {
        Firebase.crashlytics.setCustomKey("mapped_error_code", errorCode)

        return when (errorCode) {
            "ERROR_INVALID_CUSTOM_TOKEN" -> "Błędny token uwierzytelniający"
            "ERROR_CUSTOM_TOKEN_MISMATCH" -> "Token nie pasuje do tej aplikacji"
            "ERROR_INVALID_CREDENTIAL" -> "Nieprawidłowe dane uwierzytelniające"
            "ERROR_INVALID_EMAIL" -> "Nieprawidłowy adres email"
            "ERROR_WRONG_PASSWORD" -> "Nieprawidłowe hasło"
            "ERROR_USER_MISMATCH" -> "Dane nie pasują do bieżącego użytkownika"
            "ERROR_REQUIRES_RECENT_LOGIN" -> "Ta operacja wymaga ponownego zalogowania"
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                "Konto istnieje z inną metodą logowania"

            "ERROR_EMAIL_ALREADY_IN_USE" -> "Ten adres email jest już używany"
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "Te dane uwierzytelniające są już używane"
            "ERROR_USER_DISABLED" -> "Konto zostało zablokowane"
            "ERROR_USER_TOKEN_EXPIRED" -> "Sesja wygasła. Zaloguj się ponownie"
            "ERROR_USER_NOT_FOUND" -> "Nie znaleziono użytkownika"
            "ERROR_INVALID_USER_TOKEN" -> "Nieprawidłowy token użytkownika"
            "ERROR_OPERATION_NOT_ALLOWED" -> "Operacja niedozwolona"
            "ERROR_WEAK_PASSWORD" -> "Hasło musi zawierać minimum 8 znaków, wielką literę, cyfrę i znak specjalny"
            "ERROR_TOO_MANY_ATTEMPTS" -> "Zbyt wiele prób logowania. Spróbuj ponownie za kilka minut"

            else -> {
                Firebase.crashlytics.setCustomKey("error_category", "unknown_error")
                "Wystąpił nieoczekiwany błąd"
            }
        }
    }
}