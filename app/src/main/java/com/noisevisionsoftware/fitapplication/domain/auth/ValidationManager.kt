package com.noisevisionsoftware.fitapplication.domain.auth

import com.noisevisionsoftware.fitapplication.domain.exceptions.AppException

object ValidationManager {

    fun validateEmail(email: String): Result<Unit> {
        return when {
            email.isBlank() ->
                Result.failure(AppException.ValidationException("Email nie może być pusty"))

            !email.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) ->
                Result.failure(AppException.ValidationException("Nieprawidłowy format email"))

            else -> Result.success(Unit)
        }
    }

    fun validatePassword(password: String): Result<Unit> {
        return when {
            password.isBlank() ->
                Result.failure(AppException.ValidationException("Hasło nie może być puste"))

            password.length <= 6 ->
                Result.failure(AppException.ValidationException("Hasło musi mieć minimum 6 znaków"))

            password.length >= 30 ->
                Result.failure(AppException.ValidationException("Hasło nie może być dłuzsze niż 30 znaków"))

            !password.matches(Regex(".*[a-z].*")) ->
                Result.failure(AppException.ValidationException("Hasło musi zawierać przynajmniej jedną małą literę"))

            !password.matches(Regex(".*[0-9].*")) ->
                Result.failure(AppException.ValidationException("Hasło musi zawierać przynajmniej jedną cyfrę"))

            else -> Result.success(Unit)
        }
    }

    fun validateNickname(nickname: String): Result<Unit> {
        return when {
            nickname.isBlank() ->
                Result.failure(AppException.ValidationException("Nazwa użytkownika nie może być pusta"))

            nickname.length <= 3 ->
                Result.failure(AppException.ValidationException("Nazwa użytkownika musi mieć minimum 3 znaki"))

            nickname.length >= 20 ->
                Result.failure(AppException.ValidationException("Nazwa użytkownika nie może być dłuższa niż 20 znaków"))

            !nickname.matches(Regex("^[a-zA-Z0-9._]+$")) ->
                Result.failure(AppException.ValidationException("Nazwa użytkownika może zawierać tylko litery, cyfry, kropki i podkreślenia"))

            else -> Result.success(Unit)
        }
    }

    fun validatePasswordConfirmation(password: String, confirmPassword: String): Result<Unit> {
        return when {
            password != confirmPassword ->
                Result.failure(AppException.ValidationException("Hasła nie są identyczne"))

            else -> Result.success(Unit)
        }
    }
}