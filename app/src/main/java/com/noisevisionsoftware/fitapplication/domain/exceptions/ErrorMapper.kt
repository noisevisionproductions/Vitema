package com.noisevisionsoftware.fitapplication.domain.exceptions

object ErrorMapper {
    fun mapFirebaseAuthError(e: Exception): AppException {
        return when {
            e.message?.contains("weak-password") == true ->
                AppException.ValidationException("Hasło jest za słabe")

            e.message?.contains("email-already-in-use") == true ->
                AppException.AuthException("Email jest już zajęty")

            e.message?.contains("invalid-email") == true ->
                AppException.ValidationException("Nieprawidłowy adres email")

            e.message?.contains("user-not-found") == true ->
                AppException.AuthException("Użytkownik nie istnieje")

            e.message?.contains("wrong-password") == true ->
                AppException.AuthException("Nieprawidłowe hasło")

            e.message?.contains("network") == true ->
                AppException.NetworkException("Brak połączenia z internetem")

            else -> AppException.UnknownException()
        }
    }
}