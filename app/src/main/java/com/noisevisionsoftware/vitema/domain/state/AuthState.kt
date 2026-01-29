package com.noisevisionsoftware.vitema.domain.state

sealed class AuthState<out T> {
    data object Initial : AuthState<Nothing>()
    data object Loading : AuthState<Nothing>()
    data class Success<T>(val data: T) : AuthState<T>()
    data class Error(val message: String) : AuthState<Nothing>()
    data object Logout : AuthState<Nothing>()
}